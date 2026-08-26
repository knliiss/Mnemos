import type {
  BetItemType,
  BetSide,
  BoosterType,
  Currency,
  DungeonType,
  EventBetOutcome,
  GlobalEventType,
  PersonalizationType,
  SanctionType,
  SubscriptionType,
  TransactionType,
} from "./model"

export const CURRENCIES = ["COIN", "PRIME"] as const satisfies readonly Currency[]
export const TRANSACTION_TYPES = ["CREDIT", "DEBIT"] as const satisfies readonly TransactionType[]

export const SUBSCRIPTION_TYPES = [
  "ITEM_DROP",
  "BOOSTER",
  "DUNGEON",
  "RAID",
  "GLOBAL",
] as const satisfies readonly SubscriptionType[]
export const BOOSTER_TYPES = [
  "LUCK",
  "MONEY",
  "DAMAGE",
  "POWER",
] as const satisfies readonly BoosterType[]
export const DUNGEON_TYPES = [
  "EASY",
  "MEDIUM",
  "HARD",
  "EXPERT",
] as const satisfies readonly DungeonType[]
export const GLOBAL_EVENT_TYPES = [
  "DARKNESS",
  "MOON",
  "ECLIPSE",
  "EXPLOSION",
] as const satisfies readonly GlobalEventType[]

export const BET_SIDES = ["YES", "NO"] as const satisfies readonly BetSide[]
export const EVENT_BET_OUTCOMES = [
  "YES",
  "NO",
  "REFUND",
] as const satisfies readonly EventBetOutcome[]
export const BET_ITEM_TYPES = [
  "SWORD",
  "AURA",
  "PET",
  "BOOK",
  "JEWELRY",
] as const satisfies readonly BetItemType[]

export const PERSONALIZATION_TYPES = [
  "NOTIFICATION_PACK",
  "NICK_STYLE",
  "PROFILE_THEME",
  "PROFILE_BACKGROUND",
  "AVATAR_FRAME",
  "PROFILE_EFFECT",
] as const satisfies readonly PersonalizationType[]

export const SANCTION_TYPES = [
  "BAN",
  "NOTIFICATION_SUSPENSION",
] as const satisfies readonly SanctionType[]
