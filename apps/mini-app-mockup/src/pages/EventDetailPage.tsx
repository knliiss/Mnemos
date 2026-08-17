import { motion } from "framer-motion"
import { Bell, BellOff, Clock3, Radio, Users } from "lucide-react"
import { useState } from "react"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"

export function EventDetailPage({
  onBack,
  onNotify,
}: {
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [subscribed, setSubscribed] = useState(true)

  const toggleSubscription = () => {
    setSubscribed((current) => !current)
    onNotify(subscribed ? "Підписку вимкнено" : "Підписку увімкнено")
  }

  return (
    <PageFrame page="event-detail">
      <PageHeader title="Expert dungeon" eyebrow="DUNGEON" onBack={onBack} />

      <section className="detail-hero detail-hero--event">
        <div className="event-detail-time">
          <strong>19:20</strong>
          <span>через 15 хв</span>
        </div>
        <div className="event-detail-status">
          <span className="live-indicator">
            <span />
            schedule
          </span>
          <small>Московський час</small>
        </div>
      </section>

      <section className="detail-facts">
        <div>
          <Clock3 size={16} strokeWidth={1.7} />
          <span>Кожні :20 / :50</span>
        </div>
        <div>
          <Radio size={16} strokeWidth={1.7} />
          <span>Realtime</span>
        </div>
        <div>
          <Users size={16} strokeWidth={1.7} />
          <span>1 collector online</span>
        </div>
      </section>

      <section className="detail-section">
        <div className="detail-section-heading">
          <h2>Сповіщення</h2>
          <ScopeBadge scope="user" />
        </div>

        <motion.button
          className={`subscription-card ${subscribed ? "subscription-card--active" : ""}`}
          onClick={toggleSubscription}
          whileTap={{ scale: 0.99 }}
        >
          <span className="subscription-card-icon">
            {subscribed ? <Bell size={18} strokeWidth={1.8} /> : <BellOff size={18} strokeWidth={1.8} />}
          </span>
          <div>
            <strong>{subscribed ? "Увімкнено" : "Вимкнено"}</strong>
            <span>До початку та при зміні стану</span>
          </div>
          <span className={`toggle ${subscribed ? "toggle--on" : ""}`}>
            <span />
          </span>
        </motion.button>
      </section>

      <section className="detail-section">
        <div className="detail-section-heading">
          <h2>Сьогодні</h2>
        </div>

        <div className="compact-history">
          <div>
            <span>18:50</span>
            <strong>Expert dungeon</strong>
            <small>підтверджено</small>
          </div>
          <div>
            <span>18:20</span>
            <strong>Expert dungeon</strong>
            <small>підтверджено</small>
          </div>
          <div>
            <span>17:50</span>
            <strong>Expert dungeon</strong>
            <small>підтверджено</small>
          </div>
        </div>
      </section>
    </PageFrame>
  )
}
