import { AnimatePresence, motion } from "framer-motion"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { CurrencyMark, Glyph } from "../../shared/MnemosIcon"
import { CurrencyValue } from "../../shared/Primitives"

type HistorySort = "NEWEST" | "OLDEST" | "AMOUNT_DESC" | "AMOUNT_ASC"

type WalletHistoryPanelProps = {
  canViewHistory: boolean
  transactions: WalletTransactionResponse[]
  loading: boolean
  loadError: boolean
  locale: string
  onRetry(): void
}

const historySorts: HistorySort[] = ["NEWEST", "OLDEST", "AMOUNT_DESC", "AMOUNT_ASC"]

export function WalletHistoryPanel({
  canViewHistory,
  transactions,
  loading,
  loadError,
  locale,
  onRetry,
}: WalletHistoryPanelProps) {
  const { t } = useTranslation()
  const [historySort, setHistorySort] = useState<HistorySort>("NEWEST")
  const [sortOpen, setSortOpen] = useState(false)

  const history = useMemo(
    () =>
      [...transactions].sort((left, right) => {
        if (historySort === "OLDEST") {
          return new Date(left.createdAt).getTime() - new Date(right.createdAt).getTime()
        }

        if (historySort === "AMOUNT_DESC") {
          return right.amount - left.amount
        }

        if (historySort === "AMOUNT_ASC") {
          return left.amount - right.amount
        }

        return new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
      }),
    [historySort, transactions],
  )

  const selectSort = (sort: HistorySort) => {
    setHistorySort(sort)
    setSortOpen(false)
  }

  const sortLabel = (sort: HistorySort) => t(`accountWallet.sort.${sort}`)

  const formatTransactionDate = (value: string) => {
    const date = new Date(value)

    if (Number.isNaN(date.getTime())) {
      return value
    }

    return new Intl.DateTimeFormat(locale, {
      day: "numeric",
      month: "short",
      hour: "2-digit",
      minute: "2-digit",
    }).format(date)
  }

  const transactionSourceLabel = (source: string) => {
    const translated = t(`accountWallet.sources.${source}`, { defaultValue: "" })

    return translated || source.replaceAll("_", " ")
  }

  return (
    <section className="coin-history-panel coin-history-panel--detailed">
      <header className="wallet-history-head">
        <span>
          <small>{t("accountWallet.movement")}</small>
          <strong>{t("accountWallet.history")}</strong>
        </span>

        <div
          className={`wallet-history-sort wallet-history-sort--custom ${sortOpen ? "is-open" : ""}`}
        >
          <button
            type="button"
            className="wallet-history-sort__trigger"
            aria-expanded={sortOpen}
            disabled={!canViewHistory}
            onClick={() => setSortOpen((value) => !value)}
          >
            <Glyph name="swap" size={14} />
            <span>{sortLabel(historySort)}</span>
            <b>⌄</b>
          </button>

          <AnimatePresence>
            {sortOpen && canViewHistory ? (
              <motion.div
                className="wallet-history-sort__menu"
                initial={{ opacity: 0, y: -6, scale: 0.98 }}
                animate={{ opacity: 1, y: 0, scale: 1 }}
                exit={{ opacity: 0, y: -4, scale: 0.985 }}
              >
                {historySorts.map((sort) => (
                  <button
                    key={sort}
                    type="button"
                    className={historySort === sort ? "is-active" : ""}
                    onClick={() => selectSort(sort)}
                  >
                    <span>{sortLabel(sort)}</span>
                    {historySort === sort ? <Glyph name="check" size={13} /> : null}
                  </button>
                ))}
              </motion.div>
            ) : null}
          </AnimatePresence>
        </div>
      </header>

      <div className="coin-history-list">
        {canViewHistory
          ? history.map((transaction) => {
              const credit = transaction.type === "CREDIT"

              return (
                <article key={transaction.id}>
                  <span className={`coin-history-list__sign ${credit ? "is-credit" : "is-debit"}`}>
                    {credit ? "+" : "−"}
                  </span>

                  <span className="coin-history-list__copy">
                    <strong>{transactionSourceLabel(transaction.source)}</strong>
                    <small>{formatTransactionDate(transaction.createdAt)}</small>
                  </span>

                  <span className={`coin-history-list__amount ${credit ? "is-credit" : ""}`}>
                    {credit ? "+" : "−"}
                    <CurrencyValue currency="COIN" value={transaction.amount} compact />
                  </span>
                </article>
              )
            })
          : null}

        {!canViewHistory ? (
          <div className="coin-history-empty">
            <Glyph name="wallet" size={22} />
            <strong>{t("accountWallet.historyUnavailableTitle")}</strong>
            <small>{t("accountWallet.historyUnavailableCopy")}</small>
          </div>
        ) : null}

        {canViewHistory && !loading && loadError ? (
          <div className="coin-history-empty">
            <Glyph name="events" size={22} />
            <strong>{t("common.loadErrorTitle")}</strong>
            <small>{t("common.loadErrorCopy")}</small>
            <button type="button" onClick={onRetry}>
              {t("common.retry")}
            </button>
          </div>
        ) : null}

        {canViewHistory && !loading && !loadError && !history.length ? (
          <div className="coin-history-empty">
            <CurrencyMark currency="COIN" size={22} />
            <strong>{t("accountWallet.emptyTitle")}</strong>
            <small>{t("accountWallet.emptyCopy")}</small>
          </div>
        ) : null}

        {loading ? (
          <div className="coin-history-loading">
            <i />
            <i />
            <i />
          </div>
        ) : null}
      </div>
    </section>
  )
}
