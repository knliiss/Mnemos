import { useMemo } from "react"
import { useTranslation } from "react-i18next"
import "./moderation-admin-role-color.css"

const ROLE_COLORS = [
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
  disabled?: boolean
  compact?: boolean
  onChange(value: string): void
}

export function AdminRoleColorControl({ value, disabled = false, compact = false, onChange }: Props) {
  const { t } = useTranslation()
  const normalized = normalizeHex(value)
  const swatches = useMemo(
    () => (ROLE_COLORS.includes(normalized) ? ROLE_COLORS : [normalized, ...ROLE_COLORS]),
    [normalized],
  )

  return (
    <div className={compact ? "admin-role-color is-compact" : "admin-role-color"}>
      <div className="admin-role-color__swatches" aria-label={t("roleEditor.color")}>
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

      <label className="admin-role-color__hex">
        <span aria-hidden="true" style={{ background: normalized }} />
        <input
          value={value}
          disabled={disabled}
          maxLength={7}
          inputMode="text"
          autoComplete="off"
          spellCheck={false}
          aria-label={t("roleEditor.color")}
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
