import { test, expect } from '@playwright/test'

test('rejects invalid credentials, logs in, restores the session and logs out', async ({ page }) => {
  const email = `login-${crypto.randomUUID()}@example.com`
  const csrf = await (await page.request.get('/api/auth/csrf')).json()
  const registered = await page.request.post('/api/auth/register', {
    headers: { [csrf.headerName]: csrf.token }, data: { email, password: 'long-password-123' },
  })
  expect(registered.status()).toBe(201)
  expect((await page.request.get('/api/auth/me')).status()).toBe(401)
  await page.goto('/')
  await page.getByLabel('E-pošta').fill(email)
  await page.getByLabel('Geslo', { exact: true }).fill('wrong-password')
  await page.getByRole('button', { name: 'Prijava' }).click()
  await expect(page.getByRole('alert')).toContainText('E-poštni naslov ali geslo ni pravilno')
  const beforeLogin = (await page.context().cookies()).find(cookie => cookie.name === 'JSESSIONID')
  const oldCsrf = await (await page.request.get('/api/auth/csrf')).json()
  await page.getByLabel('Geslo', { exact: true }).fill('long-password-123')
  await page.getByRole('button', { name: 'Prijava' }).click()
  await expect(page.getByRole('button', { name: 'Nastavitve' })).toBeVisible()
  const afterLogin = (await page.context().cookies()).find(cookie => cookie.name === 'JSESSIONID')
  expect(afterLogin?.value).not.toBe(beforeLogin?.value)
  expect(afterLogin?.httpOnly).toBe(true)
  expect(afterLogin?.sameSite).toBe('Lax')
  expect((await page.request.post('/api/auth/logout', { headers: { [oldCsrf.headerName]: oldCsrf.token } })).status()).toBe(403)
  expect((await (await page.request.get('/api/auth/me?userId=another-account')).json()).email).toBe(email)
  await page.reload()
  await page.getByRole('button', { name: 'Nastavitve' }).click()
  await expect(page.getByText(email, { exact: true })).toBeVisible()
  expect(await page.evaluate(() => JSON.stringify(localStorage))).not.toContain('long-password-123')
  await page.getByRole('button', { name: 'Odjava' }).click()
  await expect(page.getByRole('button', { name: 'Prijava' })).toBeVisible()
  expect((await page.request.get('/api/auth/me')).status()).toBe(401)
  expect((await page.context().cookies()).find(cookie => cookie.name === 'JSESSIONID')?.value).not.toBe(afterLogin?.value)
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
})

test('registration API rejects privileged roles and requests without CSRF', async ({ request }) => {
  const data = { email: `role-${crypto.randomUUID()}@example.com`, password: 'long-password-123', role: 'ADMIN' }
  expect((await request.post('/api/auth/register', { data })).status()).toBe(403)
  const csrf = await (await request.get('/api/auth/csrf')).json()
  const response = await request.post('/api/auth/register', { headers: { [csrf.headerName]: csrf.token }, data })
  expect(response.status()).toBe(400)
  expect((await response.json()).fields.role).toBeTruthy()
  expect((await request.get('/api/auth/me')).status()).toBe(401)
})
