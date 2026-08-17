import { LayoutGroup, motion } from "framer-motion"
import { CalendarDays, Grid2X2, Home, UserRound } from "lucide-react"
import type { RootPage } from "../types"

const items = [
  { id: "home", label: "Головна", icon: Home },
  { id: "events", label: "Події", icon: CalendarDays },
  { id: "services", label: "Сервіси", icon: Grid2X2 },
  { id: "profile", label: "Профіль", icon: UserRound },
] as const

export function BottomNavigation({
  page,
  onChange,
}: {
  page: RootPage
  onChange: (page: RootPage) => void
}) {
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
                  transition={{ type: "spring", stiffness: 430, damping: 34 }}
                />
              )}
              <Icon size={20} strokeWidth={active ? 2 : 1.65} />
            </motion.button>
          )
        })}
      </nav>
    </LayoutGroup>
  )
}
