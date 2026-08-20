export type Currency = "COIN" | "PRIME"
export type WalletOwnerType = "ACCOUNT" | "USER"
export type TransactionType = "CREDIT" | "DEBIT"
export type TransactionSource = "DAILY_REWARD" | "ITEM_DROP_REWARD" | "BET_PLACE" | "BET_WIN" | "BET_REFUND" | "PERSONALIZATION_PURCHASE" | "CREATOR_REVENUE" | "SPOTLIGHT_PURCHASE" | "PRIME_PURCHASE" | "ADMIN_ADJUSTMENT"
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
export type EventBetRoundStatus = "OPEN" | "LOCKED" | "AWAITING_RESULT" | "AWAITING_ADMIN_CONFIRMATION" | "SETTLED"
export type PersonalizationType = "NOTIFICATION_PACK" | "NICK_STYLE" | "PROFILE_THEME" | "AVATAR_FRAME" | "PROFILE_EFFECT"
export type PersonalizationTarget = "USER" | "ACCOUNT"
export type PersonalizationStatus = "DRAFT" | "PRIVATE" | "IN_REVIEW" | "REJECTED" | "APPROVED" | "PUBLISHED" | "UNLISTED"
export type EffectType = "AURORA" | "PULSE" | "SHIMMER" | "PARTICLES"
export type NotificationTemplateKey = "ITEM_DROP" | "BOOSTER" | "DUNGEON" | "RAID" | "GLOBAL"
export type RolePermission = "VIEW_ACCOUNT_SETTINGS" | "EDIT_ACCOUNT_SETTINGS" | "MANAGE_ACCOUNT_INVITES" | "VIEW_MEMBERS" | "INVITE_MEMBERS" | "REMOVE_MEMBERS" | "ASSIGN_MEMBER_ROLES" | "KICK_MEMBERS" | "VIEW_ROLES" | "CREATE_ROLES" | "EDIT_ROLE_DETAILS" | "EDIT_ROLE_PERMISSIONS" | "DELETE_ROLES" | "MANAGE_ROLE_HIERARCHY" | "MANAGE_ROLES" | "VIEW_WALLET" | "VIEW_TRANSACTION_HISTORY" | "SPEND_WALLET_FUNDS" | "CLAIM_DAILY_REWARD" | "PUBLISH_COMMENTS" | "PUBLISH_REACTIONS" | "MODERATE_COMMENTS" | "VIEW_BETS" | "PLACE_BETS" | "MANAGE_BETS" | "VIEW_BET_HISTORY" | "VIEW_PERSONALIZATIONS" | "APPLY_PERSONALIZATIONS" | "BUY_PERSONALIZATIONS" | "MANAGE_PERSONALIZATIONS"
export type SystemRole = "USER" | "MODERATOR" | "ADMIN" | "SUPER_ADMIN" | "PERSONALIZATION_CREATOR" | "PERSONALIZATION_MODERATOR"
export type SanctionType = "BAN" | "NOTIFICATION_SUSPENSION"
export type SanctionStatus = "ACTIVE" | "REVOKED" | "EXPIRED"
export type WalletAdjustmentStatus = "PENDING" | "COMPLETED" | "FAILED"

export type RootRoute = "bets" | "events" | "account" | "search" | "personalization-store"
export type RouteId = RootRoute | "event-detail" | "subscriptions" | "member" | "roles" | "role-editor" | "account-wallet" | "account-invites" | "account-settings" | "account-create" | "account-join" | "public-profile" | "personalization-owned" | "personalization-store-category" | "personalization-detail" | "creator-studio" | "creator-revenue" | "personalization-editor" | "moderation" | "administration" | "moderation-user" | "personalization-review" | "bet-admin" | "badge-admin" | "user-wallet"
export type Route = { id: RouteId; params?: Record<string, string> }

export type Account = {
  id: string
  cristalixUuid: string
  nickname: string
  role: string
  skinUrl: string
  memberCount: number
  maxMembers: number
  owner: boolean
  active?: boolean
  coin: number
}

export type Member = { id: string; name: string; owner: boolean; joinedAt: string; highestRoleId: string | null; roleIds: string[] }
export type AccountRole = { id: string; name: string; color: string; hierarchyRank: number; permissions: Record<RolePermission, boolean>; locked?: boolean }
export type FeedEvent = { id: string; kind: EventKind; title: string; detail: string; occurredAt: string; relative: string; meta?: string; rarity?: string }
export type Subscription = { id: string; type: "ITEM_DROP"; droppedFor?: string; itemName?: string } | { id: string; type: "BOOSTER"; boosterType: BoosterType } | { id: string; type: "DUNGEON"; dungeonType: DungeonType } | { id: string; type: "GLOBAL"; eventType: GlobalEventType } | { id: string; type: "RAID"; locations: number[] }
export type BetRound = { id: string; eventType: BetGlobalEventType; scheduledAt: string; closesAt: string; resolvesAt: string; yesOdds: number; noOdds: number; status: EventBetRoundStatus; mySide?: BetSide; myAmount?: number }
export type ItemRace = { id: string; itemName: string; itemType: BetItemType; entryAmount: number; closesAt: string; participants: number; joined: boolean; myAmount?: number }

export type GradientStop = {
  color: string
  position: number
}

export type GlowStyle = {
  color: string
  strength: number
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
  surface: string
  accent: string
  text: string
  border: string
  glow: GlowStyle | null
}

export type AvatarFrameContent = {
  kind: "AVATAR_FRAME"
  borderColor: string
  borderWidthPx: number
  radiusPx: number
  glow: GlowStyle | null
}

export type ProfileEffectContent = {
  kind: "PROFILE_EFFECT"
  effect: EffectType
  intensity: number
  primaryColor: string
  secondaryColor: string | null
}

export type PersonalizationContent = NotificationPackContent | NickStyleContent | ProfileThemeContent | AvatarFrameContent | ProfileEffectContent

export type Personalization = {
  id: string
  type: PersonalizationType
  target: PersonalizationTarget
  name: string
  creator: string
  currency: Currency
  price: number
  status: PersonalizationStatus
  owned: boolean
  active: boolean
  preview: string
  content?: PersonalizationContent
}

export type WalletTransaction = { id: string; currency: Currency; type: TransactionType; source: TransactionSource; amount: number; createdAt: string }
export type ProfileComment = { id: string; authorAccountId: string; author: string; content: string; likes: number; replies: number; createdAt: string; liked: boolean; createdByUserId?: string; pending?: boolean; failed?: boolean }
export type PublicProfile = { accountId: string; cristalixUuid: string; nickname: string; likes: number; dislikes: number; comments: number; skinUrl: string }

export type InternalIdentityAccount = {
  accountId: string
  cristalixUuid: string
  cristalixNickname: string
  active: boolean
}

export type InternalIdentity = {
  userId: string
  username: string
  telegramId: number
  accounts: InternalIdentityAccount[]
}

export type ModerationSanction = {
  id: string
  userId: string
  type: SanctionType
  reason: string
  internalNote?: string
  createdByUserId: string
  createdAt: string
  expiresAt?: string
  status: SanctionStatus
}

export type WalletAdjustment = {
  operationId: string
  requestId: string
  targetType: WalletOwnerType
  targetId: string
  actorUserId: string
  currency: Currency
  type: TransactionType
  amount: number
  reason: string
  status: WalletAdjustmentStatus
  walletTransactionId?: string
  failureReason?: string
  createdAt: string
}
