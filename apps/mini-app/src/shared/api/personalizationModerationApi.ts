import type { Currency } from "@/ui/model"
import { api } from "@/shared/api/httpClient"
import type { PersonalizationResponse } from "@/shared/api/personalizationApi"

export async function listPersonalizationReviewQueue(): Promise<PersonalizationResponse[]> {
  return api<PersonalizationResponse[]>("/api/v1/personalizations/moderation/review")
}

export async function listManagedPersonalizations(): Promise<PersonalizationResponse[]> {
  return api<PersonalizationResponse[]>("/api/v1/personalizations/moderation/managed")
}

export async function approvePersonalization(
  personalizationId: string,
  currency: Currency,
  amount: number,
): Promise<PersonalizationResponse> {
  return api<PersonalizationResponse>(
    `/api/v1/personalizations/moderation/${personalizationId}/approve`,
    {
      method: "POST",
      body: JSON.stringify({
        price: {
          currency,
          amount,
        },
      }),
    },
  )
}

export async function rejectPersonalization(
  personalizationId: string,
  reason: string,
): Promise<PersonalizationResponse> {
  return api<PersonalizationResponse>(
    `/api/v1/personalizations/moderation/${personalizationId}/reject`,
    {
      method: "POST",
      body: JSON.stringify({ reason }),
    },
  )
}

export async function unlistPersonalization(
  personalizationId: string,
): Promise<PersonalizationResponse> {
  return api<PersonalizationResponse>(
    `/api/v1/personalizations/moderation/${personalizationId}/unlist`,
    {
      method: "POST",
    },
  )
}

export async function relistPersonalization(
  personalizationId: string,
): Promise<PersonalizationResponse> {
  return api<PersonalizationResponse>(
    `/api/v1/personalizations/moderation/${personalizationId}/relist`,
    {
      method: "POST",
    },
  )
}
