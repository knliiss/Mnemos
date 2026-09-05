import type {
  BetItemType,
  BoosterType,
  Currency,
  GlobalEventType,
  ItemRarity,
  LocalizationLanguage,
  RolePermission,
  SystemRole,
} from "@/ui/model"

export type AccountStatus = "ACTIVE" | "DEACTIVATED"

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
  status: AccountStatus
}

export type AccountDetailsResponse = AccountSummaryResponse & {
  baseRoleId: string | null
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

export type ItemCatalogResponse = {
  id: string
  gameKey: string | null
  name: string
  type: BetItemType
  rarity: ItemRarity
}

export type ItemDropFeedResponse = {
  type: "ITEM_DROP"
  eventId: string
  occurredAt: string
  itemId: string
  itemKey: string | null
  itemName: string
  itemRarity: ItemRarity
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

export type EventFeedItemResponse =
  | ItemDropFeedResponse
  | BoosterFeedResponse
  | GlobalFeedResponse
  | RaidFeedResponse

export type EventFeedResponse = {
  items: EventFeedItemResponse[]
  cursor: string | null
}
