import { useEffect, type ReactNode } from "react"
import { i18n } from "@/shared/i18n"
import { useMnemosData } from "./MnemosDataProvider"

const languageCodes = {
  UK: "uk",
  RU: "ru",
  EN: "en",
} as const

export function LanguageSync({ children }: { children: ReactNode }) {
  const data = useMnemosData()
  const language = languageCodes[data.user.language]

  useEffect(() => {
    document.documentElement.lang = language
    void i18n.changeLanguage(language)
  }, [language])

  return children
}
