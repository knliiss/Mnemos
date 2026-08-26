import { useTranslation } from "react-i18next"
import type { UserBadgesController } from "../../shell/badges/useUserBadges"
import { UserBadgeSelector } from "../../shell/badges/UserBadgeSelector"
import { UserNotificationPackSelector } from "../../shell/personalization/UserNotificationPackSelector"
import { useUserNotificationPacks } from "../../shell/personalization/useUserNotificationPacks"
import { PageHeader } from "../../shared/Primitives"
import "./user-personalization-settings.css"

type Props = {
  badges: UserBadgesController
  back: () => void
}

export function UserPersonalizationSettings({ badges, back }: Props) {
  const { t } = useTranslation()
  const notificationPacks = useUserNotificationPacks(true)
  const activeBadge = badges.activeBadge?.badge ?? null
  const activePack = notificationPacks.activePack

  return (
    <div className="screen user-personalization-settings">
      <PageHeader title={t("userPersonalization.title")} back={back} />

      <section className="user-personalization-settings__hero">
        <div className="user-personalization-settings__hero-copy">
          <small>{t("userPersonalization.eyebrow")}</small>
          <strong>{t("userPersonalization.introTitle")}</strong>
          <p>{t("userPersonalization.introDescription")}</p>

          <div className="user-personalization-settings__active-row">
            <span>
              <small>{t("userPersonalization.activeBadge")}</small>
              <b>{activeBadge?.name ?? t("userPersonalization.none")}</b>
            </span>
            <span>
              <small>{t("userPersonalization.activePack")}</small>
              <b>{activePack?.name ?? t("userPersonalization.none")}</b>
            </span>
          </div>
        </div>

        <div className="user-personalization-settings__hero-art" aria-hidden="true">
          <span className="is-badge">
            <img src="/ui-illustrations/admin-badges.svg" alt="" />
          </span>
          <span className="is-pack">
            <img src="/ui-illustrations/creator-notification.svg" alt="" />
          </span>
        </div>
      </section>

      <div className="user-personalization-settings__sections">
        <UserBadgeSelector
          badges={badges.badges}
          loading={badges.loading}
          busyBadgeId={badges.busyBadgeId}
          onActivate={(badgeId) => void badges.activate(badgeId)}
          onDeactivate={() => void badges.deactivate()}
        />

        <UserNotificationPackSelector
          packs={notificationPacks.packs}
          loading={notificationPacks.loading}
          busyPackId={notificationPacks.busyPackId}
          onActivate={(personalizationId) => void notificationPacks.activate(personalizationId)}
          onDeactivate={() => void notificationPacks.deactivate()}
        />
      </div>
    </div>
  )
}
