import { AnimatePresence, motion } from "framer-motion"
import { Check, X } from "lucide-react"
import { AccountMark } from "./AccountMark"
import type { MockAccount } from "../types"

type AccountSwitcherSheetProps = {
  accounts: MockAccount[]
  activeAccountId: string
  open: boolean
  onClose: () => void
  onSelect: (accountId: string) => void
}

export function AccountSwitcherSheet({
  accounts,
  activeAccountId,
  open,
  onClose,
  onSelect,
}: AccountSwitcherSheetProps) {
  return (
    <AnimatePresence>
      {open && (
        <motion.div
          className="sheet-layer"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.18 }}
        >
          <motion.button
            className="sheet-backdrop"
            onClick={onClose}
            aria-label="Закрити вибір аккаунта"
          />

          <motion.section
            className="account-sheet"
            initial={{ y: "105%" }}
            animate={{ y: 0 }}
            exit={{ y: "105%" }}
            transition={{ type: "spring", stiffness: 360, damping: 36, mass: 0.82 }}
            aria-label="Вибір аккаунта"
          >
            <div className="sheet-handle" aria-hidden="true" />

            <div className="sheet-title-row">
              <div>
                <span className="eyebrow">CONTEXT</span>
                <h2>Аккаунт</h2>
              </div>

              <motion.button
                className="icon-button icon-button--quiet"
                onClick={onClose}
                whileTap={{ scale: 0.9 }}
                aria-label="Закрити"
              >
                <X size={18} strokeWidth={1.7} />
              </motion.button>
            </div>

            <div className="account-list">
              {accounts.map((account) => {
                const active = account.id === activeAccountId

                return (
                  <motion.button
                    key={account.id}
                    className={`account-option ${active ? "account-option--active" : ""}`}
                    onClick={() => onSelect(account.id)}
                    whileTap={{ scale: 0.985 }}
                  >
                    <AccountMark />

                    <div className="account-option-copy">
                      <strong>{account.nickname}</strong>
                      <span>{account.shortId}</span>
                    </div>

                    {active && (
                      <motion.span
                        className="account-option-check"
                        initial={{ scale: 0.75, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                      >
                        <Check size={15} strokeWidth={2} />
                      </motion.span>
                    )}
                  </motion.button>
                )
              })}
            </div>
          </motion.section>
        </motion.div>
      )}
    </AnimatePresence>
  )
}
