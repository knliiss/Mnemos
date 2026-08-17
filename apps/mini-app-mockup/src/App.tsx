import { AnimatePresence, MotionConfig, motion } from "framer-motion"
import { ChevronDown } from "lucide-react"
import { useMemo, useState } from "react"
import { AccountMark } from "./components/AccountMark"
import { AccountSwitcherSheet } from "./components/AccountSwitcherSheet"
import { BottomNavigation } from "./components/BottomNavigation"
import { mockAccounts } from "./mockData"
import { EventsPage } from "./pages/EventsPage"
import { HomePage } from "./pages/HomePage"
import { ProfilePage } from "./pages/ProfilePage"
import type { Page } from "./types"

export default function App() {
  const [page, setPage] = useState<Page>("home")
  const [activeAccountId, setActiveAccountId] = useState(mockAccounts[0].id)
  const [accountSheetOpen, setAccountSheetOpen] = useState(false)

  const activeAccount = useMemo(
    () => mockAccounts.find((account) => account.id === activeAccountId) ?? mockAccounts[0],
    [activeAccountId],
  )

  const selectAccount = (accountId: string) => {
    setActiveAccountId(accountId)
    setAccountSheetOpen(false)
  }

  return (
    <MotionConfig reducedMotion="user">
      <div className="preview-stage">
        <main className="app-shell">
          <header className="topbar">
            <motion.button
              className="account-switcher"
              onClick={() => setAccountSheetOpen(true)}
              whileTap={{ scale: 0.97 }}
              aria-label={`Активний аккаунт: ${activeAccount.nickname}`}
            >
              <AccountMark compact />
              <span>{activeAccount.nickname}</span>
              <ChevronDown size={15} strokeWidth={1.8} />
            </motion.button>

            <span className="scope-divider" aria-hidden="true" />

            <motion.button
              className={`global-user-button ${page === "profile" ? "global-user-button--active" : ""}`}
              onClick={() => setPage("profile")}
              whileTap={{ scale: 0.92 }}
              aria-label="Профіль юзера"
            >
              K
            </motion.button>
          </header>

          <div className="screen-content">
            <AnimatePresence mode="wait" initial={false}>
              {page === "home" && <HomePage key="home" />}
              {page === "events" && <EventsPage key="events" />}
              {page === "profile" && (
                <ProfilePage key="profile" activeAccount={activeAccount} />
              )}
            </AnimatePresence>
          </div>

          <BottomNavigation page={page} onChange={setPage} />

          <AccountSwitcherSheet
            accounts={mockAccounts}
            activeAccountId={activeAccountId}
            open={accountSheetOpen}
            onClose={() => setAccountSheetOpen(false)}
            onSelect={selectAccount}
          />
        </main>
      </div>
    </MotionConfig>
  )
}
