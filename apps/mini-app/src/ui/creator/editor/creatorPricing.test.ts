import { describe, expect, it } from "vitest"
import type { PersonalizationPricingResponse } from "@/shared/api/personalizationApi"
import type { CreatorEditorForm } from "./creatorEditorModel"
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
    type: "NICK_STYLE",
    name: "Example",
    currency: "PRIME",
    price: "25",
    nickStyle: {
      colors: [
        { color: "#CBFF2D", position: 0 },
        { color: "#B98DC2", position: 100 },
      ],
      angleDegrees: 45,
      glow: {
        enabled: true,
        color: "#CBFF2D",
        strength: 48,
      },
    },
    profileTheme: {
      background: "#080B08",
      backgroundSecondary: "#151B0E",
      surface: "#151812",
      surfaceSecondary: "#20251A",
      accent: "#CBFF2D",
      text: "#F3F5EC",
      border: "#343B2E",
      gradientAngleDegrees: 145,
      textureStrength: 24,
      glow: {
        enabled: true,
        color: "#CBFF2D",
        strength: 28,
      },
    },
    avatarFrame: {
      borderColor: "#CBFF2D",
      secondaryBorderColor: "#B98DC2",
      borderWidthPx: 3,
      gapPx: 2,
      radiusPx: 14,
      style: "DOUBLE",
      glow: {
        enabled: true,
        color: "#CBFF2D",
        strength: 42,
      },
    },
    profileEffect: {
      effect: "AURORA",
      intensity: 58,
      speed: 42,
      density: 48,
      scale: 60,
      primaryColor: "#CBFF2D",
      secondaryColor: "#B98DC2",
    },
    notificationPack: {
      mixedAggregateTitle: "Several events",
      templates: {},
    },
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
