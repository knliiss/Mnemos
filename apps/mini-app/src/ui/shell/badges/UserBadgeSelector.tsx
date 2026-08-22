import { useTranslation } from "react-i18next"
import type { UserBadge } from "@/shared/api/badgeApi"
import { UserBadgeMark } from "./UserBadgeMark"

type UserBadgeSelectorProps = {
  badges: UserBadge[]
  loading: boolean
  busyBadgeId: string | null
  onActivate: (badgeId: string) => void
  onDeactivate: () => void
}

export function UserBadgeSelector({
  badges,
  loading,
  busyBadgeId,
  onActivate,
  onDeactivate,
}: UserBadgeSelectorProps) {
  const { t } = useTranslation()
  const activeBadge = badges.find((item) => item.active) ?? null

  if (loading) {
    return (
      <section className="user-hub-badges" aria-busy="true">
        <header>
          <small>{t("appShell.userMenu.badgeEyebrow")}</small>
          <strong>{t("appShell.userMenu.badgeLoading")}</strong>
        </header>
      </section>
    )
  }

  if (!badges.length) {
    return null
  }

  return (
    <section className="user-hub-badges">
      <header>
        <span>
          <small>{t("appShell.userMenu.badgeEyebrow")}</small>
          <strong>{activeBadge?.badge.name ?? t("appShell.userMenu.badgeNone")}</strong>
        </span>

        {activeBadge ? (
          <button
            type="button"
            className="user-hub-badges__clear"
            disabled={busyBadgeId !== null}
            onClick={onDeactivate}
          >
            {t("appShell.userMenu.badgeDeactivate")}
          </button>
        ) : null}
      </header>

      <div className="user-hub-badges__grid">
        {badges.map((item) => {
          const active = item.active
          const busy = busyBadgeId === item.badge.id
          const statusLabel = busy
            ? t("appShell.userMenu.badgeSaving")
            : active
              ? t("appShell.userMenu.badgeActive")
              : t("appShell.userMenu.badgeUse")

          return (
            <button
              type="button"
              key={item.badge.id}
              className={`user-hub-badge-option ${active ? "is-active" : ""}`}
              disabled={busyBadgeId !== null}
              onClick={() => {
                if (!active) {
                  onActivate(item.badge.id)
                }
              }}
            >
              <UserBadgeMark badge={item.badge} compact />
              <span>
                <strong>{item.badge.name}</strong>
                <small>{statusLabel}</small>
              </span>
            </button>
          )
        })}
      </div>
    </section>
  )
}
