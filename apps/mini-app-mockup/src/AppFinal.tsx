import { AnimatePresence, MotionConfig, motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import { CristalixHead } from "./components/CristalixHead"
import { accounts as initialAccounts, currentUser } from "./mockData"
import type { Account, RootRoute, Route } from "./model"
import { featureArea, initialStack, isRootRoute } from "./navigation/routeRegistry"
import { AccountAreaFinal } from "./features/AccountAreaFinal"
import { AdministrationHome } from "./features/AdministrationHome"
import { CreatorFeature } from "./features/CreatorFeature"
import { EventsArea } from "./features/EventsArea"
import { ModerationHomePolished } from "./features/ModerationHomePolished"
import { PeopleFeatureFinal } from "./features/PeopleFeatureFinal"
import { PersonalizationFeatureFinal } from "./features/PersonalizationFeatureFinal"
import { StaffAreaOverhaul } from "./features/StaffAreaOverhaul"
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

export default function AppFinal() {
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

  const toggleUserHub = () => {
    setUserHubOpen((value) => !value)
    setAccountPickerOpen(false)
  }

  const closeUserHub = () => {
    setUserHubOpen(false)
    setAccountPickerOpen(false)
  }

  const content = area === "events"
    ? <EventsArea routeId={current.id} params={current.params} activeAccount={activeAccount} navigate={navigate} back={back} />
    : area === "account"
      ? (
          <AccountAreaFinal
            routeId={current.id}
            params={current.params}
            activeAccount={activeAccount}
            setActiveAccount={setActiveAccount}
            accounts={accounts}
            setAccounts={setAccounts}
            navigate={navigate}
            back={back}
          />
        )
      : area === "people"
        ? <PeopleFeatureFinal routeId={current.id} params={current.params} activeAccount={activeAccount} navigate={navigate} back={back} />
        : area === "personalization"
          ? <PersonalizationFeatureFinal routeId={current.id} params={current.params} navigate={navigate} back={back} />
          : area === "creator"
            ? <CreatorFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />
            : current.id === "moderation"
              ? <ModerationHomePolished navigate={navigate} back={back} />
              : current.id === "administration"
                ? <AdministrationHome navigate={navigate} back={back} />
                : <StaffAreaOverhaul routeId={current.id} params={current.params} navigate={navigate} back={back} />

  return (
    <MotionConfig transition={{ duration: 0.19, ease: [0.22, 1, 0.36, 1] }}>
      <main className="preview-stage redesign-stage final-stage">
        <section className="app-shell redesign-shell final-shell">
          <motion.div
            ref={routeStageRef}
            className={`route-stage ${rootRoute ? `has-tabbar has-user-access route-stage--${rootRoute}` : ""}`}
            initial={false}
            animate={{ opacity: 1 }}
          >
            {rootRoute ? (
              <UserHubTrigger
                open={userHubOpen}
                onToggle={toggleUserHub}
              />
            ) : null}
            {content}
          </motion.div>

          {rootRoute ? (
            <>
              <UserHubOverlay
                open={userHubOpen}
                accountPickerOpen={accountPickerOpen}
                activeAccount={activeAccount}
                accounts={accounts}
                onClose={closeUserHub}
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
    <nav className="root-navigation root-navigation--five final-navigation" aria-label="Основна навігація">
      {rootItems.map((item) => (
        <button key={item.id} className={current === item.id ? "is-active" : ""} onClick={() => onChange(item.id)}>
          <span className="root-navigation__icon"><Glyph name={item.icon} size={20} /></span>
          <span>{item.label}</span>
        </button>
      ))}
    </nav>
  )
}

function UserHubTrigger({ open, onToggle }: { open: boolean; onToggle: () => void }) {
  return (
    <motion.button
      className={`global-user-button global-user-button--final ${open ? "is-open" : ""}`}
      whileTap={{ scale: 0.95 }}
      onClick={onToggle}
      aria-label="Меню користувача"
    >
      <UserAvatarPlaceholder
        className="global-user-button__avatar"
        size={34}
      />
      <i />
    </motion.button>
  )
}

function UserAvatarPlaceholder({ className, size }: { className?: string; size: number }) {
  return (
    <span
      className={`user-avatar-placeholder ${className ?? ""}`.trim()}
      style={{ width: size, height: size }}
      aria-hidden="true"
    >
      <Glyph name="profile-mark" size={Math.round(size * 0.48)} />
    </span>
  )
}

type UserHubOverlayProps = {
  open: boolean
  accountPickerOpen: boolean
  activeAccount: Account
  accounts: Account[]
  onClose: () => void
  onToggleAccounts: () => void
  onSelectAccount: (account: Account) => void
  navigate: (route: Route) => void
}

function UserHubOverlay({ open, accountPickerOpen, activeAccount, accounts, onClose, onToggleAccounts, onSelectAccount, navigate }: UserHubOverlayProps) {
  const creator = currentUser.systemRoles.some((role) => ["PERSONALIZATION_CREATOR", "ADMIN", "SUPER_ADMIN"].includes(role))
  const moderation = currentUser.systemRoles.some((role) => ["MODERATOR", "ADMIN", "SUPER_ADMIN"].includes(role))
  const administration = currentUser.systemRoles.some((role) => ["ADMIN", "SUPER_ADMIN"].includes(role))
  const personalizationReviewer = currentUser.systemRoles.includes("PERSONALIZATION_MODERATOR")
  const reviewerOnly = personalizationReviewer && !administration

  return (
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
            className="user-hub user-hub--final"
            initial={{ opacity: 0, y: -10, scale: 0.975 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -7, scale: 0.985 }}
          >
            <header className="final-user-card">
              <UserAvatarPlaceholder
                className="final-user-card__avatar final-user-card__avatar--placeholder"
                size={40}
              />
              <span className="final-user-card__copy">
                <small>MNEMOS USER</small>
                <strong>@{currentUser.username}</strong>
              </span>
              <button className="final-user-prime" onClick={() => navigate({ id: "user-wallet" })}>
                <CurrencyMark currency="PRIME" size={16} />
                <strong>{currentUser.prime}</strong>
                <b>›</b>
              </button>
            </header>

            <button className={`final-account-switch ${accountPickerOpen ? "is-open" : ""}`} onClick={onToggleAccounts}>
              <CristalixHead
                className="final-account-switch__mark final-account-switch__mark--skin"
                skinUrl={activeAccount.skinUrl}
                size={34}
              />
              <span>
                <small>Активний аккаунт</small>
                <strong>{activeAccount.nickname}</strong>
              </span>
              <span className="final-account-switch__action"><Glyph name="swap" size={16} />Змінити</span>
            </button>

            <AnimatePresence initial={false}>
              {accountPickerOpen ? (
                <motion.div
                  className="final-account-picker"
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
                      <CristalixHead
                        className="final-account-picker__head"
                        skinUrl={account.skinUrl}
                        size={30}
                      />
                      <strong>{account.nickname}</strong>
                      <small>{account.active === false ? account.owner ? "активувати" : "неактивний" : account.role}</small>
                      {account.id === activeAccount.id ? <Glyph name="check" size={14} /> : null}
                    </button>
                  ))}
                  <footer>
                    <button onClick={() => navigate({ id: "account-create" })}><Glyph name="plus" size={15} />Створити</button>
                    <button onClick={() => navigate({ id: "account-join" })}><Glyph name="invite" size={15} />За кодом</button>
                  </footer>
                </motion.div>
              ) : null}
            </AnimatePresence>

            <div className="final-workspace-label">Робочі області</div>
            <nav className="final-workspace-grid">
              {creator ? (
                <button className="is-creator" onClick={() => navigate({ id: "creator-studio" })}>
                  <span><Glyph name="creator" size={20} /></span>
                  <strong>Creator Studio</strong>
                  <small>Створення персоналізацій</small>
                  <b>›</b>
                </button>
              ) : null}
              {moderation && !administration ? (
                <button className="is-moderation" onClick={() => navigate({ id: "moderation" })}>
                  <span><Glyph name="moderation" size={20} /></span>
                  <strong>Модерація</strong>
                  <small>Користувачі та санкції</small>
                  <b>›</b>
                </button>
              ) : null}
              {reviewerOnly ? (
                <button className="is-creator" onClick={() => navigate({ id: "personalization-review" })}>
                  <span><Glyph name="creator" size={20} /></span>
                  <strong>Перевірка</strong>
                  <small>Черга персоналізацій</small>
                  <b>›</b>
                </button>
              ) : null}
              {administration ? (
                <button className="is-admin" onClick={() => navigate({ id: "administration" })}>
                  <span><Glyph name="admin" size={20} /></span>
                  <strong>Адміністрування</strong>
                  <small>Системні інструменти</small>
                  <b>›</b>
                </button>
              ) : null}
            </nav>
          </motion.aside>
        </>
      ) : null}
    </AnimatePresence>
  )
}
