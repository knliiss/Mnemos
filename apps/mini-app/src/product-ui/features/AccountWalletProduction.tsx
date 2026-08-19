import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
import type { WalletTransactionResponse } from "@/shared/api/backendContracts"
import { getAccountTransactions } from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useProductData } from "../data/ProductDataProvider"
import { CurrencyValue, PageHeader } from "../shared/Primitives"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"

type HistorySort = "NEWEST" | "OLDEST" | "AMOUNT_DESC" | "AMOUNT_ASC"

const sortLabels: Record<HistorySort, string> = {
  NEWEST: "Спочатку нові",
  OLDEST: "Спочатку старі",
  AMOUNT_DESC: "Сума ↓",
  AMOUNT_ASC: "Сума ↑",
}

export function AccountWalletProduction({ back }: { back: () => void }) {
  const product = useProductData()
  const activeAccount = product.activeAccount
  const [transactions, setTransactions] = useState<WalletTransactionResponse[]>([])
  const [historySort, setHistorySort] = useState<HistorySort>("NEWEST")
  const [sortOpen, setSortOpen] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!activeAccount) return

    setLoading(true)
    void getAccountTransactions(activeAccount.id, 0, 100)
      .then((response) => setTransactions(response.items))
      .catch(handleError)
      .finally(() => setLoading(false))
  }, [activeAccount?.id])

  const totalEarned = useMemo(
    () => transactions
      .filter((transaction) => transaction.type === "CREDIT")
      .reduce((sum, transaction) => sum + transaction.amount, 0),
    [transactions],
  )

  const totalSpent = useMemo(
    () => transactions
      .filter((transaction) => transaction.type === "DEBIT")
      .reduce((sum, transaction) => sum + transaction.amount, 0),
    [transactions],
  )

  const history = useMemo(() => [...transactions].sort((left, right) => {
    if (historySort === "OLDEST") {
      return new Date(left.createdAt).getTime() - new Date(right.createdAt).getTime()
    }

    if (historySort === "AMOUNT_DESC") return right.amount - left.amount
    if (historySort === "AMOUNT_ASC") return left.amount - right.amount

    return new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime()
  }), [historySort, transactions])

  if (!activeAccount) return null

  const selectSort = (sort: HistorySort) => {
    setHistorySort(sort)
    setSortOpen(false)
  }

  return (
    <div className="screen account-wallet-v2">
      <PageHeader title="Гаманець" eyebrow={activeAccount.nickname} back={back} />

      <section className="coin-wallet-hero">
        <div className="coin-wallet-hero__copy">
          <span className="coin-wallet-hero__label">
            <CurrencyMark currency="COIN" size={20} />
            <strong>COIN</strong>
          </span>
          <strong className="coin-wallet-hero__value">{activeAccount.coin.toLocaleString("uk-UA")}</strong>
          <small>Баланс аккаунта</small>
        </div>

        <img className="coin-wallet-hero__art" src="/ui-illustrations/coin-vault.png" alt="" aria-hidden="true" />

        <div className="coin-wallet-hero__stats">
          <span><small>Надійшло</small><strong>+{totalEarned.toLocaleString("uk-UA")}</strong></span>
          <span><small>Витрачено</small><strong>−{totalSpent.toLocaleString("uk-UA")}</strong></span>
        </div>
      </section>

      <section className="coin-history-panel coin-history-panel--production">
        <header className="wallet-history-head">
          <span><small>РУХ COIN</small><strong>Історія</strong></span>

          <div className={`wallet-history-sort wallet-history-sort--custom ${sortOpen ? "is-open" : ""}`}>
            <button
              type="button"
              className="wallet-history-sort__trigger"
              aria-expanded={sortOpen}
              onClick={() => setSortOpen((value) => !value)}
            >
              <Glyph name="swap" size={14} />
              <span>{sortLabels[historySort]}</span>
              <b>⌄</b>
            </button>

            <AnimatePresence>
              {sortOpen ? (
                <motion.div
                  className="wallet-history-sort__menu"
                  initial={{ opacity: 0, y: -6, scale: 0.98 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: -4, scale: 0.985 }}
                >
                  {(Object.keys(sortLabels) as HistorySort[]).map((sort) => (
                    <button key={sort} type="button" className={historySort === sort ? "is-active" : ""} onClick={() => selectSort(sort)}>
                      <span>{sortLabels[sort]}</span>
                      {historySort === sort ? <Glyph name="check" size={13} /> : null}
                    </button>
                  ))}
                </motion.div>
              ) : null}
            </AnimatePresence>
          </div>
        </header>

        <div className="coin-history-list">
          {history.map((transaction) => {
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
          })}

          {!loading && !history.length ? (
            <div className="coin-history-empty">
              <CurrencyMark currency="COIN" size={22} />
              <strong>Транзакцій ще немає</strong>
              <small>Нарахування та витрати COIN зʼявляться тут.</small>
            </div>
          ) : null}

          {loading ? <div className="coin-history-loading"><i /><i /><i /></div> : null}
        </div>
      </section>
    </div>
  )
}

function transactionSourceLabel(source: string) {
  const labels: Record<string, string> = {
    DAILY_REWARD: "Щоденна нагорода",
    ITEM_DROP_REWARD: "Нагорода за дроп",
    BET_PLACE: "Ставка",
    BET_WIN: "Виграш ставки",
    BET_REFUND: "Повернення ставки",
    PERSONALIZATION_PURCHASE: "Персоналізація",
    PERSONALIZATION_REFUND: "Повернення персоналізації",
    PRIME_PURCHASE: "Покупка PRIME",
    DONATION: "Поповнення",
    ADMIN_ADJUSTMENT: "Коригування адміністратором",
  }

  return labels[source] ?? source.replaceAll("_", " ")
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
