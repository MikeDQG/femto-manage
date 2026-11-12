import { spawn } from 'node:child_process'
import { readFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const frontend = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const backend = path.resolve(frontend, '../backend-fm')
const children = []
const launch = (command, args, options = {}) => {
  const child = spawn(command, args, { stdio: 'inherit', ...options })
  children.push(child)
  return child
}
const finished = child => new Promise((resolve, reject) => {
  child.once('error', reject)
  child.once('exit', code => code === 0 ? resolve() : reject(new Error(`Process exited with ${code}`)))
})
const ready = async (url, child) => {
  for (let attempt = 0; attempt < 90; attempt++) {
    if (child.exitCode !== null) throw new Error(`Server stopped before ${url} became available`)
    try { if ((await fetch(url)).ok) return } catch { /* Server is starting. */ }
    await new Promise(resolve => setTimeout(resolve, 1000))
  }
  throw new Error(`Server did not start: ${url}`)
}
try {
  for (const port of [8081, 5174]) {
    try { await fetch(`http://127.0.0.1:${port}`); throw new Error(`Port ${port} is already in use`) }
    catch (error) { if (!error.cause) throw error }
  }
  console.log('Preparing isolated H2 backend and test-only administrator')
  const buildArgs = ['test-compile', 'dependency:build-classpath', '-Dmdep.outputFile=target/browser-classpath.txt', '-q']
  await finished(process.platform === 'win32'
    ? launch('cmd.exe', ['/d', '/s', '/c', `.\\mvnw.cmd ${buildArgs.join(' ')}`], { cwd: backend })
    : launch('./mvnw', buildArgs, { cwd: backend }))
  const dependencies = (await readFile(path.join(backend, 'target/browser-classpath.txt'), 'utf8')).trim()
  const classpath = [path.join(backend, 'target/test-classes'), path.join(backend, 'target/classes'), dependencies].join(path.delimiter)
  const api = launch('java', ['-Dspring.devtools.restart.enabled=false', '-cp', classpath, 'si.um.feri.riris.backendfm.support.BrowserTestApplication'], { cwd: backend })
  await ready('http://127.0.0.1:8081/api/auth/csrf', api)
  const web = launch(process.execPath, ['node_modules/vite/bin/vite.js', '--host', '127.0.0.1', '--port', '5174', '--strictPort'], {
    cwd: frontend, env: { ...process.env, BACKEND_URL: 'http://127.0.0.1:8081' },
  })
  await ready('http://127.0.0.1:5174', web)
  console.log('Running browser tests against the isolated application')
  await finished(launch(process.execPath, ['node_modules/@playwright/test/cli.js', 'test', ...process.argv.slice(2)], {
    cwd: frontend, env: { ...process.env, E2E_BASE_URL: 'http://127.0.0.1:5174' },
  }))
} catch (error) {
  console.error(error.message)
  process.exitCode = 1
} finally {
  for (const child of children.reverse()) {
    if (child.exitCode !== null || !child.pid) continue
    if (process.platform === 'win32') {
      await new Promise(resolve => {
        const stop = spawn('taskkill', ['/PID', String(child.pid), '/T', '/F'], { stdio: 'ignore' })
        stop.once('exit', resolve); stop.once('error', resolve)
      })
    } else child.kill('SIGTERM')
  }
}
