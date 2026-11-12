import { test, expect, type Page } from '@playwright/test'

test.use({ timezoneId: 'Europe/Ljubljana' })
const work = '00000000-0000-0000-0000-000000000001'
const movement = '00000000-0000-0000-0000-000000000002'
const date = '2026-11-12'

async function write(page: Page, path: string, data?: unknown, method = 'POST') {
  const csrf = await (await page.request.get('/api/auth/csrf')).json()
  return page.request.fetch(`/api/${path}`, { method, headers: { [csrf.headerName]: csrf.token }, data })
}

async function setup(page: Page, midnight = false) {
  await page.clock.setFixedTime(new Date('2026-11-12T12:00:00Z'))
  const email = `edit-${crypto.randomUUID()}@example.com`
  expect((await write(page, 'auth/register', { email, password: 'long-password-123' })).status()).toBe(201)
  await page.goto('/')
  await page.getByLabel('E-pošta').fill(email)
  await page.getByLabel('Geslo', { exact: true }).fill('long-password-123')
  await page.getByRole('button', { name: 'Prijava' }).click()
  await expect(page.getByRole('button', { name: 'Koledar', exact: true })).toBeVisible()
  const place = await (await write(page, 'places', { name: 'Knjižnica', latitude: 46, longitude: 15, radiusM: 100 })).json()
  const input = { title: 'Izvorna aktivnost', description: 'Izvorni opis', categoryId: work, placeId: place.id, source: 'SUGGESTION',
    startAt: midnight ? '2026-11-12T22:30:15Z' : '2026-11-12T08:00:00Z', endAt: midnight ? '2026-11-13T01:00:45Z' : '2026-11-12T09:00:00Z' }
  const response = await write(page, 'activities', input)
  expect(response.status()).toBe(201)
  const created = await response.json()
  const activity = await (await page.request.get(`/api/activities/${created.id}`)).json()
  await page.reload()
  await page.getByRole('button', { name: 'Koledar', exact: true }).click()
  await page.getByLabel('Izberi dan').fill(date)
  await expect(page.getByLabel('Dnevna časovnica').getByText(input.title)).toBeVisible()
  return { activity, place }
}

