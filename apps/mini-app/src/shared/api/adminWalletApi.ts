import type { PageResponse, WalletTransactionResponse } from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"

export async function getAdminAccountTransactions(
  accountId: string,
  page: number,
  size = 20,
): Promise<PageResponse<WalletTransactionResponse>> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return api<PageResponse<WalletTransactionResponse>>(
    `/api/v1/admin/accounts/${accountId}/transactions?${params}`,
  )
}
