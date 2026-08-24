import type { EventBetOutcome, EventBetRoundResponse, ItemRaceResponse } from "@/shared/api/betApi"
import { api } from "@/shared/api/httpClient"
import type { BetItemType } from "@/ui/model"

export async function listAdminEventBetRounds(): Promise<EventBetRoundResponse[]> {
  return api<EventBetRoundResponse[]>("/api/v1/bets/admin/event-rounds")
}

export async function resolveEventBetRound(
  roundId: string,
  outcome: EventBetOutcome,
  reason: string,
): Promise<void> {
  await api<void>(`/api/v1/bets/admin/event-rounds/${roundId}/resolution`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      outcome,
      reason,
    }),
  })
}

export async function listAdminItemRaces(): Promise<ItemRaceResponse[]> {
  return api<ItemRaceResponse[]>("/api/v1/bets/admin/item-races")
}

export async function createItemRace(request: {
  itemName: string
  itemType: BetItemType
  durationHours: number
  entryAmount: number
}): Promise<string> {
  return api<string>("/api/v1/bets/admin/item-races", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  })
}

export async function cancelItemRace(raceId: string): Promise<void> {
  await api<void>(`/api/v1/bets/admin/item-races/${raceId}`, {
    method: "DELETE",
  })
}
