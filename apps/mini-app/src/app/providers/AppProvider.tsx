import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react"
import { useTranslation } from "react-i18next"
import type { CurrentContextResponse } from "@/shared/api/backendContracts"
import {
  createAccount,
  getCurrentContext,
  initializeCurrentUser,
  joinAccount,
  leaveActiveAccount,
  reactivateAccount,
  selectActiveAccount,
  updateCurrentLanguage,
} from "@/shared/api/productApi"
import type { LocalizationLanguage } from "@/product-ui/model"
import { handleError } from "@/shared/errors/errorHandler"
import type { Account as LegacyAccount, AvailableAccount, ThemeMode, User } from "@/shared/types/domain"

type AppState = {
  context: CurrentContextResponse | null
  loading: boolean
  user: User | null
  account: LegacyAccount | null
  availableAccounts: AvailableAccount[]
  theme: ThemeMode
  setTheme(theme: ThemeMode): void
  refresh(): Promise<CurrentContextResponse>
  selectAccount(accountId: string): Promise<void>
  registerAccount(nickname: string): Promise<void>
  joinByCode(inviteCode: string): Promise<void>
  leaveAccount(): Promise<void>
  reactivate(accountId: string): Promise<void>
  setLanguage(language: LocalizationLanguage): Promise<void>
}

const AppContext = createContext<AppState | null>(null)
const THEME_STORAGE_KEY = "mnemos-theme"

export function AppProvider({ children }: { children: ReactNode }) {
  const { i18n } = useTranslation()
  const [context, setContext] = useState<CurrentContextResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [theme, setThemeState] = useState<ThemeMode>(() => {
    const stored = window.localStorage.getItem(THEME_STORAGE_KEY)
    return stored === "light" || stored === "system" ? stored : "dark"
  })

  useEffect(() => {
    void bootstrap().catch(handleError)
  }, [])

  useEffect(() => {
    window.localStorage.setItem(THEME_STORAGE_KEY, theme)
    document.documentElement.dataset.theme = theme
  }, [theme])

  async function bootstrap() {
    setLoading(true)

    try {
      await initializeCurrentUser()
      const next = await getCurrentContext()

      setContext(next)
      await i18n.changeLanguage(next.user.localizationLanguage.toLowerCase())
    } finally {
      setLoading(false)
    }
  }

  async function refresh() {
    const next = await getCurrentContext()

    setContext(next)
    return next
  }

  async function selectAccount(accountId: string) {
    await selectActiveAccount(accountId)
    await refresh()
  }

  async function registerAccount(nickname: string) {
    const created = await createAccount(nickname)

    await selectActiveAccount(created.accountId)
    await refresh()
  }

  async function joinByCode(inviteCode: string) {
    const joined = await joinAccount(inviteCode)

    await selectActiveAccount(joined.accountId)
    await refresh()
  }

  async function leaveAccount() {
    await leaveActiveAccount()
    await refresh()
  }

  async function reactivate(accountId: string) {
    await reactivateAccount(accountId)
    await selectActiveAccount(accountId)
    await refresh()
  }

  async function setLanguage(language: LocalizationLanguage) {
    await updateCurrentLanguage(language)
    await i18n.changeLanguage(language.toLowerCase())
    await refresh()
  }

  const user = useMemo<User | null>(() => context?.user ?? null, [context?.user])

  const availableAccounts = useMemo<AvailableAccount[]>(
    () => context?.availableAccounts.map((account) => ({
      id: account.id,
      ownerUserId: account.owner ? context.user.id : "",
      cristalixUuid: account.cristalixUuid,
      cristalixNickname: account.cristalixNickname,
    })) ?? [],
    [context],
  )

  const account = useMemo<LegacyAccount | null>(() => {
    const active = context?.activeAccount

    if (!active || !context) return null

    return {
      id: active.id,
      ownerUserId: active.owner ? context.user.id : "",
      cristalixUuid: active.cristalixUuid,
      cristalixNickname: active.cristalixNickname,
      createdAt: active.createdAt,
      updatedAt: active.updatedAt,
      role: {
        id: active.owner ? "owner" : "member",
        name: active.owner ? "Власник" : "Учасник",
        color: active.owner ? "#D9B85B" : "#8A9283",
        permissions: context.effectivePermissions,
      },
      online: true,
      streak: 0,
      coins: 0,
      primes: 0,
      likes: 0,
      comments: 0,
      members: active.memberCount,
      about: "",
      personalization: {
        prefix: "",
        suffix: "",
        nicknameColor: "#F0F2EB",
        badge: "",
      },
    }
  }, [context])

  const value = useMemo<AppState>(
    () => ({
      context,
      loading,
      user,
      account,
      availableAccounts,
      theme,
      setTheme: setThemeState,
      refresh,
      selectAccount,
      registerAccount,
      joinByCode,
      leaveAccount,
      reactivate,
      setLanguage,
    }),
    [context, loading, user, account, availableAccounts, theme],
  )

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>
}

export function useApp() {
  const value = useContext(AppContext)

  if (!value) {
    throw new Error("AppProvider is missing")
  }

  return value
}
