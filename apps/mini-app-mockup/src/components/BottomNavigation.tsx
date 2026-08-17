import { LayoutGroup, motion } from "framer-motion"
import { CalendarDays, CircleUserRound, Home } from "lucide-react"
import type { Page } from "../types"

type BottomNavigationProps = {
  page: Page
  onChange: (page: Page) => void
}

const items: Array<{
  id: Page
  label: string
  icon: typeof Home
}> = [
  { id: "home", label: "Головна", icon: Home },
  { id: "events", label: "Події", icon: CalendarDays },
  { id: "profile", label: "Профіль", icon: CircleUserRound },
]

export function BottomNavigation({ page, onChange }: BottomNavigationProps) {
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
                  transition={{ type: "spring", stiffness: 430, damping: 36, mass: 0.72 }}
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
