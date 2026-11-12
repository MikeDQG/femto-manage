import { test, expect, type Page } from '@playwright/test'
test.use({ timezoneId: 'Europe/Ljubljana' })
const work='00000000-0000-0000-0000-000000000001'
const text='Danes od 9. do 11. ure sem delal'

async function setup(page: Page) {
  const email=`text-${crypto.randomUUID()}@example.com`
  const csrf=await (await page.request.get('/api/auth/csrf')).json()
  expect((await page.request.post('/api/auth/register',{headers:{[csrf.headerName]:csrf.token},data:{email,password:'long-password-123'}})).ok()).toBeTruthy()
  await page.goto('/')
  await page.getByLabel('E-pošta').fill(email)
  await page.getByLabel('Geslo',{exact:true}).fill('long-password-123')
  await page.getByRole('button',{name:'Prijava'}).click()
  await page.getByRole('button',{name:'Koledar',exact:true}).click()
  await page.getByLabel('Izberi dan').fill('2026-11-12')
  await page.getByRole('button',{name:'Dodaj aktivnost'}).click()
  await page.getByRole('button',{name:'Besedilni vnos'}).click()
  await page.getByLabel('Opis aktivnosti z besedilom').fill(text)
}
const draft={title:'Delo', startAt:'2026-11-12T08:00:00Z',endAt:'2026-11-12T10:00:00Z',categoryId:work,placeId:null,source:'AI',fields:{},placeCandidates:[]}

test('reviews extracted fields, edits and confirms one AI activity',async({page})=>{
  await setup(page)
  await page.route('**/api/activities/text-draft',async route=>{
    expect(route.request().postDataJSON()).toMatchObject({text,zone:'Europe/Ljubljana'})
    await route.fulfill({json:draft})
  })
  await page.getByRole('button',{name:'Pripravi osnutek iz besedila'}).click()
  await expect(page.getByText(/Osnutek aktivnosti/)).toBeVisible()
  await expect(page.getByLabel('Začetek')).toHaveValue('2026-11-12T09:00')
  await expect(page.getByLabel('Konec')).toHaveValue('2026-11-12T11:00')
  await page.getByLabel('Naziv').fill('Popravljeno delo')
  await page.getByRole('button',{name:'Potrdi aktivnost'}).click()
  await expect(page.getByLabel('Dnevna časovnica').getByText('Popravljeno delo')).toBeVisible()
  const saved=await(await page.request.get('/api/activities?date=2026-11-12&zone=Europe/Ljubljana')).json()
  expect(saved).toHaveLength(1);expect(saved[0].source).toBe('AI')
})

test('incomplete ambiguous draft requires choice and cancellation writes nothing',async({page})=>{
  await setup(page)
  const csrf=await(await page.request.get('/api/auth/csrf')).json()
  const places=[]
  for(let i=0;i<2;i++) places.push(await(await page.request.post('/api/places',{headers:{[csrf.headerName]:csrf.token},data:{name:'Dom',latitude:46+i,longitude:15,radiusM:100}})).json())
  await page.route('**/api/activities/text-draft',route=>route.fulfill({json:{...draft,title:null,endAt:null,placeCandidates:places.map(p=>({id:p.id,name:p.name})),fields:{title:'Dopolni naziv.',endAt:'Dopolni konec.',placeId:'Izberi kraj.'}}}))
  // Reload the workspace so its normal place list includes both new places.
  await page.getByRole('button',{name:'Prekliči',exact:true}).click()
  await page.reload()
  await page.getByRole('button',{name:'Koledar',exact:true}).click()
  await page.getByRole('button',{name:'Dodaj aktivnost'}).click()
  await page.getByRole('button',{name:'Besedilni vnos'}).click()
  await page.getByLabel('Opis aktivnosti z besedilom').fill(text)
  await page.getByRole('button',{name:'Pripravi osnutek iz besedila'}).click()
  await expect(page.getByText('Dopolni naziv.',{exact:true})).toBeVisible()
  await page.getByRole('button',{name:'Potrdi aktivnost'}).click()
  await expect(page.getByText('Izberi enega od svojih krajev.')).toBeVisible()
  await page.getByLabel('Kraj (neobvezno)').selectOption(places[1].id)
  await page.getByRole('button',{name:'Potrdi aktivnost'}).click()
  await expect(page.getByText('Naziv je obvezen.')).toBeVisible()
  await page.getByRole('button',{name:'Zavrni predlog'}).click()
  const saved=await(await page.request.get('/api/activities?date=2026-11-12&zone=Europe/Ljubljana')).json()
  expect(saved).toHaveLength(0)
})

test('unconfigured AI preserves input and manual fallback works',async({page})=>{
  await setup(page)
  await page.getByRole('button',{name:'Pripravi osnutek iz besedila'}).click()
  await expect(page.getByRole('alert')).toContainText(/AI/)
  await expect(page.getByLabel('Opis aktivnosti z besedilom')).toHaveValue(text)
  await page.getByRole('button',{name:'Nadaljuj z ročnim vnosom'}).click()
  await expect(page.getByText(text,{exact:true})).toBeVisible()
  await page.getByLabel('Naziv').fill('Ročni zapis po napaki')
  await page.getByLabel('Začetek').fill('2026-11-12T09:00')
  await page.getByLabel('Konec').fill('2026-11-12T10:00')
  await page.getByLabel('Kategorija').selectOption(work)
  await page.getByRole('button',{name:'Potrdi aktivnost'}).click()
  await expect(page.getByLabel('Dnevna časovnica').getByText('Ročni zapis po napaki')).toBeVisible()
})
