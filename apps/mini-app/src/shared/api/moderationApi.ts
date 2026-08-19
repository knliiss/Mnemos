import type {
  Currency,
  SanctionStatus,
  SanctionType,
  TransactionType,
  WalletAdjustmentStatus,
  WalletOwnerType,
} from "@/product-ui/model"
import { api } from "@/shared/api/httpClient"

export type IdentityAccountResponse = {
  accountId: string
  cristalixUuid: string
  cristalixNickname: string
  active: boolean
}

export type IdentitySearchResponse = {
  userId: string
  username: string
  telegramId: number
  accounts: IdentityAccountResponse[]
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
  const params = new URLSearchParams({
    query,
    limit: String(limit),
  })

  return api<IdentitySearchResponse[]>(`/api/v1/admin/identities?${params}`)
}

export async function listSanctions(userId: string): Promise<SanctionResponse[]> {
  const params = new URLSearchParams({ userId })
  return api<SanctionResponse[]>(`/api/v1/moderation/sanctions?${params}`)
}

export async function createSanction(request: CreateSanctionRequest): Promise<SanctionResponse> {
  return api<SanctionResponse>("/api/v1/moderation/sanctions", {
    method: "POST",
    body: JSON.stringify(request),
  })
}

export async function revokeSanction(sanctionId: string): Promise<void> {
  await api<void>(`/api/v1/moderation/sanctions/${sanctionId}`, {
    method: "DELETE",
  })
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
      "Idempotency-Key": crypto.randomUUID(),
    },
    body: JSON.stringify(request),
  })
}

export async function getWalletAdjustment(operationId: string): Promise<WalletAdjustmentResponse> {
  return api<WalletAdjustmentResponse>(`/api/v1/moderation/wallet-adjustments/${operationId}`)
}
