import { test, expect } from '@playwright/test'

test('registers an account and displays duplicate and validation errors', async ({ page }) => {
  await page.goto('/')
  await page.getByRole('button', { name: 'Registriraj se' }).click()
  const email = `register-${crypto.randomUUID()}@example.com`
  await page.getByLabel('E-pošta').fill(email)
  await page.getByLabel('Geslo', { exact: true }).fill('long-password-123')
  await page.getByRole('button', { name: 'Ustvari račun' }).click()
  await expect(page.getByRole('status')).toContainText('Račun je ustvarjen')
  await page.getByRole('button', { name: 'Registriraj se' }).click()
  await page.getByLabel('Geslo', { exact: true }).fill('long-password-123')
  await page.getByRole('button', { name: 'Ustvari račun' }).click()
  await expect(page.getByRole('alert')).toContainText('že obstaja')
  await page.getByLabel('E-pošta').fill(`utf-${crypto.randomUUID()}@example.com`)
  await page.getByLabel('Geslo', { exact: true }).fill('ž'.repeat(37))
  await page.getByRole('button', { name: 'Ustvari račun' }).click()
  await expect(page.getByText('Geslo lahko vsebuje največ')).toBeVisible()
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth)).toBe(true)
})
