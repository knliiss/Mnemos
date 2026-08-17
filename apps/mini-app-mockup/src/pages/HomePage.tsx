import { motion } from "framer-motion"
import {
  Bell,
  CalendarDays,
  ChevronRight,
  Layers3,
  Sparkles,
} from "lucide-react"
import { CurrencyMark } from "../components/CurrencyMark"
import { PageFrame } from "../components/PageFrame"

export function HomePage() {
  return (
    <PageFrame page="home">
      <section className="welcome-row">
        <div>
          <span className="eyebrow">MNEMOS</span>
          <h1>Огляд</h1>
        </div>

        <motion.button
          className="icon-button"
          whileTap={{ scale: 0.92 }}
          aria-label="Сповіщення"
        >
          <Bell size={19} strokeWidth={1.8} />
          <span className="notification-dot" />
        </motion.button>
      </section>

      <motion.section
        className="balance-panel"
        aria-label="Баланс активного аккаунта"
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.04, duration: 0.25, ease: [0.22, 1, 0.36, 1] }}
      >
        <div className="balance-item">
          <CurrencyMark currency="coin" />
          <strong>12 480</strong>
        </div>

        <div className="balance-separator" />

        <div className="balance-item">
          <CurrencyMark currency="prime" />
          <strong>340</strong>
        </div>
      </motion.section>

      <section className="section-block">
        <div className="section-heading">
          <h2>Зараз</h2>
          <span className="live-indicator">
            <span />
            live
          </span>
        </div>

        <motion.button
          className="event-focus"
          whileTap={{ scale: 0.985 }}
          transition={{ type: "spring", stiffness: 500, damping: 34 }}
        >
          <div className="event-time">
            <span>19:20</span>
            <small>+4 хв</small>
          </div>

          <div className="event-copy">
            <strong>Expert dungeon</strong>
            <span>Наступна подія</span>
          </div>

          <ChevronRight size={18} strokeWidth={1.7} />
        </motion.button>

        <div className="event-list">
          <motion.button className="event-row" whileTap={{ backgroundColor: "rgba(255,255,255,0.025)" }}>
            <span className="event-type-dot" />
            <div>
              <strong>Moon</strong>
              <span>20:00</span>
            </div>
            <span className="event-meta">2h</span>
          </motion.button>

          <motion.button className="event-row" whileTap={{ backgroundColor: "rgba(255,255,255,0.025)" }}>
            <span className="event-type-dot event-type-dot--muted" />
            <div>
              <strong>Raid</strong>
              <span>Очікується</span>
            </div>
            <span className="event-meta">—</span>
          </motion.button>
        </div>
      </section>

      <section className="section-block section-block--compact">
        <div className="section-heading">
          <h2>Швидко</h2>
        </div>

        <div className="quick-grid">
          <motion.button className="quick-action" whileTap={{ scale: 0.97 }}>
            <CalendarDays size={19} strokeWidth={1.7} />
            <span>Розклад</span>
          </motion.button>

          <motion.button className="quick-action" whileTap={{ scale: 0.97 }}>
            <Layers3 size={19} strokeWidth={1.7} />
            <span>Підписки</span>
          </motion.button>

          <motion.button className="quick-action" whileTap={{ scale: 0.97 }}>
            <Sparkles size={19} strokeWidth={1.7} />
            <span>Нагорода</span>
          </motion.button>
        </div>
      </section>
    </PageFrame>
  )
}
