import type { Currency, TransactionType } from "@/ui/model"
import { api } from "@/shared/api/httpClient"

export type AdminAccountTransactionResponse = {
  id: string
  requestId: string
  currency: Currency
  type: TransactionType
  source: string
  sourceReferenceId: string
  amount: number
  createdAt: string
}

export type AdminAccountTransactionPage = {
  items: AdminAccountTransactionResponse[]
  page: number
  size: number
  totalItems: number
  totalPages: number
  hasNextPage: boolean
}

export async function getAdminAccountTransactions(
  accountId: string,
  page: number,
  size = 20,
): Promise<AdminAccountTransactionPage> {
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  })

  return api<AdminAccountTransactionPage>(
    `/api/v1/admin/accounts/${accountId}/transactions?${params}`,
  )
}
