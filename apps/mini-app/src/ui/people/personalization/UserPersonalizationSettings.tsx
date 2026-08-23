import { useTranslation } from "react-i18next"
import type { UserBadgesController } from "../../shell/badges/useUserBadges"
import { UserBadgeSelector } from "../../shell/badges/UserBadgeSelector"
import { UserNotificationPackSelector } from "../../shell/personalization/UserNotificationPackSelector"
import { useUserNotificationPacks } from "../../shell/personalization/useUserNotificationPacks"
import { Glyph } from "../../shared/MnemosIcon"
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

  return (
    <div className="screen user-personalization-settings">
      <PageHeader title={t("userPersonalization.title")} back={back} />

      <section className="user-personalization-settings__intro">
        <span className="user-personalization-settings__mark">
          <Glyph name="creator" size={22} />
        </span>
        <span>
          <small>{t("userPersonalization.eyebrow")}</small>
          <strong>{t("userPersonalization.introTitle")}</strong>
          <p>{t("userPersonalization.introDescription")}</p>
        </span>
      </section>

      <section className="user-personalization-settings__summary">
        <article>
          <small>{t("userPersonalization.activeBadge")}</small>
          <strong>{activeBadge?.name ?? t("userPersonalization.none")}</strong>
        </article>
        <article>
          <small>{t("userPersonalization.activePack")}</small>
          <strong>{notificationPacks.activePack?.name ?? t("userPersonalization.none")}</strong>
        </article>
      </section>

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
  )
}
