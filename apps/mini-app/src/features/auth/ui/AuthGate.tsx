import { type ReactNode, useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { initializeAuth } from "@/features/auth/api/auth"
import { LoadingPage } from "@/pages/loading/LoadingPage"
import { handleError } from "@/shared/errors/errorHandler"

type AuthState = "loading" | "ready" | "telegram" | "error"

export function AuthGate({ children }: { children: ReactNode }) {
  const { t } = useTranslation()
  const [state, setState] = useState<AuthState>("loading")

  useEffect(() => {
    void initializeAuth()
      .then(() => {
        setState("ready")
      })
      .catch((error) => {
        if (error instanceof Error && error.message === "TELEGRAM_REQUIRED") {
          setState("telegram")
          return
        }

        handleError(error)
        setState("error")
      })
  }, [])

  if (state === "ready") {
    return children
  }

  if (state === "telegram") {
    return <LoadingPage label={t("auth.telegramOnly")} />
  }

  if (state === "error") {
    return <LoadingPage label={t("auth.error")} />
  }

  return <LoadingPage label={t("auth.authorizing")} />
}
