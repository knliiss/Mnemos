import {useEffect, useState, type ReactNode} from "react"
import {useTranslation} from "react-i18next"
import {initializeAuth} from "@/features/auth/api/auth"
import {handleError} from "@/shared/errors/errorHandler"
import {LoadingPage} from "@/pages/loading/LoadingPage"

export function AuthGate({children}: {children: ReactNode}) {
    const {t} = useTranslation()
    const [state, setState] = useState<"loading" | "ready" | "telegram" | "error">("loading")
    useEffect(() => {
        initializeAuth().then(() => setState("ready")).catch(error => {
            if (error instanceof Error && error.message === "TELEGRAM_REQUIRED") setState("telegram")
            else { handleError(error); setState("error") }
        })
    }, [])
    if (state === "ready") return children
    if (state === "telegram") return <LoadingPage label={t("auth.telegramOnly")}/>
    if (state === "error") return <LoadingPage label={t("auth.error")}/>
    return <LoadingPage label={t("auth.authorizing")}/>
}
