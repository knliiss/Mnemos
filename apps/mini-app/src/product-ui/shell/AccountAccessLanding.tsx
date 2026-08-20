import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { AccountArea } from "../features/account/AccountArea"
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
        navigate={navigate}
        back={back}
      />
    )
  }

  return (
    <div className="screen account-onboarding account-onboarding--create">
      <header className="account-onboarding-header account-access-landing__header">
        <div className="account-onboarding-heading">
          <small>MNEMOS × CRISTALIX</small>
          <h1>{t("productShell.onboarding.title")}</h1>
          <p>{t("productShell.onboarding.description")}</p>
        </div>
      </header>

      <section className="account-onboarding-hero account-access-landing__hero">
        <div className="account-onboarding-hero__art">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
        </div>
        <div className="account-onboarding-hero__copy">
          <small>{t("productShell.onboarding.start")}</small>
          <strong>{t("productShell.onboarding.mascotTitle")}</strong>
          <p>{t("productShell.onboarding.mascotDescription")}</p>
        </div>
      </section>

      <div className="account-onboarding-facts account-access-landing__actions">
        <motion.button
          type="button"
          whileTap={{ scale: 0.98 }}
          onClick={() => navigate({ id: "account-create" })}
        >
          <span>+</span>
          <div>
            <strong>{t("productShell.onboarding.newAccount")}</strong>
            <small>{t("productShell.onboarding.findNickname")}</small>
          </div>
        </motion.button>

        <motion.button
          type="button"
          whileTap={{ scale: 0.98 }}
          onClick={() => navigate({ id: "account-join" })}
        >
          <span>→</span>
          <div>
            <strong>{t("productShell.onboarding.joinByCode")}</strong>
            <small>{t("productShell.onboarding.joinTeam")}</small>
          </div>
        </motion.button>
      </div>
    </div>
  )
}
