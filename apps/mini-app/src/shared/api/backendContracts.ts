import type {
  BetGlobalEventType,
  BoosterType,
  Currency,
  DungeonType,
  GlobalEventType,
  LocalizationLanguage,
  RolePermission,
  SystemRole,
} from "@/product-ui/model"

export type UserResponse = {
  id: string
  username: string
  localizationLanguage: LocalizationLanguage
  activeAccountId: string | null
  createdAt: string
  updatedAt: string
}

export type AccountSummaryResponse = {
  id: string
  cristalixUuid: string
  cristalixNickname: string
  currentMemberId: string
  memberCount: number
  maxMembers: number
  owner: boolean
  active: boolean
}

export type AccountDetailsResponse = AccountSummaryResponse & {
  createdAt: string
  updatedAt: string
}

export type CurrentContextResponse = {
  user: UserResponse
  systemRoles: SystemRole[]
  availableAccounts: AccountSummaryResponse[]
  activeAccount: AccountDetailsResponse | null
  effectivePermissions: RolePermission[]
}

export type AccountMemberResponse = {
  id: string
  name: string
  owner: boolean
  highestRole: {
    id: string
    name: string
    color: string
  } | null
  joinedAt: string
}

export type AccountRoleResponse = {
  id: string
  name: string
  color: string
  hierarchyRank: number
  permissions: Partial<Record<RolePermission, boolean>>
  createdAt: string
  updatedAt: string
}

export type BalanceResponse = {
  currency: Currency
  amount: number
}

export type PageResponse<T> = {
  items: T[]
  page: number
  size: number
  totalItems: number
  totalPages: number
  hasNextPage: boolean
}

export type WalletTransactionResponse = {
  id: string
  accountId?: string | null
  userId?: string | null
  currency: Currency
  type: "CREDIT" | "DEBIT"
  source: string
  sourceReferenceId?: string | null
  amount: number
  requestId: string
  createdAt: string
}

export type DailyRewardStateResponse = {
  accountId: string
  currentStreak: number
  claimedToday: boolean
  recoverable: boolean
  recoverableStreak: number
  restoresRemaining: number
  nextCoinMin: number
  nextCoinMax: number
  primeChance: number
  rewardDate: string
}

export type ClaimDailyRewardResponse = {
  streak: number
  coins: number
  primes: number
  primeChance: number
}

export type EventFeedType = "ITEM_DROP" | "BOOSTER" | "GLOBAL" | "RAID"

export type ItemDropFeedResponse = {
  type: "ITEM_DROP"
  eventId: string
  occurredAt: string
  itemId: string
  itemName: string
  itemRarity: string
  itemType: string
  droppedFor: string
}

export type BoosterFeedResponse = {
  type: "BOOSTER"
  eventId: string
  occurredAt: string
  boosterType: BoosterType
  activatedBy: string
}

export type GlobalFeedResponse = {
  type: "GLOBAL"
  eventId: string
  occurredAt: string
  globalType: GlobalEventType
}

export type RaidFeedResponse = {
  type: "RAID"
  eventId: string
  occurredAt: string
  locations: number[]
}

export type EventFeedItemResponse = ItemDropFeedResponse | BoosterFeedResponse | GlobalFeedResponse | RaidFeedResponse

export type EventFeedResponse = {
  items: EventFeedItemResponse[]
  cursor: string | null
}

export type SubscriptionResponse = {
  id: string
  type: "ITEM_DROP" | "BOOSTER" | "DUNGEON" | "GLOBAL" | "RAID"
  createdAt: string
  droppedFor?: string | null
  itemName?: string | null
  boosterType?: BoosterType | null
  dungeonType?: DungeonType | null
  eventType?: GlobalEventType | null
  locations?: number[] | null
}

export type EventBetRoundResponse = {
  id: string
  eventType: BetGlobalEventType
  scheduledAt: string
  closesAt: string
  resolvesAt: string
  status: string
  yesOdds: number
  noOdds: number
}

export type BetActivityResponse = {
  roundId: string
  side: "YES" | "NO"
  amount: number
}

export type ItemRaceResponse = {
  id: string
  itemName: string
  itemType: string
  entryAmount: number
  closesAt: string
  participants: number
  joined?: boolean
  myAmount?: number
}

export type PublicProfileResponse = {
  accountId: string
  cristalixUuid: string
  cristalixNickname: string
  likes: number
  dislikes: number
  comments: number
}

export type IdentitySearchResponse = {
  accountId: string
  cristalixUuid: string
  cristalixNickname: string
  active: boolean
}
