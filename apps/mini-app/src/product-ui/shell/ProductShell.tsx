import { MotionConfig, motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import { ErrorNotificationBridge } from "../components/ErrorNotificationBridge"
import { ErrorNotificationProvider } from "../components/ErrorNotifications"
import { AdministrationHome } from "../features/AdministrationHome"
import { AccountArea } from "../features/account/AccountArea"
import { CreatorArea } from "../features/creator/CreatorArea"
import { EventsArea } from "../features/events/EventsArea"
import { ModerationArea } from "../features/ModerationArea"
import { NotFoundFeature } from "../features/NotFoundFeature"
import { PeopleArea } from "../features/people/PeopleArea"
import { PersonalizationArea } from "../features/personalization/PersonalizationArea"
import { StaffArea } from "../features/staff/StaffArea"
import { useProductData } from "../data/ProductDataProvider"
import type { Account, RootRoute, Route } from "../model"
import { featureArea, initialStack, isRootRoute } from "../navigation/routeRegistry"
import { AccountAccessLanding } from "./AccountAccessLanding"
import {
  canRenderWithoutActiveAccount,
  localizationOptions,
  routeKey,
} from "./navigation"
import { RootNavigation } from "./RootNavigation"
import { UserHubOverlay, UserHubTrigger } from "./UserHub"

export function ProductShell() {
  const product = useProductData()
  const [stack, setStack] = useState<Route[]>(initialStack)
  const [userHubOpen, setUserHubOpen] = useState(
    () => new URLSearchParams(window.location.search).get("hub") === "1",
  )
  const [accountPickerOpen, setAccountPickerOpen] = useState(
    () => new URLSearchParams(window.location.search).get("accounts") === "1",
  )
  const routeStageRef = useRef<HTMLDivElement>(null)
  const scrollPositions = useRef<Record<string, number>>({})

  const current = stack[stack.length - 1]
  const area = featureArea(current.id)
  const notFound = current.id === "account" && current.params?.notFound === "true"
  const rootRoute = !notFound && isRootRoute(current.id) ? current.id : null
  const activeAccount = product.activeAccount

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
    setStack((value) =>
      value.length > 1 ? value.slice(0, -1) : [{ id: "account" }],
    )
  }

  const switchRoot = (route: RootRoute) => {
    rememberScroll()
    setUserHubOpen(false)
    setAccountPickerOpen(false)
    setStack([{ id: route }])
  }

  useEffect(() => {
    const element = routeStageRef.current

    if (!element) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      element.scrollTo({
        top: scrollPositions.current[routeKey(current)] ?? 0,
        behavior: "auto",
      })
    })

    return () => window.cancelAnimationFrame(frame)
  }, [current])

  useEffect(() => {
    const option = localizationOptions.find(
      (item) => item.value === product.user.language,
    )
    document.documentElement.lang = option?.htmlLang ?? "uk"
  }, [product.user.language])

  const selectAccount = async (account: Account) => {
    if (account.active === false && !account.owner) {
      return
    }

    await product.selectAccount(account)
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

  const content = renderContent({
    area,
    current,
    notFound,
    activeAccount,
    navigate,
    back,
    switchRoot,
  })

  return (
    <MotionConfig transition={{ duration: 0.19, ease: [0.22, 1, 0.36, 1] }}>
      <ErrorNotificationProvider hasTabbar={Boolean(rootRoute && activeAccount)}>
        <ErrorNotificationBridge />

        <main className="preview-stage">
          <section className="app-shell">
            <motion.div
              ref={routeStageRef}
              className={`route-stage ${
                rootRoute && activeAccount
                  ? `has-tabbar has-user-access route-stage--${rootRoute}`
                  : ""
              }`}
              initial={false}
              animate={{ opacity: 1 }}
            >
              {rootRoute && activeAccount ? (
                rootRoute === "account" ? (
                  <div className="account-root-user-entry">
                    <span className="account-root-user-entry__identity">
                      <small>MNEMOS USER</small>
                      <strong>@{product.user.username}</strong>
                    </span>
                    <UserHubTrigger open={userHubOpen} onToggle={toggleUserHub} />
                  </div>
                ) : (
                  <UserHubTrigger open={userHubOpen} onToggle={toggleUserHub} />
                )
              ) : null}

              {content}
            </motion.div>

            {rootRoute && activeAccount ? (
              <>
                <UserHubOverlay
                  open={userHubOpen}
                  accountPickerOpen={accountPickerOpen}
                  activeAccount={activeAccount}
                  accounts={product.accounts}
                  localization={product.user.language}
                  username={product.user.username}
                  prime={product.user.prime}
                  systemRoles={product.user.systemRoles}
                  onLocalizationChange={(language) => void product.setLanguage(language)}
                  onClose={closeUserHub}
                  onToggleAccounts={() => setAccountPickerOpen((value) => !value)}
                  onSelectAccount={(account) => void selectAccount(account)}
                  navigate={navigate}
                />
                <RootNavigation current={rootRoute} onChange={switchRoot} />
              </>
            ) : null}
          </section>
        </main>
      </ErrorNotificationProvider>
    </MotionConfig>
  )
}

type RenderContentOptions = {
  area: ReturnType<typeof featureArea>
  current: Route
  notFound: boolean
  activeAccount: Account | null
  navigate: (route: Route) => void
  back: () => void
  switchRoot: (route: RootRoute) => void
}

function renderContent({
  area,
  current,
  notFound,
  activeAccount,
  navigate,
  back,
  switchRoot,
}: RenderContentOptions) {
  if (notFound) {
    return (
      <NotFoundFeature
        requested={current.params?.requested}
        onHome={() => switchRoot("account")}
      />
    )
  }

  if (!activeAccount && !canRenderWithoutActiveAccount(current)) {
    return <AccountAccessLanding route={current} navigate={navigate} back={back} />
  }

  if (area === "events" && activeAccount) {
    return (
      <EventsArea
        routeId={current.id}
        params={current.params}
        activeAccount={activeAccount}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (area === "account") {
    return (
      <AccountArea
        routeId={current.id}
        params={current.params}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (area === "people") {
    return (
      <PeopleArea
        routeId={current.id}
        params={current.params}
        activeAccount={activeAccount}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (area === "personalization") {
    return (
      <PersonalizationArea
        routeId={current.id}
        params={current.params}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (area === "creator") {
    return (
      <CreatorArea
        routeId={current.id}
        params={current.params}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (current.id === "moderation") {
    return <ModerationArea navigate={navigate} back={back} />
  }

  if (current.id === "administration") {
    return <AdministrationHome navigate={navigate} back={back} />
  }

  return (
    <StaffArea
      routeId={current.id}
      params={current.params}
      navigate={navigate}
      back={back}
    />
  )
}
