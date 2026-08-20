import { useEffect, type ReactNode } from "react"
import { i18n } from "@/shared/i18n"
import { useProductData } from "./ProductDataProvider"

const languageCodes = {
  UK: "uk",
  RU: "ru",
  EN: "en",
} as const

export function LanguageSync({ children }: { children: ReactNode }) {
  const product = useProductData()
  const language = languageCodes[product.user.language]

  useEffect(() => {
    document.documentElement.lang = language
    void i18n.changeLanguage(language)
  }, [language])

  return children
}
