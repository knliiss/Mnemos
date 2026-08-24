import { beforeEach, describe, expect, it, vi } from "vitest"
import {
  createDropSpotlight,
  getDropSpotlight,
  getDropSpotlightOffer,
} from "@/shared/api/spotlightApi"
import { api } from "@/shared/api/httpClient"

vi.mock("@/shared/api/httpClient", () => ({
  api: vi.fn(),
}))

const apiMock = vi.mocked(api)

describe("spotlightApi", () => {
  beforeEach(() => {
    apiMock.mockReset()
  })

  it("loads the current spotlight offer", async () => {
    apiMock.mockResolvedValue({
      pricePrime: 25,
      purchaseWindowSeconds: 7200,
      buyerCooldownSeconds: 1200,
    })

    await getDropSpotlightOffer()

    expect(apiMock).toHaveBeenCalledWith("/api/v1/drop-spotlights/offer")
  })

  it("creates a spotlight with a stable idempotency key", async () => {
    apiMock.mockResolvedValue({
      id: "spotlight-id",
      itemDropEventId: "drop-id",
      sourceAccountId: "account-id",
      pricePrime: 25,
      scheduledAt: "2026-08-25T00:00:00Z",
      status: "PENDING_PAYMENT",
    })

    await createDropSpotlight("drop-id", "11111111-1111-1111-1111-111111111111")

    expect(apiMock).toHaveBeenCalledWith("/api/v1/drop-spotlights/drop-id", {
      method: "POST",
      headers: {
        "Idempotency-Key": "11111111-1111-1111-1111-111111111111",
      },
    })
  })

  it("loads the latest spotlight status", async () => {
    apiMock.mockResolvedValue({
      id: "spotlight-id",
      itemDropEventId: "drop-id",
      sourceAccountId: "account-id",
      pricePrime: 25,
      scheduledAt: "2026-08-25T00:00:00Z",
      status: "QUEUED",
    })

    await getDropSpotlight("spotlight-id")

    expect(apiMock).toHaveBeenCalledWith("/api/v1/drop-spotlights/spotlight-id")
  })
})
