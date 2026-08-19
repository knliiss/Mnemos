import { describe, expect, it } from "vitest"
import { en } from "./en"
import { ru } from "./ru"
import { uk } from "./uk"

function leafKeys(value: unknown, prefix = ""): string[] {
  if (!value || typeof value !== "object" || Array.isArray(value)) {
    return prefix ? [prefix] : []
  }

  return Object.entries(value as Record<string, unknown>).flatMap(([key, child]) =>
    leafKeys(child, prefix ? `${prefix}.${key}` : key),
  )
}

describe("locales", () => {
  it("keeps UK, RU and EN translation trees in sync", () => {
    const ukKeys = leafKeys(uk).sort()
    const ruKeys = leafKeys(ru).sort()
    const enKeys = leafKeys(en).sort()

    expect(ruKeys).toEqual(ukKeys)
    expect(enKeys).toEqual(ukKeys)
  })
})
