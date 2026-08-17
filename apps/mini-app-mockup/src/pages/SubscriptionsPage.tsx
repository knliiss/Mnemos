import { motion } from "framer-motion"
import { Bell, BellOff, Search } from "lucide-react"
import { useState } from "react"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"

const initialSubscriptions = [
  { id: "expert", type: "Dungeon", title: "Expert", meta: ":20 / :50", active: true },
  { id: "moon", type: "Global", title: "Moon", meta: "кожне вікно", active: true },
  { id: "eclipse", type: "Global", title: "Eclipse", meta: "кожне вікно", active: false },
  { id: "raid", type: "Raid", title: "Raid", meta: "будь-яка локація", active: true },
  { id: "booster", type: "Booster", title: "Booster", meta: "усі типи", active: false },
  { id: "drop", type: "Item drop", title: "Secret", meta: "droppedFor: knalis", active: true },
]

export function SubscriptionsPage({
  onBack,
  onNotify,
}: {
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [items, setItems] = useState(initialSubscriptions)
  const [queryOpen, setQueryOpen] = useState(false)

  const toggle = (id: string) => {
    setItems((current) => current.map((item) => item.id === id ? { ...item, active: !item.active } : item))
    onNotify("Підписку оновлено")
  }

  return (
    <PageFrame page="subscriptions">
      <PageHeader
        title="Підписки"
        eyebrow="USER"
        onBack={onBack}
        action={
          <motion.button
            className="icon-button icon-button--quiet"
            onClick={() => setQueryOpen((value) => !value)}
            whileTap={{ scale: 0.9 }}
            aria-label="Пошук"
          >
            <Search size={18} strokeWidth={1.75} />
          </motion.button>
        }
      />

      <section className="detail-context-row">
        <ScopeBadge scope="user" />
        <span>{items.filter((item) => item.active).length} активних</span>
      </section>

      {queryOpen && (
        <motion.div
          className="mock-search"
          initial={{ opacity: 0, y: -4 }}
          animate={{ opacity: 1, y: 0 }}
        >
          <Search size={16} strokeWidth={1.7} />
          <input placeholder="Item, нік або подія" autoFocus />
        </motion.div>
      )}

      <section className="subscription-list">
        {items.map((item, index) => (
          <motion.button
            key={item.id}
            className={`subscription-row ${item.active ? "subscription-row--active" : ""}`}
            onClick={() => toggle(item.id)}
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.025, duration: 0.2 }}
            whileTap={{ scale: 0.992 }}
          >
            <span className="subscription-icon">
              {item.active ? <Bell size={17} strokeWidth={1.75} /> : <BellOff size={17} strokeWidth={1.7} />}
            </span>
            <div>
              <span>{item.type}</span>
              <strong>{item.title}</strong>
              <small>{item.meta}</small>
            </div>
            <span className={`mock-switch ${item.active ? "mock-switch--on" : ""}`}>
              <span />
            </span>
          </motion.button>
        ))}
      </section>

      <motion.button
        className="secondary-action"
        onClick={() => onNotify("Mock: відкрито створення підписки")}
        whileTap={{ scale: 0.985 }}
      >
        <Bell size={17} strokeWidth={1.75} />
        <span>Нова підписка</span>
      </motion.button>
    </PageFrame>
  )
}
