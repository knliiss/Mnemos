import { AnimatePresence, LayoutGroup, motion } from "framer-motion"
import {
  Bell,
  CalendarDays,
  ChevronDown,
  ChevronRight,
  CircleUserRound,
  Home,
  Layers3,
  Link2,
  Settings2,
  SlidersHorizontal,
} from "lucide-react"
import { useState } from "react"
import { CristalixHead } from "./components/CristalixHead"

type Page = "home" | "events" | "profile"
type Currency = "coin" | "prime"

const MOCK_SKIN_URL =
  "https://webdata.c7x.dev/textures/skin/46d7a80e-74f5-11eb-acca-1cb72caa35fd"

const pageTransition = {
  duration: 0.24,
  ease: [0.22, 1, 0.36, 1] as const,
}

function CurrencyMark({ currency }: { currency: Currency }) {
  if (currency === "coin") {
    return (
      <svg className="currency-mark" viewBox="0 0 24 24" aria-label="Coins">
        <circle cx="12" cy="12" r="8.1" />
        <circle cx="12" cy="12" r="3.15" />
      </svg>
    )
  }

  return (
    <svg className="currency-mark currency-mark--prime" viewBox="0 0 24 24" aria-label="Primes">
      <path d="M12 3.2 20.2 12 12 20.8 3.8 12 12 3.2Z" />
      <path d="m12 3.2 2.45 8.8L12 20.8 9.55 12 12 3.2Z" />
    </svg>
  )
}

function AccountMark() {
  return (
    <span className="account-mark" aria-hidden="true">
      <span />
      <span />
      <span />
      <span />
    </span>
  )
}

function PageFrame({ children, page }: { children: React.ReactNode; page: Page }) {
  return (
    <motion.div
      key={page}
      className="page"
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -5 }}
      transition={pageTransition}
    >
      {children}
    </motion.div>
  )
}

function HomePage() {
  return (
    <PageFrame page="home">
      <section className="welcome-row">
        <div>
          <span className="eyebrow">MNEMOS</span>
          <h1>Добрий вечір.</h1>
        </div>

        <motion.button className="icon-button" whileTap={{ scale: 0.92 }} aria-label="Сповіщення">
          <Bell size={19} strokeWidth={1.8} />
          <span className="notification-dot" />
        </motion.button>
      </section>

      <section className="balance-panel" aria-label="Баланс активного аккаунта">
        <div className="balance-item">
          <CurrencyMark currency="coin" />
          <strong>12 480</strong>
        </div>

        <div className="balance-separator" />

        <div className="balance-item">
          <CurrencyMark currency="prime" />
          <strong>340</strong>
        </div>
      </section>

      <section className="section-block">
        <div className="section-heading">
          <h2>Зараз</h2>
          <span className="live-indicator">
            <span />
            live
          </span>
        </div>

        <motion.button className="event-focus" whileTap={{ scale: 0.985 }}>
          <div className="event-time">
            <span>18:50</span>
            <small>+4 хв</small>
          </div>

          <div className="event-copy">
            <strong>Expert dungeon</strong>
            <span>Наступна подія</span>
          </div>

          <ChevronRight size={18} strokeWidth={1.7} />
        </motion.button>

        <div className="event-list">
          <div className="event-row">
            <span className="event-type-dot" />
            <div>
              <strong>Moon</strong>
              <span>20:00</span>
            </div>
            <span className="event-meta">2h</span>
          </div>

          <div className="event-row">
            <span className="event-type-dot event-type-dot--muted" />
            <div>
              <strong>Raid</strong>
              <span>Очікується</span>
            </div>
            <span className="event-meta">—</span>
          </div>
        </div>
      </section>

      <section className="quick-grid">
        <motion.button className="quick-action" whileTap={{ scale: 0.97 }}>
          <CalendarDays size={19} strokeWidth={1.7} />
          <span>Розклад</span>
        </motion.button>

        <motion.button className="quick-action" whileTap={{ scale: 0.97 }}>
          <Layers3 size={19} strokeWidth={1.7} />
          <span>Підписки</span>
        </motion.button>
      </section>
    </PageFrame>
  )
}

