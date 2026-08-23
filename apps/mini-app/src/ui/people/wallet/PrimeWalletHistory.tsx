import type { TFunction } from "i18next"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { Glyph } from "../../shared/MnemosIcon"
import { CurrencyValue } from "../../shared/Primitives"
import { FeatureEmpty } from "../../system/FeatureEmpty"
import { FeatureLoading } from "../../system/FeatureLoading"

type HistoryFilter = "ALL" | "CREDIT" | "DEBIT"
type HistorySort = "NEWEST" | "OLDEST" | "AMOUNT_DESC" | "AMOUNT_ASC"

type PrimeWalletHistoryProps = {
  transactions: WalletTransactionResponse[]
  loading: boolean
  loadError: boolean
  locale: string
  onRetry(): Promise<void>
}

const historyFilters: HistoryFilter[] = ["ALL", "CREDIT", "DEBIT"]
const historySorts: HistorySort[] = ["NEWEST", "OLDEST", "AMOUNT_DESC", "AMOUNT_ASC"]

export function PrimeWalletHistory({
  transactions,
  loading,
  loadError,
  locale,
  onRetry,
}: PrimeWalletHistoryProps) {
  const { t } = useTranslation()
  const [historyFilter, setHistoryFilter] = useState<HistoryFilter>("ALL")
  const [historySort, setHistorySort] = useState<HistorySort>("NEWEST")

  const visibleTransactions = useMemo(() => {
    const filtered = transactions.filter(
      (transaction) => historyFilter === "ALL" || transaction.type === historyFilter,
    )

    return [...filtered].sort((left, right) => {
      if (historySort === "OLDEST") {
        return Date.parse(left.createdAt) - Date.parse(right.createdAt)
      }

      if (historySort === "AMOUNT_DESC") {
        return right.amount - left.amount
      }

      if (historySort === "AMOUNT_ASC") {
        return left.amount - right.amount
      }

      return Date.parse(right.createdAt) - Date.parse(left.createdAt)
    })
  }, [historyFilter, historySort, transactions])

  return (
    <section className="prime-history-panel">
      <header className="wallet-history-head wallet-history-head--stacked">
        <span>
          <small>{t("userWallet.history.movement")}</small>
          <strong>{t("userWallet.history.title")}</strong>
        </span>

        <label className="wallet-history-sort">
          <Glyph name="swap" size={14} />
          <select
            value={historySort}
            onChange={(event) => setHistorySort(event.target.value as HistorySort)}
          >
            {historySorts.map((sort) => (
              <option key={sort} value={sort}>
                {t(`userWallet.history.sort.${sort}`)}
              </option>
            ))}
          </select>
        </label>
      </header>

      <div className="prime-history-filter">
        {historyFilters.map((filter) => (
          <button
            type="button"
            key={filter}
            className={historyFilter === filter ? "is-active" : ""}
            onClick={() => setHistoryFilter(filter)}
          >
            {t(`userWallet.history.filters.${filter}`)}
          </button>
        ))}
      </div>

      <div className="prime-history-list">
        {visibleTransactions.map((transaction) => (
          <PrimeHistoryRow key={transaction.id} transaction={transaction} locale={locale} t={t} />
        ))}

        {loading ? <FeatureLoading label={t("userWallet.history.loading")} /> : null}

        {!loading && loadError ? (
          <FeatureEmpty
            icon="wallet"
            title={t("common.loadErrorTitle")}
            copy={t("common.loadErrorCopy")}
          >
            <button type="button" onClick={() => void onRetry()}>
              {t("common.retry")}
            </button>
          </FeatureEmpty>
        ) : null}

        {!loading && !loadError && !visibleTransactions.length ? (
          <FeatureEmpty icon="wallet" title={t("userWallet.history.empty")} />
        ) : null}
      </div>
    </section>
  )
}

function PrimeHistoryRow({
  transaction,
  locale,
  t,
}: {
  transaction: WalletTransactionResponse
  locale: string
  t: TFunction
}) {
  const credit = transaction.type === "CREDIT"

  return (
    <article>
      <span className={`prime-history-list__sign ${credit ? "is-credit" : "is-debit"}`}>
        {credit ? "+" : "−"}
      </span>

      <span className="prime-history-list__copy">
        <strong>{transactionLabel(transaction.source, t)}</strong>
        <small>{formatTransactionDate(transaction.createdAt, locale)}</small>
      </span>

      <span className={`prime-history-list__amount ${credit ? "is-credit" : ""}`}>
        {credit ? "+" : "−"}
        <CurrencyValue currency="PRIME" value={transaction.amount} compact />
      </span>
    </article>
  )
}

function transactionLabel(source: string, t: TFunction) {
  const translated = t(`userWallet.history.sources.${source}`, { defaultValue: "" })

  if (translated) {
    return translated
  }

  return source
    .toLowerCase()
    .split("_")
    .map((part) => part.slice(0, 1).toUpperCase() + part.slice(1))
    .join(" ")
}

function formatTransactionDate(value: string, locale: string) {
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
