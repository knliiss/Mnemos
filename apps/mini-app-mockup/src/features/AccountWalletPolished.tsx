import { accountTransactions } from "../mockData"
import type { Account } from "../model"
import { transactionSourceLabels } from "../presentation"
import { CurrencyMark } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader, Section } from "../shared/Primitives"
import { WalletHeroArt } from "../components/WalletHeroArt"

export function AccountWalletPolished({ activeAccount, back }: { activeAccount: Account; back: () => void }) {
  return (
    <div className="screen account-wallet-polished">
      <PageHeader title="Гаманець" eyebrow={activeAccount.nickname} back={back} />

      <section className="wallet-hero-polished">
        <div className="wallet-hero-polished__copy">
          <span className="wallet-hero-polished__label">
            <CurrencyMark currency="COIN" size={20} />
            <strong>COIN</strong>
          </span>
          <strong className="wallet-hero-polished__value">{activeAccount.coin.toLocaleString("uk-UA")}</strong>
          <small>Баланс аккаунта</small>
        </div>
        <WalletHeroArt />
      </section>

      <Section title="Історія">
        <div className="transaction-list wallet-history-polished">
          {accountTransactions.map((transaction) => (
            <div className="transaction-row" key={transaction.id}>
              <span className={`transaction-sign ${transaction.type === "CREDIT" ? "is-credit" : ""}`}>
                {transaction.type === "CREDIT" ? "+" : "−"}
              </span>
              <span>
                <strong>{transactionSourceLabels[transaction.source]}</strong>
                <small>{transaction.createdAt}</small>
              </span>
              <CurrencyValue currency="COIN" value={transaction.amount} compact />
            </div>
          ))}
        </div>
      </Section>
    </div>
  )
}
