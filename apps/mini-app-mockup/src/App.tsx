import { MotionConfig, motion } from "framer-motion"
import { useState } from "react"
import { accounts as initialAccounts } from "./mockData"
import type { Account, RootRoute, Route } from "./model"
import { featureArea, initialStack, isRootRoute } from "./navigation/routeRegistry"
import { AccountFeature } from "./features/AccountFeature"
import { CreatorFeature } from "./features/CreatorFeature"
import { EventsArea } from "./features/EventsArea"
import { PeopleFeature } from "./features/PeopleFeature"
import { PersonalizationFeature } from "./features/PersonalizationFeature"
import { StaffFeature } from "./features/StaffFeature"
import { Glyph } from "./shared/MnemosIcon"

export default function App() {
  const [stack, setStack] = useState<Route[]>(initialStack)
  const [accounts, setAccounts] = useState<Account[]>(initialAccounts)
  const [activeAccount, setActiveAccount] = useState<Account>(initialAccounts[0])
  const current = stack[stack.length - 1]
  const area = featureArea(current.id)
  const rootRoute = isRootRoute(current.id) ? current.id : null

  const navigate = (route: Route) => {
    if (isRootRoute(route.id)) {
      setStack([route])
      return
    }

    setStack((value) => [...value, route])
  }

  const back = () => setStack((value) => value.length > 1 ? value.slice(0, -1) : value)
  const switchRoot = (route: RootRoute) => setStack([{ id: route }])

  const content = area === "events"
    ? <EventsArea routeId={current.id} params={current.params} navigate={navigate} back={back} />
    : area === "account"
      ? <AccountFeature routeId={current.id} params={current.params} activeAccount={activeAccount} setActiveAccount={setActiveAccount} accounts={accounts} setAccounts={setAccounts} navigate={navigate} back={back} />
      : area === "people"
        ? <PeopleFeature routeId={current.id} params={current.params} activeAccount={activeAccount} navigate={navigate} back={back} />
        : area === "personalization"
          ? <PersonalizationFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />
          : area === "creator"
            ? <CreatorFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />
            : <StaffFeature routeId={current.id} params={current.params} navigate={navigate} back={back} />

  return (
    <MotionConfig transition={{ duration: 0.18, ease: [0.22, 1, 0.36, 1] }}>
      <main className="preview-stage">
        <section className="app-shell">
          <motion.div className={`route-stage ${rootRoute ? "has-tabbar" : ""}`} initial={false} animate={{ opacity: 1 }}>
            {content}
          </motion.div>
          {rootRoute ? <RootNavigation current={rootRoute} onChange={switchRoot} /> : null}
        </section>
      </main>
    </MotionConfig>
  )
}

function RootNavigation({ current, onChange }: { current: RootRoute; onChange: (route: RootRoute) => void }) {
  return (
    <nav className="root-navigation" aria-label="Основна навігація">
      <button className={current === "events" ? "is-active" : ""} onClick={() => onChange("events")}>
        <Glyph name="events" size={21} />
        <span>Події</span>
      </button>
      <button className={current === "account" ? "is-active" : ""} onClick={() => onChange("account")}>
        <Glyph name="account" size={21} />
        <span>Аккаунт</span>
      </button>
      <button className={current === "me" ? "is-active" : ""} onClick={() => onChange("me")}>
        <Glyph name="person" size={21} />
        <span>Я</span>
      </button>
    </nav>
  )
}
