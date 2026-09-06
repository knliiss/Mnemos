import { motion } from "framer-motion"
import type { ReactNode } from "react"
import { useTranslation } from "react-i18next"
import { PersonalizationVisualLayers } from "../../components/personalization/PersonalizationVisualLayers"
import { CristalixHead } from "../../components/profile/CristalixHead"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { Account, Route } from "../../model"
import {
  nicknameStyleForAppearance,
  profileCardStyleForAppearance,
  profileHeadStyleForAppearance,
} from "../../people/profile/profilePresentation"
import { AccountDailyReward } from "../AccountDailyReward"
import { AccountQuickActions } from "./AccountQuickActions"
import { AccountWalletCard } from "./AccountWalletCard"
import { CurrentGlobalEventsPanel } from "./CurrentGlobalEventsPanel"
import { useAccountHomeAppearance } from "./useAccountHomeAppearance"
import "./account-home-personalization.css"
import "./current-global-events.css"

type AccountOverviewProps = {
  account: Account
  locale: string
  userHubTrigger?: ReactNode
  navigate: (route: Route) => void
  canOpenGuides: boolean
  openGuides: () => void
}

export function AccountOverview({
  account,
  locale,
  userHubTrigger,
  navigate,
  canOpenGuides,
  openGuides,
}: AccountOverviewProps) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const appearance = useAccountHomeAppearance(account.id)
  const reward = data.dailyReward
  const streak = reward?.currentStreak || reward?.recoverableStreak || 0
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
            <CristalixHead skinUrl={account.skinUrl} size={76} />
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

            <span className="account-home-profile__meta">
              <span className="account-home-profile__pulse" aria-hidden="true">
                <i />
                <i />
                <i />
              </span>

              {reward?.claimedToday ? (
                <span className="account-home-profile__streak">
                  <motion.img
                    src="/ui-icons/flame.png"
                    alt=""
                    aria-hidden="true"
                    animate={{ y: [0, -1.5, 0], scale: [1, 1.04, 1] }}
                    transition={{ duration: 1.8, repeat: Infinity, ease: "easeInOut" }}
                  />
                  <strong>{streak}</strong>
                  <small>{t("accountHome.streakCompact")}</small>
                </span>
              ) : null}
            </span>
          </span>
        </motion.button>

        {userHubTrigger ? (
          <div className="account-home-profile__user-access">{userHubTrigger}</div>
        ) : null}
      </section>

      <CurrentGlobalEventsPanel navigate={navigate} />

      <AccountDailyReward />

      <AccountWalletCard account={account} locale={locale} navigate={navigate} />

      <AccountQuickActions
        navigate={navigate}
        canOpenGuides={canOpenGuides}
        openGuides={openGuides}
      />
    </>
  )
}
