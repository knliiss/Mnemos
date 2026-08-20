import type {
  BoosterType,
  DungeonType,
  GlobalEventType,
} from "@/ui/model"
import type { PageResponse } from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"
import type { SubscriptionResponse } from "@/shared/api/subscriptionContracts"

export async function getSubscriptions(
  page = 0,
  size = 100,
): Promise<PageResponse<SubscriptionResponse>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })

  return api<PageResponse<SubscriptionResponse>>(`/api/v1/subscriptions?${params}`)
}

export async function createItemDropSubscription(
  droppedFor?: string,
  itemName?: string,
): Promise<void> {
  await postJson("/api/v1/subscriptions/item-drops", {
    droppedFor: droppedFor?.trim() || null,
    itemName: itemName?.trim() || null,
  })
}

export async function createBoosterSubscription(boosterType: BoosterType): Promise<void> {
  await postJson("/api/v1/subscriptions/boosters", { boosterType })
}

export async function createDungeonSubscription(dungeonType: DungeonType): Promise<void> {
  await postJson("/api/v1/subscriptions/dungeons", { dungeonType })
}

export async function createGlobalSubscription(eventType: GlobalEventType): Promise<void> {
  await postJson("/api/v1/subscriptions/global-events", { eventType })
}

export async function createRaidSubscription(locations: number[]): Promise<void> {
  await postJson("/api/v1/subscriptions/raids", { locations })
}

export async function deleteSubscription(subscriptionId: string): Promise<void> {
  await api<void>(`/api/v1/subscriptions/${subscriptionId}`, { method: "DELETE" })
}

async function postJson(path: string, body: unknown): Promise<void> {
  await api<unknown>(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  })
}
