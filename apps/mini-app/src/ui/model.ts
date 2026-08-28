export type Currency = "COIN" | "PRIME"
export type WalletOwnerType = "ACCOUNT" | "USER"
export type TransactionType = "CREDIT" | "DEBIT"
export type LocalizationLanguage = "EN" | "RU" | "UK"
export type DropSpotlightPresentationMode = "FULL" | "COMPACT"
export type EventKind = "ITEM_DROP" | "BOOSTER" | "GLOBAL" | "RAID"
export type SubscriptionType = "ITEM_DROP" | "BOOSTER" | "DUNGEON" | "RAID" | "GLOBAL"
export type BoosterType = "LUCK" | "MONEY" | "DAMAGE" | "POWER"
export type DungeonType = "EASY" | "MEDIUM" | "HARD" | "EXPERT"
export type GlobalEventType = "DARKNESS" | "MOON" | "ECLIPSE" | "EXPLOSION" | "COMET_CHAOS"
export type BetGlobalEventType = "MOON" | "ECLIPSE" | "EXPLOSION"
export type BetSide = "YES" | "NO"
export type EventBetOutcome = "YES" | "NO" | "REFUND"
export type BetItemType = "SWORD" | "AURA" | "PET" | "BOOK" | "JEWELRY"
export type ItemRarity = "MYTHICAL" | "SECRET"
export type PersonalizationType =
  | "NOTIFICATION_PACK"
  | "NICK_STYLE"
  | "PROFILE_THEME"
  | "PROFILE_BACKGROUND"
  | "AVATAR_FRAME"
  | "PROFILE_EFFECT"
export type PersonalizationTarget = "USER" | "ACCOUNT"
export type PersonalizationStatus =
  | "DRAFT"
  | "PRIVATE"
  | "IN_REVIEW"
  | "REJECTED"
  | "APPROVED"
  | "PUBLISHED"
  | "UNLISTED"
export type EffectType = "AURORA" | "PULSE" | "SHIMMER" | "PARTICLES"
export type FrameStyle = "SOLID" | "DOUBLE" | "SEGMENTED"
export type VisualLayerBlendMode = "NORMAL" | "SCREEN" | "MULTIPLY" | "OVERLAY" | "SOFT_LIGHT"
export type VisualLayerAnimation = "NONE" | "FLOAT" | "PULSE" | "SPIN" | "DRIFT"
export type NotificationTemplateKey =
  | "ITEM_DROP"
  | "BOOSTER"
  | "DUNGEON"
  | "RAID"
  | "GLOBAL"
  | "PLAYER_OFFLINE"
export type NotificationPackVariantField =
  | "ITEM_RARITY"
  | "ITEM_TYPE"
  | "BOOSTER"
  | "DUNGEON"
  | "GLOBAL_EVENT"
export type NotificationPackValueKey =
  | "ITEM_RARITY"
  | "ITEM_TYPE"
  | "BOOSTER"
  | "DUNGEON"
  | "GLOBAL_EVENT"
export type RolePermission =
  | "MANAGE_MEMBERS"
  | "ASSIGN_MEMBER_ROLES"
  | "MANAGE_ROLES"
  | "MANAGE_ROLE_HIERARCHY"
  | "MANAGE_BASE_ROLE"
  | "VIEW_TRANSACTION_HISTORY"
  | "PLACE_BETS"
  | "BUY_ACCOUNT_PERSONALIZATIONS"
  | "APPLY_ACCOUNT_PERSONALIZATIONS"
  | "MODERATE_COMMENTS"
export type SystemRole =
  | "USER"
  | "MODERATOR"
  | "ADMIN"
  | "SUPER_ADMIN"
  | "PERSONALIZATION_CREATOR"
  | "PERSONALIZATION_MODERATOR"
export type SanctionType = "BAN" | "NOTIFICATION_SUSPENSION"
export type SanctionStatus = "ACTIVE" | "REVOKED" | "EXPIRED"
export type WalletAdjustmentStatus = "PENDING" | "COMPLETED" | "FAILED"

export type RootRoute = "bets" | "events" | "account" | "search" | "personalization-store"
export type RouteId =
  | RootRoute
  | "event-detail"
  | "subscriptions"
  | "leaderboards"
  | "member"
  | "roles"
  | "role-editor"
  | "account-wallet"
  | "account-invites"
  | "account-settings"
  | "account-create"
  | "account-join"
  | "public-profile"
  | "sao-realms"
  | "personalization-owned"
  | "personalization-store-category"
  | "personalization-detail"
  | "creator-studio"
  | "creator-revenue"
  | "personalization-editor"
  | "moderation"
  | "administration"
  | "moderation-user"
  | "personalization-review"
  | "bet-admin"
  | "badge-admin"
  | "collector-admin"
  | "wipe-admin"
  | "user-personalization"
  | "user-wallet"
export type Route = { id: RouteId; params?: Record<string, string> }

