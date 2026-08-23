import type { LocalizationLanguage } from "@/ui/model"
import type { CurrentContextResponse, UserResponse } from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"

export async function initializeCurrentUser(): Promise<UserResponse> {
  return api<UserResponse>("/api/v1/users/me", { method: "PUT" })
}

export async function getCurrentContext(): Promise<CurrentContextResponse> {
  return api<CurrentContextResponse>("/api/v1/me/context")
}

export async function updateCurrentLanguage(
  localizationLanguage: LocalizationLanguage,
): Promise<UserResponse> {
  return api<UserResponse>("/api/v1/users/me/localization", {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ localizationLanguage }),
  })
}
