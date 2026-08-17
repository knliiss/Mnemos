import { motion } from "framer-motion"
import { Check, Clock3, Minus, Plus } from "lucide-react"
import { useMemo, useState } from "react"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import type { MockAccount } from "../types"

const quickAmounts = [100, 250, 500, 1000]

export function BetDetailPage({
  activeAccount,
  onBack,
  onNotify,
}: {
  activeAccount: MockAccount
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [outcome, setOutcome] = useState<"yes" | "no">("yes")
  const [amount, setAmount] = useState(250)
  const [placed, setPlaced] = useState(false)

  const potential = useMemo(() => Math.round(amount * (outcome === "yes" ? 1.72 : 2.36)), [amount, outcome])

  const placeBet = () => {
    if (placed) {
      return
    }

    setPlaced(true)
    onNotify(`Ставку ${amount.toLocaleString("uk-UA")} прийнято`)
  }

  return (
    <PageFrame page="bet-detail">
      <PageHeader title="Moon · 20:00" eyebrow="EVENT BET" onBack={onBack} />

      <section className="detail-context-row detail-context-row--between">
        <div><ScopeBadge scope="account" /><span>{activeAccount.nickname}</span></div>
        <span className="inline-balance"><CurrencyMark currency="coin" />{activeAccount.coinBalance.toLocaleString("uk-UA")}</span>
      </section>

      <section className="bet-detail-hero">
        <span className="bet-detail-status"><Clock3 size={14} strokeWidth={1.7} />до 19:55</span>
        <h2>Чи відбудеться Moon?</h2>
        <div className="bet-detail-pool">
          <span>Банк</span>
          <strong><CurrencyMark currency="coin" />18 420</strong>
        </div>
      </section>

      <section className="choice-grid">
        <motion.button
          className={`choice-button ${outcome === "yes" ? "choice-button--active" : ""}`}
          onClick={() => setOutcome("yes")}
          whileTap={{ scale: 0.98 }}
        >
          <strong>YES</strong>
          <span>58%</span>
        </motion.button>
        <motion.button
          className={`choice-button ${outcome === "no" ? "choice-button--active" : ""}`}
          onClick={() => setOutcome("no")}
          whileTap={{ scale: 0.98 }}
        >
          <strong>NO</strong>
          <span>42%</span>
        </motion.button>
      </section>

      <section className="amount-panel">
        <div className="amount-control">
          <button onClick={() => setAmount((value) => Math.max(50, value - 50))} aria-label="Зменшити">
            <Minus size={17} strokeWidth={1.8} />
          </button>
          <div>
            <CurrencyMark currency="coin" />
            <strong>{amount.toLocaleString("uk-UA")}</strong>
          </div>
          <button onClick={() => setAmount((value) => Math.min(activeAccount.coinBalance, value + 50))} aria-label="Збільшити">
            <Plus size={17} strokeWidth={1.8} />
          </button>
        </div>

        <div className="quick-amounts">
          {quickAmounts.map((value) => (
            <button key={value} className={amount === value ? "quick-amount quick-amount--active" : "quick-amount"} onClick={() => setAmount(value)}>
              {value}
            </button>
          ))}
        </div>
      </section>

      <section className="bet-summary">
        <div><span>Результат</span><strong>{outcome.toUpperCase()}</strong></div>
        <div><span>Потенційно</span><strong className="inline-balance"><CurrencyMark currency="coin" />{potential.toLocaleString("uk-UA")}</strong></div>
      </section>

      <motion.button
        className={`primary-action ${placed ? "primary-action--done" : ""}`}
        onClick={placeBet}
        whileTap={placed ? undefined : { scale: 0.985 }}
        disabled={placed}
      >
        {placed && <Check size={18} strokeWidth={2} />}
        <span>{placed ? "Ставку прийнято" : "Поставити"}</span>
      </motion.button>
    </PageFrame>
  )
}
