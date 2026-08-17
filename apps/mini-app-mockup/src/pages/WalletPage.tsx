import { motion } from "framer-motion"
import { ArrowDownLeft, ArrowUpRight } from "lucide-react"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import { mockTransactions } from "../mockData"
import type { MockAccount } from "../types"

export function WalletPage({
  activeAccount,
  onBack,
}: {
  activeAccount: MockAccount
  onBack: () => void
}) {
  return (
    <PageFrame page="wallet">
      <PageHeader title="Гаманець" eyebrow="ACCOUNT" onBack={onBack} />

      <div className="detail-scope-line">
        <ScopeBadge scope="account" />
        <span>{activeAccount.nickname}</span>
      </div>

      <section className="wallet-balance-grid">
        <article className="wallet-balance-card">
          <CurrencyMark currency="coin" />
          <div>
            <span>Coin</span>
            <strong>{activeAccount.coinBalance.toLocaleString("uk-UA")}</strong>
          </div>
        </article>

        <article className="wallet-balance-card wallet-balance-card--prime">
          <CurrencyMark currency="prime" />
          <div>
            <span>Prime</span>
            <strong>{activeAccount.primeBalance.toLocaleString("uk-UA")}</strong>
          </div>
        </article>
      </section>

      <section className="section-block section-block--compact">
        <div className="section-heading">
          <h2>Історія</h2>
          <button className="text-action">Усі</button>
        </div>

        <div className="transaction-list">
          {mockTransactions.map((transaction, index) => {
            const incoming = transaction.amount > 0

            return (
              <motion.article
                key={transaction.id}
                className="transaction-row"
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.025, duration: 0.2 }}
              >
                <span className={`transaction-direction ${incoming ? "transaction-direction--in" : ""}`}>
                  {incoming ? (
                    <ArrowDownLeft size={16} strokeWidth={1.8} />
                  ) : (
                    <ArrowUpRight size={16} strokeWidth={1.8} />
                  )}
                </span>

                <div className="transaction-copy">
                  <strong>{transaction.source}</strong>
                  <span>{transaction.time}</span>
                </div>

                <div className={`transaction-amount ${incoming ? "transaction-amount--in" : ""}`}>
                  <span>{incoming ? "+" : ""}{transaction.amount.toLocaleString("uk-UA")}</span>
                  <CurrencyMark currency={transaction.currency} />
                </div>
              </motion.article>
            )
          })}
        </div>
      </section>
    </PageFrame>
  )
}
