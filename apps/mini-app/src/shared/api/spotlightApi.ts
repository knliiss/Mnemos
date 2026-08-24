import { api } from "@/shared/api/httpClient"

export type DropSpotlightStatus = "PENDING_PAYMENT" | "QUEUED" | "PUBLISHED" | "PAYMENT_FAILED"

export type DropSpotlightOfferResponse = {
  pricePrime: number
  purchaseWindowSeconds: number
  buyerCooldownSeconds: number
}

export type DropSpotlightResponse = {
  id: string
  itemDropEventId: string
  sourceAccountId: string
  pricePrime: number
  scheduledAt: string
  status: DropSpotlightStatus
}

export async function getDropSpotlightOffer(): Promise<DropSpotlightOfferResponse> {
  return api<DropSpotlightOfferResponse>("/api/v1/drop-spotlights/offer")
}

export async function createDropSpotlight(
  itemDropEventId: string,
  idempotencyKey = crypto.randomUUID(),
): Promise<DropSpotlightResponse> {
  return api<DropSpotlightResponse>(`/api/v1/drop-spotlights/${itemDropEventId}`, {
    method: "POST",
    headers: {
      "Idempotency-Key": idempotencyKey,
    },
  })
}

export async function getDropSpotlight(spotlightId: string): Promise<DropSpotlightResponse> {
  return api<DropSpotlightResponse>(`/api/v1/drop-spotlights/${spotlightId}`)
}
