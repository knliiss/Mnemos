import { MotionConfig, motion } from "framer-motion"
import { useState } from "react"
import { accounts } from "./mockData"
import type { RootRoute, Route } from "./model"
import { AccountFeature } from "./features/AccountFeature"
import { BetsPage, EventsHome, SubscriptionsPage } from "./features/EventsFeature"
import { ModerationFeature } from "./features/ModerationFeature"
import { PeopleFeature } from "./features/PeopleFeature"
import { PersonalizationFeature } from "./features/PersonalizationFeature"
import { Glyph } from "./shared/MnemosIcon"

const rootRoutes: RootRoute[] = ["events", "account", "me"]
const accountRoutes = new Set<Route["id"]>(["account", "member", "roles", "role-editor", "account-wallet"])
const peopleRoutes = new Set<Route["id"]>(["me", "search", "public-profile", "user-wallet", "settings"])
const personalizationRoutes = new Set<Route["id"]>(["personalization-store", "personalization-owned", "personalization-detail", "creator-studio", "personalization-editor"])
const moderationRoutes = new Set<Route["id"]>(["moderation", "moderation-user", "personalization-review", "bet-admin"])

export default function App() {
  const [stack, setStack] = useState<Route[]>([{ id: "events" }])
  const [activeAccount, setActiveAccount] = useState(accounts[0])
  const current = stack[stack.length - 1]
  const isRoot = rootRoutes.includes(current.id as RootRoute)

  const navigate = (route: Route) => setStack((currentStack) => [...currentStack, route])
  const back = () => setStack((currentStack) => currentStack.length > 1 ? currentStack.slice(0, -1) : currentStack)
  const switchRoot = (root: RootRoute) => setStack([{ id: root }])

  const content = (() => {
    if (current.id === "events") return <EventsHome navigate={navigate} />
    if (current.id === "subscriptions") return <SubscriptionsPage back={back} />
    if (current.id === "bets") return <BetsPage back={back} />
    if (accountRoutes.has(current.id)) return <AccountFeature routeId={current.id} params={current.params} activeAccount={activeAccount} setActiveAccount={setActiveAccount} navigate={navigate} back={back} />
    if (peopleRoutes.has(current.id)) return <PeopleFeature routeId={current.id} params={current.params} activeAccount={activeAccount} navigate={navigate} back={back} />
    if (personalizationRoutes.has(current.id)) return <PersonalizationFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />
    if (moderationRoutes.has(current.id)) return <ModerationFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />
    return <EventsHome navigate={navigate} />
  })()

  return (
    <MotionConfig transition={{ duration: 0.2, ease: [0.22, 1, 0.36, 1] }}>
      <main className="preview-stage">
        <section className="app-shell">
          <motion.div className={`route-stage ${isRoot ? "has-tabbar" : ""}`} initial={{ opacity: 0.75, y: 3 }} animate={{ opacity: 1, y: 0 }} key={`${current.id}-${stack.length}`}>
            {content}
          </motion.div>
          {isRoot ? <RootNavigation current={current.id as RootRoute} onChange={switchRoot} /> : null}
        </section>
      </main>
    </MotionConfig>
  )
}

function RootNavigation({ current, onChange }: { current: RootRoute; onChange: (route: RootRoute) => void }) {
  return <nav className="root-navigation" aria-label="Основна навігація">
    <button className={current === "events" ? "is-active" : ""} onClick={() => onChange("events")}><Glyph name="events" size={21} /><span>Події</span></button>
    <button className={current === "account" ? "is-active" : ""} onClick={() => onChange("account")}><Glyph name="account" size={21} /><span>Аккаунт</span></button>
    <button className={current === "me" ? "is-active" : ""} onClick={() => onChange("me")}><Glyph name="person" size={21} /><span>Я</span></button>
  </nav>
}
