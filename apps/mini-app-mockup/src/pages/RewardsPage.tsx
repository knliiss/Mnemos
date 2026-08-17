import { motion } from "framer-motion"
import { Check, Gift, RefreshCcw } from "lucide-react"
import { useState } from "react"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import type { MockAccount } from "../types"

const days = [
  { label: "Пн", value: 120, done: true },
  { label: "Вт", value: 160, done: true },
  { label: "Ср", value: 220, done: true },
  { label: "Чт", value: 280, done: true },
  { label: "Пт", value: 340, done: true },
  { label: "Сб", value: 380, done: true },
  { label: "Нд", value: 420, done: false },
]

export function RewardsPage({
  activeAccount,
  onBack,
  onNotify,
}: {
  activeAccount: MockAccount
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [claimed, setClaimed] = useState(false)

  const claim = () => {
    if (claimed) {
      return
    }

    setClaimed(true)
    onNotify("Нагороду отримано · +420")
  }

  return (
    <PageFrame page="rewards">
      <PageHeader title="Нагороди" eyebrow="DAILY" onBack={onBack} />

      <section className="detail-context-row">
        <ScopeBadge scope="account" />
        <span>{activeAccount.nickname}</span>
      </section>

      <motion.section
        className={`reward-hero ${claimed ? "reward-hero--claimed" : ""}`}
        layout
      >
        <div className="reward-streak">
          <span>Streak</span>
          <strong>{activeAccount.streak}</strong>
          <small>днів</small>
        </div>

        <div className="reward-value">
          <CurrencyMark currency="coin" />
          <strong>{claimed ? "Отримано" : "+420"}</strong>
        </div>
      </motion.section>

      <section className="week-strip" aria-label="Нагороди тижня">
        {days.map((day, index) => {
          const done = day.done || (index === days.length - 1 && claimed)

          return (
            <div key={day.label} className={`week-day ${done ? "week-day--done" : "week-day--today"}`}>
              <span>{day.label}</span>
              <div>{done ? <Check size={13} strokeWidth={2} /> : <Gift size={13} strokeWidth={1.8} />}</div>
              <small>{day.value}</small>
            </div>
          )
        })}
      </section>

      <motion.button
        className={`primary-action ${claimed ? "primary-action--done" : ""}`}
        onClick={claim}
        whileTap={claimed ? undefined : { scale: 0.985 }}
        disabled={claimed}
      >
        {claimed ? <Check size={18} strokeWidth={2} /> : <Gift size={18} strokeWidth={1.8} />}
        <span>{claimed ? "Отримано" : "Забрати"}</span>
      </motion.button>

      <section className="detail-section">
        <div className="detail-section-heading">
          <h2>Відновлення</h2>
          <span className="detail-counter">2 / місяць</span>
        </div>

        <div className="info-row">
          <span className="info-row-icon">
            <RefreshCcw size={17} strokeWidth={1.7} />
          </span>
          <div>
            <strong>Streak recovery</strong>
            <span>Доступно 2 відновлення</span>
          </div>
          <button onClick={() => onNotify("Streak не потребує відновлення")}>Перевірити</button>
        </div>
      </section>

      <section className="detail-section">
        <div className="detail-section-heading">
          <h2>Останні</h2>
        </div>

        <div className="reward-history">
          <div>
            <span className="reward-history-mark"><CurrencyMark currency="coin" /></span>
            <div><strong>Daily</strong><span>сьогодні · 10:14</span></div>
            <strong>+420</strong>
          </div>
          <div>
            <span className="reward-history-mark"><CurrencyMark currency="coin" /></span>
            <div><strong>Item drop</strong><span>15 серп. · 21:31</span></div>
            <strong>+180</strong>
          </div>
          <div>
            <span className="reward-history-mark"><CurrencyMark currency="prime" /></span>
            <div><strong>Daily</strong><span>14 серп. · 08:12</span></div>
            <strong>+10</strong>
          </div>
        </div>
      </section>
    </PageFrame>
  )
}
