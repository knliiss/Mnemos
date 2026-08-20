import type { TFunction } from "i18next"
import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { getCurrentUserTransactions } from "@/shared/api/userWalletApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useMnemosData } from "../data/MnemosDataProvider"
import type { Route } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../shared/Primitives"
import { FeatureEmpty } from "../system/FeatureEmpty"
import { FeatureLoading } from "../system/FeatureLoading"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

type HistoryFilter = "ALL" | "CREDIT" | "DEBIT"
type HistorySort = "NEWEST" | "OLDEST" | "AMOUNT_DESC" | "AMOUNT_ASC"

const historyFilters: HistoryFilter[] = ["ALL", "CREDIT", "DEBIT"]
const historySorts: HistorySort[] = ["NEWEST", "OLDEST", "AMOUNT_DESC", "AMOUNT_ASC"]

export function UserPrimeWallet({ navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const data = useMnemosData()
  const userId = data.user.id
  const [transactions, setTransactions] = useState<WalletTransactionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [loadedUserId, setLoadedUserId] = useState<string | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [historyFilter, setHistoryFilter] = useState<HistoryFilter>("ALL")
  const [historySort, setHistorySort] = useState<HistorySort>("NEWEST")
  const requestRevision = useRef(0)
  const locale = localeForLanguage(i18n.resolvedLanguage)

  const loadTransactions = useCallback(async () => {
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
      void loadTransactions()
    })

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [loadTransactions])

  const scopedTransactions = useMemo(
    () => (loadedUserId === userId ? transactions : []),
    [loadedUserId, transactions, userId],
  )
  const visibleLoading = loading || loadedUserId !== userId

  const visibleTransactions = useMemo(() => {
    const filtered = scopedTransactions.filter(
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
  }, [historyFilter, historySort, scopedTransactions])

  const spent = scopedTransactions
    .filter((transaction) => transaction.type === "DEBIT")
    .reduce((sum, transaction) => sum + transaction.amount, 0)

  const earned = scopedTransactions
    .filter((transaction) => transaction.type === "CREDIT")
    .reduce((sum, transaction) => sum + transaction.amount, 0)

  const prime = data.user.prime

  return (
    <div className="screen prime-wallet-screen">
      <PageHeader title="PRIME" eyebrow={t("userWallet.userEyebrow")} back={back} />

      <section className="prime-wallet-hero">
        <div className="prime-wallet-hero__copy">
          <span className="prime-wallet-hero__label">
            <CurrencyMark currency="PRIME" size={19} />
            <strong>PRIME</strong>
          </span>
          <strong className="prime-wallet-hero__value">{prime.toLocaleString(locale)}</strong>
          <small>{t("userWallet.description")}</small>
        </div>

        <img
          className="prime-wallet-hero__art"
          src="/ui-illustrations/prime-cache.png"
          alt=""
          aria-hidden="true"
        />

        <div className="prime-wallet-hero__stats">
          <span>
            <small>{t("userWallet.earned")}</small>
            <strong>+{earned.toLocaleString(locale)}</strong>
          </span>
          <span>
            <small>{t("userWallet.spent")}</small>
            <strong>−{spent.toLocaleString(locale)}</strong>
          </span>
        </div>
      </section>

      <nav className="prime-wallet-shortcuts" aria-label={t("userWallet.shortcutsAria")}>
        <button type="button" onClick={() => navigate({ id: "personalization-store" })}>
          <span className="is-plum">
            <Glyph name="spark" size={19} />
          </span>
          <strong>{t("userWallet.store")}</strong>
          <small>{t("userWallet.personalizations")}</small>
          <b>›</b>
        </button>
        <button type="button" onClick={() => navigate({ id: "account-wallet" })}>
          <span className="is-lime">
            <Glyph name="wallet" size={19} />
          </span>
          <strong>COIN</strong>
          <small>{t("userWallet.accountWallet")}</small>
          <b>›</b>
        </button>
      </nav>

      <section className="prime-topup-panel">
        <header>
          <span>
            <small>{t("userWallet.topup.eyebrow")}</small>
            <strong>{t("userWallet.topup.title")}</strong>
            <em>{t("userWallet.topup.copy")}</em>
          </span>
          <span className="prime-topup-status">{t("userWallet.topup.soon")}</span>
        </header>

        <button type="button" className="prime-topup-disabled" disabled>
          <Glyph name="clock" size={16} />
          {t("userWallet.topup.unavailable")}
        </button>
      </section>

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

          {visibleLoading ? <FeatureLoading label={t("userWallet.history.loading")} /> : null}

          {!visibleLoading && loadError ? (
            <FeatureEmpty
              icon="wallet"
              title={t("common.loadErrorTitle")}
              copy={t("common.loadErrorCopy")}
            >
              <button type="button" onClick={() => void loadTransactions()}>
                {t("common.retry")}
              </button>
            </FeatureEmpty>
          ) : null}

          {!visibleLoading && !loadError && !visibleTransactions.length ? (
            <FeatureEmpty icon="wallet" title={t("userWallet.history.empty")} />
          ) : null}
        </div>
      </section>
    </div>
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

function localeForLanguage(language?: string) {
  if (language === "en") {
    return "en-US"
  }
  if (language === "ru") {
    return "ru-RU"
  }
  return "uk-UA"
}
