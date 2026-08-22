import type { Badge } from "@/shared/api/badgeApi"

type UserBadgeMarkProps = {
  badge: Badge
  compact?: boolean
}

export function UserBadgeMark({ badge, compact = false }: UserBadgeMarkProps) {
  return (
    <span
      className={`user-badge-mark ${compact ? "user-badge-mark--compact" : ""}`}
      style={{ "--user-badge-accent": badge.color } as React.CSSProperties}
      aria-label={badge.name}
    >
      <img src={badge.imageUrl} alt="" aria-hidden="true" />
      <span className="user-badge-mark__tooltip" role="tooltip">
        {badge.name}
      </span>
    </span>
  )
}
