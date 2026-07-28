import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react"
import { useTranslation } from "react-i18next"
import type { Account, AvailableAccount, ThemeMode, User } from "@/shared/types/domain"
import {handleError} from "@/shared/errors/errorHandler"
import { activateAccount, createAccount, getActiveAccount, getAvailableAccounts, initializeUser, leaveActiveAccount, updateLanguage } from "@/entities/account/api/accountService"

interface AppState {
  user: User | null
  account: Account | null
  availableAccounts: AvailableAccount[]
  theme: ThemeMode
  loading: boolean
  refresh(): Promise<void>
  selectAccount(id: string): Promise<void>
  registerAccount(nickname: string): Promise<void>
  leaveAccount(): Promise<void>
  setTheme(theme: ThemeMode): void
  setLanguage(language: User["localizationLanguage"]): Promise<void>
}

const AppContext = createContext<AppState | null>(null)

export function AppProvider({ children }: { children: ReactNode }) {
  const { i18n } = useTranslation()
  const [user, setUser] = useState<User | null>(null)
  const [account, setAccount] = useState<Account | null>(null)
  const [availableAccounts, setAvailableAccounts] = useState<AvailableAccount[]>([])
  const [theme, setThemeState] = useState<ThemeMode>(() => (localStorage.getItem("mnemos-theme") as ThemeMode) || "dark")
  const [loading, setLoading] = useState(true)

  useEffect(() => { void refresh().catch(handleError) }, [])
  useEffect(() => {
    const resolved = theme === "system" ? (matchMedia("(prefers-color-scheme: light)").matches ? "light" : "dark") : theme
    document.documentElement.dataset.theme = resolved
  }, [theme])

  async function refresh() {
    setLoading(true)
    try {
      const currentUser = await initializeUser()
      setUser(currentUser)
      await i18n.changeLanguage(currentUser.localizationLanguage.toLowerCase())
      const [active, available] = await Promise.all([getActiveAccount(), getAvailableAccounts()])
      setAccount(active)
      setAvailableAccounts(available)
    } finally { setLoading(false) }
  }

  async function selectAccount(id: string) {
    setAccount(await activateAccount(id))
  }

  async function registerAccount(nickname: string) {
    const created = await createAccount(nickname)
    setAccount(created)
    setAvailableAccounts(await getAvailableAccounts())
  }

  async function leaveAccount() {
    setAvailableAccounts(await leaveActiveAccount())
    setAccount(null)
  }

  function setTheme(next: ThemeMode) {
    localStorage.setItem("mnemos-theme", next)
    setThemeState(next)
  }

  async function setLanguage(language: User["localizationLanguage"]) {
    const updated = await updateLanguage(language)
    setUser(updated)
    await i18n.changeLanguage(language.toLowerCase())
  }

  const value = useMemo(() => ({ user, account, availableAccounts, theme, loading, refresh, selectAccount, registerAccount, leaveAccount, setTheme, setLanguage }), [user, account, availableAccounts, theme, loading])
  return <AppContext.Provider value={value}>{children}</AppContext.Provider>
}

export function useApp() {
  const value = useContext(AppContext)
  if (!value) throw new Error("AppProvider is missing")
  return value
}
