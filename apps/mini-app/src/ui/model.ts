export type Currency = "COIN" | "PRIME"
export type WalletOwnerType = "ACCOUNT" | "USER"
export type TransactionType = "CREDIT" | "DEBIT"
export type LocalizationLanguage = "EN" | "RU" | "UK"
export type DropSpotlightPresentationMode = "FULL" | "COMPACT"
export type EventKind = "ITEM_DROP" | "BOOSTER" | "GLOBAL" | "RAID"
export type SubscriptionType = "ITEM_DROP" | "BOOSTER" | "DUNGEON" | "RAID" | "GLOBAL"
export type BoosterType = "LUCK" | "MONEY" | "DAMAGE" | "POWER"
export type DungeonType = "EASY" | "MEDIUM" | "HARD" | "EXPERT"
export type GlobalEventType = "DARKNESS" | "MOON" | "ECLIPSE" | "EXPLOSION"
export type BetGlobalEventType = "MOON" | "ECLIPSE" | "EXPLOSION"
export type BetSide = "YES" | "NO"
export type EventBetOutcome = "YES" | "NO" | "REFUND"
export type BetItemType = "SWORD" | "AURA" | "PET" | "BOOK" | "JEWELRY"
export type ItemRarity = "MYTHICAL" | "SECRET"
export type PersonalizationType =
  | "NOTIFICATION_PACK"
  | "NICK_STYLE"
  | "PROFILE_THEME"
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
export type NotificationTemplateKey = "ITEM_DROP" | "BOOSTER" | "DUNGEON" | "RAID" | "GLOBAL"
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
  | "member"
  | "roles"
  | "role-editor"
  | "account-wallet"
  | "account-invites"
  | "account-settings"
  | "account-create"
  | "account-join"
  | "public-profile"
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

export type Subscription =
  | { id: string; type: "ITEM_DROP"; droppedFor?: string; itemName?: string }
  | { id: string; type: "BOOSTER"; boosterType: BoosterType }
  | { id: string; type: "DUNGEON"; dungeonType: DungeonType }
  | { id: string; type: "GLOBAL"; eventType: GlobalEventType }
  | { id: string; type: "RAID"; locations: number[] }

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
  assetUrl: string
  xPercent: number
  yPercent: number
  widthPercent: number
  opacity: number
  rotationDegrees: number
  blendMode: VisualLayerBlendMode
  animation: VisualLayerAnimation
  zIndex: number
}

export type NotificationTemplate = {
  emoji: string | null
  soloTitle: string
  soloBody: string
  compact: string
  aggregateTitle: string
}

export type NotificationPackContent = {
  kind: "NOTIFICATION_PACK"
  mixedAggregateTitle: string
  templates: Record<NotificationTemplateKey, NotificationTemplate>
}

export type NickStyleContent = {
  kind: "NICK_STYLE"
  colors: GradientStop[]
  angleDegrees: number
  glow: GlowStyle | null
}

export type ProfileThemeContent = {
  kind: "PROFILE_THEME"
  background: string
  backgroundSecondary: string
  surface: string
  surfaceSecondary: string
  accent: string
  text: string
  border: string
  gradientAngleDegrees: number
  textureStrength: number
  glow: GlowStyle | null
  layers: VisualLayer[]
}

export type AvatarFrameContent = {
  kind: "AVATAR_FRAME"
  borderColor: string
  secondaryBorderColor: string
  borderWidthPx: number
  gapPx: number
  radiusPx: number
  style: FrameStyle
  glow: GlowStyle | null
  layers: VisualLayer[]
}

export type ProfileEffectContent = {
  kind: "PROFILE_EFFECT"
  effect: EffectType
  intensity: number
  speed: number
  density: number
  scale: number
  primaryColor: string
  secondaryColor: string | null
  layers: VisualLayer[]
}

export type PersonalizationContent =
  | NotificationPackContent
  | NickStyleContent
  | ProfileThemeContent
  | AvatarFrameContent
  | ProfileEffectContent

export type Personalization = {
  id: string
  type: PersonalizationType
  target: PersonalizationTarget
  name: string
  creator: string
  createdByCurrentUser: boolean
  currency: Currency
  price: number
  status: PersonalizationStatus
  owned: boolean
  active: boolean
  preview: string
  content?: PersonalizationContent
}
