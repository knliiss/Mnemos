import { useTranslation } from "react-i18next"
import type { PublicProfileResponse } from "@/shared/api/profileSocialApi"
import { usePresenceSubscription } from "./usePresenceSubscription"
import {
  canSubscribeToPresence,
  formatPresenceFreshness,
  formatPresenceTimestamp,
  presenceStatusClassName,
} from "./presencePresentation"
import "./presence.css"

type Props = {
  profile: PublicProfileResponse
}

export function PublicProfilePresencePanel({ profile }: Props) {
  const { t, i18n } = useTranslation()
  const subscription = usePresenceSubscription(profile.cristalixUuid)
  const presence = profile.presence
  const canSubscribe = canSubscribeToPresence(presence.status)
  const lastSeen = formatPresenceTimestamp(presence.lastSeenOnline, i18n.language)
  const freshness = formatPresenceFreshness(presence.observedAt)

  return (
    <section className="public-profile-presence-panel">
      <div className="public-profile-presence-panel__state">
        <span className={presenceStatusClassName(presence.status)}>
          <i />
          {t(`people.presence.status.${presence.status}`)}
        </span>

        <span>
          <small>{t("people.presence.currentRealm")}</small>
          <strong>{presence.status === "ONLINE" && presence.realm ? presence.realm : "—"}</strong>
        </span>

        <span>
          <small>{t("people.presence.lastSeen")}</small>
          <strong>{lastSeen ?? t("people.presence.unavailable")}</strong>
        </span>
      </div>

      <div className="public-profile-presence-panel__footer">
        <small>
          {freshness === null
            ? t("people.presence.notVerified")
            : t("people.presence.verifiedAgo", { seconds: freshness })}
        </small>

        <button
          type="button"
          disabled={!canSubscribe || subscription.loading || subscription.busy}
          onClick={() => void subscription.toggle()}
        >
          {subscription.busy
            ? t("people.presence.saving")
            : subscription.subscribed
              ? t("people.presence.unsubscribe")
              : t("people.presence.subscribe")}
        </button>
      </div>

      {!canSubscribe ? (
        <p>
          {presence.status === "HIDDEN"
            ? t("people.presence.hiddenSubscription")
            : t("people.presence.unverifiedSubscription")}
        </p>
      ) : null}
    </section>
  )
}
