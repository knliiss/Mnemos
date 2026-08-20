import type {
  BalanceResponse,
  PageResponse,
  WalletTransactionResponse,
} from "@/shared/api/backendContracts"
import { api } from "@/shared/api/httpClient"

export async function getAccountBalances(accountId: string): Promise<BalanceResponse[]> {
  return api<BalanceResponse[]>(`/api/v1/accounts/${accountId}/balances`)
}

export async function getCurrentUserBalances(): Promise<BalanceResponse[]> {
  return api<BalanceResponse[]>("/api/v1/users/me/balances")
}

export async function getAccountTransactions(
  accountId: string,
  page = 0,
  size = 30,
): Promise<PageResponse<WalletTransactionResponse>> {
  const params = new URLSearchParams({ page: String(page), size: String(size) })

  return api<PageResponse<WalletTransactionResponse>>(
    `/api/v1/accounts/${accountId}/transactions?${params}`,
  )
}
