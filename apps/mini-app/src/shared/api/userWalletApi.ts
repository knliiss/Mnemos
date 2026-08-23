import type { PageResponse, WalletTransactionResponse } from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"

export async function getCurrentUserTransactions(
  page = 0,
  size = 100,
): Promise<PageResponse<WalletTransactionResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return api<PageResponse<WalletTransactionResponse>>(`/api/v1/users/me/transactions?${params}`)
}
