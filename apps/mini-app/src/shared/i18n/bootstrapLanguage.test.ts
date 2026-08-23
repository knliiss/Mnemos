import { describe, expect, it } from "vitest"
import { normalizeLanguage, resolveBootstrapLanguage } from "@/shared/i18n/bootstrapLanguage"

describe("bootstrap language", () => {
  it("prefers the Telegram user language", () => {
    expect(resolveBootstrapLanguage("uk-UA", "en-US")).toBe("uk")
  })

  it("falls back to the browser language when Telegram has no supported language", () => {
    expect(resolveBootstrapLanguage("de-DE", "en-US")).toBe("en")
  })

  it("uses Russian when no supported localization can be resolved", () => {
    expect(resolveBootstrapLanguage("de-DE", "pl-PL")).toBe("ru")
  })

  it("normalizes the Ukrainian ua alias", () => {
    expect(normalizeLanguage("ua")).toBe("uk")
  })
})
