import { motion } from "framer-motion"
import type { ReactNode } from "react"
import type { Page } from "../types"

type PageFrameProps = {
  page: Page
  children: ReactNode
}

export function PageFrame({ page, children }: PageFrameProps) {
  return (
    <motion.div
      key={page}
      className="page"
      initial={{ opacity: 0, y: 9 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -5 }}
      transition={{ duration: 0.24, ease: [0.22, 1, 0.36, 1] }}
    >
      {children}
    </motion.div>
  )
}
