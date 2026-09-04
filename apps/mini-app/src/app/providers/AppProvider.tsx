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
import { LoadingPage } from "@/pages/loading/LoadingPage"
import {
  createAccount,
  joinAccount,
  leaveActiveAccount,
  selectActiveAccount,
} from "@/shared/api/accountApi"
import type { CurrentContextResponse } from "@/shared/api/backendContracts"
import {
  getUserBanDetails,
  isServiceUnavailableError,
  type UserBanDetails,
} from "@/shared/api/httpClient"
import {
  getCurrentContext,
  initializeCurrentUser,
  updateCurrentLanguage,
} from "@/shared/api/userApi"
import { handleError } from "@/shared/errors/errorHandler"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import type { RealtimeEvent } from "@/shared/realtime/realtime.types"
import { userBanEvents } from "@/shared/session/userBanEvents"
import type { LocalizationLanguage } from "@/ui/model"
import { BanScreen } from "@/ui/system/BanScreen"
import { ServiceUnavailableScreen } from "@/ui/system/ServiceUnavailableScreen"

type AppState = {
  context: CurrentContextResponse | null
  loading: boolean
  refresh(): Promise<CurrentContextResponse>
  selectAccount(accountId: string): Promise<void>
  registerAccount(nickname: string): Promise<void>
  joinByCode(inviteCode: string): Promise<void>
  leaveAccount(): Promise<void>
  setLanguage(language: LocalizationLanguage): Promise<void>
}

const AUTOMATIC_RETRY_DELAY_MS = 10_000
const AppContext = createContext<AppState | null>(null)

export function AppProvider({ children }: { children: ReactNode }) {
  const { i18n, t } = useTranslation()
  const [context, setContext] = useState<CurrentContextResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [serviceUnavailable, setServiceUnavailable] = useState(false)
  const [activeBan, setActiveBan] = useState<UserBanDetails | null>(null)
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
      userBanEvents.clear()
      setActiveBan(null)
      setServiceUnavailable(false)
    } catch (error) {
      if (!mountedRef.current) {
        return
      }

      const ban = getUserBanDetails(error)

      if (ban) {
        userBanEvents.publish(ban)
        setServiceUnavailable(false)
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
    return userBanEvents.subscribe((ban) => {
      setActiveBan(ban)
      setServiceUnavailable(false)
      setLoading(false)
    })
  }, [])

  useEffect(() => {
    return realtimeEvents.subscribe((event) => {
      if (event.type === "moderation.sanction.activated" && event.payload.type === "BAN") {
        userBanEvents.publish({
          sanctionId: event.payload.sanctionId,
          reason: event.payload.reason?.trim() ?? "",
          occurredAt: event.occurredAt,
          expiresAt: event.payload.expiresAt ?? null,
        })
        return
      }

      if (event.type === "moderation.sanction.deactivated" && event.payload.type === "BAN") {
        userBanEvents.clear()
        setActiveBan((current) =>
          current?.sanctionId === event.payload.sanctionId ? null : current,
        )
        void bootstrap()
        return
      }

      if (!isAccountLifecycleEvent(event)) {
        return
      }

      void refresh().catch((error) => {
        const ban = getUserBanDetails(error)

        if (ban) {
          userBanEvents.publish(ban)
          return
        }

        handleError(error)
      })
    })
  }, [bootstrap, refresh])

  useEffect(() => {
    if (!serviceUnavailable || activeBan) {
      return
    }

    const retryTimer = window.setTimeout(() => {
      void bootstrap()
    }, AUTOMATIC_RETRY_DELAY_MS)

    return () => {
      window.clearTimeout(retryTimer)
    }
  }, [activeBan, bootstrap, serviceUnavailable])

  const value = useMemo<AppState>(
    () => ({
      context,
      loading,
      refresh,
      selectAccount,
      registerAccount,
      joinByCode,
      leaveAccount,
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
      setLanguage,
    ],
  )

  if (activeBan) {
    return <BanScreen ban={activeBan} onRefresh={bootstrap} />
  }

  if (serviceUnavailable) {
    return <ServiceUnavailableScreen reason="service" onRetry={bootstrap} />
  }

  if (loading || context === null) {
    return <LoadingPage label={t("common.loading")} />
  }

  return <AppContext.Provider value={value}>{children}</AppContext.Provider>
}

function isAccountLifecycleEvent(event: RealtimeEvent): boolean {
  return (
    event.type === "account.activated" ||
    event.type === "account.deactivated" ||
    event.type === "account.deleted" ||
    event.type === "account.owner.transferred" ||
    event.type === "account.member.joined" ||
    event.type === "account.member.left" ||
    event.type === "account.member.removed"
  )
}

export function useApp() {
  const value = useContext(AppContext)

  if (!value) {
    throw new Error("AppProvider is missing")
  }

  return value
}
