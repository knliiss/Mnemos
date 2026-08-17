import { Bell, Gift, MessageCircle, Radio, Wallet } from "lucide-react"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"
import { mockNotifications } from "../mockData"

const kindIcon = {
  event: Radio,
  reward: Gift,
  wallet: Wallet,
  social: MessageCircle,
}

export function NotificationsPage({ onBack }: { onBack: () => void }) {
  return (
    <PageFrame page="notifications">
      <PageHeader title="Сповіщення" eyebrow="INBOX" onBack={onBack} action={<Bell size={18} strokeWidth={1.7} />} />

      <section className="detail-context-row"><ScopeBadge scope="user" /><span>knalis</span></section>

      <section className="notification-feed">
        {mockNotifications.map((notification) => {
          const Icon = kindIcon[notification.kind]

          return (
            <article key={notification.id} className={`notification-card ${notification.unread ? "notification-card--unread" : ""}`}>
              <span className="notification-kind"><Icon size={17} strokeWidth={1.7} /></span>
              <div>
                <div className="notification-card-title">
                  <strong>{notification.title}</strong>
                  <span>{notification.time}</span>
                </div>
                <p>{notification.body}</p>
              </div>
            </article>
          )
        })}
      </section>
    </PageFrame>
  )
}
