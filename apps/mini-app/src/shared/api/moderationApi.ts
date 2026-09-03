import type {
  Currency,
  SanctionStatus,
  SanctionType,
  TransactionType,
  WalletAdjustmentStatus,
  WalletOwnerType,
} from "@/ui/model"
import { api } from "@/shared/api/httpClient"

export type AccountLifecycleStatus = "ACTIVE" | "DEACTIVATED" | "DELETED"

export type IdentityAccountResponse = {
  accountId: string
  cristalixUuid: string
  cristalixNickname: string
  status: AccountLifecycleStatus
}

export type IdentitySearchResponse = {
  userId: string
  username: string
  telegramId: number
  accounts: IdentityAccountResponse[]
}

export type IdentityDirectoryOrder = "NEWEST" | "OLDEST"

export type IdentityDirectoryPage<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export type UserIdentityDirectoryResponse = {
  userId: string
  username: string
  telegramId: number
  activeAccountId: string | null
  accountCount: number
  createdAt: string
}

export type AccountIdentityDirectoryResponse = {
  accountId: string
  cristalixUuid: string
  cristalixNickname: string
  status: AccountLifecycleStatus
  ownerUserId: string
  ownerUsername: string
  createdAt: string
}

export type IdentityDirectoryQuery = {
  query: string
  page: number
  size?: number
  order: IdentityDirectoryOrder
}

export type AdminAccountMemberResponse = {
  memberId: string
  userId: string
  username: string
  owner: boolean
  joinedAt: string
}

export type AdminAccountDetailsResponse = {
  accountId: string
  cristalixUuid: string
  cristalixNickname: string
  status: AccountLifecycleStatus
  ownerMemberId: string
  baseRoleId: string | null
  maxMembers: number
  createdAt: string
  deactivatedAt: string | null
  deletedAt: string | null
  members: AdminAccountMemberResponse[]
}

export type AdminAccountRoleResponse = {
  id: string
  name: string
  color: string
  hierarchyRank: number
  permissions: Record<string, boolean>
  createdAt: string
  updatedAt: string
}

export type SanctionResponse = {
  id: string
  userId: string
  type: SanctionType
  reason: string
  internalNote: string | null
  createdByUserId: string
  createdAt: string
  expiresAt: string | null
  status: SanctionStatus
}

export type CreateSanctionRequest = {
  userId: string
  type: SanctionType
  reason: string
  internalNote: string | null
  durationHours: number | null
}

export type WalletBalanceResponse = {
  targetType: WalletOwnerType
  targetId: string
  currency: Currency
  amount: number
}

export type WalletAdjustmentResponse = {
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
  walletTransactionId: string | null
  failureReason: string | null
  createdAt: string
}

export type CreateWalletAdjustmentRequest = {
  targetType: WalletOwnerType
  targetId: string
  currency: Currency
  type: TransactionType
  amount: number
  reason: string
}

export async function searchIdentities(
  query: string,
  limit = 20,
): Promise<IdentitySearchResponse[]> {
  const params = new URLSearchParams({ query, limit: String(limit) })
  return api<IdentitySearchResponse[]>(`/api/v1/admin/identities?${params}`)
}

export async function searchUserDirectory(
  query: IdentityDirectoryQuery,
): Promise<IdentityDirectoryPage<UserIdentityDirectoryResponse>> {
  const params = createIdentityDirectoryParams(query)
  return api<IdentityDirectoryPage<UserIdentityDirectoryResponse>>(
    `/api/v1/admin/identities/users?${params}`,
  )
}

export async function searchAccountDirectory(
  query: IdentityDirectoryQuery,
): Promise<IdentityDirectoryPage<AccountIdentityDirectoryResponse>> {
  const params = createIdentityDirectoryParams(query)
  return api<IdentityDirectoryPage<AccountIdentityDirectoryResponse>>(
    `/api/v1/admin/identities/accounts?${params}`,
  )
}

export async function getAdminAccount(accountId: string): Promise<AdminAccountDetailsResponse> {
  return api<AdminAccountDetailsResponse>(`/api/v1/admin/accounts/${accountId}`)
}

export async function getAdminAccountRoles(accountId: string): Promise<AdminAccountRoleResponse[]> {
  return api<AdminAccountRoleResponse[]>(`/api/v1/admin/accounts/${accountId}/roles`)
}

export async function getAdminMemberRoles(
  accountId: string,
  memberId: string,
): Promise<AdminAccountRoleResponse[]> {
  return api<AdminAccountRoleResponse[]>(
    `/api/v1/admin/accounts/${accountId}/members/${memberId}/roles`,
  )
}

export async function reactivateAccount(accountId: string): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/reactivate`, { method: "POST" })
}

export async function deactivateAccount(accountId: string): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/deactivate`, { method: "POST" })
}

export async function addAccountMember(accountId: string, userId: string): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/members/${userId}`, { method: "POST" })
}

export async function removeAccountMember(accountId: string, memberId: string): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/members/${memberId}`, { method: "DELETE" })
}

export async function transferAccountOwnership(accountId: string, userId: string): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}/owner/${userId}`, { method: "PUT" })
}

export async function deleteAccountPermanently(accountId: string): Promise<void> {
  await api<void>(`/api/v1/admin/accounts/${accountId}`, { method: "DELETE" })
}

export async function listSanctions(userId: string): Promise<SanctionResponse[]> {
  const params = new URLSearchParams({ userId })
  return api<SanctionResponse[]>(`/api/v1/moderation/sanctions?${params}`)
}

export async function createSanction(request: CreateSanctionRequest): Promise<SanctionResponse> {
  return api<SanctionResponse>("/api/v1/moderation/sanctions", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(request),
  })
}

export async function revokeSanction(sanctionId: string): Promise<void> {
  await api<void>(`/api/v1/moderation/sanctions/${sanctionId}`, { method: "DELETE" })
}

export async function getWalletBalance(
  targetType: WalletOwnerType,
  targetId: string,
): Promise<WalletBalanceResponse> {
  const params = new URLSearchParams({ targetType, targetId })
  return api<WalletBalanceResponse>(`/api/v1/moderation/wallet-balances?${params}`)
}

export async function listWalletAdjustments(
  targetType: WalletOwnerType,
  targetId: string,
): Promise<WalletAdjustmentResponse[]> {
  const params = new URLSearchParams({ targetType, targetId })
  return api<WalletAdjustmentResponse[]>(`/api/v1/moderation/wallet-adjustments?${params}`)
}

export async function createWalletAdjustment(
  request: CreateWalletAdjustmentRequest,
): Promise<WalletAdjustmentResponse> {
  return api<WalletAdjustmentResponse>("/api/v1/moderation/wallet-adjustments", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Idempotency-Key": crypto.randomUUID(),
    },
    body: JSON.stringify(request),
  })
}

export async function getWalletAdjustment(operationId: string): Promise<WalletAdjustmentResponse> {
  return api<WalletAdjustmentResponse>(`/api/v1/moderation/wallet-adjustments/${operationId}`)
}

function createIdentityDirectoryParams(query: IdentityDirectoryQuery): URLSearchParams {
  const params = new URLSearchParams({
    page: String(query.page),
    size: String(query.size ?? 20),
    order: query.order,
  })
  const normalizedQuery = query.query.trim()

  if (normalizedQuery) {
    params.set("query", normalizedQuery)
  }

  return params
}