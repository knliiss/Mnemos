import { useCallback, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { getAdminAccountTransactions } from "@/shared/api/adminWalletApi"
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
    void refresh()
  }, [refresh])

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
