import { useTranslation } from "react-i18next"
import type { Route } from "../../model"

export function LeaderboardPromoCard({ navigate }: { navigate: (route: Route) => void }) {
  const { t } = useTranslation()

  return (
    <button
      type="button"
      className="account-leaderboard-card"
      onClick={() => navigate({ id: "leaderboards" })}
    >
      <span className="account-leaderboard-card__art" aria-hidden="true">
        <img src="/ui-illustrations/leaderboard-hero.svg" alt="" />
      </span>
      <span className="account-leaderboard-card__copy">
        <small>{t("leaderboards.homeEyebrow")}</small>
        <strong>{t("leaderboards.homeTitle")}</strong>
        <span>{t("leaderboards.homeDescription")}</span>
      </span>
      <b>{t("leaderboards.open")}</b>
    </button>
  )
}
