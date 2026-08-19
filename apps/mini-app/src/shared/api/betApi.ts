import { api } from "@/shared/api/httpClient"
import type { BetGlobalEventType, BetItemType, BetSide } from "@/product-ui/model"

export type EventBetStatus =
  | "ACTIVE"
  | "SETTLEMENT_PENDING"
  | "SETTLEMENT_FAILED"
  | "SETTLED"
  | "REFUND_PENDING"
  | "REFUND_FAILED"
  | "REFUNDED"

export type EventBetRoundStatus =
  | "OPEN"
  | "LOCKED"
  | "AWAITING_RESULT"
  | "AWAITING_ADMIN_CONFIRMATION"
  | "SETTLED"

export type EventBetOutcome = "YES" | "NO" | "REFUND"

export type EventBetResponse = {
  id: string
  roundId: string
  side: BetSide
  confirmedAmount: number
  pendingAmount: number
  status: EventBetStatus
}

export type EventBetRoundResponse = {
  id: string
  eventType: BetGlobalEventType
  scheduledAt: string
  closesAt: string
  resolvesAt: string
  yesOdds: number
  noOdds: number
  status: EventBetRoundStatus
  outcome: EventBetOutcome | null
  myBet: EventBetResponse | null
}

export type ItemRaceStatus =
  | "REGISTRATION"
  | "ACTIVE"
  | "SETTLING"
  | "SETTLED"
  | "REFUNDING"
  | "REFUNDED"
  | "CANCELLED"

export type ItemRaceParticipantStatus = string

export type ItemRaceParticipantResponse = {
  id: string
  raceId: string
  accountId: string
  accountNickname: string
  confirmedAmount: number
  pendingAmount: number
  status: ItemRaceParticipantStatus
}

export type ItemRaceResponse = {
  id: string
  itemName: string
  itemType: BetItemType
  registrationClosesAt: string
  startsAt: string
  expiresAt: string
  entryAmount: number
  feePercent: number
  status: ItemRaceStatus
  winnerAccountId: string | null
  participants: ItemRaceParticipantResponse[]
}

export type AcceptedBetOperationResponse = {
  resourceId: string
  operationId: string
}

export async function listEventBetRounds(): Promise<EventBetRoundResponse[]> {
  return api<EventBetRoundResponse[]>("/api/v1/bets/event-rounds")
}

export async function placeEventBet(
  roundId: string,
  side: BetSide,
  amount: number,
): Promise<AcceptedBetOperationResponse> {
  return betWrite("/api/v1/bets/event-bets", "POST", {
    roundId,
    side,
    amount,
  })
}

export async function increaseEventBet(
  roundId: string,
  amount: number,
): Promise<AcceptedBetOperationResponse> {
  return betWrite("/api/v1/bets/event-bets/increase", "PATCH", {
    roundId,
    amount,
  })
}

export async function listItemRaces(): Promise<ItemRaceResponse[]> {
  return api<ItemRaceResponse[]>("/api/v1/bets/item-races")
}

export async function joinItemRace(raceId: string): Promise<AcceptedBetOperationResponse> {
  return betWrite("/api/v1/bets/item-race-participants", "POST", { raceId })
}

export async function increaseItemRaceBet(
  raceId: string,
  amount: number,
): Promise<AcceptedBetOperationResponse> {
  return betWrite("/api/v1/bets/item-race-participants/increase", "PATCH", {
    raceId,
    amount,
  })
}

async function betWrite(
  path: string,
  method: "POST" | "PATCH",
  body: unknown,
): Promise<AcceptedBetOperationResponse> {
  return api<AcceptedBetOperationResponse>(path, {
    method,
    headers: {
      "Content-Type": "application/json",
      "Idempotency-Key": crypto.randomUUID(),
    },
    body: JSON.stringify(body),
  })
}
