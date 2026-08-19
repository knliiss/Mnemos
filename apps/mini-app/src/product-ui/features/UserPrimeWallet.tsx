import type { TFunction } from "i18next"
import { useCallback, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { getCurrentUserTransactions } from "@/shared/api/userWalletApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useProductData } from "../data/ProductDataProvider"
import type { Route } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../shared/Primitives"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

type HistoryFilter = "ALL" | "CREDIT" | "DEBIT"
type HistorySort = "NEWEST" | "OLDEST" | "AMOUNT_DESC" | "AMOUNT_ASC"
type PrimePackLabel = "start" | "popular" | "reserve"

type PrimePack = {
  id: string
  amount: number
  bonus: number
  label: PrimePackLabel
}

const primePacks: PrimePack[] = [
  { id: "prime-50", amount: 50, bonus: 0, label: "start" },
  { id: "prime-120", amount: 120, bonus: 8, label: "popular" },
  { id: "prime-300", amount: 300, bonus: 30, label: "reserve" },
]

const historyFilters: HistoryFilter[] = ["ALL", "CREDIT", "DEBIT"]
const historySorts: HistorySort[] = ["NEWEST", "OLDEST", "AMOUNT_DESC", "AMOUNT_ASC"]

export function UserPrimeWallet({ navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const product = useProductData()
  const [transactions, setTransactions] = useState<WalletTransactionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedPackId, setSelectedPackId] = useState(primePacks[1].id)
  const [historyFilter, setHistoryFilter] = useState<HistoryFilter>("ALL")
  const [historySort, setHistorySort] = useState<HistorySort>("NEWEST")
  const selectedPack = primePacks.find((pack) => pack.id === selectedPackId) ?? primePacks[0]
  const locale = localeForLanguage(i18n.resolvedLanguage)

  const loadTransactions = useCallback(async () => {
    setLoading(true)

    try {
      const page = await getCurrentUserTransactions()
      setTransactions(page.items.filter((transaction) => transaction.currency === "PRIME"))
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void loadTransactions()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [loadTransactions])

  const visibleTransactions = useMemo(() => {
    const filtered = transactions.filter(
      (transaction) => historyFilter === "ALL" || transaction.type === historyFilter,
    )

    return [...filtered].sort((left, right) => {
      if (historySort === "OLDEST") return Date.parse(left.createdAt) - Date.parse(right.createdAt)
      if (historySort === "AMOUNT_DESC") return right.amount - left.amount
      if (historySort === "AMOUNT_ASC") return left.amount - right.amount
      return Date.parse(right.createdAt) - Date.parse(left.createdAt)
    })
  }, [historyFilter, historySort, transactions])

  const spent = transactions
    .filter((transaction) => transaction.type === "DEBIT")
    .reduce((sum, transaction) => sum + transaction.amount, 0)

  const earned = transactions
    .filter((transaction) => transaction.type === "CREDIT")
    .reduce((sum, transaction) => sum + transaction.amount, 0)

  const packTotal = selectedPack.amount + selectedPack.bonus
  const prime = product.user.prime

  return (
    <div className="screen prime-wallet-v2">
      <PageHeader title="PRIME" eyebrow="USER" back={back} />

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
        <button onClick={() => navigate({ id: "personalization-store" })}>
          <span className="is-plum">
            <Glyph name="spark" size={19} />
          </span>
          <strong>{t("userWallet.store")}</strong>
          <small>{t("userWallet.personalizations")}</small>
          <b>›</b>
        </button>
        <button onClick={() => navigate({ id: "account-wallet" })}>
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
          <span className="prime-topup-status">SOON</span>
        </header>

        <div className="prime-pack-grid">
          {primePacks.map((pack) => (
            <button
              key={pack.id}
              className={selectedPackId === pack.id ? "is-selected" : ""}
              aria-pressed={selectedPackId === pack.id}
              onClick={() => setSelectedPackId(pack.id)}
            >
              <img src="/ui-illustrations/prime-package.png" alt="" aria-hidden="true" />
              <span>
                <small>{t(`userWallet.topup.packs.${pack.label}`)}</small>
                <strong>{pack.amount} PRIME</strong>
                <em>
                  {pack.bonus
                    ? t("userWallet.topup.bonus", { count: pack.bonus })
                    : t("userWallet.topup.noBonus")}
                </em>
              </span>
            </button>
          ))}
        </div>

        <div className="prime-topup-summary">
          <span>
            <small>{t("userWallet.topup.after")}</small>
            <strong>
              {prime.toLocaleString(locale)} → {(prime + packTotal).toLocaleString(locale)} PRIME
            </strong>
            <em>
              {selectedPack.bonus
                ? t("userWallet.topup.bonusSummary", { count: selectedPack.bonus })
                : t("userWallet.topup.baseSummary")}
            </em>
          </span>
          <span className="prime-topup-summary__amount">
            <CurrencyValue currency="PRIME" value={packTotal} compact />
          </span>
        </div>

        <button className="prime-topup-disabled" disabled>
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

          {loading ? (
            <div className="events-r2-loading" aria-label={t("userWallet.history.loading")}>
              <i />
              <i />
              <i />
            </div>
          ) : null}

          {!loading && !visibleTransactions.length ? (
            <div className="events-r2-empty">
              <Glyph name="wallet" size={22} />
              <strong>{t("userWallet.history.empty")}</strong>
            </div>
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
  if (translated) return translated

  return source
    .toLowerCase()
    .split("_")
    .map((part) => part.slice(0, 1).toUpperCase() + part.slice(1))
    .join(" ")
}

function formatTransactionDate(value: string, locale: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat(locale, {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date)
}

function localeForLanguage(language?: string) {
  if (language === "en") return "en-US"
  if (language === "ru") return "ru-RU"
  return "uk-UA"
}
