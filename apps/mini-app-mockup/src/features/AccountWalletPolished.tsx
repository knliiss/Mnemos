import { useMemo, useState } from "react"
import { accountTransactions, currentUser } from "../mockData"
import type { Account, WalletTransaction } from "../model"
import { transactionSourceLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../shared/Primitives"

type Props = {
  activeAccount: Account
  setActiveAccount: (account: Account) => void
  accounts: Account[]
  setAccounts: (accounts: Account[]) => void
  back: () => void
}

type CoinPack = {
  id: string
  prime: number
  coin: number
  badge: string
}

type PurchaseLog = {
  id: string
  coin: number
  prime: number
  createdAt: string
}

type HistorySort = "NEWEST" | "OLDEST" | "AMOUNT_DESC" | "AMOUNT_ASC"

type HistoryEntry =
  | {
      kind: "PURCHASE"
      id: string
      amount: number
      order: number
      purchase: PurchaseLog
    }
  | {
      kind: "TRANSACTION"
      id: string
      amount: number
      order: number
      transaction: WalletTransaction
    }

const coinPacks: CoinPack[] = [
  { id: "coin-2k", prime: 8, coin: 2_000, badge: "Старт" },
  { id: "coin-55k", prime: 20, coin: 5_500, badge: "+10%" },
  { id: "coin-12k", prime: 40, coin: 12_000, badge: "+20%" },
]

const sortLabels: Record<HistorySort, string> = {
  NEWEST: "Спочатку нові",
  OLDEST: "Спочатку старі",
  AMOUNT_DESC: "Сума ↓",
  AMOUNT_ASC: "Сума ↑",
}

export function AccountWalletPolished({
  activeAccount,
  setActiveAccount,
  accounts,
  setAccounts,
  back,
}: Props) {
  const [coinBalance, setCoinBalance] = useState(activeAccount.coin)
  const [primeBalance, setPrimeBalance] = useState(currentUser.prime)
  const [selectedPackId, setSelectedPackId] = useState(coinPacks[1].id)
  const [purchaseLog, setPurchaseLog] = useState<PurchaseLog[]>([])
  const [purchaseState, setPurchaseState] = useState<"IDLE" | "SUCCESS">("IDLE")
  const [historySort, setHistorySort] = useState<HistorySort>("NEWEST")

  const selectedPack = coinPacks.find((pack) => pack.id === selectedPackId) ?? coinPacks[0]
  const canBuy = primeBalance >= selectedPack.prime
  const afterCoin = coinBalance + selectedPack.coin
  const afterPrime = primeBalance - selectedPack.prime

  const totalEarned = useMemo(
    () => accountTransactions
      .filter((transaction) => transaction.type === "CREDIT")
      .reduce((sum, transaction) => sum + transaction.amount, 0) + purchaseLog.reduce((sum, item) => sum + item.coin, 0),
    [purchaseLog],
  )

  const totalSpent = accountTransactions
    .filter((transaction) => transaction.type === "DEBIT")
    .reduce((sum, transaction) => sum + transaction.amount, 0)

  const historyEntries = useMemo<HistoryEntry[]>(() => {
    const purchaseEntries: HistoryEntry[] = purchaseLog.map((purchase, index) => ({
      kind: "PURCHASE",
      id: purchase.id,
      amount: purchase.coin,
      order: 10_000 + purchaseLog.length - index,
      purchase,
    }))

    const transactionEntries: HistoryEntry[] = accountTransactions.map((transaction, index) => ({
      kind: "TRANSACTION",
      id: transaction.id,
      amount: transaction.amount,
      order: accountTransactions.length - index,
      transaction,
    }))

    return [...purchaseEntries, ...transactionEntries].sort((left, right) => {
      if (historySort === "OLDEST") return left.order - right.order
      if (historySort === "AMOUNT_DESC") return right.amount - left.amount || right.order - left.order
      if (historySort === "AMOUNT_ASC") return left.amount - right.amount || right.order - left.order
      return right.order - left.order
    })
  }, [historySort, purchaseLog])

  const buyCoins = () => {
    if (!canBuy) return

    const nextCoinBalance = coinBalance + selectedPack.coin
    const nextPrimeBalance = primeBalance - selectedPack.prime
    const updatedAccount = {
      ...activeAccount,
      coin: nextCoinBalance,
    }

    setCoinBalance(nextCoinBalance)
    setPrimeBalance(nextPrimeBalance)
    currentUser.prime = nextPrimeBalance
    setActiveAccount(updatedAccount)
    setAccounts(accounts.map((account) => account.id === activeAccount.id ? updatedAccount : account))
    setPurchaseLog((items) => [
      {
        id: `coin-purchase-${Date.now()}`,
        coin: selectedPack.coin,
        prime: selectedPack.prime,
        createdAt: "щойно",
      },
      ...items,
    ])
    setPurchaseState("SUCCESS")

    window.setTimeout(() => setPurchaseState("IDLE"), 2200)
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
          <strong className="coin-wallet-hero__value">{coinBalance.toLocaleString("uk-UA")}</strong>
          <small>Баланс аккаунта</small>
        </div>

        <img
          className="coin-wallet-hero__art"
          src="/ui-illustrations/coin-vault.png"
          alt=""
          aria-hidden="true"
        />

        <div className="coin-wallet-hero__stats">
          <span>
            <small>Надійшло</small>
            <strong>+{totalEarned.toLocaleString("uk-UA")}</strong>
          </span>
          <span>
            <small>Витрачено</small>
            <strong>−{totalSpent.toLocaleString("uk-UA")}</strong>
          </span>
        </div>
      </section>

      <section className="coin-buy-panel">
        <header>
          <span>
            <small>ОБМІН</small>
            <strong>Купити COIN за PRIME</strong>
            <em>Моковий purchase flow. Пакети й курс можна буде змінити після узгодження backend.</em>
          </span>
          <span className="coin-buy-panel__prime">
            <small>Доступно</small>
            <CurrencyValue currency="PRIME" value={primeBalance} compact />
          </span>
        </header>

        <div className="coin-pack-grid">
          {coinPacks.map((pack) => (
            <button
              key={pack.id}
              className={selectedPackId === pack.id ? "is-selected" : ""}
              onClick={() => {
                setSelectedPackId(pack.id)
                setPurchaseState("IDLE")
              }}
            >
              <span className="coin-pack-grid__coin">
                <CurrencyMark currency="COIN" size={21} />
              </span>
              <span className="coin-pack-grid__copy">
                <small>{pack.badge}</small>
                <strong>{pack.coin.toLocaleString("uk-UA")} COIN</strong>
                <em>{pack.prime} PRIME</em>
              </span>
              <b>{selectedPackId === pack.id ? <Glyph name="check" size={13} /> : null}</b>
            </button>
          ))}
        </div>

        <div className="coin-buy-summary">
          <div>
            <small>COIN після покупки</small>
            <strong>{coinBalance.toLocaleString("uk-UA")} → {afterCoin.toLocaleString("uk-UA")}</strong>
          </div>
          <div>
            <small>PRIME після покупки</small>
            <strong>{primeBalance} → {Math.max(0, afterPrime)}</strong>
          </div>
        </div>

        <button className="coin-buy-action" disabled={!canBuy} onClick={buyCoins}>
          {purchaseState === "SUCCESS" ? (
            <>
              <Glyph name="check" size={16} />
              Куплено {selectedPack.coin.toLocaleString("uk-UA")} COIN
            </>
          ) : canBuy ? (
            <>
              Купити за
              <CurrencyValue currency="PRIME" value={selectedPack.prime} compact />
            </>
          ) : (
            "Недостатньо PRIME"
          )}
        </button>
      </section>

      <section className="coin-history-panel">
        <header className="wallet-history-head">
          <span>
            <small>РУХ COIN</small>
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

        <div className="coin-history-list">
          {historyEntries.map((entry) => {
            if (entry.kind === "PURCHASE") {
              const purchase = entry.purchase

              return (
                <article key={entry.id} className="is-purchase">
                  <span className="coin-history-list__sign is-credit">+</span>
                  <span className="coin-history-list__copy">
                    <strong>Купівля за PRIME</strong>
                    <small>{purchase.createdAt} · −{purchase.prime} PRIME</small>
                  </span>
                  <span className="coin-history-list__amount is-credit">
                    +<CurrencyValue currency="COIN" value={purchase.coin} compact />
                  </span>
                </article>
              )
            }

            const transaction = entry.transaction
            const credit = transaction.type === "CREDIT"

            return (
              <article key={entry.id}>
                <span className={`coin-history-list__sign ${credit ? "is-credit" : "is-debit"}`}>
                  {credit ? "+" : "−"}
                </span>
                <span className="coin-history-list__copy">
                  <strong>{transactionSourceLabels[transaction.source]}</strong>
                  <small>{transaction.createdAt}</small>
                </span>
                <span className={`coin-history-list__amount ${credit ? "is-credit" : ""}`}>
                  {credit ? "+" : "−"}
                  <CurrencyValue currency="COIN" value={transaction.amount} compact />
                </span>
              </article>
            )
          })}
        </div>
      </section>
    </div>
  )
}
