import { test, expect } from '@playwright/test'

test('administrator assigns an existing account through the real API', async ({ playwright }) => {
  const admin=await playwright.request.newContext({ baseURL:process.env.E2E_BASE_URL??'http://localhost:5173' })
  const csrf=await(await admin.get('/api/auth/csrf')).json()
  const login=await admin.post('/api/auth/login',{headers:{[csrf.headerName]:csrf.token},data:{email:'browser-admin@example.test',password:'test-only-password-123'}})
  expect(login.ok(),'Run with npm run test:e2e:isolated to prepare the test administrator').toBeTruthy()
  const email=`mentor-assignment-${crypto.randomUUID()}@example.com`
  const tokens=await(await admin.get('/api/auth/csrf')).json()
  const headers={ [tokens.headerName]:tokens.token }
  expect((await admin.post('/api/auth/register',{headers,data:{email,password:'long-password-123'}})).status()).toBe(201)
  const lookup=await(await admin.get(`/api/admin/accounts?email=${encodeURIComponent(email)}`)).json()
  expect(lookup).toEqual({found:true,account:{email,role:'USER'}})
  expect((await admin.post('/api/admin/accounts/mentor',{headers,data:{email,confirmed:false}})).status()).toBe(400)
  expect((await admin.post('/api/admin/accounts/mentor',{headers,data:{email,confirmed:true}})).ok()).toBeTruthy()
  const repeat=await(await admin.post('/api/admin/accounts/mentor',{headers,data:{email,confirmed:true}})).json()
  expect(repeat).toEqual({account:{email,role:'MENTOR'},changed:false})
  const mentor=await playwright.request.newContext({baseURL:process.env.E2E_BASE_URL??'http://localhost:5173'})
  const mentorCsrf=await(await mentor.get('/api/auth/csrf')).json()
  const mentorLogin=await mentor.post('/api/auth/login',{headers:{[mentorCsrf.headerName]:mentorCsrf.token},data:{email,password:'long-password-123'}})
  expect((await mentorLogin.json()).role).toBe('MENTOR')
  expect((await mentor.get(`/api/admin/accounts?email=${encodeURIComponent(email)}`)).status()).toBe(403)
  await admin.dispose();await mentor.dispose()
})
