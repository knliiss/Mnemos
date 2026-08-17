import { History } from "lucide-react"
import { useState } from "react"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import { mockTransactions } from "../mockData"
import type { Currency, MockAccount } from "../types"

export function WalletPage({
  activeAccount,
  onBack,
}: {
  activeAccount: MockAccount
  onBack: () => void
}) {
  const [currency, setCurrency] = useState<Currency>("coin")
  const transactions = mockTransactions.filter((transaction) => transaction.currency === currency)
  const balance = currency === "coin" ? activeAccount.coinBalance : activeAccount.primeBalance

  return (
    <PageFrame page="wallet">
      <PageHeader title="Гаманець" eyebrow="WALLET" onBack={onBack} />

      <section className="detail-context-row">
        <ScopeBadge scope="account" />
        <span>{activeAccount.nickname}</span>
      </section>

      <section className="wallet-balance">
        <CurrencyMark currency={currency} />
        <strong>{balance.toLocaleString("uk-UA")}</strong>
      </section>

      <div className="segmented-control segmented-control--detail">
        <button className={currency === "coin" ? "segmented-control__active" : ""} onClick={() => setCurrency("coin")}>
          <CurrencyMark currency="coin" /> Coin
        </button>
        <button className={currency === "prime" ? "segmented-control__active" : ""} onClick={() => setCurrency("prime")}>
          <CurrencyMark currency="prime" /> Prime
        </button>
      </div>

      <section className="detail-section wallet-history-section">
        <div className="detail-section-heading">
          <h2>Історія</h2>
          <History size={16} strokeWidth={1.7} />
        </div>

        <div className="transaction-list">
          {transactions.map((transaction) => (
            <div key={transaction.id} className="transaction-row">
              <span className={`transaction-sign ${transaction.amount > 0 ? "transaction-sign--positive" : ""}`}>
                {transaction.amount > 0 ? "+" : "−"}
              </span>
              <div>
                <strong>{transaction.source}</strong>
                <span>{transaction.time}</span>
              </div>
              <strong className={transaction.amount > 0 ? "transaction-amount--positive" : ""}>
                {transaction.amount > 0 ? "+" : ""}{transaction.amount.toLocaleString("uk-UA")}
              </strong>
            </div>
          ))}
        </div>
      </section>
    </PageFrame>
  )
}
