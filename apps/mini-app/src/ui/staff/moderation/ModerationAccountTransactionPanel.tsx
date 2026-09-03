import { useCallback, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { getAdminAccountTransactions } from "@/shared/api/adminWalletApi"
import type { PageResponse, WalletTransactionResponse } from "@/shared/api/backendContracts"
import { handleError } from "@/shared/errors/errorHandler"
import { localeForLanguage } from "../../account/accountPresentation"
import { WalletHistoryPanel } from "../../account/wallet/WalletHistoryPanel"
import { Glyph } from "../../shared/MnemosIcon"
import "./moderation-admin-transactions.css"

type Props = {
  accountId: string
}

const PAGE_SIZE = 30

export function ModerationAccountTransactionPanel({ accountId }: Props) {
  const { t, i18n } = useTranslation()
  const [page, setPage] = useState(0)
  const [result, setResult] = useState<PageResponse<WalletTransactionResponse> | null>(null)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)

  const loadPage = useCallback(
    async (nextPage: number) => {
      setLoading(true)
      setLoadError(false)

      try {
        const response = await getAdminAccountTransactions(accountId, nextPage, PAGE_SIZE)
        setResult(response)
        setPage(response.page)
      } catch (error) {
        setLoadError(true)
        handleError(error)
      } finally {
        setLoading(false)
      }
    },
    [accountId],
  )

  useEffect(() => {
    let active = true

    void getAdminAccountTransactions(accountId, 0, PAGE_SIZE)
      .then((response) => {
        if (active) {
          setResult(response)
          setPage(response.page)
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

  const totalPages = result?.totalPages ?? 0
  const transactions = result?.items ?? []
  const canGoPrevious = page > 0 && !loading
  const canGoNext = Boolean(result?.hasNextPage) && !loading

  return (
    <section className="moderation-admin-transactions">
      <header className="moderation-admin-transactions__header">
        <span className="moderation-admin-transactions__mark" aria-hidden="true">
          <Glyph name="wallet" size={17} />
        </span>
        <span>
          <small>{t("moderation.accountWalletHistory", "ACCOUNT WALLET")}</small>
          <strong>{t("account.wallet.historyTitle", "Transaction history")}</strong>
        </span>
        <em>{result?.totalItems ?? 0}</em>
      </header>

      <WalletHistoryPanel
        canViewHistory
        transactions={transactions}
        loading={loading}
        loadError={loadError}
        locale={localeForLanguage(i18n.resolvedLanguage)}
        onRetry={() => void loadPage(page)}
      />

      {!loadError && totalPages > 1 ? (
        <nav
          className="moderation-admin-transactions__pagination"
          aria-label={t("moderation.transactionPagination", "Transaction history pages")}
        >
          <button
            type="button"
            disabled={!canGoPrevious}
            onClick={() => void loadPage(page - 1)}
          >
            <span aria-hidden="true">‹</span>
            {t("moderation.previous", "Previous")}
          </button>

          <span>
            <strong>{page + 1}</strong>
            <small>/ {totalPages}</small>
          </span>

          <button
            type="button"
            disabled={!canGoNext}
            onClick={() => void loadPage(page + 1)}
          >
            {t("moderation.next", "Next")}
            <span aria-hidden="true">›</span>
          </button>
        </nav>
      ) : null}
    </section>
  )
}
