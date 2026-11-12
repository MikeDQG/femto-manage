import { test, expect, type Page } from '@playwright/test'

async function setup(page: Page) {
  const email=`reflection-${crypto.randomUUID()}@example.com`
  const csrf=await(await page.request.get('/api/auth/csrf')).json()
  expect((await page.request.post('/api/auth/register',{headers:{[csrf.headerName]:csrf.token},data:{email,password:'long-password-123'}})).ok()).toBeTruthy()
  await page.goto('/')
  await page.getByLabel('E-pošta').fill(email)
  await page.getByLabel('Geslo',{exact:true}).fill('long-password-123')
  await page.getByRole('button',{name:'Prijava'}).click()
  await page.getByRole('button',{name:'Koledar',exact:true}).click()
  await page.getByLabel('Izberi dan').fill('2026-11-12')
  await expect(page.getByLabel('Zasebna opomba')).toBeVisible()
}

test('saves reopens edits and switches days without changing totals',async({page})=>{
  await setup(page)
  const before=await(await page.request.get('/api/journal/day?date=2026-11-12&zone=Europe/Ljubljana')).json()
  await page.getByLabel('Zasebna opomba').fill('Samo zame')
  await page.getByLabel('Počutje (neobvezno)').fill('Dobro')
  await page.getByLabel('Energija (1–5, neobvezno)').fill('1')
  await page.getByRole('button',{name:'Shrani refleksijo'}).click()
  await expect(page.getByText('Refleksija shranjena.')).toBeVisible()
  await page.reload()
  await page.getByRole('button',{name:'Koledar',exact:true}).click()
  await page.getByLabel('Izberi dan').fill('2026-11-12')
  await expect(page.getByLabel('Zasebna opomba')).toHaveValue('Samo zame')
  await expect(page.getByLabel('Energija (1–5, neobvezno)')).toHaveValue('1')
  await page.getByLabel('Zasebna opomba').fill('Dopolnjeno')
  await page.getByLabel('Energija (1–5, neobvezno)').fill('5')
  await page.getByRole('button',{name:'Shrani refleksijo'}).click()
  await expect(page.getByText('Refleksija shranjena.')).toBeVisible()
  await page.getByLabel('Izberi dan').fill('2026-11-13')
  await expect(page.getByLabel('Zasebna opomba')).toHaveValue('')
  await page.getByLabel('Izberi dan').fill('2026-11-12')
  await expect(page.getByLabel('Zasebna opomba')).toHaveValue('Dopolnjeno')
  await expect(page.getByLabel('Energija (1–5, neobvezno)')).toHaveValue('5')
  const after=await(await page.request.get('/api/journal/day?date=2026-11-12&zone=Europe/Ljubljana')).json()
  expect(after).toEqual(before)
})

test('invalid energy and server failure retain edits',async({page})=>{
  await setup(page)
  await page.getByLabel('Zasebna opomba').fill('Ohrani vnos')
  for(const energy of ['0','6','2.5']) {
    await page.getByLabel('Energija (1–5, neobvezno)').fill(energy)
    await page.getByRole('button',{name:'Shrani refleksijo'}).click()
    await expect(page.getByText('Energija mora biti celo število od 1 do 5.',{exact:true})).toBeVisible()
  }
  await page.route('**/api/journal/reflection?*',route=>route.request().method()==='PUT' ? route.fulfill({status:503,json:{message:'Shranjevanje ni dosegljivo.'}}) : route.continue())
  await page.getByLabel('Energija (1–5, neobvezno)').fill('3')
  await page.getByRole('button',{name:'Shrani refleksijo'}).click()
  await expect(page.getByText('Shranjevanje ni dosegljivo.')).toBeVisible()
  await expect(page.getByLabel('Zasebna opomba')).toHaveValue('Ohrani vnos')
})

test('failed load cannot overwrite existing reflection with an empty form',async({page})=>{
  await setup(page)
  await page.route('**/api/journal/reflection?*',route=>route.fulfill({status:503,json:{}}))
  await page.getByLabel('Izberi dan').fill('2026-11-14')
  await expect(page.getByText('Refleksije ni bilo mogoče naložiti.')).toBeVisible()
  await expect(page.getByRole('button',{name:'Shrani refleksijo'})).toHaveCount(0)
  await page.unroute('**/api/journal/reflection?*')
  await page.getByRole('button',{name:'Ponovno naloži refleksijo'}).click()
  await expect(page.getByLabel('Zasebna opomba')).toHaveValue('')
})
