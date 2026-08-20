import type {
  BetGlobalEventType,
  BetItemType,
  BetSide,
  BoosterType,
  Currency,
  DropSpotlightPresentationMode,
  DungeonType,
  EffectType,
  EventBetOutcome,
  EventKind,
  GlobalEventType,
  LocalizationLanguage,
  NotificationTemplateKey,
  PersonalizationType,
  SanctionType,
  SubscriptionType,
  TransactionType,
  WalletOwnerType,
} from "./model"

export const CURRENCIES = ["COIN", "PRIME"] as const satisfies readonly Currency[]
export const WALLET_OWNER_TYPES = ["USER", "ACCOUNT"] as const satisfies readonly WalletOwnerType[]
export const TRANSACTION_TYPES = ["CREDIT", "DEBIT"] as const satisfies readonly TransactionType[]
export const LOCALIZATION_LANGUAGES = ["UK", "EN", "RU"] as const satisfies readonly LocalizationLanguage[]
export const SPOTLIGHT_PRESENTATION_MODES = ["FULL", "COMPACT"] as const satisfies readonly DropSpotlightPresentationMode[]

export const EVENT_KINDS = ["ITEM_DROP", "BOOSTER", "GLOBAL", "RAID"] as const satisfies readonly EventKind[]
export const SUBSCRIPTION_TYPES = ["ITEM_DROP", "BOOSTER", "DUNGEON", "RAID", "GLOBAL"] as const satisfies readonly SubscriptionType[]
export const BOOSTER_TYPES = ["LUCK", "MONEY", "DAMAGE", "POWER"] as const satisfies readonly BoosterType[]
export const DUNGEON_TYPES = ["EASY", "MEDIUM", "HARD", "EXPERT"] as const satisfies readonly DungeonType[]
export const GLOBAL_EVENT_TYPES = ["DARKNESS", "MOON", "ECLIPSE", "EXPLOSION"] as const satisfies readonly GlobalEventType[]

export const BET_GLOBAL_EVENT_TYPES = ["MOON", "ECLIPSE", "EXPLOSION"] as const satisfies readonly BetGlobalEventType[]
export const BET_SIDES = ["YES", "NO"] as const satisfies readonly BetSide[]
export const EVENT_BET_OUTCOMES = ["YES", "NO", "REFUND"] as const satisfies readonly EventBetOutcome[]
export const BET_ITEM_TYPES = ["SWORD", "AURA", "PET", "BOOK", "JEWELRY"] as const satisfies readonly BetItemType[]

export const PERSONALIZATION_TYPES = [
  "NOTIFICATION_PACK",
  "NICK_STYLE",
  "PROFILE_THEME",
  "AVATAR_FRAME",
  "PROFILE_EFFECT",
] as const satisfies readonly PersonalizationType[]
export const EFFECT_TYPES = ["AURORA", "PULSE", "SHIMMER", "PARTICLES"] as const satisfies readonly EffectType[]
export const NOTIFICATION_TEMPLATE_KEYS = ["ITEM_DROP", "BOOSTER", "DUNGEON", "RAID", "GLOBAL"] as const satisfies readonly NotificationTemplateKey[]

export const SANCTION_TYPES = ["BAN", "NOTIFICATION_SUSPENSION"] as const satisfies readonly SanctionType[]
