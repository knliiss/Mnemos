import { api } from "@/shared/api/httpClient"
import type {
  AccountMemberResponse,
  AccountRoleResponse,
  BalanceResponse,
  ClaimDailyRewardResponse,
  CurrentContextResponse,
  DailyRewardStateResponse,
  EventFeedResponse,
  EventFeedType,
  ItemCatalogResponse,
  PageResponse,
  SubscriptionResponse,
  UserResponse,
  WalletTransactionResponse,
} from "@/shared/api/backendContracts"
import type {
  BoosterType,
  DungeonType,
  GlobalEventType,
  LocalizationLanguage,
} from "@/product-ui/model"

export async function initializeCurrentUser(): Promise<UserResponse> {
  return api<UserResponse>("/api/v1/users/me", { method: "PUT" })
}

export async function getCurrentContext(): Promise<CurrentContextResponse> {
  return api<CurrentContextResponse>("/api/v1/me/context")
}

export async function updateCurrentLanguage(localizationLanguage: LocalizationLanguage): Promise<UserResponse> {
  return api<UserResponse>("/api/v1/users/me/localization", {
    method: "PATCH",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ localizationLanguage }),
  })
}

export async function selectActiveAccount(accountId: string): Promise<void> {
  await api<void>(`/api/v1/me/accounts/active/${accountId}`, { method: "PUT" })
}

export async function createAccount(cristalixNickname: string): Promise<{ accountId: string; memberId: string }> {
  return api<{ accountId: string; memberId: string }>("/api/v1/accounts", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ cristalixNickname }),
  })
}

export async function joinAccount(inviteCode: string): Promise<{ accountId: string; memberId: string }> {
  return api<{ accountId: string; memberId: string }>("/api/v1/account-memberships", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ inviteCode }),
  })
}

export async function leaveActiveAccount(): Promise<void> {
  await api<void>("/api/v1/me/accounts/active/leave", { method: "POST" })
}

export async function reactivateAccount(accountId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/reactivate`, { method: "POST" })
}

export async function getAccountMembers(accountId: string): Promise<AccountMemberResponse[]> {
  return api<AccountMemberResponse[]>(`/api/v1/accounts/${accountId}/members`)
}

export async function kickAccountMember(accountId: string, memberId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/members/${memberId}`, { method: "DELETE" })
}

export async function getAccountRoles(accountId: string): Promise<AccountRoleResponse[]> {
  return api<AccountRoleResponse[]>(`/api/v1/accounts/${accountId}/roles`)
}

export async function getMemberRoles(accountId: string, memberId: string): Promise<AccountRoleResponse[]> {
  return api<AccountRoleResponse[]>(`/api/v1/accounts/${accountId}/members/${memberId}/roles`)
}

export async function assignMemberRole(accountId: string, memberId: string, roleId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/members/${memberId}/roles/${roleId}`, { method: "PUT" })
}

export async function revokeMemberRole(accountId: string, memberId: string, roleId: string): Promise<void> {
  await api<void>(`/api/v1/accounts/${accountId}/members/${memberId}/roles/${roleId}`, { method: "DELETE" })
}

export async function getAccountBalances(accountId: string): Promise<BalanceResponse[]> {
  return api<BalanceResponse[]>(`/api/v1/accounts/${accountId}/balances`)
}

export async function getCurrentUserBalances(): Promise<BalanceResponse[]> {
  return api<BalanceResponse[]>("/api/v1/users/me/balances")
}

export async function getAccountTransactions(accountId: string, page = 0, size = 30): Promise<PageResponse<WalletTransactionResponse>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  return api<PageResponse<WalletTransactionResponse>>(`/api/v1/accounts/${accountId}/transactions?${params}`)
}

export async function getDailyRewardState(): Promise<DailyRewardStateResponse> {
  return api<DailyRewardStateResponse>("/api/v1/rewards/daily")
}

export async function claimDailyReward(): Promise<ClaimDailyRewardResponse> {
  return api<ClaimDailyRewardResponse>("/api/v1/rewards/daily/claim", { method: "POST" })
}

export async function restoreDailyReward(): Promise<{ currentStreak: number }> {
  return api<{ currentStreak: number }>("/api/v1/rewards/daily/restore", { method: "POST" })
}

export type EventFeedQuery = {
  after?: string | null
  types?: EventFeedType[]
  search?: string
  droppedFor?: string
  itemRarity?: string
  sort?: "NEWEST" | "OLDEST"
  limit?: number
}

export async function getEventFeed(query: EventFeedQuery = {}): Promise<EventFeedResponse> {
  const params = new URLSearchParams()

  if (query.after) params.set("after", query.after)
  query.types?.forEach((type) => params.append("type", type))
  if (query.search?.trim()) params.set("search", query.search.trim())
  if (query.droppedFor?.trim()) params.set("droppedFor", query.droppedFor.trim())
  if (query.itemRarity) params.set("itemRarity", query.itemRarity)
  params.set("sort", query.sort ?? "NEWEST")
  params.set("limit", String(query.limit ?? 50))

  return api<EventFeedResponse>(`/api/v1/events/feed?${params}`)
}

export async function searchItemCatalog(search: string, limit = 8): Promise<ItemCatalogResponse[]> {
  const params = new URLSearchParams({
    search: search.trim(),
    limit: String(limit),
  })

  return api<ItemCatalogResponse[]>(`/api/v1/events/items?${params}`)
}

export async function getSubscriptions(page = 0, size = 100): Promise<PageResponse<SubscriptionResponse>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  return api<PageResponse<SubscriptionResponse>>(`/api/v1/subscriptions?${params}`)
}

export async function createItemDropSubscription(droppedFor?: string, itemName?: string): Promise<void> {
  await postJson("/api/v1/subscriptions/item-drops", {
    droppedFor: droppedFor?.trim() || null,
    itemName: itemName?.trim() || null,
  })
}

export async function createBoosterSubscription(boosterType: BoosterType): Promise<void> {
  await postJson("/api/v1/subscriptions/boosters", { boosterType })
}

export async function createDungeonSubscription(dungeonType: DungeonType): Promise<void> {
  await postJson("/api/v1/subscriptions/dungeons", { dungeonType })
}

export async function createGlobalSubscription(eventType: GlobalEventType): Promise<void> {
  await postJson("/api/v1/subscriptions/global-events", { eventType })
}

export async function createRaidSubscription(locations: number[]): Promise<void> {
  await postJson("/api/v1/subscriptions/raids", { locations })
}

export async function deleteSubscription(subscriptionId: string): Promise<void> {
  await api<void>(`/api/v1/subscriptions/${subscriptionId}`, { method: "DELETE" })
}

async function postJson(path: string, body: unknown): Promise<void> {
  await api<unknown>(path, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  })
}
