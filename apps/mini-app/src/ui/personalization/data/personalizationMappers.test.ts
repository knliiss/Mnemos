import { describe, expect, it } from "vitest"
import type { PersonalizationResponse, VisualLayerResponse } from "@/shared/api/personalizationApi"
import { toPersonalization } from "./personalizationMappers"

describe("personalization mappers", () => {
  it("preserves uploaded visual layers in profile themes", () => {
    const layer = {
      id: "layer-1",
      assetId: "019d0000-0000-7000-8000-000000000003",
      assetUrl: "/api/v1/personalizations/assets/019d0000-0000-7000-8000-000000000003/content",
      xPercent: 42,
      yPercent: 58,
      widthPercent: 72,
      opacity: 84,
      rotationDegrees: -12,
      blendMode: "SCREEN",
      animation: "FLOAT",
      zIndex: 4,
    } satisfies VisualLayerResponse
    const response: PersonalizationResponse = {
      id: "019d0000-0000-7000-8000-000000000001",
      creatorUserId: "019d0000-0000-7000-8000-000000000002",
      type: "PROFILE_THEME",
      origin: "CREATOR",
      visibility: "PUBLIC",
      name: "Layered theme",
      content: {
        background: "#080B08",
        backgroundSecondary: "#151B0E",
        surface: "#151812",
        surfaceSecondary: "#20251A",
        accent: "#CBFF2D",
        text: "#F3F5EC",
        border: "#343B2E",
        gradientAngleDegrees: 145,
        textureStrength: 24,
        glow: null,
        layers: [layer],
      },
      price: {
        currency: "PRIME",
        amount: 25,
      },
      status: "PUBLISHED",
      rejectionReason: null,
      createdAt: "2026-08-23T12:00:00Z",
      updatedAt: "2026-08-23T12:00:00Z",
    }

    const personalization = toPersonalization(response, null)

    expect(personalization.content).toMatchObject({
      kind: "PROFILE_THEME",
      layers: [layer],
    })
  })
})
