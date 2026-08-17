import { motion } from "framer-motion"
import { SlidersHorizontal } from "lucide-react"
import { PageFrame } from "../components/PageFrame"

export function EventsPage() {
  return (
    <PageFrame page="events">
      <section className="page-title-row">
        <div>
          <span className="eyebrow">LIVE</span>
          <h1>Події</h1>
        </div>

        <motion.button
          className="icon-button"
          whileTap={{ scale: 0.92 }}
          aria-label="Фільтри"
        >
          <SlidersHorizontal size={19} strokeWidth={1.8} />
        </motion.button>
      </section>

      <div className="event-segments" role="tablist" aria-label="Режим подій">
        <button className="event-segment event-segment--active" type="button" role="tab" aria-selected="true">
          Зараз
        </button>
        <button className="event-segment" type="button" role="tab" aria-selected="false">
          Розклад
        </button>
      </div>

      <section className="timeline" aria-label="Найближчі події">
        <div className="timeline-row timeline-row--active">
          <div className="timeline-time">19:20</div>
          <div className="timeline-line">
            <span />
          </div>
          <motion.button className="timeline-card" whileTap={{ scale: 0.99 }}>
            <span>Dungeon</span>
            <strong>Expert</strong>
            <small>через 4 хв</small>
          </motion.button>
        </div>

        <div className="timeline-row">
          <div className="timeline-time">20:00</div>
          <div className="timeline-line">
            <span />
          </div>
          <motion.button className="timeline-card" whileTap={{ scale: 0.99 }}>
            <span>Global</span>
            <strong>Moon</strong>
            <small>50%</small>
          </motion.button>
        </div>

        <div className="timeline-row">
          <div className="timeline-time">20:20</div>
          <div className="timeline-line">
            <span />
          </div>
          <motion.button className="timeline-card" whileTap={{ scale: 0.99 }}>
            <span>Dungeon</span>
            <strong>Expert</strong>
            <small>за розкладом</small>
          </motion.button>
        </div>

        <div className="timeline-row">
          <div className="timeline-time">21:00</div>
          <div className="timeline-line">
            <span />
          </div>
          <motion.button className="timeline-card" whileTap={{ scale: 0.99 }}>
            <span>Booster</span>
            <strong>Очікується</strong>
            <small>—</small>
          </motion.button>
        </div>
      </section>
    </PageFrame>
  )
}
