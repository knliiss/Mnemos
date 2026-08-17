import { motion } from "framer-motion"
import { BellRing, ChevronRight, SlidersHorizontal } from "lucide-react"
import { useState } from "react"
import { PageFrame } from "../components/PageFrame"
import { mockEvents } from "../mockData"
import type { Page } from "../types"

const filters = ["Усі", "Dungeon", "Global", "Raid"] as const

export function EventsPage({ onNavigate }: { onNavigate: (page: Page) => void }) {
  const [mode, setMode] = useState<"now" | "schedule">("now")
  const [filter, setFilter] = useState<(typeof filters)[number]>("Усі")

  const visibleEvents = mockEvents.filter((event) => filter === "Усі" || event.type === filter)

  return (
    <PageFrame page="events">
      <section className="page-title-row">
        <div>
          <span className="eyebrow">LIVE FEED</span>
          <h1>Події</h1>
        </div>

        <motion.button
          className="icon-button"
          onClick={() => setFilter((current) => {
            const index = filters.indexOf(current)
            return filters[(index + 1) % filters.length]
          })}
          whileTap={{ scale: 0.92 }}
          aria-label="Змінити фільтр"
        >
          <SlidersHorizontal size={19} strokeWidth={1.8} />
        </motion.button>
      </section>

      <div className="segmented-control">
        <button
          className={mode === "now" ? "segmented-control__active" : ""}
          onClick={() => setMode("now")}
        >
          Зараз
        </button>
        <button
          className={mode === "schedule" ? "segmented-control__active" : ""}
          onClick={() => setMode("schedule")}
        >
          Розклад
        </button>
      </div>

      <div className="filter-strip" aria-label="Фільтр подій">
        {filters.map((item) => (
          <button
            key={item}
            className={filter === item ? "filter-chip filter-chip--active" : "filter-chip"}
            onClick={() => setFilter(item)}
          >
            {item}
          </button>
        ))}
      </div>

      {mode === "now" ? (
        <section className="event-stream">
          {visibleEvents.map((event, index) => (
            <motion.button
              key={event.id}
              className={`stream-card stream-card--${event.status}`}
              onClick={() => onNavigate("event-detail")}
              initial={{ opacity: 0, y: 8 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.035, duration: 0.22 }}
              whileTap={{ scale: 0.99 }}
            >
              <div className="stream-card-time">
                <strong>{event.time}</strong>
                <span>{event.relative}</span>
              </div>

              <div className="stream-card-copy">
                <span>{event.type}</span>
                <strong>{event.title}</strong>
                <small>{event.subtitle}</small>
              </div>

              <ChevronRight size={17} strokeWidth={1.7} />
            </motion.button>
          ))}
        </section>
      ) : (
        <section className="timeline">
          {mockEvents.slice(0, 3).map((event, index) => (
            <button
              key={event.id}
              className={`timeline-row ${index === 0 ? "timeline-row--active" : ""}`}
              onClick={() => onNavigate("event-detail")}
            >
              <div className="timeline-time">{event.time}</div>
              <div className="timeline-line">
                <span />
              </div>
              <div className="timeline-card">
                <span>{event.type}</span>
                <strong>{event.title}</strong>
                <small>{event.relative}</small>
              </div>
            </button>
          ))}
        </section>
      )}

      <motion.button
        className="footer-action"
        onClick={() => onNavigate("subscriptions")}
        whileTap={{ scale: 0.985 }}
      >
        <BellRing size={17} strokeWidth={1.75} />
        <span>Підписки</span>
        <span className="footer-action-value">8</span>
        <ChevronRight size={17} strokeWidth={1.7} />
      </motion.button>
    </PageFrame>
  )
}
