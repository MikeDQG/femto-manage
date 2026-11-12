import { test, expect, type Page } from '@playwright/test'

test.use({ timezoneId: 'Europe/Ljubljana' })

async function account(page: Page) {
  const email = `calendar-${crypto.randomUUID()}@example.com`
  const csrf = await (await page.request.get('/api/auth/csrf')).json()
  const response = await page.request.post('/api/auth/register', { headers: { [csrf.headerName]: csrf.token },
    data: { email, password: 'long-password-123' } })
  expect(response.ok()).toBeTruthy()
  await page.goto('/')
  await page.getByLabel('E-pošta').fill(email)
  await page.getByLabel('Geslo', { exact: true }).fill('long-password-123')
  await page.getByRole('button', { name: 'Prijava' }).click()
  await expect(page.getByRole('button', { name: 'Koledar' })).toBeVisible()
}

async function addActivity(page: Page, title: string, startAt: string, endAt: string) {
  const csrf = await (await page.request.get('/api/auth/csrf')).json()
  const response = await page.request.post('/api/activities', { headers: { [csrf.headerName]: csrf.token },
    data: { title, startAt, endAt, categoryId: '00000000-0000-0000-0000-000000000001' } })
  expect(response.status()).toBe(201)
}

test('day choice, totals, empty day and midnight split', async ({ page }) => {
  await account(page)
  await addActivity(page, 'Dopoldne', '2026-11-12T08:00:00Z', '2026-11-12T09:00:00Z')
  await addActivity(page, 'Kasneje', '2026-11-12T10:00:00Z', '2026-11-12T10:30:00Z')
  await addActivity(page, 'Čez polnoč', '2026-11-12T22:30:00Z', '2026-11-12T23:30:00Z')
  await page.getByRole('button', { name: 'Koledar' }).click()
  await page.getByLabel('Izberi dan').fill('2026-11-12')
  await expect(page.getByLabel('Dnevna časovnica').getByText('Čez polnoč')).toBeVisible()
  await expect(page.getByText('Trajanje po kategorijah')).toBeVisible()
  await expect(page.getByText('2 h 0 min', { exact: true })).toBeVisible()
  await expect(page.getByText('Pri potrjenih prekrivanjih se čas lahko šteje večkrat.')).toBeVisible()
  await page.getByLabel('Izberi dan').fill('2026-11-13')
  await expect(page.getByLabel('Dnevna časovnica').getByText('Čez polnoč')).toBeVisible()
  await expect(page.getByText('30 min', { exact: true })).toBeVisible()
  await page.getByLabel('Izberi dan').fill('2026-11-15')
  await expect(page.getByText('Za izbrani dan še ni potrjenih aktivnosti.')).toBeVisible()
})

test('confirmed activity refreshes selected day without reload', async ({ page }) => {
  await account(page)
  await page.getByRole('button', { name: 'Koledar' }).click()
  const date = '2026-11-10'
  await page.getByLabel('Izberi dan').fill(date)
  await expect(page.getByText('Za izbrani dan še ni potrjenih aktivnosti.')).toBeVisible()
  await page.getByRole('button', { name: 'Dodaj aktivnost' }).click()
  await page.getByLabel('Naziv').fill('Novo delo')
  await page.getByLabel('Začetek').fill(`${date}T09:00`)
  await page.getByLabel('Konec').fill(`${date}T10:00`)
  await page.getByLabel('Kategorija').selectOption('00000000-0000-0000-0000-000000000001')
  await page.getByRole('button', { name: 'Potrdi aktivnost' }).click()
  await expect(page.getByLabel('Izberi dan')).toHaveValue(date)
  await expect(page.getByLabel('Dnevna časovnica').getByText('Novo delo')).toBeVisible()
  await expect(page.getByText('1 h 0 min', { exact: true })).toBeVisible()
})
