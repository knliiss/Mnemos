import { api } from "@/shared/api/httpClient"

export type StarPrimePurchaseStatus =
  | "CREATED"
  | "INVOICE_READY"
  | "PRE_CHECKOUT_APPROVED"
  | "PAID"
  | "FULFILLED"
  | "FULFILLMENT_FAILED"

export type StarPrimePackageResponse = {
  id: string
  primeAmount: number
  starsAmount: number
}

export type CreatedStarPrimePurchaseResponse = {
  purchaseId: string
  packageId: string
  primeAmount: number
  starsAmount: number
  status: StarPrimePurchaseStatus
  invoiceUrl: string
}

export type StarPrimePurchaseResponse = {
  purchaseId: string
  packageId: string
  primeAmount: number
  starsAmount: number
  status: StarPrimePurchaseStatus
}

export async function getStarPrimePackages(): Promise<StarPrimePackageResponse[]> {
  return api<StarPrimePackageResponse[]>("/api/v1/payments/stars/prime/catalog")
}

export async function createStarPrimePurchase(
  packageId: string,
): Promise<CreatedStarPrimePurchaseResponse> {
  return api<CreatedStarPrimePurchaseResponse>("/api/v1/payments/stars/prime/purchases", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ packageId }),
  })
}

export async function getStarPrimePurchase(
  purchaseId: string,
): Promise<StarPrimePurchaseResponse> {
  return api<StarPrimePurchaseResponse>(`/api/v1/payments/stars/prime/purchases/${purchaseId}`)
}
