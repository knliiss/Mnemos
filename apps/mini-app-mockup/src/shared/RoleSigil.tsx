import type { CSSProperties } from "react"

type RoleSigilProps = {
  color: string
  size?: number
  compact?: boolean
  className?: string
}

export function RoleSigil({ color, size = 38, compact = false, className }: RoleSigilProps) {
  const style: CSSProperties = {
    display: "grid",
    placeItems: "center",
    flex: "0 0 auto",
    width: size,
    height: size,
    border: "1px solid",
    borderColor: `color-mix(in srgb, ${color} 26%, transparent)`,
    borderRadius: compact ? size * 0.31 : size * 0.34,
    color,
    background: `radial-gradient(circle at 34% 24%, color-mix(in srgb, ${color} 17%, transparent), transparent 58%), rgba(255,255,255,.018)`,
    boxShadow: `inset 0 1px rgba(255,255,255,.035), 0 7px 18px color-mix(in srgb, ${color} 5%, transparent)`,
  }

  return (
    <span
      className={`role-sigil ${compact ? "role-sigil--compact" : ""} ${className ?? ""}`}
      style={style}
      aria-hidden="true"
    >
      <svg viewBox="0 0 24 24" width={compact ? size * 0.58 : size * 0.62} height={compact ? size * 0.58 : size * 0.62}>
        <path
          d="M12 3.4 18.5 7.2v7.6L12 20.6l-6.5-5.8V7.2Z"
          fill="color-mix(in srgb, currentColor 10%, transparent)"
          stroke="currentColor"
          strokeWidth="1.35"
          strokeLinejoin="round"
        />
        <path
          d="m8.1 9.3 3.9-2.2 3.9 2.2v4.5L12 16.1l-3.9-2.3Z"
          fill="color-mix(in srgb, currentColor 18%, transparent)"
          stroke="currentColor"
          strokeWidth="1.15"
          strokeLinejoin="round"
        />
        <path d="M12 7.1v9M8.1 9.3l7.8 4.5M15.9 9.3l-7.8 4.5" fill="none" stroke="currentColor" strokeWidth=".75" opacity=".62" />
        <circle cx="12" cy="11.6" r="1.15" fill="currentColor" />
      </svg>
    </span>
  )
}
