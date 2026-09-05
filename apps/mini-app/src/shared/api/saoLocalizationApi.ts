import type { SaoLocalizationPackResponse } from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"

export async function getSaoLocalization(locale: string): Promise<SaoLocalizationPackResponse> {
  return api<SaoLocalizationPackResponse>(
    `/api/v1/events/localizations/sao/${encodeURIComponent(locale)}`,
  )
}