async function edit(page: Page, title = 'Izvorna aktivnost') {
  await page.getByRole('button', { name: `Podrobnosti: ${title}`, exact: true }).click()
  const dialog = page.getByRole('dialog', { name: 'Podrobnosti aktivnosti' })
  await expect(dialog.getByRole('heading', { name: title, exact: true })).toBeVisible()
  await dialog.getByRole('button', { name: 'Uredi', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Uredi aktivnost' })).toBeVisible()
}

test('edits all fields in the same activity and refreshes calendar totals and home', async ({ page }, testInfo) => {
  const { activity } = await setup(page)
  const otherPlace = await (await write(page, 'places', { name: 'Park', latitude: 46.1, longitude: 15.1, radiusM: 100 })).json()
  await page.reload()
  await page.getByRole('button', { name: 'Koledar', exact: true }).click()
  await page.getByLabel('Izberi dan').fill(date)
  await page.getByRole('button', { name: 'Podrobnosti: Izvorna aktivnost', exact: true }).click()
  await expect(page.getByRole('dialog').getByText('Izvorni opis')).toBeVisible()
  await page.screenshot({ path: testInfo.outputPath('activity-details.png') })
  await page.keyboard.press('Escape')
  await expect(page.getByRole('dialog')).toHaveCount(0)
  await edit(page)
  await page.screenshot({ path: testInfo.outputPath('activity-edit.png'), fullPage: true })
  await expect(page.getByLabel('Naziv')).toHaveValue('Izvorna aktivnost')
  await expect(page.getByLabel('Opis (neobvezno)')).toHaveValue('Izvorni opis')
  await expect(page.getByLabel('Začetek')).toHaveValue(`${date}T09:00`)
  await page.getByLabel('Naziv').fill('Popravljena aktivnost')
  await page.getByLabel('Začetek').fill(`${date}T09:30`)
  await page.getByLabel('Konec').fill(`${date}T11:00`)
  await page.getByLabel('Kategorija').selectOption(movement)
  await page.getByLabel('Kraj (neobvezno)').selectOption(otherPlace.id)
  await page.getByLabel('Opis (neobvezno)').fill('Popravljen opis')
  await page.getByRole('button', { name: 'Shrani spremembe' }).click()
  await expect(page.getByLabel('Izberi dan')).toHaveValue(date)
  await expect(page.getByLabel('Dnevna časovnica').getByText('Popravljena aktivnost')).toBeVisible()
  await expect(page.getByText('1 h 30 min', { exact: true })).toBeVisible()
  const saved = await (await page.request.get(`/api/activities/${activity.id}`)).json()
  expect(saved).toMatchObject({ id: activity.id, title: 'Popravljena aktivnost', categoryId: movement, placeId: otherPlace.id,
    description: 'Popravljen opis', source: 'SUGGESTION', status: 'CONFIRMED', createdAt: activity.createdAt })
  expect(saved.updatedAt).not.toBe(activity.updatedAt)
  expect(await (await page.request.get(`/api/activities?date=${date}&zone=Europe/Ljubljana`)).json()).toHaveLength(1)
  await edit(page, 'Popravljena aktivnost')
  await expect(page.getByLabel('Opis (neobvezno)')).toHaveValue('Popravljen opis')
  await expect(page.getByLabel('Kraj (neobvezno)')).toHaveValue(otherPlace.id)
  await page.getByRole('button', { name: 'Prekliči', exact: true }).click()
  await page.getByRole('button', { name: 'Domov', exact: true }).click()
  await expect(page.getByText('Popravljena aktivnost', { exact: true })).toBeVisible()
})

test('cancel validation and failed saving preserve data and entered values', async ({ page }) => {
  const { activity } = await setup(page)
  await edit(page)
  await page.getByLabel('Naziv').fill('Ne shrani')
  await page.getByRole('button', { name: 'Prekliči', exact: true }).click()
  expect((await (await page.request.get(`/api/activities/${activity.id}`)).json()).title).toBe(activity.title)
  await edit(page)
  await page.getByLabel('Naziv').fill('')
  await page.getByRole('button', { name: 'Shrani spremembe' }).click()
  await expect(page.getByText('Naziv je obvezen.')).toBeVisible()
  expect((await (await page.request.get(`/api/activities/${activity.id}`)).json()).title).toBe(activity.title)
  await page.getByLabel('Naziv').fill('Ponovni poskus')
  await page.route(`**/api/activities/${activity.id}`, route => route.request().method() === 'PUT'
    ? route.fulfill({ status: 503, json: { message: 'Shranjevanje trenutno ni na voljo.' } }) : route.continue())
  await page.getByRole('button', { name: 'Shrani spremembe' }).click()
  await expect(page.getByText('Shranjevanje trenutno ni na voljo.')).toBeVisible()
  await expect(page.getByLabel('Naziv')).toHaveValue('Ponovni poskus')
  expect((await (await page.request.get(`/api/activities/${activity.id}`)).json()).title).toBe(activity.title)
  await page.unroute(`**/api/activities/${activity.id}`)
  let updates = 0
  await page.route(`**/api/activities/${activity.id}`, async route => {
    if (route.request().method() === 'PUT') { updates++; await new Promise(resolve => setTimeout(resolve, 500)) }
    await route.continue()
  })
  await page.getByRole('button', { name: 'Shrani spremembe' }).click()
  await expect(page.getByRole('button', { name: 'Shrani spremembe' })).toBeDisabled()
  await expect(page.getByLabel('Dnevna časovnica').getByText('Ponovni poskus')).toBeVisible()
  expect(updates).toBe(1)
})

test('self is excluded and another overlap needs explicit confirmation', async ({ page }) => {
  const { activity } = await setup(page)
  expect((await write(page, 'activities', { title: 'Druga aktivnost', startAt: '2026-11-12T10:00:00Z', endAt: '2026-11-12T11:00:00Z', categoryId: work })).status()).toBe(201)
  await edit(page)
  await page.getByRole('button', { name: 'Shrani spremembe' }).click()
  await expect(page.getByLabel('Dnevna časovnica')).toBeVisible()
  await edit(page)
  await page.getByLabel('Konec').fill(`${date}T11:30`)
  await page.getByRole('button', { name: 'Shrani spremembe' }).click()
  await expect(page.getByText('Prekrivanje s potrjeno aktivnostjo')).toBeVisible()
  await expect(page.getByRole('alert').getByText(/Druga aktivnost/)).toBeVisible()
  expect((await (await page.request.get(`/api/activities/${activity.id}`)).json()).endAt).toBe(activity.endAt)
  await page.getByRole('button', { name: 'Nadaljuj kljub prekrivanju' }).click()
  await expect(page.getByLabel('Dnevna časovnica')).toBeVisible()
  expect((await (await page.request.get(`/api/activities/${activity.id}`)).json()).endAt).toBe('2026-11-12T10:30:00Z')
})

test('midnight edit uses original interval keeps deleted place and moves between days', async ({ page }) => {
  const { activity, place } = await setup(page, true)
  expect((await write(page, `places/${place.id}`, undefined, 'DELETE')).status()).toBe(204)
  await page.reload()
  await page.getByRole('button', { name: 'Koledar', exact: true }).click()
  await page.getByLabel('Izberi dan').fill('2026-11-13')
  await edit(page)
  await expect(page.getByLabel('Začetek')).toHaveValue('2026-11-12T23:30')
  await expect(page.getByLabel('Konec')).toHaveValue('2026-11-13T02:00')
  await expect(page.getByLabel('Kraj (neobvezno)')).toHaveValue(place.id)
  await expect(page.getByLabel('Kraj (neobvezno)')).toContainText('Knjižnica (izbrisan')
  await page.getByLabel('Opis (neobvezno)').fill('Zgodovinski popravek')
  await page.getByRole('button', { name: 'Shrani spremembe' }).click()
  await expect(page.getByLabel('Izberi dan')).toHaveValue('2026-11-13')
  const saved = await (await page.request.get(`/api/activities/${activity.id}`)).json()
  expect(saved.startAt).toBe(activity.startAt)
  expect(saved.endAt).toBe(activity.endAt)
  expect(saved.placeId).toBe(place.id)
  await edit(page)
  await page.getByLabel('Začetek').fill('2026-11-14T09:00')
  await page.getByLabel('Konec').fill('2026-11-14T10:00')
  await page.getByRole('button', { name: 'Shrani spremembe' }).click()
  await expect(page.getByLabel('Izberi dan')).toHaveValue('2026-11-13')
  await expect(page.getByText('Za izbrani dan še ni potrjenih aktivnosti.')).toBeVisible()
  await page.getByLabel('Izberi dan').fill('2026-11-14')
  await expect(page.getByLabel('Dnevna časovnica').getByText('Izvorna aktivnost')).toBeVisible()
  await expect(page.getByText('1 h 0 min', { exact: true })).toBeVisible()
})
