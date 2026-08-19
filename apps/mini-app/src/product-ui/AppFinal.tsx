import { AnimatePresence, MotionConfig, motion } from "framer-motion"
import { useEffect, useRef, useState } from "react"
import { CristalixHead } from "./components/CristalixHead"
import { ErrorNotificationProvider } from "./components/ErrorNotifications"
import { RootNavIllustration } from "./components/RootNavIllustration"
import { useProductData } from "./data/ProductDataProvider"
import type { Account, LocalizationLanguage, RootRoute, Route } from "./model"
import { featureArea, initialStack, isRootRoute } from "./navigation/routeRegistry"
import { AccountAreaFinal } from "./features/AccountAreaFinal"
import { AdministrationHome } from "./features/AdministrationHome"
import { CreatorFeature } from "./features/CreatorFeatureRethink"
import { EventsArea } from "./features/EventsAreaRoundFour"
import { ModerationHomePolished } from "./features/ModerationHomePolished"
import { NotFoundFeature } from "./features/NotFoundFeature"
import { PeopleFeatureFinal } from "./features/PeopleFeatureFinal"
import { PersonalizationFeatureFinal } from "./features/PersonalizationFeatureFinal"
import { StaffAreaOverhaul } from "./features/StaffAreaOverhaul"
import { CurrencyMark, Glyph } from "./shared/MnemosIcon"

const rootItems: { id: RootRoute; label: string }[] = [
  { id: "bets", label: "Ставки" },
  { id: "events", label: "Події" },
  { id: "account", label: "Головна" },
  { id: "search", label: "Пошук" },
  { id: "personalization-store", label: "Магазин" },
]

const localizationOptions: { value: LocalizationLanguage; short: string; label: string; htmlLang: string }[] = [
  { value: "UK", short: "UA", label: "Українська", htmlLang: "uk" },
  { value: "RU", short: "RU", label: "Русский", htmlLang: "ru" },
  { value: "EN", short: "EN", label: "English", htmlLang: "en" },
]

function routeKey(route: Route) {
  return `${route.id}:${JSON.stringify(route.params ?? {})}`
}

