import { api } from "@/shared/api/httpClient"
import type { OwnedPersonalizationResponse } from "@/shared/api/personalizationApi"

export async function getAdminAccountPersonalizations(
  accountId: string,
): Promise<OwnedPersonalizationResponse[]> {
  return api<OwnedPersonalizationResponse[]>(
    `/api/v1/admin/accounts/${accountId}/personalizations`,
  )
}

export async function grantAdminAccountPersonalization(
  accountId: string,
  personalizationId: string,
): Promise<OwnedPersonalizationResponse> {
  return api<OwnedPersonalizationResponse>(
    `/api/v1/admin/accounts/${accountId}/personalizations/${personalizationId}`,
    { method: "PUT" },
  )
}

export async function activateAdminAccountPersonalization(
  accountId: string,
  personalizationId: string,
): Promise<OwnedPersonalizationResponse> {
  return api<OwnedPersonalizationResponse>(
    `/api/v1/admin/accounts/${accountId}/personalizations/${personalizationId}/active`,
    { method: "PUT" },
  )
}

export async function deactivateAdminAccountPersonalization(
  accountId: string,
  personalizationId: string,
): Promise<OwnedPersonalizationResponse> {
  return api<OwnedPersonalizationResponse>(
    `/api/v1/admin/accounts/${accountId}/personalizations/${personalizationId}/active`,
    { method: "DELETE" },
  )
}

export async function revokeAdminAccountPersonalization(
  accountId: string,
  personalizationId: string,
): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/personalizations/${personalizationId}`, {
    method: "DELETE",
  })
}
