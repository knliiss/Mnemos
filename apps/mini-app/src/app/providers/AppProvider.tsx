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

type AppState = {
  context: CurrentContextResponse | null
  loading: boolean
  refresh(): Promise<CurrentContextResponse>
  selectAccount(accountId: string): Promise<void>
  registerAccount(nickname: string): Promise<void>
  joinByCode(inviteCode: string): Promise<void>
  leaveAccount(): Promise<void>
  reactivate(accountId: string): Promise<void>
  setLanguage(language: LocalizationLanguage): Promise<void>
}

const AppContext = createContext<AppState | null>(null)

export function AppProvider({ children }: { children: ReactNode }) {
  const { i18n } = useTranslation()
  const [context, setContext] = useState<CurrentContextResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    void bootstrap().catch(handleError)
  }, [])

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

  const value = useMemo<AppState>(
    () => ({
      context,
      loading,
      refresh,
      selectAccount,
      registerAccount,
      joinByCode,
      leaveAccount,
      reactivate,
      setLanguage,
    }),
    [context, loading],
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
