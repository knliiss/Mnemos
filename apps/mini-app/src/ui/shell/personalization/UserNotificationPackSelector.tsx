import { useTranslation } from "react-i18next"
import type { UserNotificationPack } from "./useUserNotificationPacks"
import "./user-notification-packs.css"

type UserNotificationPackSelectorProps = {
  packs: UserNotificationPack[]
  loading: boolean
  busyPackId: string | null
  onActivate: (personalizationId: string) => void
  onDeactivate: () => void
}

export function UserNotificationPackSelector({
  packs,
  loading,
  busyPackId,
  onActivate,
  onDeactivate,
}: UserNotificationPackSelectorProps) {
  const { t } = useTranslation()
  const activePack = packs.find((pack) => pack.active) ?? null

  if (loading) {
    return (
      <section className="user-hub-notification-packs" aria-busy="true">
        <header>
          <span>
            <small>{t("personalization.types.NOTIFICATION_PACK.title")}</small>
            <strong>{t("common.loading")}</strong>
          </span>
        </header>
      </section>
    )
  }

  return (
    <section className="user-hub-notification-packs">
      <header>
        <span>
          <small>{t("personalization.types.NOTIFICATION_PACK.title")}</small>
          <strong>{activePack?.name ?? t("personalization.owned.notSelected")}</strong>
        </span>

        {activePack ? (
          <button
            type="button"
            className="user-hub-notification-packs__clear"
            disabled={busyPackId !== null}
            onClick={onDeactivate}
          >
            {t("personalization.detail.disable")}
          </button>
        ) : null}
      </header>

      {packs.length ? (
        <div className="user-hub-notification-packs__grid">
          {packs.map((pack) => {
            const active = pack.active
            const busy = busyPackId === pack.id
            const statusLabel = busy
              ? t("personalization.detail.saving")
              : active
                ? t("personalization.owned.active")
                : t("personalization.detail.enable")

            return (
              <button
                type="button"
                key={pack.id}
                className={`user-hub-notification-pack-option ${active ? "is-active" : ""}`}
                disabled={busyPackId !== null}
                onClick={() => {
                  if (!active) {
                    onActivate(pack.id)
                  }
                }}
              >
                <span className="user-hub-notification-pack-option__mark" aria-hidden="true">
                  <img src="/ui-illustrations/creator-notification.svg" alt="" />
                </span>
                <span>
                  <strong>{pack.name}</strong>
                  <small>{statusLabel}</small>
                </span>
              </button>
            )
          })}
        </div>
      ) : (
        <p className="user-hub-personalization-empty">{t("personalization.owned.empty")}</p>
      )}
    </section>
  )
}
