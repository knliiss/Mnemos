import { AnimatePresence, motion } from "framer-motion"
import { Check } from "lucide-react"

export function Toast({ message }: { message: string | null }) {
  return (
    <AnimatePresence>
      {message && (
        <motion.div
          className="toast"
          initial={{ opacity: 0, y: 18, scale: 0.97 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{ opacity: 0, y: 12, scale: 0.98 }}
          transition={{ duration: 0.2, ease: [0.22, 1, 0.36, 1] }}
          role="status"
        >
          <span className="toast-icon">
            <Check size={14} strokeWidth={2} />
          </span>
          <span>{message}</span>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
