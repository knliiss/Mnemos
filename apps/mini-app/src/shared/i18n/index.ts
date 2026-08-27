import i18n from "i18next"
import { initReactI18next } from "react-i18next"
import { resolveBootstrapLanguage } from "@/shared/i18n/bootstrapLanguage"
import { en } from "./locales/en"
import { playerDirectoryEn, playerDirectoryRu, playerDirectoryUk } from "./locales/playerDirectory"
import { systemEn } from "./locales/system/en"
import { systemRu } from "./locales/system/ru"
import { systemUk } from "./locales/system/uk"
import { ru } from "./locales/ru"
import { uk } from "./locales/uk"

const enTranslation = {
  ...en,
  ...systemEn,
  events: {
    ...en.events,
    subscriptions: {
      ...en.events.subscriptions,
      ...playerDirectoryEn.events.subscriptions,
    },
  },
  creator: {
    ...en.creator,
    editor: {
      ...en.creator.editor,
      notificationTypes: {
        ...en.creator.editor.notificationTypes,
        ...playerDirectoryEn.creator.editor.notificationTypes,
      },
    },
  },
}

const ruTranslation = {
  ...ru,
  ...systemRu,
  events: {
    ...ru.events,
    subscriptions: {
      ...ru.events.subscriptions,
      ...playerDirectoryRu.events.subscriptions,
    },
  },
  creator: {
    ...ru.creator,
    editor: {
      ...ru.creator.editor,
      notificationTypes: {
        ...ru.creator.editor.notificationTypes,
        ...playerDirectoryRu.creator.editor.notificationTypes,
      },
    },
  },
}

const ukTranslation = {
  ...uk,
  ...systemUk,
  events: {
    ...uk.events,
    subscriptions: {
      ...uk.events.subscriptions,
      ...playerDirectoryUk.events.subscriptions,
    },
  },
  creator: {
    ...uk.creator,
    editor: {
      ...uk.creator.editor,
      notificationTypes: {
        ...uk.creator.editor.notificationTypes,
        ...playerDirectoryUk.creator.editor.notificationTypes,
      },
    },
  },
}

void i18n.use(initReactI18next).init({
  lng: resolveBootstrapLanguage(),
  fallbackLng: "ru",
  resources: {
    en: { translation: enTranslation },
    ru: { translation: ruTranslation },
    uk: { translation: ukTranslation },
  },
  interpolation: {
    escapeValue: false,
  },
})

export { i18n }
