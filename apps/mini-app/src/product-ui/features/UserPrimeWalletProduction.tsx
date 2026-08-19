import { useCallback, useEffect, useMemo, useState } from "react"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { getCurrentUserTransactions } from "@/shared/api/userWalletApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useProductData } from "../data/ProductDataProvider"
import type { Route, TransactionSource } from "../model"
import { transactionSourceLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../shared/Primitives"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

type HistoryFilter = "ALL" | "CREDIT" | "DEBIT"
type HistorySort = "NEWEST" | "OLDEST" | "AMOUNT_DESC" | "AMOUNT_ASC"

type PrimePack = {
  id: string
  amount: number
  bonus: number
  label: string
}

const primePacks: PrimePack[] = [
  { id: "prime-50", amount: 50, bonus: 0, label: "Старт" },
  { id: "prime-120", amount: 120, bonus: 8, label: "Популярний" },
  { id: "prime-300", amount: 300, bonus: 30, label: "Запас" },
]

const historyLabels: Record<HistoryFilter, string> = {
  ALL: "Усі",
  CREDIT: "Надходження",
  DEBIT: "Витрати",
}

const sortLabels: Record<HistorySort, string> = {
  NEWEST: "Спочатку нові",
  OLDEST: "Спочатку старі",
  AMOUNT_DESC: "Сума ↓",
  AMOUNT_ASC: "Сума ↑",
}

export function UserPrimeWalletProduction({ navigate, back }: Props) {
  const product = useProductData()
  const [transactions, setTransactions] = useState<WalletTransactionResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [selectedPackId, setSelectedPackId] = useState(primePacks[1].id)
  const [historyFilter, setHistoryFilter] = useState<HistoryFilter>("ALL")
  const [historySort, setHistorySort] = useState<HistorySort>("NEWEST")
  const selectedPack = primePacks.find((pack) => pack.id === selectedPackId) ?? primePacks[0]

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
    const filtered = transactions.filter((transaction) =>
      historyFilter === "ALL" || transaction.type === historyFilter,
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
          <strong className="prime-wallet-hero__value">{prime.toLocaleString("uk-UA")}</strong>
          <small>Персональна преміум-валюта Mnemos</small>
        </div>

        <img
          className="prime-wallet-hero__art"
          src="/ui-illustrations/prime-cache.png"
          alt=""
          aria-hidden="true"
        />

        <div className="prime-wallet-hero__stats">
          <span>
            <small>Надійшло</small>
            <strong>+{earned.toLocaleString("uk-UA")}</strong>
          </span>
          <span>
            <small>Витрачено</small>
            <strong>−{spent.toLocaleString("uk-UA")}</strong>
          </span>
        </div>
      </section>

      <nav className="prime-wallet-shortcuts" aria-label="Швидкі дії PRIME">
        <button onClick={() => navigate({ id: "personalization-store" })}>
          <span className="is-plum"><Glyph name="spark" size={19} /></span>
          <strong>Магазин</strong>
          <small>Персоналізації</small>
          <b>›</b>
        </button>
        <button onClick={() => navigate({ id: "account-wallet" })}>
          <span className="is-lime"><Glyph name="wallet" size={19} /></span>
          <strong>COIN</strong>
          <small>Гаманець аккаунта</small>
          <b>›</b>
        </button>
      </nav>

      <section className="prime-topup-panel">
        <header>
          <span>
            <small>ПОПОВНЕННЯ</small>
            <strong>Додати PRIME</strong>
            <em>Пакети показують майбутній purchase flow. Списання та зарахування тут поки вимкнені.</em>
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
                <small>{pack.label}</small>
                <strong>{pack.amount} PRIME</strong>
                <em>{pack.bonus ? `+${pack.bonus} бонус` : "без бонусу"}</em>
              </span>
            </button>
          ))}
        </div>

        <div className="prime-topup-summary">
          <span>
            <small>Після поповнення</small>
            <strong>{prime} → {prime + packTotal} PRIME</strong>
            <em>{selectedPack.bonus ? `Пакет містить ${selectedPack.bonus} бонусних PRIME.` : "Базовий пакет без бонусу."}</em>
          </span>
          <span className="prime-topup-summary__amount">
            <CurrencyValue currency="PRIME" value={packTotal} compact />
          </span>
        </div>

        <button className="prime-topup-disabled" disabled>
          <Glyph name="clock" size={16} />
          Тимчасово недоступно
        </button>
      </section>

      <section className="prime-history-panel">
        <header className="wallet-history-head wallet-history-head--stacked">
          <span>
            <small>РУХ PRIME</small>
            <strong>Історія</strong>
          </span>

          <label className="wallet-history-sort">
            <Glyph name="swap" size={14} />
            <select value={historySort} onChange={(event) => setHistorySort(event.target.value as HistorySort)}>
              {(Object.keys(sortLabels) as HistorySort[]).map((sort) => (
                <option key={sort} value={sort}>{sortLabels[sort]}</option>
              ))}
            </select>
          </label>
        </header>

        <div className="prime-history-filter">
          {(["ALL", "CREDIT", "DEBIT"] as const).map((filter) => (
            <button
              key={filter}
              className={historyFilter === filter ? "is-active" : ""}
              onClick={() => setHistoryFilter(filter)}
            >
              {historyLabels[filter]}
            </button>
          ))}
        </div>

        <div className="prime-history-list">
          {visibleTransactions.map((transaction) => (
            <PrimeHistoryRow key={transaction.id} transaction={transaction} />
          ))}

          {loading ? (
            <div className="events-r2-loading" aria-label="Завантаження історії"><i /><i /><i /></div>
          ) : null}

          {!loading && !visibleTransactions.length ? (
            <div className="events-r2-empty">
              <Glyph name="wallet" size={22} />
              <strong>Операцій PRIME ще немає</strong>
            </div>
          ) : null}
        </div>
      </section>
    </div>
  )
}

function PrimeHistoryRow({ transaction }: { transaction: WalletTransactionResponse }) {
  const credit = transaction.type === "CREDIT"

  return (
    <article>
      <span className={`prime-history-list__sign ${credit ? "is-credit" : "is-debit"}`}>
        {credit ? "+" : "−"}
      </span>
      <span className="prime-history-list__copy">
        <strong>{transactionLabel(transaction.source)}</strong>
        <small>{formatTransactionDate(transaction.createdAt)}</small>
      </span>
      <span className={`prime-history-list__amount ${credit ? "is-credit" : ""}`}>
        {credit ? "+" : "−"}
        <CurrencyValue currency="PRIME" value={transaction.amount} compact />
      </span>
    </article>
  )
}

function transactionLabel(source: string) {
  const known = transactionSourceLabels[source as TransactionSource]
  if (known) return known

  return source
    .toLowerCase()
    .split("_")
    .map((part) => part.slice(0, 1).toUpperCase() + part.slice(1))
    .join(" ")
}

function formatTransactionDate(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat("uk-UA", {
    day: "numeric",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date)
}
