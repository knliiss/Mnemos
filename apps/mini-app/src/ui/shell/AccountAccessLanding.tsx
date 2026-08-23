import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import "../account/onboarding/account-onboarding.css"
import "../account/onboarding/account-onboarding-layout.css"
import { AccountArea } from "../account/AccountArea"
import type { Route } from "../model"

type Props = {
  route: Route
  navigate: (route: Route) => void
  back: () => void
}

export function AccountAccessLanding({ route, navigate, back }: Props) {
  const { t } = useTranslation()

  if (route.id === "account-create" || route.id === "account-join") {
    return (
      <AccountArea
        routeId={route.id}
        params={route.params}
        activeUserBadge={null}
        navigate={navigate}
        back={back}
      />
    )
  }

  return (
    <div className="screen account-onboarding account-onboarding--create account-access-landing">
      <header className="account-onboarding-header account-access-landing__header">
        <div className="account-onboarding-heading">
          <small>MNEMOS × CRISTALIX</small>
          <h1>{t("appShell.onboarding.title")}</h1>
          <p>{t("appShell.onboarding.description")}</p>
        </div>
      </header>

      <section className="account-onboarding-hero account-access-landing__hero">
        <div className="account-onboarding-hero__art account-onboarding-hero-art">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
        </div>
        <div className="account-onboarding-hero__copy">
          <small>{t("appShell.onboarding.start")}</small>
          <strong>{t("appShell.onboarding.mascotTitle")}</strong>
          <p>{t("appShell.onboarding.mascotDescription")}</p>
        </div>
      </section>

      <div className="account-onboarding-facts account-access-landing__actions">
        <motion.button
          type="button"
          whileTap={{ scale: 0.98 }}
          onClick={() => navigate({ id: "account-create" })}
        >
          <span aria-hidden="true">+</span>
          <div>
            <strong>{t("appShell.onboarding.newAccount")}</strong>
            <small>{t("appShell.onboarding.findNickname")}</small>
          </div>
        </motion.button>

        <motion.button
          type="button"
          whileTap={{ scale: 0.98 }}
          onClick={() => navigate({ id: "account-join" })}
        >
          <span aria-hidden="true">→</span>
          <div>
            <strong>{t("appShell.onboarding.joinByCode")}</strong>
            <small>{t("appShell.onboarding.joinTeam")}</small>
          </div>
        </motion.button>
      </div>
    </div>
  )
}
