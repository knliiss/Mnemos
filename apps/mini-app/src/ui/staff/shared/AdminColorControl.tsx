import { useMemo } from "react"
import "./admin-color-control.css"

const ADMIN_COLORS = [
  "#CBE86E",
  "#82D5A9",
  "#6EB8E8",
  "#8C9CFF",
  "#B79AF2",
  "#D49ACF",
  "#E59A8E",
  "#E7B46D",
  "#D9D28A",
  "#AAB3A6",
]

type Props = {
  value: string
  label: string
  disabled?: boolean
  compact?: boolean
  onChange(value: string): void
}

export function AdminColorControl({
  value,
  label,
  disabled = false,
  compact = false,
  onChange,
}: Props) {
  const normalized = normalizeHex(value)
  const swatches = useMemo(
    () => (ADMIN_COLORS.includes(normalized) ? ADMIN_COLORS : [normalized, ...ADMIN_COLORS]),
    [normalized],
  )

  return (
    <div className={compact ? "admin-color-control is-compact" : "admin-color-control"}>
      <div className="admin-color-control__swatches" aria-label={label}>
        {swatches.map((color) => (
          <button
            type="button"
            key={color}
            disabled={disabled}
            className={color === normalized ? "is-active" : ""}
            aria-label={color}
            title={color}
            onClick={() => onChange(color)}
          >
            <i style={{ background: color }} />
          </button>
        ))}
      </div>

      <label className="admin-color-control__hex">
        <span aria-hidden="true" style={{ background: normalized }} />
        <input
          value={value}
          disabled={disabled}
          maxLength={7}
          inputMode="text"
          autoComplete="off"
          spellCheck={false}
          aria-label={label}
          onChange={(event) => onChange(event.target.value.toUpperCase())}
          onBlur={() => onChange(normalized)}
        />
      </label>
    </div>
  )
}

function normalizeHex(value: string): string {
  const trimmed = value.trim().toUpperCase()

  if (/^#[0-9A-F]{6}$/.test(trimmed)) {
    return trimmed
  }

  return "#CBE86E"
}