function EventsPage() {
  return (
    <PageFrame page="events">
      <section className="page-title-row">
        <div>
          <span className="eyebrow">LIVE</span>
          <h1>Події</h1>
        </div>

        <motion.button className="icon-button" whileTap={{ scale: 0.92 }} aria-label="Фільтри">
          <SlidersHorizontal size={19} strokeWidth={1.8} />
        </motion.button>
      </section>

      <section className="timeline">
        <div className="timeline-row timeline-row--active">
          <div className="timeline-time">18:50</div>
          <div className="timeline-line">
            <span />
          </div>
          <div className="timeline-card">
            <span>Dungeon</span>
            <strong>Expert</strong>
            <small>через 4 хв</small>
          </div>
        </div>

        <div className="timeline-row">
          <div className="timeline-time">20:00</div>
          <div className="timeline-line">
            <span />
          </div>
          <div className="timeline-card">
            <span>Global</span>
            <strong>Moon</strong>
            <small>50%</small>
          </div>
        </div>

        <div className="timeline-row">
          <div className="timeline-time">20:20</div>
          <div className="timeline-line">
            <span />
          </div>
          <div className="timeline-card">
            <span>Dungeon</span>
            <strong>Expert</strong>
            <small>за розкладом</small>
          </div>
        </div>
      </section>
    </PageFrame>
  )
}

function ProfilePage() {
  return (
    <PageFrame page="profile">
      <section className="profile-topline">
        <div className="user-identity">
          <span className="user-avatar">K</span>
          <div>
            <strong>knalis</strong>
            <span>@knalis</span>
          </div>
        </div>

        <motion.button className="icon-button" whileTap={{ scale: 0.92 }} aria-label="Налаштування">
          <Settings2 size={19} strokeWidth={1.8} />
        </motion.button>
      </section>

      <motion.section
        className="profile-hero"
        initial={{ opacity: 0, scale: 0.98 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ delay: 0.05, ...pageTransition }}
      >
        <div className="head-stage">
          <div className="head-shadow" />
          <CristalixHead skinUrl={MOCK_SKIN_URL} size={118} />
        </div>

        <div className="profile-account-copy">
          <span className="linked-account-label">
            <Link2 size={13} strokeWidth={1.8} />
            Cristalix
          </span>
          <h1>knalis</h1>
          <span className="account-id">46d7a80e · active</span>
        </div>
      </motion.section>

      <section className="scope-section">
        <div className="scope-heading">
          <CircleUserRound size={16} strokeWidth={1.7} />
          <span>Юзер</span>
        </div>

        <div className="settings-list">
          <motion.button className="settings-row" whileTap={{ scale: 0.99 }}>
            <div>
              <strong>Notification pack</strong>
              <span>Default</span>
            </div>
            <ChevronRight size={17} strokeWidth={1.7} />
          </motion.button>

          <motion.button className="settings-row" whileTap={{ scale: 0.99 }}>
            <div>
              <strong>Персоналізація</strong>
              <span>2 активні</span>
            </div>
            <ChevronRight size={17} strokeWidth={1.7} />
          </motion.button>
        </div>
      </section>
    </PageFrame>
  )
}

function BottomNavigation({ page, onChange }: { page: Page; onChange: (page: Page) => void }) {
  const items: Array<{ id: Page; label: string; icon: typeof Home }> = [
    { id: "home", label: "Головна", icon: Home },
    { id: "events", label: "Події", icon: CalendarDays },
    { id: "profile", label: "Профіль", icon: CircleUserRound },
  ]

  return (
    <LayoutGroup>
      <nav className="bottom-navigation" aria-label="Основна навігація">
        {items.map((item) => {
          const Icon = item.icon
          const active = item.id === page

          return (
            <motion.button
              key={item.id}
              className={`nav-button ${active ? "nav-button--active" : ""}`}
              onClick={() => onChange(item.id)}
              whileTap={{ scale: 0.9 }}
              aria-label={item.label}
              aria-current={active ? "page" : undefined}
            >
              {active && (
                <motion.span
                  className="nav-active-surface"
                  layoutId="active-navigation"
                  transition={{ type: "spring", stiffness: 420, damping: 34 }}
                />
              )}
              <Icon size={20} strokeWidth={active ? 2 : 1.7} />
            </motion.button>
          )
        })}
      </nav>
    </LayoutGroup>
  )
}

export default function App() {
  const [page, setPage] = useState<Page>("home")

  return (
    <div className="preview-stage">
      <main className="app-shell">
        <header className="topbar">
          <motion.button className="account-switcher" whileTap={{ scale: 0.97 }}>
            <AccountMark />
            <span>knalis</span>
            <ChevronDown size={15} strokeWidth={1.8} />
          </motion.button>

          <span className="scope-divider" />

          <motion.button className="global-user-button" whileTap={{ scale: 0.92 }} aria-label="Юзер">
            K
          </motion.button>
        </header>

        <div className="screen-content">
          <AnimatePresence mode="wait" initial={false}>
            {page === "home" && <HomePage key="home" />}
            {page === "events" && <EventsPage key="events" />}
            {page === "profile" && <ProfilePage key="profile" />}
          </AnimatePresence>
        </div>

        <BottomNavigation page={page} onChange={setPage} />
      </main>
    </div>
  )
}
