import { MotionConfig, motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import { ErrorNotificationBridge } from "../components/errors/ErrorNotificationBridge"
import { ErrorNotificationProvider } from "../components/errors/ErrorNotifications"
import { useMnemosData } from "../data/MnemosDataProvider"
import type { Account, RootRoute, Route } from "../model"
import { initialStack, isRootRoute } from "../navigation/routeRegistry"
import "../shared/layout.css"
import "../shared/form-controls.css"
import { localizationOptions, routeKey } from "./navigation"
import { RootNavigation } from "./RootNavigation"
import { RouteContent } from "./RouteContent"
import "./shell.css"
import "./native-shell.css"
import "./user-hub-polish.css"
import "./shell-layout.css"
import { UserHubOverlay } from "./UserHubOverlay"
import { UserHubTrigger } from "./UserHubTrigger"

export function AppShell() {
  const data = useMnemosData()
  const [stack, setStack] = useState<Route[]>(initialStack)
  const [userHubOpen, setUserHubOpen] = useState(
    () => new URLSearchParams(window.location.search).get("hub") === "1",
  )
  const [accountPickerOpen, setAccountPickerOpen] = useState(
    () => new URLSearchParams(window.location.search).get("accounts") === "1",
  )
  const [switchingAccountId, setSwitchingAccountId] = useState<string | null>(null)
  const routeStageRef = useRef<HTMLDivElement>(null)
  const scrollPositions = useRef<Record<string, number>>({})
  const accountSwitchInProgress = useRef(false)

  const current = stack[stack.length - 1]
  const notFound = current.id === "account" && current.params?.notFound === "true"
  const rootRoute = !notFound && isRootRoute(current.id) ? current.id : null
  const activeAccount = data.activeAccount
  const activeAccountId = activeAccount?.id ?? "none"
  const currentScrollKey = `${activeAccountId}:${routeKey(current)}`

  const rememberScroll = () => {
    scrollPositions.current[currentScrollKey] = routeStageRef.current?.scrollTop ?? 0
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
    setStack((value) => (value.length > 1 ? value.slice(0, -1) : [{ id: "account" }]))
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
        top: scrollPositions.current[currentScrollKey] ?? 0,
        behavior: "auto",
      })
    })

    return () => window.cancelAnimationFrame(frame)
  }, [currentScrollKey])

  useEffect(() => {
    const option = localizationOptions.find((item) => item.value === data.user.language)

    document.documentElement.lang = option?.htmlLang ?? "uk"
  }, [data.user.language])

  const selectAccount = async (account: Account) => {
    if (accountSwitchInProgress.current || (account.active === false && !account.owner)) {
      return
    }

    if (account.id === activeAccount?.id && account.active !== false) {
      setAccountPickerOpen(false)
      return
    }

    accountSwitchInProgress.current = true
    setSwitchingAccountId(account.id)

    try {
      const switched = await data.selectAccount(account)
      if (switched) {
        setAccountPickerOpen(false)
      }
    } finally {
      accountSwitchInProgress.current = false
      setSwitchingAccountId(null)
    }
  }

  const toggleUserHub = () => {
    setUserHubOpen((value) => !value)
    setAccountPickerOpen(false)
  }

  const closeUserHub = () => {
    setUserHubOpen(false)
    setAccountPickerOpen(false)
  }

  const userHubTrigger =
    rootRoute && activeAccount ? (
      <div className="root-user-access">
        <UserHubTrigger open={userHubOpen} onToggle={toggleUserHub} />
      </div>
    ) : null

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
              {userHubTrigger}

              <div className="route-content-frame">
                <RouteContent
                  key={activeAccountId}
                  current={current}
                  activeAccount={activeAccount}
                  navigate={navigate}
                  back={back}
                  switchRoot={switchRoot}
                />
              </div>
            </motion.div>

            {rootRoute && activeAccount ? (
              <>
                <UserHubOverlay
                  open={userHubOpen}
                  accountPickerOpen={accountPickerOpen}
                  activeAccount={activeAccount}
                  accounts={data.accounts}
                  switchingAccountId={switchingAccountId}
                  localization={data.user.language}
                  username={data.user.username}
                  prime={data.user.prime}
                  systemRoles={data.user.systemRoles}
                  onLocalizationChange={(language) => void data.setLanguage(language)}
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
