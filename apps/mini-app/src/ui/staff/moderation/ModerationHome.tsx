import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type { Route } from "../../model"
import { PageHeader } from "../../shared/Primitives"
import "./moderation-directory.css"
import { resolveModerationDirectoryCopy } from "./moderationDirectoryTranslations"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function ModerationHome({ navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const copy = resolveModerationDirectoryCopy(i18n.language)

  return (
    <div className="screen moderation-screen">
      <PageHeader title={t("moderation.title")} back={back} />

      <section className="moderation-hero moderation-hero--illustrated">
        <span className="moderation-hero__art" aria-hidden="true">
          <img src="/ui-illustrations/admin-users.svg" alt="" />
        </span>
        <span>
          <small>{t("moderation.internal")}</small>
          <strong>{t("moderation.heroTitle")}</strong>
          <p>{t("moderation.heroDescription")}</p>
        </span>
      </section>

      <section className="moderation-management-grid" aria-label={copy.searchMode}>
        <motion.button
          type="button"
          whileTap={{ scale: 0.985 }}
          className="moderation-management-card"
          onClick={() => navigate({ id: "moderation-users" })}
        >
          <span className="moderation-management-card__mark moderation-management-card__mark--user">
            <img src="/ui-illustrations/mnemos-user-mark.svg" alt="" aria-hidden="true" />
          </span>
          <span>
            <small>{t("moderation.user")}</small>
            <strong>{copy.userDirectory}</strong>
            <p>{copy.userSearchPlaceholder}</p>
          </span>
          <b aria-hidden="true">›</b>
        </motion.button>

        <motion.button
          type="button"
          whileTap={{ scale: 0.985 }}
          className="moderation-management-card"
          onClick={() => navigate({ id: "moderation-accounts" })}
        >
          <span className="moderation-management-card__mark moderation-management-card__mark--account">
            C
          </span>
          <span>
            <small>{t("moderation.accounts")}</small>
            <strong>{copy.accountDirectory}</strong>
            <p>{copy.accountSearchPlaceholder}</p>
          </span>
          <b aria-hidden="true">›</b>
        </motion.button>
      </section>
    </div>
  )
}
