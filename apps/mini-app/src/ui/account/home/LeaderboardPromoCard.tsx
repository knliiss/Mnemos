import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type { Route } from "../../model"

export function LeaderboardPromoCard({ navigate }: { navigate: (route: Route) => void }) {
  const { t } = useTranslation()

  return (
    <motion.button
      type="button"
      className="account-leaderboard-card"
      whileTap={{ scale: 0.988 }}
      onClick={() => navigate({ id: "leaderboards" })}
    >
      <span className="account-leaderboard-card__copy">
        <small>{t("leaderboards.homeEyebrow")}</small>
        <strong>{t("leaderboards.homeTitle")}</strong>
        <span>{t("leaderboards.homeDescription")}</span>
        <span className="account-leaderboard-card__categories" aria-hidden="true">
          <i>{t("leaderboards.tabs.drops")}</i>
          <i>{t("leaderboards.tabs.coins")}</i>
          <i>{t("leaderboards.tabs.predictors")}</i>
        </span>
      </span>

      <span className="account-leaderboard-card__art" aria-hidden="true">
        <img src="/ui-illustrations/leaderboard-hero.svg" alt="" />
      </span>
    </motion.button>
  )
}