export type Account = {
  id: string
  currentMemberId: string | null
  cristalixUuid: string
  nickname: string
  role: "OWNER" | "MEMBER"
  skinUrl: string
  memberCount: number
  maxMembers: number
  owner: boolean
  active?: boolean
  coin: number
}

export type Member = {
  id: string
  name: string
  owner: boolean
  joinedAt: string
  highestRoleId: string | null
  roleIds: string[]
}

export type AccountRole = {
  id: string
  name: string
  color: string
  hierarchyRank: number
  permissions: Record<RolePermission, boolean>
  locked?: boolean
}

export type FeedEvent = {
  id: string
  kind: EventKind
  title: string
  detail: string
  occurredAt: string
  relative: string
  meta?: string
  rarity?: ItemRarity
}

type SubscriptionBase = {
  id: string
  createdAt: string
}

export type Subscription = SubscriptionBase &
  (
    | { type: "ITEM_DROP"; droppedFor?: string; itemName?: string }
    | { type: "BOOSTER"; boosterType: BoosterType }
    | { type: "DUNGEON"; dungeonType: DungeonType }
    | { type: "GLOBAL"; eventType: GlobalEventType }
    | { type: "RAID"; locations: number[] }
  )

export type GradientStop = {
  color: string
  position: number
}

export type GlowStyle = {
  color: string
  strength: number
}

export type VisualLayer = {
  id: string
  assetId: string
  opacity: number
  blendMode: VisualLayerBlendMode
  animation: VisualLayerAnimation
  animationSpeed: number
  scale: number
  offsetX: number
  offsetY: number
  rotation: number
}

export type ProfileBackground = {
  baseColor: string
  gradient?: {
    type: "LINEAR" | "RADIAL"
    angle: number
    stops: GradientStop[]
  }
  layers: VisualLayer[]
}

export type ProfileTheme = {
  background: string
  surface: string
  surfaceStrong: string
  text: string
  muted: string
  accent: string
  accentSoft: string
  border: string
  shadow: string
}

export type NickStyle = {
  color?: string
  gradient?: GradientStop[]
  glow?: GlowStyle
  bold?: boolean
  italic?: boolean
}

export type AvatarFrame = {
  imageUrl?: string
  color?: string
  style?: FrameStyle
  thickness?: number
  glow?: GlowStyle
}

export type ProfileEffect = {
  type: EffectType
  intensity: number
  speed: number
  density?: number
  assetUrl?: string
}

export type ProfileAppearance = {
  theme?: ProfileTheme
  background?: ProfileBackground
  nickStyle?: NickStyle
  avatarFrame?: AvatarFrame
  effect?: ProfileEffect
}

export type PersonalizationPrice = {
  currency: Currency
  amount: number
}

export type NotificationTemplateCondition = {
  field: NotificationPackVariantField
  value: string
}

export type NotificationTemplateVariant = {
  id: string
  conditions: NotificationTemplateCondition[]
  emoji?: string | null
  soloTitle: string
  soloBody: string
  compact: string
  aggregateTitle: string
}

export type NotificationTemplate = {
  emoji?: string | null
  soloTitle: string
  soloBody: string
  compact: string
  aggregateTitle: string
  variants: NotificationTemplateVariant[]
}

export type NotificationValueForms = {
  default: string
  forms: Record<string, string>
}

export type NotificationPackContent = {
  mixedAggregateTitle: string
  templates: Record<NotificationTemplateKey, NotificationTemplate>
  valueMappings: Partial<Record<NotificationPackValueKey, Record<string, NotificationValueForms>>>
}

export type PersonalizationContent =
  | { type: "NOTIFICATION_PACK"; notificationPack: NotificationPackContent }
  | { type: "NICK_STYLE"; nickStyle: NickStyle }
  | { type: "PROFILE_THEME"; profileTheme: ProfileTheme }
  | { type: "PROFILE_BACKGROUND"; profileBackground: ProfileBackground }
  | { type: "AVATAR_FRAME"; avatarFrame: AvatarFrame }
  | { type: "PROFILE_EFFECT"; profileEffect: ProfileEffect }

export type Personalization = {
  id: string
  catalogKey?: string | null
  type: PersonalizationType
  target: PersonalizationTarget
  status: PersonalizationStatus
  origin: "SYSTEM" | "CREATOR"
  creatorId?: string | null
  name: string
  price?: PersonalizationPrice | null
  content: PersonalizationContent
  owned: boolean
  active: boolean
}

export type PersonalizationOwnership = {
  personalizationId: string
  ownerId: string
  acquiredAt: string
  active: boolean
}

export type Sanction = {
  id: string
  userId: string
  type: SanctionType
  status: SanctionStatus
  reason: string
  createdAt: string
  expiresAt?: string | null
  revokedAt?: string | null
}

export type WalletAdjustment = {
  requestId: string
  status: WalletAdjustmentStatus
  error?: string | null
}
