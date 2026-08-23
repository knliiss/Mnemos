import i18n from "i18next"
import { initReactI18next } from "react-i18next"
import { resolveBootstrapLanguage } from "@/shared/i18n/bootstrapLanguage"
import { en } from "./locales/en"
import { systemEn } from "./locales/system/en"
import { systemRu } from "./locales/system/ru"
import { systemUk } from "./locales/system/uk"
import { ru } from "./locales/ru"
import { uk } from "./locales/uk"

void i18n.use(initReactI18next).init({
  lng: resolveBootstrapLanguage(),
  fallbackLng: "ru",
  resources: {
    en: { translation: { ...en, ...systemEn } },
    ru: { translation: { ...ru, ...systemRu } },
    uk: { translation: { ...uk, ...systemUk } },
  },
  interpolation: {
    escapeValue: false,
  },
})

export { i18n }
