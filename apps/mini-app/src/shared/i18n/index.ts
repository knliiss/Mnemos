import i18n from "i18next"
import { initReactI18next } from "react-i18next"
import { en } from "./locales/en"
import { ru } from "./locales/ru"
import { uk } from "./locales/uk"

void i18n
  .use(initReactI18next)
  .init({
    resources: {
      en: { translation: en },
      ru: { translation: ru },
      uk: { translation: uk },
    },
    fallbackLng: "uk",
    interpolation: {
      escapeValue: false,
    },
  })

export { i18n }
