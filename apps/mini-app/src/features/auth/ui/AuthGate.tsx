import { type ReactNode, useCallback, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  initializeAuth,
  retryAuth,
  subscribeAuthRuntimeState,
} from "@/features/auth/api/auth"
import { isAuthError } from "@/features/auth/api/authErrors"
import { LoadingPage } from "@/pages/loading/LoadingPage"
import { handleError } from "@/shared/errors/errorHandler"
import { ServiceUnavailableScreen } from "@/ui/system/ServiceUnavailableScreen"

type AuthState = "loading" | "ready" | "telegram" | "unavailable" | "session-expired" | "error"

const AUTOMATIC_RETRY_DELAY_MS = 10_000

export function AuthGate({ children }: { children: ReactNode }) {
  const { t } = useTranslation()
  const [state, setState] = useState<AuthState>("loading")

  const startAuthentication = useCallback(async () => {
    try {
      await initializeAuth()
      setState("ready")
    } catch (error) {
      applyAuthFailure(error, setState)
    }
  }, [])

  const retryAuthentication = useCallback(async () => {
    try {
      await retryAuth()
      setState("ready")
    } catch (error) {
      applyAuthFailure(error, setState)
    }
  }, [])

  useEffect(() => {
    const unsubscribe = subscribeAuthRuntimeState((runtimeState) => {
      if (runtimeState === "ready") {
        setState("ready")
      } else if (runtimeState === "unavailable") {
        setState("unavailable")
      } else {
        setState("session-expired")
      }
    })
    const frame = window.requestAnimationFrame(() => {
      void startAuthentication()
    })

    return () => {
      window.cancelAnimationFrame(frame)
      unsubscribe()
    }
  }, [startAuthentication])

  useEffect(() => {
    if (state !== "unavailable") {
      return
    }

    const retryTimer = window.setTimeout(() => {
      void retryAuthentication()
    }, AUTOMATIC_RETRY_DELAY_MS)

    return () => {
      window.clearTimeout(retryTimer)
    }
  }, [retryAuthentication, state])

  if (state === "ready") {
    return children
  }

  if (state === "telegram") {
    return <LoadingPage label={t("auth.telegramOnly")} />
  }

  if (state === "unavailable") {
    return <ServiceUnavailableScreen reason="service" onRetry={retryAuthentication} />
  }

  if (state === "session-expired") {
    return <ServiceUnavailableScreen reason="session" onRetry={retryAuthentication} />
  }

  if (state === "error") {
    return <ServiceUnavailableScreen reason="auth" onRetry={retryAuthentication} />
  }

  return <LoadingPage label={t("auth.authorizing")} />
}

function applyAuthFailure(
  error: unknown,
  setState: (state: AuthState) => void,
): void {
  if (isAuthError(error, "TELEGRAM_REQUIRED")) {
    setState("telegram")
    return
  }

  if (isAuthError(error, "AUTH_UNAVAILABLE")) {
    setState("unavailable")
    return
  }

  if (isAuthError(error, "TELEGRAM_SESSION_EXPIRED")) {
    setState("session-expired")
    return
  }

  handleError(error)
  setState("error")
}
