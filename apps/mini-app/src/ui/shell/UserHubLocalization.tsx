import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type { LocalizationLanguage } from "../model"
import { localizationOptions } from "./navigation"

type UserHubLocalizationProps = {
  localization: LocalizationLanguage
  onChange: (localization: LocalizationLanguage) => void
}

export function UserHubLocalization({ localization, onChange }: UserHubLocalizationProps) {
  const { t } = useTranslation()
  const activeOption =
    localizationOptions.find((option) => option.value === localization) ?? localizationOptions[0]

  return (
    <section className="user-hub-localization" aria-label={t("appShell.userMenu.languageAria")}>
      <span className="user-hub-localization__identity">
        <span className="user-hub-localization__mark" aria-hidden="true" />
        <span className="user-hub-localization__copy">
          <small>{t("appShell.userMenu.language")}</small>
          <strong>{activeOption.label}</strong>
        </span>
      </span>

      <span
        className="user-hub-localization__choices"
        role="group"
        aria-label={t("appShell.userMenu.chooseLanguage")}
      >
        {localizationOptions.map((option) => (
          <motion.button
            key={option.value}
            type="button"
            className={option.value === localization ? "is-active" : ""}
            aria-pressed={option.value === localization}
            aria-label={option.label}
            whileTap={{ scale: 0.93 }}
            onClick={() => onChange(option.value)}
          >
            {option.short}
          </motion.button>
        ))}
      </span>
    </section>
  )
}
