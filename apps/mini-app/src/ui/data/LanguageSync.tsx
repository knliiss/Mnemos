import { useEffect, type ReactNode } from "react"
import { i18n } from "@/shared/i18n"
import {
  saoLanguageConfig,
  saoLocalizationRefreshIntervalMs,
  synchronizeSaoLocalization,
} from "@/shared/i18n/saoLocalization"
import { useMnemosData } from "./MnemosDataProvider"

export function LanguageSync({ children }: { children: ReactNode }) {
  const data = useMnemosData()
  const language = data.user.language

  useEffect(() => {
    const config = saoLanguageConfig(language)
    let cancelled = false

    document.documentElement.lang = config.i18nLanguage
    void i18n.changeLanguage(config.i18nLanguage)

    const synchronize = async () => {
      const changed = await synchronizeSaoLocalization(language)

      if (!cancelled && changed) {
        await i18n.changeLanguage(config.i18nLanguage)
      }
    }

    void synchronize()
    const interval = window.setInterval(() => {
      void synchronize()
    }, saoLocalizationRefreshIntervalMs())

    return () => {
      cancelled = true
      window.clearInterval(interval)
    }
  }, [language])

  return children
}
