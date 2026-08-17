import { motion } from "framer-motion"
import { ChevronRight } from "lucide-react"
import type { ReactNode } from "react"

export function NavRow({
  icon,
  title,
  subtitle,
  value,
  onClick,
}: {
  icon: ReactNode
  title: string
  subtitle?: string
  value?: ReactNode
  onClick: () => void
}) {
  return (
    <motion.button className="nav-row" onClick={onClick} whileTap={{ scale: 0.992 }}>
      <span className="nav-row-icon">{icon}</span>

      <span className="nav-row-copy">
        <strong>{title}</strong>
        {subtitle && <small>{subtitle}</small>}
      </span>

      {value && <span className="nav-row-value">{value}</span>}
      <ChevronRight size={17} strokeWidth={1.7} />
    </motion.button>
  )
}
