import { describe, expect, it } from "vitest"
import { resolveBootstrapLanguage } from "@/shared/i18n/bootstrapLanguage"

describe("bootstrap language", () => {
  it("uses Russian as the default interface language", () => {
    expect(resolveBootstrapLanguage()).toBe("ru")
  })
})
