import { MotionConfig, motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import { ErrorNotificationBridge } from "../components/errors/ErrorNotificationBridge"
import { ErrorNotificationProvider } from "../components/errors/ErrorNotifications"
import { useProductData } from "../data/ProductDataProvider"
import type { Account, RootRoute, Route } from "../model"
import { initialStack, isRootRoute } from "../navigation/routeRegistry"
import { localizationOptions, routeKey } from "./navigation"
import { ProductRouteContent } from "./ProductRouteContent"
import { RootNavigation } from "./RootNavigation"
import { UserHubOverlay } from "./UserHubOverlay"
import { UserHubTrigger } from "./UserHubTrigger"

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

  const userHubTrigger =
    rootRoute && activeAccount ? (
      <UserHubTrigger open={userHubOpen} onToggle={toggleUserHub} />
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
              {rootRoute === "account" && activeAccount ? (
                <div className="account-root-user-entry">
                  <span className="account-root-user-entry__identity">
                    <small>MNEMOS USER</small>
                    <strong>@{product.user.username}</strong>
                  </span>
                  {userHubTrigger}
                </div>
              ) : (
                userHubTrigger
              )}

              <ProductRouteContent
                current={current}
                activeAccount={activeAccount}
                navigate={navigate}
                back={back}
                switchRoot={switchRoot}
              />
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
