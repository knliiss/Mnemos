import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { getCurrentUserTransactions } from "@/shared/api/userWalletApi"
import { handleError } from "@/shared/errors/errorHandler"

type UserPrimeTransactions = {
  transactions: WalletTransactionResponse[]
  loading: boolean
  loadError: boolean
  earned: number
  spent: number
  reload(): Promise<void>
}

export function useUserPrimeTransactions(userId: string): UserPrimeTransactions {
  const [transactions, setTransactions] = useState<WalletTransactionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadedUserId, setLoadedUserId] = useState<string | null>(null)
  const [loadError, setLoadError] = useState(false)
  const requestRevision = useRef(0)

  const reload = useCallback(async () => {
    const requestedUserId = userId
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)
    setLoadError(false)

    try {
      const page = await getCurrentUserTransactions()

      if (revision === requestRevision.current) {
        setTransactions(page.items.filter((transaction) => transaction.currency === "PRIME"))
        setLoadedUserId(requestedUserId)
      }
    } catch (error) {
      if (revision === requestRevision.current) {
        setTransactions([])
        setLoadedUserId(requestedUserId)
        setLoadError(true)
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current) {
        setLoading(false)
      }
    }
  }, [userId])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void reload()
    })

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [reload])

  const scopedTransactions = useMemo(
    () => (loadedUserId === userId ? transactions : []),
    [loadedUserId, transactions, userId],
  )
  const visibleLoading = loading || loadedUserId !== userId

  const earned = useMemo(
    () =>
      scopedTransactions
        .filter((transaction) => transaction.type === "CREDIT")
        .reduce((sum, transaction) => sum + transaction.amount, 0),
    [scopedTransactions],
  )

  const spent = useMemo(
    () =>
      scopedTransactions
        .filter((transaction) => transaction.type === "DEBIT")
        .reduce((sum, transaction) => sum + transaction.amount, 0),
    [scopedTransactions],
  )

  return {
    transactions: scopedTransactions,
    loading: visibleLoading,
    loadError,
    earned,
    spent,
    reload,
  }
}
