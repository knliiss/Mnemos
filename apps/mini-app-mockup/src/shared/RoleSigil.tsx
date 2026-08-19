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
        <rect x="10" y="4" width="12" height="3" fill="currentColor" opacity=".42" />
        <rect x="7" y="7" width="18" height="4" fill="currentColor" opacity=".78" />
        <rect x="8" y="12" width="16" height="7" fill="currentColor" opacity=".82" />
        <rect x="14" y="10" width="4" height="3" fill="currentColor" />
        <rect x="11" y="13" width="10" height="4" fill="currentColor" />
        <rect x="14" y="17" width="4" height="3" fill="currentColor" />
        <rect x="9" y="20" width="14" height="4" fill="currentColor" opacity=".62" />
        <rect x="11" y="24" width="4" height="4" fill="currentColor" opacity=".42" />
        <rect x="17" y="24" width="4" height="4" fill="currentColor" opacity=".42" />
      </svg>
    </span>
  )
}
