import { describe, expect, it } from "vitest"
import type { PersonalizationPricingResponse } from "@/shared/api/personalizationApi"
import { createDefaultCreatorEditorForm, type CreatorEditorForm } from "./creatorEditorModel"
import {
  isCreatorPriceValid,
  normalizeCreatorPrice,
  resolveCreatorPriceRange,
} from "./creatorPricing"

const pricing: PersonalizationPricingResponse = {
  ranges: [
    { type: "NICK_STYLE", origin: "CREATOR", currency: "PRIME", min: 5, max: 500 },
    { type: "NICK_STYLE", origin: "CREATOR", currency: "COIN", min: 500, max: 50_000 },
    { type: "PROFILE_THEME", origin: "CREATOR", currency: "PRIME", min: 10, max: 1_000 },
    {
      type: "PROFILE_THEME",
      origin: "CREATOR",
      currency: "COIN",
      min: 1_000,
      max: 100_000,
    },
    {
      type: "NOTIFICATION_PACK",
      origin: "CREATOR",
      currency: "PRIME",
      min: 10,
      max: 500,
    },
  ],
  customNotificationPackPurchasePrice: {
    currency: "PRIME",
    amount: 50,
  },
}

function form(overrides: Partial<CreatorEditorForm> = {}): CreatorEditorForm {
  return {
    ...createDefaultCreatorEditorForm(),
    name: "Example",
    ...overrides,
  }
}

describe("creator pricing", () => {
  it("resolves the backend creator range for type and currency", () => {
    expect(resolveCreatorPriceRange(pricing, "PROFILE_THEME", "COIN")).toEqual({
      type: "PROFILE_THEME",
      origin: "CREATOR",
      currency: "COIN",
      min: 1_000,
      max: 100_000,
    })
  })

  it("moves an invalid price to the backend minimum after currency change", () => {
    expect(
      normalizeCreatorPrice(
        form({
          type: "PROFILE_THEME",
          currency: "COIN",
          price: "25",
        }),
        pricing,
      ).price,
    ).toBe("1000")
  })

  it("keeps a price that remains valid for the selected range", () => {
    expect(
      normalizeCreatorPrice(
        form({
          type: "PROFILE_THEME",
          currency: "PRIME",
          price: "25",
        }),
        pricing,
      ).price,
    ).toBe("25")
  })

  it("forces notification packs to PRIME pricing", () => {
    const normalized = normalizeCreatorPrice(
      form({
        type: "NOTIFICATION_PACK",
        currency: "COIN",
        price: "25",
      }),
      pricing,
    )

    expect(normalized.currency).toBe("PRIME")
    expect(isCreatorPriceValid(normalized, pricing)).toBe(true)
  })

  it("rejects prices outside the backend range", () => {
    expect(
      isCreatorPriceValid(
        form({
          type: "PROFILE_THEME",
          currency: "COIN",
          price: "25",
        }),
        pricing,
      ),
    ).toBe(false)
  })
})
