import { AnimatePresence, MotionConfig, motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import { accounts as initialAccounts, currentUser } from "./mockData"
import type { Account, RootRoute, Route } from "./model"
import { featureArea, initialStack, isRootRoute } from "./navigation/routeRegistry"
import { AccountFeature } from "./features/AccountFeature"
import { AdministrationHome } from "./features/AdministrationHome"
import { CreatorFeature } from "./features/CreatorFeature"
import { EventsArea } from "./features/EventsArea"
import { PeopleFeature } from "./features/PeopleFeature"
import { PersonalizationFeature } from "./features/PersonalizationFeature"
import { StaffFeature } from "./features/StaffFeature"
import { CurrencyMark, Glyph } from "./shared/MnemosIcon"

const rootItems: { id: RootRoute; icon: "bet" | "events" | "home" | "search" | "store"; label: string }[] = [
  { id: "bets", icon: "bet", label: "Ставки" },
  { id: "events", icon: "events", label: "Події" },
  { id: "account", icon: "home", label: "Головна" },
  { id: "search", icon: "search", label: "Пошук" },
  { id: "personalization-store", icon: "store", label: "Магазин" },
]

function routeKey(route: Route) {
  return `${route.id}:${JSON.stringify(route.params ?? {})}`
}

export default function App() {
  const [stack, setStack] = useState<Route[]>(initialStack)
  const [accounts, setAccounts] = useState<Account[]>(initialAccounts)
  const [activeAccount, setActiveAccount] = useState<Account>(initialAccounts[0])
  const [userHubOpen, setUserHubOpen] = useState(() => new URLSearchParams(window.location.search).get("hub") === "1")
  const [accountPickerOpen, setAccountPickerOpen] = useState(() => new URLSearchParams(window.location.search).get("accounts") === "1")
  const routeStageRef = useRef<HTMLDivElement>(null)
  const scrollPositions = useRef<Record<string, number>>({})

  const current = stack[stack.length - 1]
  const area = featureArea(current.id)
  const rootRoute = isRootRoute(current.id) ? current.id : null

  const rememberScroll = () => {
    scrollPositions.current[routeKey(current)] = routeStageRef.current?.scrollTop ?? 0
  }

  const navigate = (route: Route) => {
    rememberScroll()
    setUserHubOpen(false)
    setAccountPickerOpen(false)

    if (isRootRoute(route.id)) {
      setStack([route])
      return
    }

    setStack((value) => [...value, route])
  }

  const back = () => {
    rememberScroll()
    setStack((value) => value.length > 1 ? value.slice(0, -1) : value)
  }

  const switchRoot = (route: RootRoute) => {
    rememberScroll()
    setUserHubOpen(false)
    setAccountPickerOpen(false)
    setStack([{ id: route }])
  }

  useEffect(() => {
    const element = routeStageRef.current
    if (!element) return

    const frame = window.requestAnimationFrame(() => {
      element.scrollTo({ top: scrollPositions.current[routeKey(current)] ?? 0, behavior: "auto" })
    })

    return () => window.cancelAnimationFrame(frame)
  }, [current])

  const selectAccount = (account: Account) => {
    if (account.active === false) {
      if (!account.owner) return

      const reactivated = { ...account, active: true }
      setAccounts(accounts.map((item) => item.id === account.id ? reactivated : item))
      setActiveAccount(reactivated)
    } else {
      setActiveAccount(account)
    }

    setAccountPickerOpen(false)
  }

  const content = area === "events"
    ? <EventsArea routeId={current.id} params={current.params} activeAccount={activeAccount} navigate={navigate} back={back} />
    : area === "account"
      ? <AccountFeature routeId={current.id} params={current.params} activeAccount={activeAccount} setActiveAccount={setActiveAccount} accounts={accounts} setAccounts={setAccounts} navigate={navigate} back={back} />
      : area === "people"
        ? <PeopleFeature routeId={current.id} params={current.params} activeAccount={activeAccount} navigate={navigate} back={back} />
        : area === "personalization"
          ? <PersonalizationFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />
          : area === "creator"
            ? <CreatorFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />
            : current.id === "administration"
              ? <AdministrationHome navigate={navigate} back={back} />
              : <StaffFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />

  return (
    <MotionConfig transition={{ duration: 0.2, ease: [0.22, 1, 0.36, 1] }}>
      <main className="preview-stage redesign-stage">
        <section className="app-shell redesign-shell">
          <motion.div
            ref={routeStageRef}
            className={`route-stage ${rootRoute ? "has-tabbar has-user-access" : ""}`}
            initial={false}
            animate={{ opacity: 1 }}
          >
            {content}
          </motion.div>

          {rootRoute ? (
            <>
              <UserAccess
                open={userHubOpen}
                accountPickerOpen={accountPickerOpen}
                activeAccount={activeAccount}
                accounts={accounts}
                onToggle={() => {
                  setUserHubOpen((value) => !value)
                  setAccountPickerOpen(false)
                }}
                onClose={() => {
                  setUserHubOpen(false)
                  setAccountPickerOpen(false)
                }}
                onToggleAccounts={() => setAccountPickerOpen((value) => !value)}
                onSelectAccount={selectAccount}
                navigate={navigate}
              />
              <RootNavigation current={rootRoute} onChange={switchRoot} />
            </>
          ) : null}
        </section>
      </main>
    </MotionConfig>
  )
}

function RootNavigation({ current, onChange }: { current: RootRoute; onChange: (route: RootRoute) => void }) {
  return (
    <nav className="root-navigation root-navigation--five" aria-label="Основна навігація">
      {rootItems.map((item) => (
        <button key={item.id} className={current === item.id ? "is-active" : ""} onClick={() => onChange(item.id)}>
          <span className="root-navigation__icon"><Glyph name={item.icon} size={20} /></span>
          <span>{item.label}</span>
        </button>
      ))}
    </nav>
  )
}

type UserAccessProps = {
  open: boolean
  accountPickerOpen: boolean
  activeAccount: Account
  accounts: Account[]
  onToggle: () => void
  onClose: () => void
  onToggleAccounts: () => void
  onSelectAccount: (account: Account) => void
  navigate: (route: Route) => void
}

function UserAccess({ open, accountPickerOpen, activeAccount, accounts, onToggle, onClose, onToggleAccounts, onSelectAccount, navigate }: UserAccessProps) {
  const creator = currentUser.systemRoles.some((role) => ["PERSONALIZATION_CREATOR", "ADMIN", "SUPER_ADMIN"].includes(role))
  const moderation = currentUser.systemRoles.some((role) => ["MODERATOR", "ADMIN", "SUPER_ADMIN", "PERSONALIZATION_MODERATOR"].includes(role))
  const administration = currentUser.systemRoles.some((role) => ["ADMIN", "SUPER_ADMIN"].includes(role))

  return (
    <>
      <button className={`global-user-button ${open ? "is-open" : ""}`} onClick={onToggle} aria-label="Меню користувача">
        <span>K</span>
        <i />
      </button>

      <AnimatePresence>
        {open ? (
          <>
            <motion.button
              className="user-hub-backdrop"
              aria-label="Закрити меню"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={onClose}
            />
            <motion.aside
              className="user-hub"
              initial={{ opacity: 0, y: -8, scale: 0.98 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: -6, scale: 0.985 }}
            >
              <header className="user-hub__identity">
                <span className="user-hub__avatar">K</span>
                <span>
                  <strong>@{currentUser.username}</strong>
                  <small>Користувач Mnemos</small>
                </span>
                <button className="user-prime" onClick={() => navigate({ id: "user-wallet" })}>
                  <CurrencyMark currency="PRIME" size={15} />
                  <b>{currentUser.prime}</b>
                </button>
              </header>

              <button className="user-hub__account" onClick={onToggleAccounts}>
                <span>
                  <small>Активний аккаунт</small>
                  <strong>{activeAccount.nickname}</strong>
                </span>
                <Glyph name={accountPickerOpen ? "close" : "swap"} size={17} />
              </button>

              <AnimatePresence initial={false}>
                {accountPickerOpen ? (
                  <motion.div
                    className="user-hub__accounts"
                    initial={{ opacity: 0, height: 0 }}
                    animate={{ opacity: 1, height: "auto" }}
                    exit={{ opacity: 0, height: 0 }}
                  >
                    {accounts.map((account) => (
                      <button
                        key={account.id}
                        className={account.id === activeAccount.id ? "is-active" : ""}
                        aria-disabled={account.active === false && !account.owner}
                        onClick={() => onSelectAccount(account)}
                      >
                        <span>{account.nickname.slice(0, 1).toUpperCase()}</span>
                        <b>{account.nickname}</b>
                        <small>{account.active === false ? account.owner ? "активувати" : "неактивний" : account.role}</small>
                        {account.id === activeAccount.id ? <Glyph name="check" size={15} /> : null}
                      </button>
                    ))}
                    <div className="user-hub__account-actions">
                      <button onClick={() => navigate({ id: "account-create" })}><Glyph name="plus" size={15} />Створити</button>
                      <button onClick={() => navigate({ id: "account-join" })}><Glyph name="invite" size={15} />За кодом</button>
                    </div>
                  </motion.div>
                ) : null}
              </AnimatePresence>

              <nav className="user-hub__workspaces">
                {creator ? <button onClick={() => navigate({ id: "creator-studio" })}><Glyph name="creator" size={18} /><span><strong>Creator Studio</strong><small>Персоналізації та дохід</small></span><b>›</b></button> : null}
                {moderation ? <button onClick={() => navigate({ id: "moderation" })}><Glyph name="moderation" size={18} /><span><strong>Модерація</strong><small>Санкції та перевірки</small></span><b>›</b></button> : null}
                {administration ? <button onClick={() => navigate({ id: "administration" })}><Glyph name="admin" size={18} /><span><strong>Адміністрування</strong><small>Системні інструменти</small></span><b>›</b></button> : null}
              </nav>
            </motion.aside>
          </>
        ) : null}
      </AnimatePresence>
    </>
  )
}
