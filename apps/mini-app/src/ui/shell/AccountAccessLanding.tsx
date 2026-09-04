import { motion } from "framer-motion"
import { useState } from "react"
import { useTranslation } from "react-i18next"
import "../account/onboarding/account-onboarding.css"
import "../account/onboarding/account-onboarding-layout.css"
import "../account/onboarding/account-access-landing.css"
import { AccountArea } from "../account/AccountArea"
import { useMnemosData } from "../data/MnemosDataProvider"
import type { Account, Route } from "../model"

type Props = {
  route: Route
  navigate: (route: Route) => void
  back: () => void
}

export function AccountAccessLanding({ route, navigate, back }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [switchingAccountId, setSwitchingAccountId] = useState<string | null>(null)

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

  const selectAccount = async (account: Account) => {
    if (account.active === false || switchingAccountId) {
      return
    }

    setSwitchingAccountId(account.id)

    try {
      await data.selectAccount(account)
    } finally {
      setSwitchingAccountId(null)
    }
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

      {data.accounts.length > 0 ? (
        <section
          className="account-access-landing__existing"
          aria-label={t("appShell.onboarding.title")}
        >
          <div className="account-access-landing__existing-heading">
            <span>
              <small>{t("appShell.userMenu.activeAccount")}</small>
              <strong>{t("appShell.onboarding.title")}</strong>
            </span>
            <em>{data.accounts.length}</em>
          </div>

          <div className="account-access-landing__account-list">
            {data.accounts.map((account) => {
              const switching = switchingAccountId === account.id
              const selectable = account.active !== false

              return (
                <motion.button
                  key={account.id}
                  type="button"
                  className="account-access-landing__account"
                  whileTap={selectable ? { scale: 0.985 } : undefined}
                  disabled={!selectable || Boolean(switchingAccountId)}
                  onClick={() => void selectAccount(account)}
                >
                  <span className="account-access-landing__skin" aria-hidden="true">
                    <img src={account.skinUrl} alt="" />
                  </span>

                  <span className="account-access-landing__account-copy">
                    <strong>{account.nickname}</strong>
                    <small>
                      {account.owner ? t("accountSettings.owner") : t("accountSettings.member")}
                      {" · "}
                      {t("accountSettings.members")} {account.memberCount}/{account.maxMembers}
                    </small>
                  </span>

                  <span
                    className={`account-access-landing__account-state ${
                      selectable ? "is-active" : "is-inactive"
                    }`}
                  >
                    {switching
                      ? t("appShell.userMenu.switching")
                      : selectable
                        ? "→"
                        : t("appShell.userMenu.inactive")}
                  </span>
                </motion.button>
              )
            })}
          </div>
        </section>
      ) : (
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
      )}

      <div className="account-onboarding-facts account-access-landing__actions">
        <motion.button
          type="button"
          whileTap={{ scale: 0.98 }}
          onClick={() => navigate({ id: "account-create" })}
        >
          <span className="account-access-landing__action-art" aria-hidden="true">
            <img src="/ui-illustrations/account-create.svg" alt="" />
          </span>
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
          <span className="account-access-landing__action-art" aria-hidden="true">
            <img src="/ui-illustrations/account-join.svg" alt="" />
          </span>
          <div>
            <strong>{t("appShell.onboarding.joinByCode")}</strong>
            <small>{t("appShell.onboarding.joinTeam")}</small>
          </div>
        </motion.button>
      </div>
    </div>
  )
}
