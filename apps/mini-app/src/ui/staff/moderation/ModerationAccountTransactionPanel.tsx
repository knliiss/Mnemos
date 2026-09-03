import { useCallback, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { getAdminAccountTransactions } from "@/shared/api/adminWalletApi"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { handleError } from "@/shared/errors/errorHandler"
import { localeForLanguage } from "../../account/accountPresentation"
import { WalletHistoryPanel } from "../../account/wallet/WalletHistoryPanel"

type Props = {
  accountId: string
}

export function ModerationAccountTransactionPanel({ accountId }: Props) {
  const { i18n } = useTranslation()
  const [transactions, setTransactions] = useState<WalletTransactionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)

  const refresh = useCallback(async () => {
    setLoading(true)
    setLoadError(false)

    try {
      const firstPage = await getAdminAccountTransactions(accountId, 0, 50)
      setTransactions(firstPage.items)
    } catch (error) {
      setLoadError(true)
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [accountId])

  useEffect(() => {
    let active = true

    void getAdminAccountTransactions(accountId, 0, 50)
      .then((firstPage) => {
        if (active) {
          setTransactions(firstPage.items)
          setLoadError(false)
        }
      })
      .catch((error: unknown) => {
        if (active) {
          setLoadError(true)
          handleError(error)
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [accountId])

  return (
    <div className="moderation-admin-transactions">
      <WalletHistoryPanel
        canViewHistory
        transactions={transactions}
        loading={loading}
        loadError={loadError}
        locale={localeForLanguage(i18n.resolvedLanguage)}
        onRetry={() => void refresh()}
      />
    </div>
  )
}
