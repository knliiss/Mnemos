import { AnimatePresence, MotionConfig, motion } from "framer-motion"
import { ChevronDown } from "lucide-react"
import { useMemo, useRef, useState } from "react"
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

const rootPages = new Set<Page>(["home", "events", "services", "profile"])

export default function App() {
  const [navigation, setNavigation] = useState<Page[]>(["home"])
  const [activeAccountId, setActiveAccountId] = useState(mockAccounts[0].id)
  const [accountSheetOpen, setAccountSheetOpen] = useState(false)
  const [toast, setToast] = useState<string | null>(null)
  const toastTimerRef = useRef<number | null>(null)

  const page = navigation[navigation.length - 1]

  const activeAccount = useMemo(
    () => mockAccounts.find((account) => account.id === activeAccountId) ?? mockAccounts[0],
    [activeAccountId],
  )

  const activeRoot = useMemo<RootPage>(() => {
    for (let index = navigation.length - 1; index >= 0; index -= 1) {
      const candidate = navigation[index]
      if (rootPages.has(candidate)) {
        return candidate as RootPage
      }
    }

    return "home"
  }, [navigation])

  const notify = (message: string) => {
    setToast(message)

    if (toastTimerRef.current !== null) {
      window.clearTimeout(toastTimerRef.current)
    }

    toastTimerRef.current = window.setTimeout(() => setToast(null), 2200)
  }

  const navigate = (nextPage: Page) => {
    if (rootPages.has(nextPage)) {
      setNavigation([nextPage])
      return
    }

    setNavigation((current) => [...current, nextPage])
  }

  const navigateRoot = (nextPage: RootPage) => {
    setNavigation([nextPage])
  }

  const goBack = () => {
    setNavigation((current) => current.length > 1 ? current.slice(0, -1) : ["home"])
  }

  const selectAccount = (accountId: string) => {
    setActiveAccountId(accountId)
    setAccountSheetOpen(false)
    notify("Активний аккаунт змінено")
  }

  const rootPageVisible = rootPages.has(page)

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
              className={`global-user-button ${activeRoot === "profile" ? "global-user-button--active" : ""}`}
              onClick={() => navigateRoot("profile")}
              whileTap={{ scale: 0.92 }}
              aria-label="Профіль юзера"
            >
              K
            </motion.button>
          </header>

          <div className="screen-content">
            <AnimatePresence mode="wait" initial={false}>
              {page === "home" && (
                <HomePage
                  key="home"
                  activeAccount={activeAccount}
                  onNavigate={navigate}
                />
              )}
              {page === "events" && (
                <EventsPage key="events" onNavigate={navigate} />
              )}
              {page === "services" && (
                <ServicesPage
                  key="services"
                  activeAccount={activeAccount}
                  onNavigate={navigate}
                />
              )}
              {page === "profile" && (
                <ProfilePage
                  key="profile"
                  activeAccount={activeAccount}
                  onNavigate={navigate}
                />
              )}
              {page === "event-detail" && (
                <EventDetailPage key="event-detail" onBack={goBack} onNotify={notify} />
              )}
              {page === "rewards" && (
                <RewardsPage
                  key="rewards"
                  activeAccount={activeAccount}
                  onBack={goBack}
                  onNotify={notify}
                />
              )}
              {page === "bets" && (
                <BetsPage
                  key="bets"
                  activeAccount={activeAccount}
                  onBack={goBack}
                  onNavigate={navigate}
                />
              )}
              {page === "bet-detail" && (
                <BetDetailPage
                  key="bet-detail"
                  activeAccount={activeAccount}
                  onBack={goBack}
                  onNotify={notify}
                />
              )}
              {page === "wallet" && (
                <WalletPage key="wallet" activeAccount={activeAccount} onBack={goBack} />
              )}
              {page === "personalization" && (
                <PersonalizationPage
                  key="personalization"
                  onBack={goBack}
                  onNotify={notify}
                />
              )}
              {page === "subscriptions" && (
                <SubscriptionsPage
                  key="subscriptions"
                  onBack={goBack}
                  onNotify={notify}
                />
              )}
              {page === "notifications" && (
                <NotificationsPage key="notifications" onBack={goBack} />
              )}
              {page === "social" && (
                <SocialPage
                  key="social"
                  activeAccount={activeAccount}
                  onBack={goBack}
                  onNotify={notify}
                />
              )}
              {page === "settings" && (
                <SettingsPage key="settings" onBack={goBack} />
              )}
            </AnimatePresence>
          </div>

          <AnimatePresence initial={false}>
            {rootPageVisible && (
              <motion.div
                className="bottom-navigation-slot"
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 10 }}
                transition={{ duration: 0.18 }}
              >
                <BottomNavigation page={activeRoot} onChange={navigateRoot} />
              </motion.div>
            )}
          </AnimatePresence>

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
