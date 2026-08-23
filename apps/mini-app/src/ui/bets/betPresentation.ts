import type { TFunction } from "i18next"
import type {
  EventBetResponse,
  EventBetRoundResponse,
  ItemRaceParticipantResponse,
  ItemRaceResponse,
} from "@/shared/api/betApi"

export type BetMode = "EVENTS" | "MY" | "RACES"

export const betModes: BetMode[] = ["EVENTS", "MY", "RACES"]

export function betAmount(bet: NonNullable<EventBetRoundResponse["myBet"]>) {
  return bet.confirmedAmount + bet.pendingAmount
}

export function racePositionAmount(position: ItemRaceParticipantResponse) {
  return position.confirmedAmount + position.pendingAmount
}

export function globalEventLabel(t: TFunction, eventType: EventBetRoundResponse["eventType"]) {
  return t(`events.globals.${eventType}`, { defaultValue: eventType })
}

export function itemTypeLabel(t: TFunction, itemType: ItemRaceResponse["itemType"]) {
  return t(`bets.itemTypes.${itemType}`, { defaultValue: itemType })
}

export function formatOdds(value: number, locale: string) {
  return Number(value).toLocaleString(locale, { maximumFractionDigits: 2 })
}

export function formatBetMoment(value: string, locale: string) {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat(locale, {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date)
}

export function roundStatusLabel(t: TFunction, status: EventBetRoundResponse["status"]) {
  return t(`bets.roundStatus.${status}`, { defaultValue: status })
}

export function eventBetStatusLabel(t: TFunction, status: EventBetResponse["status"]) {
  return t(`bets.history.eventStatus.${status}`, { defaultValue: status })
}

export function eventOutcomeLabel(
  t: TFunction,
  outcome: NonNullable<EventBetRoundResponse["outcome"]>,
) {
  return t(`bets.history.outcome.${outcome}`, { defaultValue: outcome })
}

export function raceStatusLabel(t: TFunction, status: ItemRaceResponse["status"]) {
  return t(`bets.raceStatus.${status}`, { defaultValue: status })
}

export function raceParticipantStatusLabel(
  t: TFunction,
  status: ItemRaceParticipantResponse["status"],
) {
  return t(`bets.history.participantStatus.${status}`, { defaultValue: status })
}

export function betLocale(language?: string) {
  if (language === "en") {
    return "en-US"
  }

  if (language === "ru") {
    return "ru-RU"
  }

  return "uk-UA"
}
