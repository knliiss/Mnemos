import { AnimatePresence, MotionConfig, motion } from "framer-motion"
import { ChevronDown } from "lucide-react"
import { useEffect, useMemo, useState } from "react"
import { AccountMark } from "./components/AccountMark"
import { AccountSwitcherSheet } from "./components/AccountSwitcherSheet"
import { BottomNavigation } from "./components/BottomNavigation"
import { Toast } from "./components/Toast"
import { mockAccounts } from "./mockData"
import { BetDetailPage } from "./pages/BetDetailPage"
import { BetsPage } from "./pages/BetsPage"
import { EventDetailPage } from "./pages/EventDetailPage"
import { EventsPage } from "./pages/EventsPage"
import { HomePage } from "./pages/HomePage"
import { NotificationsPage } from "./pages/NotificationsPage"
import { PersonalizationPage } from "./pages/PersonalizationPage"
import { ProfilePage } from "./pages/ProfilePage"
import { RewardsPage } from "./pages/RewardsPage"
import { ServicesPage } from "./pages/ServicesPage"
import { SettingsPage } from "./pages/SettingsPage"
import { SocialPage } from "./pages/SocialPage"
import { SubscriptionsPage } from "./pages/SubscriptionsPage"
import { WalletPage } from "./pages/WalletPage"
import type { Page, RootPage } from "./types"

const rootPages: RootPage[] = ["home", "events", "services", "profile"]

function isRootPage(page: Page): page is RootPage {
  return rootPages.includes(page as RootPage)
}

export default function App() {
  const [navigationStack, setNavigationStack] = useState<Page[]>(["home"])
  const [activeAccountId, setActiveAccountId] = useState(mockAccounts[0].id)
  const [accountSheetOpen, setAccountSheetOpen] = useState(false)
  const [toast, setToast] = useState<string | null>(null)

  const page = navigationStack[navigationStack.length - 1]
  const rootPage = navigationStack.find(isRootPage) ?? "home"

  const activeAccount = useMemo(
    () => mockAccounts.find((account) => account.id === activeAccountId) ?? mockAccounts[0],
    [activeAccountId],
  )

  useEffect(() => {
    if (!toast) {
      return
    }

    const timeout = window.setTimeout(() => setToast(null), 2200)
    return () => window.clearTimeout(timeout)
  }, [toast])

  const notify = (message: string) => {
    setToast(message)
  }

  const setRootPage = (nextPage: RootPage) => {
    setNavigationStack([nextPage])
  }

  const navigate = (nextPage: Page) => {
    if (isRootPage(nextPage)) {
      setRootPage(nextPage)
      return
    }

    setNavigationStack((current) => [...current, nextPage])
  }

  const goBack = () => {
    setNavigationStack((current) => current.length > 1 ? current.slice(0, -1) : current)
  }

  const selectAccount = (accountId: string) => {
    setActiveAccountId(accountId)
    setAccountSheetOpen(false)
    notify("Активний аккаунт змінено")
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
              className={`global-user-button ${rootPage === "profile" ? "global-user-button--active" : ""}`}
              onClick={() => setRootPage("profile")}
              whileTap={{ scale: 0.92 }}
              aria-label="Профіль юзера"
            >
              K
            </motion.button>
          </header>

          <div className="screen-content">
            <AnimatePresence mode="wait" initial={false}>
              {page === "home" && <HomePage key="home" activeAccount={activeAccount} onNavigate={navigate} />}
              {page === "events" && <EventsPage key="events" onNavigate={navigate} />}
              {page === "services" && <ServicesPage key="services" activeAccount={activeAccount} onNavigate={navigate} />}
              {page === "profile" && <ProfilePage key="profile" activeAccount={activeAccount} onNavigate={navigate} />}
              {page === "event-detail" && <EventDetailPage key="event-detail" onBack={goBack} onNotify={notify} />}
              {page === "rewards" && <RewardsPage key="rewards" activeAccount={activeAccount} onBack={goBack} onNotify={notify} />}
              {page === "bets" && <BetsPage key="bets" activeAccount={activeAccount} onBack={goBack} onNavigate={navigate} />}
              {page === "bet-detail" && <BetDetailPage key="bet-detail" activeAccount={activeAccount} onBack={goBack} onNotify={notify} />}
              {page === "wallet" && <WalletPage key="wallet" activeAccount={activeAccount} onBack={goBack} />}
              {page === "personalization" && <PersonalizationPage key="personalization" onBack={goBack} onNotify={notify} />}
              {page === "subscriptions" && <SubscriptionsPage key="subscriptions" onBack={goBack} onNotify={notify} />}
              {page === "notifications" && <NotificationsPage key="notifications" onBack={goBack} />}
              {page === "social" && <SocialPage key="social" activeAccount={activeAccount} onBack={goBack} onNotify={notify} />}
              {page === "settings" && <SettingsPage key="settings" onBack={goBack} onNotify={notify} />}
            </AnimatePresence>
          </div>

          {isRootPage(page) && <BottomNavigation page={page} onChange={setRootPage} />}

          <AccountSwitcherSheet
            accounts={mockAccounts}
            activeAccountId={activeAccountId}
            open={accountSheetOpen}
            onClose={() => setAccountSheetOpen(false)}
            onSelect={selectAccount}
          />

          <Toast message={toast} />
        </main>
      </div>
    </MotionConfig>
  )
}
