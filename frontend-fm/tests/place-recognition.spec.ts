import { test, expect, type Page } from '@playwright/test'

async function login(page: Page) {
  const email = `recognition-${crypto.randomUUID()}@example.com`
  const csrf = await (await page.request.get('/api/auth/csrf')).json()
  const registration = await page.request.post('/api/auth/register', { headers: { [csrf.headerName]: csrf.token }, data: { email, password: 'long-password-123' } })
  expect(registration.ok()).toBeTruthy()
  await page.goto('/')
  await page.getByLabel('E-pošta').fill(email)
  await page.getByLabel('Geslo', { exact: true }).fill('long-password-123')
  await page.getByRole('button', { name: 'Prijava' }).click()
  await expect(page.getByRole('button', { name: 'Preveri trenutni kraj' })).toBeVisible()
}

test('shows nearest match and allows explicit selection in activity form', async ({ page, context }) => {
  await context.grantPermissions(['geolocation'])
  await context.setGeolocation({ latitude: 46.55, longitude: 15.64 })
  let placeId = ''
  await page.route('**/api/places/recognition?*', route => route.fulfill({ json: {
    recognized: true, candidates: [{ id: placeId, name: 'FERI', distanceM: 12, radiusM: 100 }],
  } }))
  await login(page)
  const csrf = await (await page.request.get('/api/auth/csrf')).json()
  const place = await page.request.post('/api/places', { headers: { [csrf.headerName]: csrf.token },
    data: { name: 'FERI', latitude: 46.55, longitude: 15.64, radiusM: 100 } })
  placeId = (await place.json()).id
  await page.reload()
  await expect(page.getByText('Predlagani kraj:')).toBeVisible()
  await expect(page.getByText(/Natančnost meritve: .* m/)).toBeVisible()
  await page.getByRole('button', { name: 'Koledar' }).click()
  await page.getByRole('button', { name: 'Dodaj aktivnost' }).click()
  await expect(page.getByText('FERI (12 m)')).toBeVisible()
  await page.getByRole('button', { name: 'Izberi FERI' }).click()
  await expect(page.getByLabel('Kraj (neobvezno)')).toHaveValue(placeId)
})

test('handles ties and no match', async ({ page, context }) => {
  await login(page)
  let tied = true
  let requests = 0
  await page.route('**/api/places/recognition?*', route => { requests++; return route.fulfill({ json: tied
    ? { recognized: true, candidates: [
      { id: 'a', name: 'Dom', distanceM: 0, radiusM: 100 },
      { id: 'b', name: 'Pisarna', distanceM: 0, radiusM: 100 },
    ] }
    : { recognized: false, candidates: [] } }) })
  expect(requests).toBe(0)
  await context.grantPermissions(['geolocation'])
  await context.setGeolocation({ latitude: 46.55, longitude: 15.64 })
  await page.getByRole('button', { name: 'Preveri trenutni kraj' }).click()
  await expect(page.getByText('Enako blizu je več krajev. Kraj izberi pri vnosu aktivnosti.')).toBeVisible()
  tied = false
  await page.getByRole('button', { name: 'Preveri trenutni kraj' }).click()
  await expect(page.getByText('Znani kraj ni prepoznan.')).toBeVisible()
})

test('API failure shows an error and keeps manual choice', async ({ page, context }) => {
  await login(page)
  await context.grantPermissions(['geolocation'])
  await context.setGeolocation({ latitude: 46.55, longitude: 15.64 })
  await page.route('**/api/places/recognition?*', route => route.fulfill({ status: 503, json: {} }))
  await page.getByRole('button', { name: 'Preveri trenutni kraj' }).click()
  await expect(page.getByText('Preverjanje kraja ni uspelo.')).toBeVisible()
  await page.getByRole('button', { name: 'Koledar' }).click()
  await page.getByRole('button', { name: 'Dodaj aktivnost' }).click()
  await expect(page.getByLabel('Kraj (neobvezno)')).toBeVisible()
})

test('permission denial and measurement error keep manual place choice', async ({ page }) => {
  await login(page)
  await page.evaluate(() => { navigator.geolocation.getCurrentPosition = (_ok, fail) => fail?.({ code: 1 } as GeolocationPositionError) })
  await page.getByRole('button', { name: 'Preveri trenutni kraj' }).click()
  await expect(page.getByText('Dovoljenje za lokacijo je zavrnjeno.')).toBeVisible()
  await page.evaluate(() => { navigator.geolocation.getCurrentPosition = (_ok, fail) => fail?.({ code: 2 } as GeolocationPositionError) })
  await page.getByRole('button', { name: 'Preveri trenutni kraj' }).click()
  await expect(page.getByText('Trenutna lokacija ni dosegljiva.')).toBeVisible()
  await page.getByRole('button', { name: 'Koledar' }).click()
  await page.getByRole('button', { name: 'Dodaj aktivnost' }).click()
  await expect(page.getByLabel('Kraj (neobvezno)')).toBeVisible()
})
