import { MotionConfig, motion } from "framer-motion"
import { useState } from "react"
import { accounts as initialAccounts } from "./mockData"
import type { Account, RootRoute, Route } from "./model"
import { AccountFeature } from "./features/AccountFeature"
import { CreatorFeature } from "./features/CreatorFeature"
import { BetsPage, EventsHome, SubscriptionsPage } from "./features/EventsFeature"
import { PeopleFeature } from "./features/PeopleFeature"
import { PersonalizationFeature } from "./features/PersonalizationFeature"
import { StaffFeature } from "./features/StaffFeature"
import { Glyph } from "./shared/MnemosIcon"

const roots: RootRoute[] = ["events", "account", "me"]
const accountRoutes = new Set<Route["id"]>(["account", "member", "roles", "role-editor", "account-wallet", "account-invites", "account-settings", "account-create"])
const peopleRoutes = new Set<Route["id"]>(["me", "search", "public-profile", "user-wallet", "settings"])
const personalizationRoutes = new Set<Route["id"]>(["personalization-store", "personalization-owned", "personalization-detail"])
const creatorRoutes = new Set<Route["id"]>(["creator-studio", "personalization-editor"])
const staffRoutes = new Set<Route["id"]>(["moderation", "moderation-user", "personalization-review", "bet-admin", "badge-admin"])

export default function App() {
  const [stack, setStack] = useState<Route[]>([{ id: "events" }])
  const [accounts, setAccounts] = useState<Account[]>(initialAccounts)
  const [activeAccount, setActiveAccount] = useState<Account>(initialAccounts[0])
  const current = stack[stack.length - 1]
  const isRoot = roots.includes(current.id as RootRoute)

  const navigate = (route: Route) => {
    if (roots.includes(route.id as RootRoute)) {
      setStack([route])
      return
    }
    setStack((value) => [...value, route])
  }
  const back = () => setStack((value) => (value.length > 1 ? value.slice(0, -1) : value))
  const switchRoot = (route: RootRoute) => setStack([{ id: route }])

  let content
  if (current.id === "events") content = <EventsHome navigate={navigate} />
  else if (current.id === "subscriptions") content = <SubscriptionsPage back={back} />
  else if (current.id === "bets") content = <BetsPage back={back} />
  else if (accountRoutes.has(current.id)) content = <AccountFeature routeId={current.id} params={current.params} activeAccount={activeAccount} setActiveAccount={setActiveAccount} accounts={accounts} setAccounts={setAccounts} navigate={navigate} back={back} />
  else if (peopleRoutes.has(current.id)) content = <PeopleFeature routeId={current.id} params={current.params} activeAccount={activeAccount} navigate={navigate} back={back} />
  else if (personalizationRoutes.has(current.id)) content = <PersonalizationFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />
  else if (creatorRoutes.has(current.id)) content = <CreatorFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />
  else if (staffRoutes.has(current.id)) content = <StaffFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />
  else content = <EventsHome navigate={navigate} />

  return <MotionConfig transition={{ duration: 0.18, ease: [0.22, 1, 0.36, 1] }}><main className="preview-stage"><section className="app-shell"><motion.div className={`route-stage ${isRoot ? "has-tabbar" : ""}`} initial={false} animate={{ opacity: 1 }}>{content}</motion.div>{isRoot ? <RootNavigation current={current.id as RootRoute} onChange={switchRoot} /> : null}</section></main></MotionConfig>
}

function RootNavigation({ current, onChange }: { current: RootRoute; onChange: (route: RootRoute) => void }) {
  return <nav className="root-navigation" aria-label="Основна навігація"><button className={current === "events" ? "is-active" : ""} onClick={() => onChange("events")}><Glyph name="events" size={21} /><span>Події</span></button><button className={current === "account" ? "is-active" : ""} onClick={() => onChange("account")}><Glyph name="account" size={21} /><span>Аккаунт</span></button><button className={current === "me" ? "is-active" : ""} onClick={() => onChange("me")}><Glyph name="person" size={21} /><span>Я</span></button></nav>
}
