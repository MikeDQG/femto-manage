import { test, expect } from '@playwright/test'

test('home spacing and appearance controls on both screen sizes', async ({ page }, testInfo) => {
  const email = `ui-${crypto.randomUUID()}@example.com`
  const csrf = await (await page.request.get('/api/auth/csrf')).json()
  const registered = await page.request.post('/api/auth/register', { headers: { [csrf.headerName]: csrf.token }, data: { email, password: 'long-password-123' } })
  expect(registered.ok()).toBeTruthy()
  await page.goto('/')
  await page.getByLabel('E-pošta').fill(email)
  await page.getByLabel('Geslo', { exact: true }).fill('long-password-123')
  await page.getByRole('button', { name: 'Prijava' }).click()
  await expect(page.getByText('Trenutni kraj', { exact: true })).toBeVisible()
  const gaps = await page.locator('.home-cards').evaluate(element => {
    const grid = element.querySelector('.grid')!
    const first = grid.children[0].getBoundingClientRect(), second = grid.children[1].getBoundingClientRect()
    const next = element.children[1].getBoundingClientRect()
    return { first: second.top > first.top + 2 ? second.top - first.bottom : second.left - first.right, next: next.top - grid.getBoundingClientRect().bottom }
  })
  expect(gaps.next).toBeCloseTo(gaps.first)
  await page.screenshot({ path: testInfo.outputPath('home.png'), fullPage: true })
  for (const [nav, heading] of [['Koledar', 'Časovnica.'], ['Cilji', 'Cilji & kategorije.'], ['Nastavitve', 'Nastavitve.']]) {
    await page.getByRole('button', { name: nav, exact: true }).click()
    await expect(page.getByRole('heading', { name: heading, exact: true })).toBeVisible()
    await expect(page.getByText('FEMTO · TVOJ PROSTOR', { exact: true })).toHaveCount(0)
    if (nav === 'Koledar') await expect(page.getByText('Trajanje po kategorijah', { exact: true })).toBeVisible()
    await page.screenshot({ path: testInfo.outputPath(`${nav}.png`), fullPage: true })
  }
  await expect(page.getByText('Videz', { exact: true })).toHaveCount(1)
  await expect(page.getByText('Vzorčni podatki · brez povezave')).toHaveCount(0)
  await page.getByRole('button', { name: 'Temna', exact: true }).click()
  await expect(page.locator('.app')).toHaveAttribute('data-theme', 'dark')
  await page.reload()
  await expect(page.locator('.app')).toHaveAttribute('data-theme', 'dark')
  await page.getByRole('button', { name: 'Nastavitve', exact: true }).click()
  await page.getByRole('button', { name: 'Svetla', exact: true }).click()
  await expect(page.locator('.app')).toHaveAttribute('data-theme', 'light')
})
