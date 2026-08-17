import { motion } from "framer-motion"
import { ChevronLeft } from "lucide-react"
import type { ReactNode } from "react"

export function PageHeader({
  title,
  eyebrow,
  onBack,
  action,
}: {
  title: string
  eyebrow?: string
  onBack: () => void
  action?: ReactNode
}) {
  return (
    <header className="detail-header">
      <motion.button
        className="icon-button icon-button--quiet"
        onClick={onBack}
        whileTap={{ scale: 0.9 }}
        aria-label="Назад"
      >
        <ChevronLeft size={20} strokeWidth={1.8} />
      </motion.button>

      <div className="detail-header-copy">
        {eyebrow && <span className="eyebrow">{eyebrow}</span>}
        <h1>{title}</h1>
      </div>

      <div className="detail-header-action">{action}</div>
    </header>
  )
}
