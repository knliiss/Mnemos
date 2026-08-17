import { Bell, Search } from "lucide-react"
import { useState } from "react"
import { PageFrame } from "../components/PageFrame"
import { PageHeader } from "../components/PageHeader"
import { ScopeBadge } from "../components/ScopeBadge"

const initialSubscriptions = [
  { id: "expert", title: "Expert dungeon", meta: "за 10 хв", enabled: true },
  { id: "moon", title: "Moon", meta: "за 5 хв", enabled: true },
  { id: "eclipse", title: "Eclipse", meta: "за 5 хв", enabled: true },
  { id: "raid", title: "Raid", meta: "при появі", enabled: true },
  { id: "booster", title: "Booster", meta: "агрегація", enabled: false },
]

export function SubscriptionsPage({
  onBack,
  onNotify,
}: {
  onBack: () => void
  onNotify: (message: string) => void
}) {
  const [subscriptions, setSubscriptions] = useState(initialSubscriptions)
  const [query, setQuery] = useState("")

  const toggle = (id: string) => {
    setSubscriptions((current) => current.map((item) => item.id === id ? { ...item, enabled: !item.enabled } : item))
    onNotify("Підписки оновлено")
  }

  return (
    <PageFrame page="subscriptions">
      <PageHeader title="Підписки" eyebrow="NOTIFY" onBack={onBack} />

      <section className="detail-context-row"><ScopeBadge scope="user" /><span>knalis</span></section>

      <label className="search-field">
        <Search size={17} strokeWidth={1.7} />
        <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Пошук" />
      </label>

      <section className="subscription-list">
        {subscriptions
          .filter((item) => item.title.toLowerCase().includes(query.toLowerCase()))
          .map((item) => (
            <button key={item.id} className="subscription-row" onClick={() => toggle(item.id)}>
              <span className="subscription-row-icon"><Bell size={17} strokeWidth={1.7} /></span>
              <div><strong>{item.title}</strong><span>{item.meta}</span></div>
              <span className={`toggle ${item.enabled ? "toggle--on" : ""}`}><span /></span>
            </button>
          ))}
      </section>

      <section className="detail-section">
        <div className="detail-section-heading"><h2>Item drop</h2><span className="detail-counter">3</span></div>
        <div className="tag-list">
          <button>Secret</button>
          <button>Mythical</button>
          <button>knalis</button>
          <button className="tag-list-add">+</button>
        </div>
      </section>
    </PageFrame>
  )
}
