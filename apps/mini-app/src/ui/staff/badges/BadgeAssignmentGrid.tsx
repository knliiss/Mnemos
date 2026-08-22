import { useTranslation } from "react-i18next"
import type { BadgeResponse, UserBadgeResponse } from "@/shared/api/badgeAdminApi"

type BadgeAssignmentGridProps = {
  badges: BadgeResponse[]
  userBadges: UserBadgeResponse[]
  assignedIds: Set<string>
  userSelected: boolean
  busy: boolean
  onToggleAssignment(badge: BadgeResponse): void
}

export function BadgeAssignmentGrid({
  badges,
  userBadges,
  assignedIds,
  userSelected,
  busy,
  onToggleAssignment,
}: BadgeAssignmentGridProps) {
  const { t } = useTranslation()

  return (
    <section className="review-queue-grid">
      {badges.map((badge) => {
        const assigned = assignedIds.has(badge.id)
        const userBadge = userBadges.find((item) => item.badge.id === badge.id)

        return (
          <button
            type="button"
            key={badge.id}
            className={assigned ? "review-queue-card is-selected" : "review-queue-card"}
            disabled={!userSelected || busy}
            onClick={() => onToggleAssignment(badge)}
          >
            <span
              className="review-overview__mark review-overview__mark--badge-image"
              style={{ borderColor: badge.color }}
              aria-hidden="true"
            >
              <img src={badge.imageUrl} alt="" />
            </span>

            <span className="review-queue-card__copy">
              <span className="review-queue-card__meta">
                <small>{badge.code}</small>
              </span>
              <strong>{badge.name}</strong>
              <em>
                {userSelected
                  ? assigned
                    ? t("badgeAdmin.assigned")
                    : t("badgeAdmin.clickToAssign")
                  : t("badgeAdmin.selectUserFirst")}
              </em>
            </span>

            <span className="review-queue-card__side">
              {userBadge?.active ? (
                <b className="status-published">{t("badgeAdmin.active")}</b>
              ) : assigned ? (
                <b className="status-approved">{t("badgeAdmin.owned")}</b>
              ) : null}
            </span>
          </button>
        )
      })}

      {!badges.length ? <p className="inline-hint">{t("badgeAdmin.noBadges")}</p> : null}
    </section>
  )
}
