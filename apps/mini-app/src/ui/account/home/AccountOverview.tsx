import { motion } from "framer-motion"
import type { ReactNode } from "react"
import { useTranslation } from "react-i18next"
import { PersonalizationVisualLayers } from "../../components/personalization/PersonalizationVisualLayers"
import { CristalixHead } from "../../components/profile/CristalixHead"
import type { Account, Route } from "../../model"
import {
  nicknameStyleForAppearance,
  profileCardStyleForAppearance,
  profileHeadStyleForAppearance,
} from "../../people/profile/profilePresentation"
import { CurrencyMark } from "../../shared/MnemosIcon"
import { RoleSigil } from "../../shared/RoleSigil"
import { AccountDailyReward } from "../AccountDailyReward"
import { CurrentGlobalEventsPanel } from "./CurrentGlobalEventsPanel"
import { LeaderboardPromoCard } from "./LeaderboardPromoCard"
import { useAccountHomeAppearance } from "./useAccountHomeAppearance"
import "./account-home-personalization.css"
import "./current-global-events.css"

type AccountOverviewProps = {
  account: Account
  locale: string
  userHubTrigger?: ReactNode
  navigate: (route: Route) => void
}

export function AccountOverview({
  account,
  locale,
  userHubTrigger,
  navigate,
}: AccountOverviewProps) {
  const { t } = useTranslation()
  const appearance = useAccountHomeAppearance(account.id)
  const cardLayers = [
    ...(appearance?.profileTheme?.layers ?? []),
    ...(appearance?.profileEffect?.layers ?? []),
  ]
  const frameLayers = appearance?.avatarFrame?.layers ?? []
  const profileClassName = [
    "account-home-profile",
    appearance?.profileTheme ? "has-profile-theme" : "",
    appearance?.profileEffect ? "has-profile-effect" : "",
    appearance?.profileEffect
      ? `account-home-effect--${appearance.profileEffect.effect.toLowerCase()}`
      : "",
  ]
    .filter(Boolean)
    .join(" ")
  const headClassName = [
    "account-home-profile__head",
    appearance?.avatarFrame ? "has-avatar-frame" : "",
    appearance?.avatarFrame
      ? `account-home-frame--${appearance.avatarFrame.style.toLowerCase()}`
      : "",
  ]
    .filter(Boolean)
    .join(" ")

  return (
    <>
      <section
        className={profileClassName}
        style={appearance ? profileCardStyleForAppearance(appearance) : undefined}
      >
        <PersonalizationVisualLayers
          layers={cardLayers}
          className="account-home-profile__appearance-layers"
        />

        {appearance?.profileEffect ? (
          <span className="account-home-profile__effect" aria-hidden="true">
            <i />
            <i />
            <i />
            <i />
            <i />
            <i />
          </span>
        ) : null}

        <motion.button
          type="button"
          className="account-home-profile__account"
          whileTap={{ scale: 0.992 }}
          onClick={() => navigate({ id: "public-profile", params: { accountId: account.id } })}
        >
          <span
            className={headClassName}
            style={appearance ? profileHeadStyleForAppearance(appearance) : undefined}
          >
            <CristalixHead skinUrl={account.skinUrl} size={82} />
            <PersonalizationVisualLayers
              layers={frameLayers}
              className="account-home-profile__frame-layers"
            />
          </span>

          <span className="account-home-profile__copy">
            <small>{t("accountHome.activeAccount")}</small>
            <strong style={appearance ? nicknameStyleForAppearance(appearance) : undefined}>
              {account.nickname}
            </strong>
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

        {userHubTrigger ? (
          <div className="account-home-profile__user-access">{userHubTrigger}</div>
        ) : null}
      </section>

      <CurrentGlobalEventsPanel navigate={navigate} />

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
            <strong className="account-home-balance">
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

      <LeaderboardPromoCard navigate={navigate} />

      <nav className="account-home-actions" aria-label={t("accountHome.managementAria")}>
        <motion.button
          type="button"
          whileTap={{ scale: 0.97 }}
          onClick={() => navigate({ id: "roles" })}
        >
          <span className="account-home-actions__icon account-home-actions__icon--role">
            <RoleSigil color="#A985A8" size={27} compact />
          </span>
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
          <span className="account-home-actions__icon account-home-actions__icon--illustrated">
            <img src="/ui-illustrations/account-invites.svg" alt="" aria-hidden="true" />
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
          <span className="account-home-actions__icon account-home-actions__icon--illustrated">
            <img src="/ui-illustrations/account-settings.svg" alt="" aria-hidden="true" />
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
