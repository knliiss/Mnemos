import { useEffect, useMemo, useState } from "react"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { getAccountTransactions } from "@/shared/api/walletApi"
import { handleError } from "@/shared/errors/errorHandler"

type AccountWalletTransactions = {
  transactions: WalletTransactionResponse[]
  loading: boolean
  loadError: boolean
  totalEarned: number
  totalSpent: number
  retry(): void
}

export function useAccountWalletTransactions(
  activeAccountId: string | undefined,
  canViewHistory: boolean,
): AccountWalletTransactions {
  const [transactions, setTransactions] = useState<WalletTransactionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadedAccountId, setLoadedAccountId] = useState<string | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [reloadRevision, setReloadRevision] = useState(0)

  useEffect(() => {
    if (!activeAccountId) {
      return
    }

    let cancelled = false
    const timeout = window.setTimeout(() => {
      setLoading(true)
      setLoadError(false)

      if (!canViewHistory) {
        setTransactions([])
        setLoadedAccountId(activeAccountId)
        setLoading(false)
        return
      }

      void getAccountTransactions(activeAccountId, 0, 100)
        .then((response) => {
          if (!cancelled) {
            setTransactions(response.items)
            setLoadedAccountId(activeAccountId)
          }
        })
        .catch((error) => {
          if (!cancelled) {
            setTransactions([])
            setLoadError(true)
            setLoadedAccountId(activeAccountId)
            handleError(error)
          }
        })
        .finally(() => {
          if (!cancelled) {
            setLoading(false)
          }
        })
    }, 0)

    return () => {
      cancelled = true
      window.clearTimeout(timeout)
    }
  }, [activeAccountId, canViewHistory, reloadRevision])

  const visibleTransactions = useMemo(
    () => (loadedAccountId === activeAccountId ? transactions : []),
    [activeAccountId, loadedAccountId, transactions],
  )
  const visibleLoading = canViewHistory && (loading || loadedAccountId !== activeAccountId)

  const totalEarned = useMemo(
    () =>
      visibleTransactions
        .filter((transaction) => transaction.type === "CREDIT")
        .reduce((sum, transaction) => sum + transaction.amount, 0),
    [visibleTransactions],
  )

  const totalSpent = useMemo(
    () =>
      visibleTransactions
        .filter((transaction) => transaction.type === "DEBIT")
        .reduce((sum, transaction) => sum + transaction.amount, 0),
    [visibleTransactions],
  )

  return {
    transactions: visibleTransactions,
    loading: visibleLoading,
    loadError,
    totalEarned,
    totalSpent,
    retry: () => setReloadRevision((value) => value + 1),
  }
}
