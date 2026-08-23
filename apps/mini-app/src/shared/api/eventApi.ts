import type { ItemRarity } from "@/ui/model"
import type {
  EventFeedItemResponse,
  EventFeedResponse,
  EventFeedType,
  ItemCatalogResponse,
} from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"

export type EventFeedQuery = {
  after?: string | null
  types?: EventFeedType[]
  search?: string
  droppedFor?: string
  itemRarity?: ItemRarity
  sort?: "NEWEST" | "OLDEST"
  limit?: number
}

export async function getEventFeed(query: EventFeedQuery = {}): Promise<EventFeedResponse> {
  const params = new URLSearchParams()

  if (query.after) {
    params.set("after", query.after)
  }

  query.types?.forEach((type) => params.append("type", type))

  if (query.search?.trim()) {
    params.set("search", query.search.trim())
  }

  if (query.droppedFor?.trim()) {
    params.set("droppedFor", query.droppedFor.trim())
  }

  if (query.itemRarity) {
    params.set("itemRarity", query.itemRarity)
  }

  params.set("sort", query.sort ?? "NEWEST")
  params.set("limit", String(query.limit ?? 50))

  return api<EventFeedResponse>(`/api/v1/events/feed?${params}`)
}

export async function getEventFeedItem(eventId: string): Promise<EventFeedItemResponse> {
  return api<EventFeedItemResponse>(`/api/v1/events/feed/${eventId}`)
}

export async function searchItemCatalog(search: string, limit = 8): Promise<ItemCatalogResponse[]> {
  const params = new URLSearchParams({
    search: search.trim(),
    limit: String(limit),
  })

  return api<ItemCatalogResponse[]>(`/api/v1/events/items?${params}`)
}
