import type {
  SaoCatalogItemPageResponse,
  SaoCatalogItemRarity,
  SaoCatalogItemType,
} from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"

export type SaoCatalogSearch = {
  search?: string
  type?: SaoCatalogItemType
  rarity?: SaoCatalogItemRarity
  locationId?: number
  page?: number
  size?: number
}

export async function getSaoCatalogItems(
  filters: SaoCatalogSearch = {},
): Promise<SaoCatalogItemPageResponse> {
  const params = new URLSearchParams()

  if (filters.search?.trim()) {
    params.set("search", filters.search.trim())
  }

  if (filters.type) {
    params.set("type", filters.type)
  }

  if (filters.rarity) {
    params.set("rarity", filters.rarity)
  }

  if (filters.locationId !== undefined) {
    params.set("locationId", String(filters.locationId))
  }

  if (filters.page !== undefined) {
    params.set("page", String(filters.page))
  }

  if (filters.size !== undefined) {
    params.set("size", String(filters.size))
  }

  const query = params.toString()

  return api<SaoCatalogItemPageResponse>(`/api/v1/events/catalog/items${query ? `?${query}` : ""}`)
}
