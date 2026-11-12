import { test, expect, type APIRequestContext, type Page, type Playwright } from '@playwright/test'
test.use({ timezoneId: 'Europe/Ljubljana' })
const baseURL=process.env.E2E_BASE_URL??'http://localhost:5173'
const work='00000000-0000-0000-0000-000000000001'

async function post(api: APIRequestContext,path:string,data:unknown) {
  const csrf=await(await api.get('/api/auth/csrf')).json()
  return api.post(`/api/${path}`,{headers:{[csrf.headerName]:csrf.token},data})
}
async function recipient(playwright:Playwright) {
  const admin=await playwright.request.newContext({baseURL})
  expect((await post(admin,'auth/login',{email:'browser-admin@example.test',password:'test-only-password-123'})).ok(),
    'Run sharing tests with npm run test:e2e:isolated').toBeTruthy()
  const email=`sharing-mentor-${crypto.randomUUID()}@example.com`
  expect((await post(admin,'auth/register',{email,password:'long-password-123'})).status()).toBe(201)
  expect((await post(admin,'admin/accounts/mentor',{email,confirmed:true})).ok()).toBeTruthy()
  await admin.dispose()
  return email
}
async function setup(page:Page,playwright:Playwright) {
  const mentorEmail=await recipient(playwright)
  const email=`sharing-owner-${crypto.randomUUID()}@example.com`
  expect((await post(page.request,'auth/register',{email,password:'long-password-123'})).status()).toBe(201)
  await page.goto('/')
  await page.getByLabel('E-pošta').fill(email)
  await page.getByLabel('Geslo',{exact:true}).fill('long-password-123')
  await page.getByRole('button',{name:'Prijava'}).click()
  await expect(page.getByRole('button',{name:'Koledar',exact:true})).toBeVisible()
  expect((await post(page.request,'activities',{title:'Zasebna aktivnost',description:'Skrivni opis',startAt:'2026-11-12T08:00:00Z',endAt:'2026-11-12T09:00:00Z',categoryId:work})).status()).toBe(201)
  await page.getByRole('button',{name:'Koledar',exact:true}).click()
  await page.getByLabel('Izberi dan').fill('2026-11-12')
  await page.getByLabel('Mentor za deljenje').selectOption({label:mentorEmail})
  return mentorEmail
}

test('preview cancellation, exact sharing and revocation across real accounts',async({page,playwright})=>{
  const mentorEmail=await setup(page,playwright)
  await page.getByLabel('Zasebna opomba').fill('Samo moja refleksija')
  await page.getByRole('button',{name:'Shrani refleksijo'}).click()
  await expect(page.getByText('Refleksija shranjena.')).toBeVisible()
  await expect(page.getByLabel('Komentar za mentorja (neobvezno)')).toHaveValue('')
  await page.getByLabel('Komentar za mentorja (neobvezno)').fill('To namenjam mentorju')
  await page.getByRole('button',{name:'Predogled za deljenje'}).click()
  const preview=page.getByLabel('Predogled deljenega povzetka')
  await expect(preview.getByText('60 min',{exact:true})).toBeVisible()
  await expect(preview).not.toContainText('Samo moja refleksija')
  await expect(preview).not.toContainText('Zasebna aktivnost')
  expect(await(await page.request.get('/api/summaries/shares')).json()).toEqual([])
  await page.getByRole('button',{name:'Prekliči predogled'}).click()
  expect(await(await page.request.get('/api/summaries/shares')).json()).toEqual([])
  await page.getByRole('button',{name:'Predogled za deljenje'}).click()
  await page.getByRole('button',{name:'Potrdi deljenje z mentorjem'}).click()
  await expect(page.getByText('Povzetek je deljen.')).toBeVisible()
  const shares=await(await page.request.get('/api/summaries/shares')).json()
  expect(shares).toHaveLength(1)
  const mentor=await playwright.request.newContext({baseURL})
  expect((await post(mentor,'auth/login',{email:mentorEmail,password:'long-password-123'})).ok()).toBeTruthy()
  const snapshot=await mentor.get(`/api/summaries/${shares[0].summaryId}`)
  expect(snapshot.status()).toBe(200)
  const content=await snapshot.json()
  expect(Object.keys(content).sort()).toEqual(['comment','date','totals'])
  expect(content.comment).toBe('To namenjam mentorju')
  expect(content.totals.find((t:{code:string})=>t.code==='WORK').minutes).toBe(60)
  await page.getByRole('button',{name:'Prekliči deljenje',exact:true}).click()
  await page.getByRole('button',{name:'Ohrani deljenje'}).click()
  expect((await mentor.get(`/api/summaries/${shares[0].summaryId}`)).status()).toBe(200)
  await page.getByRole('button',{name:'Prekliči deljenje',exact:true}).click()
  await page.getByRole('button',{name:'Potrdi preklic deljenja'}).click()
  await expect(page.getByText('Preklicano',{exact:true})).toBeVisible()
  expect((await mentor.get(`/api/summaries/${shares[0].summaryId}`)).status()).toBe(404)
  await expect(page.getByLabel('Zasebna opomba')).toHaveValue('Samo moja refleksija')
  const journal=await(await page.request.get('/api/journal/day?date=2026-11-12&zone=Europe/Ljubljana')).json()
  expect(journal.activities).toHaveLength(1)
  await mentor.dispose()
})

test('changed data requires a new preview and a second confirmation',async({page,playwright})=>{
  await setup(page,playwright)
  await page.getByRole('button',{name:'Predogled za deljenje'}).click()
  await expect(page.getByLabel('Predogled deljenega povzetka').getByText('60 min',{exact:true})).toBeVisible()
  expect((await post(page.request,'activities',{title:'Dodatno',startAt:'2026-11-12T10:00:00Z',endAt:'2026-11-12T11:00:00Z',categoryId:work})).status()).toBe(201)
  await page.getByRole('button',{name:'Potrdi deljenje z mentorjem'}).click()
  await expect(page.getByText('Podatki so se spremenili. Preglej nov predogled in ponovno potrdi deljenje.')).toBeVisible()
  await expect(page.getByLabel('Predogled deljenega povzetka').getByText('120 min',{exact:true})).toBeVisible()
  expect(await(await page.request.get('/api/summaries/shares')).json()).toEqual([])
  await page.getByRole('button',{name:'Potrdi deljenje z mentorjem'}).click()
  await expect(page.getByText('Povzetek je deljen.')).toBeVisible()
  expect(await(await page.request.get('/api/summaries/shares')).json()).toHaveLength(1)
})

test('no mentors has a clear empty state and no sharing action',async({page,playwright})=>{
  await setup(page,playwright)
  await page.route('**/api/summaries/mentors',route=>route.fulfill({json:[]}))
  await page.getByLabel('Izberi dan').fill('2026-11-13')
  await expect(page.getByText('Ni računa z vlogo mentorja. Za dodelitev se obrni na skrbnika.')).toBeVisible()
  await expect(page.getByRole('button',{name:'Predogled za deljenje'})).toHaveCount(0)
  await expect(page.getByText('Še nisi delil nobenega povzetka.')).toBeVisible()
})
