import type { CSSProperties } from "react"

type RoleSigilProps = {
  color: string
  roleId?: string
  size?: number
  compact?: boolean
  className?: string
}

export function RoleSigil({
  color,
  size = 38,
  compact = false,
  className,
}: RoleSigilProps) {
  const style = {
    width: size,
    height: size,
    color,
    "--role-sigil-color": color,
  } as CSSProperties

  return (
    <span
      className={`role-sigil role-sigil--shared ${compact ? "role-sigil--compact" : ""} ${className ?? ""}`.trim()}
      style={style}
      aria-hidden="true"
    >
      <svg viewBox="0 0 32 32" shapeRendering="crispEdges">
        <rect x="11" y="5" width="10" height="3" fill="currentColor" opacity=".42" />
        <rect x="8" y="8" width="16" height="4" fill="currentColor" opacity=".72" />
        <rect x="6" y="12" width="20" height="10" fill="currentColor" opacity=".28" />
        <rect x="9" y="13" width="14" height="8" fill="currentColor" opacity=".82" />
        <rect x="13" y="15" width="6" height="4" fill="currentColor" />
        <rect x="10" y="22" width="12" height="3" fill="currentColor" opacity=".56" />
        <rect x="13" y="25" width="6" height="3" fill="currentColor" opacity=".32" />
      </svg>
    </span>
  )
}
