import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { CristalixHead } from "../../components/profile/CristalixHead"
import type { Account, Route } from "../../model"
import { CurrencyMark, Glyph } from "../../shared/MnemosIcon"
import { RoleSigil } from "../../shared/RoleSigil"
import { AccountDailyReward } from "../AccountDailyReward"

type AccountOverviewProps = {
  account: Account
  locale: string
  navigate: (route: Route) => void
}

export function AccountOverview({ account, locale, navigate }: AccountOverviewProps) {
  const { t } = useTranslation()

  return (
    <>
      <motion.button
        type="button"
        className="account-home-profile"
        whileTap={{ scale: 0.992 }}
        onClick={() => navigate({ id: "public-profile", params: { accountId: account.id } })}
      >
        <span className="account-home-profile__head">
          <CristalixHead skinUrl={account.skinUrl} size={82} />
        </span>

        <span className="account-home-profile__copy">
          <small>{t("accountHome.activeAccount")}</small>
          <strong>{account.nickname}</strong>
          <span className="account-home-profile__pulse" aria-hidden="true">
            <i />
            <i />
            <i />
          </span>
        </span>

        <img
          className="account-home-profile__art"
          src="/ui-illustrations/mnemos-mascot-cat.svg"
          alt=""
          aria-hidden="true"
        />
      </motion.button>

      <AccountDailyReward />

      <section className="account-home-feature-grid">
        <motion.button
          type="button"
          className="account-home-wallet"
          whileTap={{ scale: 0.985 }}
          onClick={() => navigate({ id: "account-wallet" })}
        >
          <span className="account-home-feature-copy">
            <small>{t("accountHome.wallet")}</small>
            <strong>
              <CurrencyMark currency="COIN" size={19} />
              {account.coin.toLocaleString(locale)}
            </strong>
            <span>COIN</span>
          </span>
          <img src="/ui-illustrations/coin-vault.png" alt="" aria-hidden="true" />
        </motion.button>

        <motion.button
          type="button"
          className="account-home-personalization"
          whileTap={{ scale: 0.985 }}
          onClick={() => navigate({ id: "personalization-owned", params: { target: "ACCOUNT" } })}
        >
          <span className="account-home-feature-copy">
            <small>{t("accountHome.appearance")}</small>
            <strong>{t("accountHome.personalization")}</strong>
            <span>{t("accountHome.personalizationSummary")}</span>
          </span>
          <img src="/ui-illustrations/personalization-studio.svg" alt="" aria-hidden="true" />
        </motion.button>
      </section>

      <nav className="account-home-actions" aria-label={t("accountHome.managementAria")}>
        <motion.button
          type="button"
          whileTap={{ scale: 0.97 }}
          onClick={() => navigate({ id: "roles" })}
        >
          <RoleSigil color="#A985A8" size={27} compact />
          <span>
            <strong>{t("accountHome.roles")}</strong>
            <small>{t("accountHome.accessRights")}</small>
          </span>
        </motion.button>

        <motion.button
          type="button"
          whileTap={{ scale: 0.97 }}
          onClick={() => navigate({ id: "account-invites" })}
        >
          <span className="account-home-actions__icon is-amber">
            <Glyph name="invite" size={17} />
          </span>
          <span>
            <strong>{t("accountHome.invites")}</strong>
            <small>{t("accountHome.invitations")}</small>
          </span>
        </motion.button>

        <motion.button
          type="button"
          whileTap={{ scale: 0.97 }}
          onClick={() => navigate({ id: "account-settings" })}
        >
          <span className="account-home-actions__icon">
            <Glyph name="settings" size={17} />
          </span>
          <span>
            <strong>{t("accountHome.settings")}</strong>
            <small>{t("accountHome.account")}</small>
          </span>
        </motion.button>
      </nav>
    </>
  )
}
