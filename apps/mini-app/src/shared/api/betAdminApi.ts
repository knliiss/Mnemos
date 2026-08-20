import type { BetItemType } from "@/ui/model"
import { api } from "@/shared/api/httpClient"
import type { EventBetOutcome } from "@/shared/api/betApi"

export async function resolveEventBetRound(
  roundId: string,
  outcome: EventBetOutcome,
  reason: string,
): Promise<void> {
  await api<void>(`/api/v1/bets/admin/event-rounds/${roundId}/resolution`, {
    method: "POST",
    body: JSON.stringify({
      outcome,
      reason,
    }),
  })
}

export async function createItemRace(request: {
  itemName: string
  itemType: BetItemType
  durationHours: number
  entryAmount: number
}): Promise<string> {
  return api<string>("/api/v1/bets/admin/item-races", {
    method: "POST",
    body: JSON.stringify(request),
  })
}

export async function cancelItemRace(raceId: string): Promise<void> {
  await api<void>(`/api/v1/bets/admin/item-races/${raceId}`, {
    method: "DELETE",
  })
}