export default function AppFinal() {
  const product = useProductData()
  const [stack, setStack] = useState<Route[]>(initialStack)
  const [userHubOpen, setUserHubOpen] = useState(() => new URLSearchParams(window.location.search).get("hub") === "1")
  const [accountPickerOpen, setAccountPickerOpen] = useState(() => new URLSearchParams(window.location.search).get("accounts") === "1")
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
    setStack((value) => value.length > 1 ? value.slice(0, -1) : [{ id: "account" }])
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

  const content = notFound
    ? (
        <NotFoundFeature
          requested={current.params?.requested}
          onHome={() => switchRoot("account")}
        />
      )
    : !activeAccount
      ? (
          <AccountAccessLanding
            route={current}
            navigate={navigate}
            back={back}
          />
        )
      : area === "events"
        ? <EventsArea routeId={current.id} params={current.params} activeAccount={activeAccount} navigate={navigate} back={back} />
        : area === "account"
          ? (
              <AccountAreaFinal
                routeId={current.id}
                params={current.params}
                activeAccount={activeAccount}
                setActiveAccount={(account) => void product.selectAccount(account)}
                accounts={product.accounts}
                setAccounts={() => undefined}
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
      <ErrorNotificationProvider hasTabbar={Boolean(rootRoute && activeAccount)}>
        <main className="preview-stage redesign-stage final-stage">
          <section className="app-shell redesign-shell final-shell">
            <motion.div
              ref={routeStageRef}
              className={`route-stage ${rootRoute && activeAccount ? `has-tabbar has-user-access route-stage--${rootRoute}` : ""}`}
              initial={false}
              animate={{ opacity: 1 }}
            >
              {rootRoute && activeAccount ? (
                <UserHubTrigger
                  open={userHubOpen}
                  onToggle={toggleUserHub}
                />
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

function AccountAccessLanding({ route, navigate, back }: { route: Route; navigate: (route: Route) => void; back: () => void }) {
  if (route.id === "account-create" || route.id === "account-join") {
    return (
      <AccountAreaFinal
        routeId={route.id}
        params={route.params}
        activeAccount={emptyAccount}
        setActiveAccount={() => undefined}
        accounts={[]}
        setAccounts={() => undefined}
        navigate={navigate}
        back={back}
      />
    )
  }

  return (
    <div className="screen account-onboarding account-onboarding--create">
      <header className="account-onboarding-header account-access-landing__header">
        <div className="account-onboarding-heading">
          <small>MNEMOS × CRISTALIX</small>
          <h1>Обери аккаунт</h1>
          <p>Створи новий Mnemos аккаунт для Cristalix профілю або приєднайся за кодом.</p>
        </div>
      </header>

      <section className="account-onboarding-hero account-access-landing__hero">
        <div className="account-onboarding-hero__art">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
        </div>
        <div className="account-onboarding-hero__copy">
          <small>ПОЧАТОК</small>
          <strong>Кіт уже чекає на аккаунт</strong>
          <p>Після привʼязки відкриються події, нагороди, команда та персоналізації.</p>
        </div>
      </section>

      <div className="account-onboarding-facts account-access-landing__actions">
        <motion.button whileTap={{ scale: 0.98 }} onClick={() => navigate({ id: "account-create" })}>
          <span>+</span>
          <div><strong>Новий аккаунт</strong><small>Знайти Cristalix nickname</small></div>
        </motion.button>
        <motion.button whileTap={{ scale: 0.98 }} onClick={() => navigate({ id: "account-join" })}>
          <span>→</span>
          <div><strong>Вхід за кодом</strong><small>Приєднатися до команди</small></div>
        </motion.button>
      </div>
    </div>
  )
}

const emptyAccount: Account = {
  id: "onboarding",
  cristalixUuid: "00000000-0000-0000-0000-000000000000",
  nickname: "",
  role: "",
  skinUrl: "",
  memberCount: 0,
  maxMembers: 0,
  owner: false,
  active: false,
  coin: 0,
}

function RootNavigation({ current, onChange }: { current: RootRoute; onChange: (route: RootRoute) => void }) {
  return (
    <nav className="root-navigation root-navigation--five final-navigation root-navigation--illustrated" aria-label="Основна навігація">
      {rootItems.map((item) => (
        <button key={item.id} className={current === item.id ? "is-active" : ""} onClick={() => onChange(item.id)}>
          <span className="root-navigation__icon root-navigation__icon--illustrated">
            <RootNavIllustration route={item.id} />
          </span>
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
  const creator = systemRoles.some((role) => ["PERSONALIZATION_CREATOR", "ADMIN", "SUPER_ADMIN"].includes(role))
  const moderation = systemRoles.some((role) => ["MODERATOR", "ADMIN", "SUPER_ADMIN"].includes(role))
  const administration = systemRoles.some((role) => ["ADMIN", "SUPER_ADMIN"].includes(role))
  const personalizationReviewer = systemRoles.includes("PERSONALIZATION_MODERATOR")
  const reviewerOnly = personalizationReviewer && !administration
  const localizationLabel = localizationOptions.find((option) => option.value === localization)?.label ?? "Українська"

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
                <strong>@{username}</strong>
              </span>
              <button className="final-user-prime" onClick={() => navigate({ id: "user-wallet" })}>
                <CurrencyMark currency="PRIME" size={16} />
                <strong>{prime.toLocaleString("uk-UA")}</strong>
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

            <section className="user-hub-localization" aria-label="Мова інтерфейсу">
              <span className="user-hub-localization__identity">
                <span className="user-hub-localization__mark" aria-hidden="true" />
                <span className="user-hub-localization__copy">
                  <small>Мова</small>
                  <strong>{localizationLabel}</strong>
                </span>
              </span>
              <span className="user-hub-localization__choices" role="group" aria-label="Оберіть мову">
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
