import type { CSSProperties } from "react"

type RoleSigilProps = {
  color: string
  roleId?: string
  size?: number
  compact?: boolean
  className?: string
}

const roleAssets: Record<string, string> = {
  "role-owner": "/role-icons/owner.svg",
  "role-admin": "/role-icons/admin.svg",
  "role-moderator": "/role-icons/moderator.svg",
  "role-member": "/role-icons/member.svg",
}

const roleAssetsByColor: Record<string, string> = {
  "#D8D4C9": "/role-icons/owner.svg",
  "#C08B67": "/role-icons/admin.svg",
  "#A985A8": "/role-icons/moderator.svg",
  "#8A877F": "/role-icons/member.svg",
}

export function RoleSigil({
  color,
  roleId,
  size = 38,
  compact = false,
  className,
}: RoleSigilProps) {
  const asset = (roleId ? roleAssets[roleId] : undefined) ?? roleAssetsByColor[color.toUpperCase()]
  const style: CSSProperties = {
    width: size,
    height: size,
    "--role-sigil-color": color,
  } as CSSProperties

  return (
    <span
      className={`role-sigil role-sigil--asset ${compact ? "role-sigil--compact" : ""} ${className ?? ""}`.trim()}
      style={style}
      aria-hidden="true"
    >
      <img src={asset ?? "/role-icons/custom.svg"} alt="" draggable={false} />
    </span>
  )
}
