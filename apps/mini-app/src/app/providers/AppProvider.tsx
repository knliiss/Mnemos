import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react"
import { useTranslation } from "react-i18next"
import type { LocalizationLanguage } from "@/product-ui/model"
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

  const refresh = useCallback(async () => {
    const next = await getCurrentContext()

    setContext(next)
    return next
  }, [])

  const bootstrap = useCallback(async () => {
    setLoading(true)

    try {
      await initializeCurrentUser()
      const next = await getCurrentContext()

      setContext(next)
      await i18n.changeLanguage(next.user.localizationLanguage.toLowerCase())
    } finally {
      setLoading(false)
    }
  }, [i18n])

  const selectAccount = useCallback(
    async (accountId: string) => {
      await selectActiveAccount(accountId)
      await refresh()
    },
    [refresh],
  )

  const registerAccount = useCallback(
    async (nickname: string) => {
      const created = await createAccount(nickname)

      await selectActiveAccount(created.accountId)
      await refresh()
    },
    [refresh],
  )

  const joinByCode = useCallback(
    async (inviteCode: string) => {
      const joined = await joinAccount(inviteCode)

      await selectActiveAccount(joined.accountId)
      await refresh()
    },
    [refresh],
  )

  const leaveAccount = useCallback(async () => {
    await leaveActiveAccount()
    await refresh()
  }, [refresh])

  const reactivate = useCallback(
    async (accountId: string) => {
      await reactivateAccount(accountId)
      await selectActiveAccount(accountId)
      await refresh()
    },
    [refresh],
  )

  const setLanguage = useCallback(
    async (language: LocalizationLanguage) => {
      await updateCurrentLanguage(language)
      await i18n.changeLanguage(language.toLowerCase())
      await refresh()
    },
    [i18n, refresh],
  )

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void bootstrap().catch(handleError)
    })

    return () => window.cancelAnimationFrame(frame)
  }, [bootstrap])

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
    [
      context,
      loading,
      refresh,
      selectAccount,
      registerAccount,
      joinByCode,
      leaveAccount,
      reactivate,
      setLanguage,
    ],
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
