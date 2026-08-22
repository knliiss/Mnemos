import {
  createContext,
  type ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react"
import { useTranslation } from "react-i18next"
import { getToken } from "@/features/auth/api/auth"
import { isAuthError } from "@/features/auth/api/authErrors"
import {
  createAccount,
  joinAccount,
  leaveActiveAccount,
  reactivateAccount,
  selectActiveAccount,
} from "@/shared/api/accountApi"
import type { CurrentContextResponse } from "@/shared/api/backendContracts"
import { isServiceUnavailableError } from "@/shared/api/httpClient"
import {
  getCurrentContext,
  initializeCurrentUser,
  updateCurrentLanguage,
} from "@/shared/api/userApi"
import { handleError } from "@/shared/errors/errorHandler"
import { ServiceUnavailableScreen } from "@/ui/system/ServiceUnavailableScreen"
import type { LocalizationLanguage } from "@/ui/model"

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

const AUTOMATIC_RETRY_DELAY_MS = 10_000
const AppContext = createContext<AppState | null>(null)

export function AppProvider({ children }: { children: ReactNode }) {
  const { i18n } = useTranslation()
  const [context, setContext] = useState<CurrentContextResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [serviceUnavailable, setServiceUnavailable] = useState(false)
  const contextRevision = useRef(0)
  const mountedRef = useRef(true)
  const bootstrapPromiseRef = useRef<Promise<void> | null>(null)

  const refresh = useCallback(async () => {
    const revision = contextRevision.current + 1

    contextRevision.current = revision
    const next = await getCurrentContext()

    if (revision === contextRevision.current) {
      setContext(next)
    }

    return next
  }, [])

  const runBootstrap = useCallback(async () => {
    setLoading(true)

    try {
      await initializeCurrentUser()
      await getToken({ forceRefresh: true })

      if (!mountedRef.current) {
        return
      }

      const next = await refresh()

      if (!mountedRef.current) {
        return
      }

      await i18n.changeLanguage(next.user.localizationLanguage.toLowerCase())
      setServiceUnavailable(false)
    } catch (error) {
      if (!mountedRef.current) {
        return
      }

      if (isServiceUnavailableError(error) || isAuthError(error, "AUTH_UNAVAILABLE")) {
        setServiceUnavailable(true)
        return
      }

      handleError(error)
      setServiceUnavailable(true)
    } finally {
      if (mountedRef.current) {
        setLoading(false)
      }
    }
  }, [i18n, refresh])

  const bootstrap = useCallback((): Promise<void> => {
    if (bootstrapPromiseRef.current) {
      return bootstrapPromiseRef.current
    }

    const attempt = runBootstrap()
    bootstrapPromiseRef.current = attempt

    void attempt.then(
      () => {
        if (bootstrapPromiseRef.current === attempt) {
          bootstrapPromiseRef.current = null
        }
      },
      () => {
        if (bootstrapPromiseRef.current === attempt) {
          bootstrapPromiseRef.current = null
        }
      },
    )

    return attempt
  }, [runBootstrap])

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
    mountedRef.current = true
    const frame = window.requestAnimationFrame(() => {
      void bootstrap()
    })

    return () => {
      contextRevision.current += 1
      mountedRef.current = false
      window.cancelAnimationFrame(frame)
    }
  }, [bootstrap])

  useEffect(() => {
    if (!serviceUnavailable) {
      return
    }

    const retryTimer = window.setTimeout(() => {
      void bootstrap()
    }, AUTOMATIC_RETRY_DELAY_MS)

    return () => {
      window.clearTimeout(retryTimer)
    }
  }, [bootstrap, serviceUnavailable])

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

  if (serviceUnavailable) {
    return <ServiceUnavailableScreen reason="service" onRetry={bootstrap} />
  }

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>
}

export function useApp() {
  const value = useContext(AppContext)

  if (!value) {
    throw new Error("AppProvider is missing")
  }

  return value
}
