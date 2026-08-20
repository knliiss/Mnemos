import type { TFunction } from "i18next"
import type { EventBetOutcome, EventBetRoundResponse, ItemRaceResponse } from "@/shared/api/betApi"
import type { BetGlobalEventType, BetItemType } from "../../model"

export type BetAdminSection = "home" | "rounds" | "history" | "round" | "races"

export function normalizeBetAdminSection(value?: string): BetAdminSection {
  if (value === "rounds" || value === "history" || value === "round" || value === "races") {
    return value
  }

  return "home"
}

export function betAdminEventLabel(t: TFunction, eventType: BetGlobalEventType) {
  const labels: Record<BetGlobalEventType, string> = {
    MOON: t("betAdmin.eventMoon"),
    ECLIPSE: t("betAdmin.eventEclipse"),
    EXPLOSION: t("betAdmin.eventExplosion"),
  }

  return labels[eventType]
}

export function betAdminItemLabel(t: TFunction, itemType: BetItemType) {
  const labels: Record<BetItemType, string> = {
    SWORD: t("betAdmin.itemSword"),
    AURA: t("betAdmin.itemAura"),
    PET: t("betAdmin.itemPet"),
    BOOK: t("betAdmin.itemBook"),
    JEWELRY: t("betAdmin.itemJewelry"),
  }

  return labels[itemType]
}

export function betAdminOutcomeLabel(t: TFunction, outcome: EventBetOutcome) {
  const labels: Record<EventBetOutcome, string> = {
    YES: t("betAdmin.yes"),
    NO: t("betAdmin.no"),
    REFUND: t("betAdmin.refund"),
  }

  return labels[outcome]
}

export function formatBetAdminDate(value: string, language: string) {
  const locale = language.startsWith("ru") ? "ru-RU" : language.startsWith("en") ? "en-US" : "uk-UA"

  return new Intl.DateTimeFormat(locale, {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value))
}

export function activeBetRounds(rounds: EventBetRoundResponse[]) {
  return rounds.filter((round) => round.status !== "SETTLED")
}

export function settledBetRounds(rounds: EventBetRoundResponse[]) {
  return rounds.filter((round) => round.status === "SETTLED")
}

export function activeItemRaces(races: ItemRaceResponse[]) {
  return races.filter((race) => !["SETTLED", "REFUNDED", "CANCELLED"].includes(race.status))
}
