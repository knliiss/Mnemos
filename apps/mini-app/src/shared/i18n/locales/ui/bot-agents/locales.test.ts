import { describe, expect, it } from "vitest"
import { botAgentsEN } from "./en"
import { botAgentsRU } from "./ru"
import { botAgentsUK } from "./uk"

const interpolationKeys = (message: string) =>
  [...message.matchAll(/{{\\s*([a-zA-Z]+)\\s*}}/g)]
    .map((match) => match[1])
    .sort()

describe("C7x bot manager localizations", () => {
  it("has matching nonempty labels in all three languages", () => {
    const keys = Object.keys(botAgentsUK).sort()
    expect(keys.length).toBeGreaterThan(100)
    expect(Object.keys(botAgentsRU).sort()).toEqual(keys)
    expect(Object.keys(botAgentsEN).sort()).toEqual(keys)
    for (const key of keys) {
      const uk = botAgentsUK[key as keyof typeof botAgentsUK]
      const ru = botAgentsRU[key as keyof typeof botAgentsRU]
      const en = botAgentsEN[key as keyof typeof botAgentsEN]
      expect(uk.trim()).not.toBe("")
      expect(ru.trim()).not.toBe("")
      expect(en.trim()).not.toBe("")
      expect(interpolationKeys(ru)).toEqual(interpolationKeys(uk))
      expect(interpolationKeys(en)).toEqual(interpolationKeys(uk))
    }
  })
})
