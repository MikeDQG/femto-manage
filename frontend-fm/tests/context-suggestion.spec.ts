import { test, expect, type Page } from '@playwright/test'

test.use({ timezoneId: 'Europe/Ljubljana' })

async function post(page: Page, path: string, data: unknown) {
  const csrf = await (await page.request.get('/api/auth/csrf')).json()
  return page.request.post(`/api/${path}`, { headers: { [csrf.headerName]: csrf.token }, data })
}

async function setup(page: Page, history: boolean) {
  const email = `context-${crypto.randomUUID()}@example.com`
  expect((await post(page, 'auth/register', { email, password: 'long-password-123' })).ok()).toBeTruthy()
  await page.goto('/')
  await page.getByLabel('E-pošta').fill(email)
  await page.getByLabel('Geslo', { exact: true }).fill('long-password-123')
  await page.getByRole('button', { name: 'Prijava' }).click()
  await expect(page.getByRole('button', { name: 'Koledar', exact: true })).toBeVisible()
  const place = await (await post(page, 'places', { name: 'Knjižnica', latitude: 46, longitude: 15, radiusM: 100 })).json()
  if (history) expect((await post(page, 'activities', { title: 'Zgodovina', startAt: '2026-11-11T08:00:00Z', endAt: '2026-11-11T09:00:00Z', categoryId: '00000000-0000-0000-0000-000000000001', placeId: place.id })).status()).toBe(201)
  await page.reload()
  await page.getByRole('button', { name: 'Koledar', exact: true }).click()
  await page.getByLabel('Izberi dan').fill('2026-11-12')
  await page.getByRole('button', { name: 'Dodaj aktivnost' }).click()
  await page.getByRole('button', { name: 'Kontekstni predlog', exact: true }).click()
  await page.getByLabel('Kraj za predlog').selectOption(place.id)
  await page.getByLabel('Začetek za predlog').fill('2026-11-12T09:00')
  return place
}

test('draft needs completion, allows correction and creates one activity', async ({ page }) => {
  await setup(page, true)
  await page.getByRole('button', { name: 'Pripravi kontekstni predlog' }).click()
  await expect(page.getByText(/Osnutek aktivnosti/)).toBeVisible()
  await expect(page.getByLabel('Naziv')).toHaveValue('')
  await expect(page.getByLabel('Konec')).toHaveValue('')
  await expect(page.getByLabel('Kategorija')).toHaveValue('00000000-0000-0000-0000-000000000001')
  await page.getByRole('button', { name: 'Potrdi aktivnost' }).click()
  await expect(page.getByText('Naziv je obvezen.')).toBeVisible()
  await page.getByLabel('Naziv').fill('Popravljen predlog')
  await page.getByLabel('Konec').fill('2026-11-12T10:00')
  await page.getByLabel('Kategorija').selectOption('00000000-0000-0000-0000-000000000002')
  await page.getByRole('button', { name: 'Potrdi aktivnost' }).click()
  await expect(page.getByLabel('Dnevna časovnica').getByText('Popravljen predlog')).toBeVisible()
  const activities = await (await page.request.get('/api/activities?date=2026-11-12&zone=Europe/Ljubljana')).json()
  expect(activities).toHaveLength(1)
  expect(activities[0].source).toBe('SUGGESTION')
})

test('rejecting draft leaves totals and activities unchanged', async ({ page }) => {
  await setup(page, true)
  const before = await (await page.request.get('/api/journal/day?date=2026-11-12&zone=Europe/Ljubljana')).json()
  await page.getByRole('button', { name: 'Pripravi kontekstni predlog' }).click()
  await page.getByRole('button', { name: 'Zavrni predlog' }).click()
  await expect(page.getByLabel('Dnevna časovnica')).toBeVisible()
  const after = await (await page.request.get('/api/journal/day?date=2026-11-12&zone=Europe/Ljubljana')).json()
  expect(after).toEqual(before)
})

test('empty history offers manual entry and unavailable API explains failure', async ({ page }) => {
  await setup(page, false)
  await page.getByRole('button', { name: 'Pripravi kontekstni predlog' }).click()
  await expect(page.getByText('Ni podatkov za predlog. Nadaljuj z ročnim vnosom.')).toBeVisible()
  await expect(page.getByText(/Osnutek aktivnosti/)).toHaveCount(0)
  await page.route('**/api/activities/context-suggestion', route => route.fulfill({ status: 503, json: {} }))
  await page.getByRole('button', { name: 'Pripravi kontekstni predlog' }).click()
  await expect(page.getByText('Predloga ni bilo mogoče pridobiti. Na voljo je ročni vnos.')).toBeVisible()
  await page.getByRole('button', { name: 'Ročni vnos', exact: true }).last().click()
  await expect(page.getByLabel('Naziv')).toBeVisible()
})

test('overlapping suggestion requires explicit confirmation', async ({ page }) => {
  const place = await setup(page, true)
  expect((await post(page, 'activities', { title: 'Obstoječa', startAt: '2026-11-12T08:00:00Z', endAt: '2026-11-12T09:00:00Z', categoryId: '00000000-0000-0000-0000-000000000001', placeId: place.id })).status()).toBe(201)
  await page.getByRole('button', { name: 'Pripravi kontekstni predlog' }).click()
  await page.getByLabel('Naziv').fill('Prekrivanje predloga')
  await page.getByLabel('Konec').fill('2026-11-12T10:00')
  await page.getByRole('button', { name: 'Potrdi aktivnost' }).click()
  await expect(page.getByText('Prekrivanje s potrjeno aktivnostjo')).toBeVisible()
  await page.getByRole('button', { name: 'Nadaljuj kljub prekrivanju' }).click()
  await expect(page.getByLabel('Dnevna časovnica').getByText('Prekrivanje predloga')).toBeVisible()
})
