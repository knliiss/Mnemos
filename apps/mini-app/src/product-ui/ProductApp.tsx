import { AnimatePresence, MotionConfig, motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { CristalixHead } from "./components/CristalixHead"
import { ErrorNotificationBridge } from "./components/ErrorNotificationBridge"
import { ErrorNotificationProvider } from "./components/ErrorNotifications"
import { RootNavIllustration } from "./components/RootNavIllustration"
import { useProductData } from "./data/ProductDataProvider"
import type { Account, LocalizationLanguage, RootRoute, Route } from "./model"
import { featureArea, initialStack, isRootRoute } from "./navigation/routeRegistry"
import { AccountArea } from "./features/AccountArea"
import { AdministrationHome } from "./features/AdministrationHome"
import { CreatorArea } from "./features/CreatorArea"
import { EventsArea } from "./features/EventsArea"
import { ModerationArea } from "./features/ModerationArea"
import { NotFoundFeature } from "./features/NotFoundFeature"
import { PeopleArea } from "./features/PeopleArea"
import { PersonalizationArea } from "./features/PersonalizationArea"
import { StaffArea } from "./features/StaffArea"
import { CurrencyMark, Glyph } from "./shared/MnemosIcon"

const rootItems: RootRoute[] = ["bets", "events", "account", "search", "personalization-store"]

const rootNavigationKeys: Record<RootRoute, string> = {
  bets: "productShell.navigation.bets",
  events: "productShell.navigation.events",
  account: "productShell.navigation.home",
  search: "productShell.navigation.search",
  "personalization-store": "productShell.navigation.store",
}

const localizationOptions: {
  value: LocalizationLanguage
  short: string
  label: string
  htmlLang: string
  numberLocale: string
}[] = [
  { value: "UK", short: "UA", label: "Українська", htmlLang: "uk", numberLocale: "uk-UA" },
  { value: "RU", short: "RU", label: "Русский", htmlLang: "ru", numberLocale: "ru-RU" },
  { value: "EN", short: "EN", label: "English", htmlLang: "en", numberLocale: "en-US" },
]

function routeKey(route: Route) {
  return `${route.id}:${JSON.stringify(route.params ?? {})}`
}

export default function ProductApp() {
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
    if (!element) return

    const frame = window.requestAnimationFrame(() => {
      element.scrollTo({ top: scrollPositions.current[routeKey(current)] ?? 0, behavior: "auto" })
    })

    return () => window.cancelAnimationFrame(frame)
  }, [current])

  useEffect(() => {
    const option = localizationOptions.find((item) => item.value === product.user.language)
    document.documentElement.lang = option?.htmlLang ?? "uk"
  }, [product.user.language])

  const selectAccount = async (account: Account) => {
    if (account.active === false && !account.owner) return

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

  const content = notFound ? (
    <NotFoundFeature requested={current.params?.requested} onHome={() => switchRoot("account")} />
  ) : !activeAccount ? (
    <AccountAccessLanding route={current} navigate={navigate} back={back} />
  ) : area === "events" ? (
    <EventsArea
      routeId={current.id}
      params={current.params}
      activeAccount={activeAccount}
      navigate={navigate}
      back={back}
    />
  ) : area === "account" ? (
    <AccountArea routeId={current.id} params={current.params} navigate={navigate} back={back} />
  ) : area === "people" ? (
    <PeopleArea
      routeId={current.id}
      params={current.params}
      activeAccount={activeAccount}
      navigate={navigate}
      back={back}
    />
  ) : area === "personalization" ? (
    <PersonalizationArea
      routeId={current.id}
      params={current.params}
      navigate={navigate}
      back={back}
    />
  ) : area === "creator" ? (
    <CreatorArea routeId={current.id} params={current.params} navigate={navigate} back={back} />
  ) : current.id === "moderation" ? (
    <ModerationArea navigate={navigate} back={back} />
  ) : current.id === "administration" ? (
    <AdministrationHome navigate={navigate} back={back} />
  ) : (
    <StaffArea routeId={current.id} params={current.params} navigate={navigate} back={back} />
  )

  return (
    <MotionConfig transition={{ duration: 0.19, ease: [0.22, 1, 0.36, 1] }}>
      <ErrorNotificationProvider hasTabbar={Boolean(rootRoute && activeAccount)}>
        <ErrorNotificationBridge />
        <main className="preview-stage">
          <section className="app-shell">
            <motion.div
              ref={routeStageRef}
              className={`route-stage ${rootRoute && activeAccount ? `has-tabbar has-user-access route-stage--${rootRoute}` : ""}`}
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

function AccountAccessLanding({
  route,
  navigate,
  back,
}: {
  route: Route
  navigate: (route: Route) => void
  back: () => void
}) {
  const { t } = useTranslation()

  if (route.id === "account-create" || route.id === "account-join") {
    return <AccountArea routeId={route.id} params={route.params} navigate={navigate} back={back} />
  }

  return (
    <div className="screen account-onboarding account-onboarding--create">
      <header className="account-onboarding-header account-access-landing__header">
        <div className="account-onboarding-heading">
          <small>MNEMOS × CRISTALIX</small>
          <h1>{t("productShell.onboarding.title")}</h1>
          <p>{t("productShell.onboarding.description")}</p>
        </div>
      </header>

      <section className="account-onboarding-hero account-access-landing__hero">
        <div className="account-onboarding-hero__art">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
        </div>
        <div className="account-onboarding-hero__copy">
          <small>{t("productShell.onboarding.start")}</small>
          <strong>{t("productShell.onboarding.mascotTitle")}</strong>
          <p>{t("productShell.onboarding.mascotDescription")}</p>
        </div>
      </section>

      <div className="account-onboarding-facts account-access-landing__actions">
        <motion.button
          whileTap={{ scale: 0.98 }}
          onClick={() => navigate({ id: "account-create" })}
        >
          <span>+</span>
          <div>
            <strong>{t("productShell.onboarding.newAccount")}</strong>
            <small>{t("productShell.onboarding.findNickname")}</small>
          </div>
        </motion.button>
        <motion.button whileTap={{ scale: 0.98 }} onClick={() => navigate({ id: "account-join" })}>
          <span>→</span>
          <div>
            <strong>{t("productShell.onboarding.joinByCode")}</strong>
            <small>{t("productShell.onboarding.joinTeam")}</small>
          </div>
        </motion.button>
      </div>
    </div>
  )
}

function RootNavigation({
  current,
  onChange,
}: {
  current: RootRoute
  onChange: (route: RootRoute) => void
}) {
  const { t } = useTranslation()

  return (
    <nav
      className="root-navigation root-navigation--five root-navigation--illustrated"
      aria-label={t("productShell.navigation.aria")}
    >
      {rootItems.map((item) => (
        <button
          key={item}
          className={current === item ? "is-active" : ""}
          onClick={() => onChange(item)}
        >
          <span className="root-navigation__icon root-navigation__icon--illustrated">
            <RootNavIllustration route={item} />
          </span>
          <span>{t(rootNavigationKeys[item])}</span>
        </button>
      ))}
    </nav>
  )
}

function UserHubTrigger({ open, onToggle }: { open: boolean; onToggle: () => void }) {
  const { t } = useTranslation()

  return (
    <motion.button
      className={`global-user-button global-user-button ${open ? "is-open" : ""}`}
      whileTap={{ scale: 0.95 }}
      onClick={onToggle}
      aria-label={t("productShell.userMenu.open")}
    >
      <UserAvatarPlaceholder className="global-user-button__avatar" size={34} />
      <i />
    </motion.button>
  )
}

function UserAvatarPlaceholder({ className, size }: { className?: string; size: number }) {
  const product = useProductData()
  const skinUrl = product.activeAccount?.skinUrl

  if (skinUrl) {
    return (
      <CristalixHead
        className={`${className ?? ""} user-avatar-head`.trim()}
        skinUrl={skinUrl}
        size={size}
      />
    )
  }

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
  localization: LocalizationLanguage
  username: string
  prime: number
  systemRoles: string[]
  onLocalizationChange: (localization: LocalizationLanguage) => void
  onClose: () => void
  onToggleAccounts: () => void
  onSelectAccount: (account: Account) => void
  navigate: (route: Route) => void
}

function UserHubOverlay({
  open,
  accountPickerOpen,
  activeAccount,
  accounts,
  localization,
  username,
  prime,
  systemRoles,
  onLocalizationChange,
  onClose,
  onToggleAccounts,
  onSelectAccount,
  navigate,
}: UserHubOverlayProps) {
  const { t } = useTranslation()
  const creator = systemRoles.some((role) =>
    ["PERSONALIZATION_CREATOR", "ADMIN", "SUPER_ADMIN"].includes(role),
  )
  const moderation = systemRoles.some((role) =>
    ["MODERATOR", "ADMIN", "SUPER_ADMIN"].includes(role),
  )
  const administration = systemRoles.some((role) => ["ADMIN", "SUPER_ADMIN"].includes(role))
  const personalizationReviewer = systemRoles.includes("PERSONALIZATION_MODERATOR")
  const reviewerOnly = personalizationReviewer && !administration
  const localizationOption =
    localizationOptions.find((option) => option.value === localization) ?? localizationOptions[0]

  return (
    <AnimatePresence>
      {open ? (
        <>
          <motion.button
            className="user-hub-backdrop"
            aria-label={t("productShell.userMenu.close")}
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
          />
          <motion.aside
            className="user-hub"
            initial={{ opacity: 0, y: -10, scale: 0.975 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -7, scale: 0.985 }}
          >
            <header className="user-hub-card">
              <UserAvatarPlaceholder
                className="user-hub-card__avatar user-hub-card__avatar--placeholder"
                size={40}
              />
              <span className="user-hub-card__copy">
                <small>MNEMOS USER</small>
                <strong>@{username}</strong>
              </span>
              <button className="user-hub-prime" onClick={() => navigate({ id: "user-wallet" })}>
                <CurrencyMark currency="PRIME" size={16} />
                <strong>{prime.toLocaleString(localizationOption.numberLocale)}</strong>
                <b>›</b>
              </button>
            </header>

            <button
              className={`user-hub-account-switch ${accountPickerOpen ? "is-open" : ""}`}
              onClick={onToggleAccounts}
            >
              <CristalixHead
                className="user-hub-account-switch__mark user-hub-account-switch__mark--skin"
                skinUrl={activeAccount.skinUrl}
                size={34}
              />
              <span>
                <small>{t("productShell.userMenu.activeAccount")}</small>
                <strong>{activeAccount.nickname}</strong>
              </span>
              <span className="user-hub-account-switch__action">
                <Glyph name="swap" size={16} />
                {t("productShell.userMenu.change")}
              </span>
            </button>

            <AnimatePresence initial={false}>
              {accountPickerOpen ? (
                <motion.div
                  className="user-hub-account-picker"
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
                        className="user-hub-account-picker__head"
                        skinUrl={account.skinUrl}
                        size={30}
                      />
                      <strong>{account.nickname}</strong>
                      <small>
                        {account.active === false
                          ? account.owner
                            ? t("productShell.userMenu.reactivate")
                            : t("productShell.userMenu.inactive")
                          : account.role}
                      </small>
                      {account.id === activeAccount.id ? <Glyph name="check" size={14} /> : null}
                    </button>
                  ))}
                  <footer>
                    <button onClick={() => navigate({ id: "account-create" })}>
                      <Glyph name="plus" size={15} />
                      {t("productShell.userMenu.create")}
                    </button>
                    <button onClick={() => navigate({ id: "account-join" })}>
                      <Glyph name="invite" size={15} />
                      {t("productShell.userMenu.joinCode")}
                    </button>
                  </footer>
                </motion.div>
              ) : null}
            </AnimatePresence>

            <section
              className="user-hub-localization"
              aria-label={t("productShell.userMenu.languageAria")}
            >
              <span className="user-hub-localization__identity">
                <span className="user-hub-localization__mark" aria-hidden="true" />
                <span className="user-hub-localization__copy">
                  <small>{t("productShell.userMenu.language")}</small>
                  <strong>{localizationOption.label}</strong>
                </span>
              </span>
              <span
                className="user-hub-localization__choices"
                role="group"
                aria-label={t("productShell.userMenu.chooseLanguage")}
              >
                {localizationOptions.map((option) => (
                  <motion.button
                    key={option.value}
                    type="button"
                    className={option.value === localization ? "is-active" : ""}
                    aria-pressed={option.value === localization}
                    aria-label={option.label}
                    whileTap={{ scale: 0.93 }}
                    onClick={() => onLocalizationChange(option.value)}
                  >
                    {option.short}
                  </motion.button>
                ))}
              </span>
            </section>

            <div className="user-hub-workspace-label">{t("productShell.userMenu.workspaces")}</div>
            <nav className="user-hub-workspace-grid">
              {creator ? (
                <button className="is-creator" onClick={() => navigate({ id: "creator-studio" })}>
                  <span>
                    <Glyph name="creator" size={20} />
                  </span>
                  <strong>Creator Studio</strong>
                  <small>{t("productShell.userMenu.creatorDescription")}</small>
                  <b>›</b>
                </button>
              ) : null}
              {moderation && !administration ? (
                <button className="is-moderation" onClick={() => navigate({ id: "moderation" })}>
                  <span>
                    <Glyph name="moderation" size={20} />
                  </span>
                  <strong>{t("productShell.userMenu.moderation")}</strong>
                  <small>{t("productShell.userMenu.moderationDescription")}</small>
                  <b>›</b>
                </button>
              ) : null}
              {reviewerOnly ? (
                <button
                  className="is-creator"
                  onClick={() => navigate({ id: "personalization-review" })}
                >
                  <span>
                    <Glyph name="creator" size={20} />
                  </span>
                  <strong>{t("productShell.userMenu.review")}</strong>
                  <small>{t("productShell.userMenu.reviewDescription")}</small>
                  <b>›</b>
                </button>
              ) : null}
              {administration ? (
                <button className="is-admin" onClick={() => navigate({ id: "administration" })}>
                  <span>
                    <Glyph name="admin" size={20} />
                  </span>
                  <strong>{t("productShell.userMenu.administration")}</strong>
                  <small>{t("productShell.userMenu.administrationDescription")}</small>
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
