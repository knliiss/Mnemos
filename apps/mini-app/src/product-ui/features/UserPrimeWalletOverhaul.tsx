import { useMemo, useState } from "react"
import { currentUser, userTransactions } from "../mockData"
import type { Route, WalletTransaction } from "../model"
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

export function UserPrimeWalletOverhaul({ navigate, back }: Props) {
  const [selectedPackId, setSelectedPackId] = useState(primePacks[1].id)
  const [historyFilter, setHistoryFilter] = useState<HistoryFilter>("ALL")
  const [historySort, setHistorySort] = useState<HistorySort>("NEWEST")
  const selectedPack = primePacks.find((pack) => pack.id === selectedPackId) ?? primePacks[0]

  const visibleTransactions = useMemo(() => {
    const filtered = userTransactions
      .map((transaction, index) => ({ transaction, order: userTransactions.length - index }))
      .filter(({ transaction }) => historyFilter === "ALL" || transaction.type === historyFilter)

    return filtered
      .sort((left, right) => {
        if (historySort === "OLDEST") return left.order - right.order
        if (historySort === "AMOUNT_DESC") return right.transaction.amount - left.transaction.amount || right.order - left.order
        if (historySort === "AMOUNT_ASC") return left.transaction.amount - right.transaction.amount || right.order - left.order
        return right.order - left.order
      })
      .map(({ transaction }) => transaction)
  }, [historyFilter, historySort])

  const spent = userTransactions
    .filter((transaction) => transaction.type === "DEBIT")
    .reduce((sum, transaction) => sum + transaction.amount, 0)

  const earned = userTransactions
    .filter((transaction) => transaction.type === "CREDIT")
    .reduce((sum, transaction) => sum + transaction.amount, 0)

  const packTotal = selectedPack.amount + selectedPack.bonus

  return (
    <div className="screen prime-wallet-v2">
      <PageHeader title="PRIME" eyebrow="USER" back={back} />

      <section className="prime-wallet-hero">
        <div className="prime-wallet-hero__copy">
          <span className="prime-wallet-hero__label">
            <CurrencyMark currency="PRIME" size={19} />
            <strong>PRIME</strong>
          </span>
          <strong className="prime-wallet-hero__value">{currentUser.prime.toLocaleString("uk-UA")}</strong>
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
            <strong>+{earned}</strong>
          </span>
          <span>
            <small>Витрачено</small>
            <strong>−{spent}</strong>
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
          <small>Купити для аккаунта</small>
          <b>›</b>
        </button>
      </nav>

      <section className="prime-topup-panel">
        <header>
          <span>
            <small>ПОПОВНЕННЯ</small>
            <strong>Додати PRIME</strong>
            <em>Інтерфейс уже готовий. Платіжний backend буде підключений пізніше.</em>
          </span>
          <span className="prime-topup-status">SOON</span>
        </header>

        <div className="prime-pack-grid">
          {primePacks.map((pack) => (
            <button
              key={pack.id}
              className={selectedPackId === pack.id ? "is-selected" : ""}
              onClick={() => setSelectedPackId(pack.id)}
            >
              <img src="/ui-illustrations/prime-package.png" alt="" aria-hidden="true" />
              <span>
                <small>{pack.label}</small>
                <strong>{pack.amount} PRIME</strong>
                <em>{pack.bonus ? `+${pack.bonus} бонус` : "без бонусу"}</em>
              </span>
              <b>{selectedPackId === pack.id ? <Glyph name="check" size={13} /> : null}</b>
            </button>
          ))}
        </div>

        <div className="prime-topup-summary">
          <span>
            <small>Після поповнення</small>
            <strong>{currentUser.prime} → {currentUser.prime + packTotal} PRIME</strong>
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
        <p className="prime-topup-note">Платіжний метод з'явиться після реалізації backend purchase flow.</p>
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
        </div>
      </section>
    </div>
  )
}

function PrimeHistoryRow({ transaction }: { transaction: WalletTransaction }) {
  const credit = transaction.type === "CREDIT"

  return (
    <article>
      <span className={`prime-history-list__sign ${credit ? "is-credit" : "is-debit"}`}>
        {credit ? "+" : "−"}
      </span>
      <span className="prime-history-list__copy">
        <strong>{transactionSourceLabels[transaction.source]}</strong>
        <small>{transaction.createdAt}</small>
      </span>
      <span className={`prime-history-list__amount ${credit ? "is-credit" : ""}`}>
        {credit ? "+" : "−"}
        <CurrencyValue currency="PRIME" value={transaction.amount} compact />
      </span>
    </article>
  )
}
