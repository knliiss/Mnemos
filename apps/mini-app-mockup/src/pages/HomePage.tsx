import { motion } from "framer-motion"
import {
  Bell,
  ChevronRight,
  Gift,
  Layers,
  Sparkles,
  Target,
} from "lucide-react"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"
import { mockEvents } from "../mockData"
import type { MockAccount, Page } from "../types"

export function HomePage({
  activeAccount,
  onNavigate,
}: {
  activeAccount: MockAccount
  onNavigate: (page: Page) => void
}) {
  const nextEvent = mockEvents[0]

  return (
    <PageFrame page="home">
      <section className="welcome-row">
        <div>
          <span className="eyebrow">MNEMOS</span>
          <h1>Сьогодні</h1>
        </div>

        <motion.button
          className="icon-button"
          onClick={() => onNavigate("notifications")}
          whileTap={{ scale: 0.92 }}
          aria-label="Сповіщення"
        >
          <Bell size={19} strokeWidth={1.8} />
          <span className="notification-dot" />
        </motion.button>
      </section>

      <section className="account-scope-line">
        <Layers size={13} strokeWidth={1.8} />
        <span>{activeAccount.nickname}</span>
      </section>

      <motion.button
        className="balance-panel balance-panel--button"
        onClick={() => onNavigate("wallet")}
        whileTap={{ scale: 0.992 }}
        aria-label="Відкрити гаманець"
      >
        <div className="balance-item">
          <CurrencyMark currency="coin" />
          <strong>{activeAccount.coinBalance.toLocaleString("uk-UA")}</strong>
        </div>

        <div className="balance-separator" />

        <div className="balance-item">
          <CurrencyMark currency="prime" />
          <strong>{activeAccount.primeBalance.toLocaleString("uk-UA")}</strong>
        </div>

        <ChevronRight className="balance-chevron" size={16} strokeWidth={1.7} />
      </motion.button>

      <section className="section-block">
        <div className="section-heading">
          <h2>Наступне</h2>
          <span className="live-indicator">
            <span />
            live
          </span>
        </div>

        <motion.button
          className="event-focus"
          onClick={() => onNavigate("event-detail")}
          whileTap={{ scale: 0.985 }}
        >
          <div className="event-time">
            <span>{nextEvent.time}</span>
            <small>{nextEvent.relative}</small>
          </div>

          <div className="event-copy">
            <strong>{nextEvent.title}</strong>
            <span>{nextEvent.meta}</span>
          </div>

          <ChevronRight size={18} strokeWidth={1.7} />
        </motion.button>
      </section>

      <section className="home-focus-grid">
        <motion.button
          className="home-focus-card"
          onClick={() => onNavigate("rewards")}
          whileTap={{ scale: 0.975 }}
        >
          <span className="home-focus-icon">
            <Gift size={18} strokeWidth={1.75} />
          </span>
          <div>
            <small>Streak</small>
            <strong>{activeAccount.streak} днів</strong>
          </div>
          <span className="home-focus-meta">+420</span>
        </motion.button>

        <motion.button
          className="home-focus-card"
          onClick={() => onNavigate("bets")}
          whileTap={{ scale: 0.975 }}
        >
          <span className="home-focus-icon">
            <Target size={18} strokeWidth={1.75} />
          </span>
          <div>
            <small>Bet</small>
            <strong>Moon</strong>
          </div>
          <span className="home-focus-meta">19:55</span>
        </motion.button>
      </section>

      <section className="section-block section-block--compact">
        <div className="section-heading">
          <h2>Активність</h2>
        </div>

        <div className="activity-list">
          <button className="activity-row" onClick={() => onNavigate("events")}>
            <span className="activity-mark activity-mark--live" />
            <div>
              <strong>Booster x2</strong>
              <span>активний · ще 18 хв</span>
            </div>
            <span className="activity-time">18:58</span>
          </button>

          <button className="activity-row" onClick={() => onNavigate("personalization")}>
            <span className="activity-icon">
              <Sparkles size={15} strokeWidth={1.7} />
            </span>
            <div>
              <strong>Default</strong>
              <span>активний notification pack</span>
            </div>
            <ChevronRight size={16} strokeWidth={1.7} />
          </button>
        </div>
      </section>
    </PageFrame>
  )
}
