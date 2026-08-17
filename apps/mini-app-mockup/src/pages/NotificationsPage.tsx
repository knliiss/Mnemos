import { motion } from "framer-motion"
import { Bell, CheckCheck, Gift, MessageCircle, Radio, Wallet } from "lucide-react"
import { useState } from "react"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import { mockNotifications } from "../mockData"

const icons = {
  event: Radio,
  reward: Gift,
  wallet: Wallet,
  social: MessageCircle,
}

export function NotificationsPage({ onBack }: { onBack: () => void }) {
  const [readIds, setReadIds] = useState(() => new Set(mockNotifications.filter((item) => !item.unread).map((item) => item.id)))

  const markAllRead = () => {
    setReadIds(new Set(mockNotifications.map((item) => item.id)))
  }

  return (
    <PageFrame page="notifications">
      <PageHeader
        title="Сповіщення"
        eyebrow="USER"
        onBack={onBack}
        action={
          <motion.button
            className="icon-button icon-button--quiet"
            onClick={markAllRead}
            whileTap={{ scale: 0.9 }}
            aria-label="Позначити все прочитаним"
          >
            <CheckCheck size={18} strokeWidth={1.8} />
          </motion.button>
        }
      />

      <section className="detail-context-row">
        <ScopeBadge scope="user" />
        <span>{mockNotifications.filter((item) => !readIds.has(item.id)).length} непрочитаних</span>
      </section>

      <section className="notification-feed">
        {mockNotifications.map((notification, index) => {
          const Icon = icons[notification.kind] ?? Bell
          const unread = !readIds.has(notification.id)

          return (
            <motion.button
              key={notification.id}
              className={`notification-row ${unread ? "notification-row--unread" : ""}`}
              onClick={() => setReadIds((current) => new Set([...current, notification.id]))}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.03, duration: 0.2 }}
              whileTap={{ scale: 0.994 }}
            >
              <span className="notification-kind">
                <Icon size={17} strokeWidth={1.75} />
              </span>

              <div className="notification-copy">
                <div>
                  <strong>{notification.title}</strong>
                  <span>{notification.time}</span>
                </div>
                <p>{notification.body}</p>
              </div>

              {unread && <span className="notification-unread-dot" />}
            </motion.button>
          )
        })}
      </section>
    </PageFrame>
  )
}
