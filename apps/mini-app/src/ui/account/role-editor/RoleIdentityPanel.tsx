import { useTranslation } from "react-i18next"

const roleColors = [
  "#A985A8",
  "#D8A64A",
  "#CFFF35",
  "#67B7DC",
  "#E57C73",
  "#8E9AE6",
  "#A9B1A4",
] as const

type RoleIdentityPanelProps = {
  name: string
  color: string
  busy: boolean
  editable: boolean
  accessMessage: string
  onNameChange(name: string): void
  onColorChange(color: string): void
}

export function RoleIdentityPanel({
  name,
  color,
  busy,
  editable,
  accessMessage,
  onNameChange,
  onColorChange,
}: RoleIdentityPanelProps) {
  const { t } = useTranslation()

  return (
    <section className="role-identity-panel">
      <header>
        <span>
          <small>{t("roleEditor.appearance")}</small>
          <strong>{t("roleEditor.nameAndColor")}</strong>
        </span>
      </header>

      <label className="role-editor-field">
        <span>{t("roleEditor.name")}</span>
        <input
          value={name}
          maxLength={32}
          disabled={busy || !editable}
          title={!editable ? accessMessage : undefined}
          onChange={(event) => onNameChange(event.target.value)}
          placeholder={t("roleEditor.namePlaceholder")}
        />
        <small>{name.length}/32</small>
      </label>

      <div className="role-color-editor">
        <label>
          <span>{t("roleEditor.color")}</span>
          <span className="role-color-input">
            <input
              type="color"
              value={color.slice(0, 7)}
              disabled={busy || !editable}
              title={!editable ? accessMessage : undefined}
              onChange={(event) => onColorChange(event.target.value.toUpperCase())}
            />
            <code>{color}</code>
          </span>
        </label>

        <div className="role-color-presets" aria-label={t("roleEditor.quickColors")}>
          {roleColors.map((presetColor) => (
            <button
              key={presetColor}
              type="button"
              disabled={busy || !editable}
              title={!editable ? accessMessage : undefined}
              className={color.toUpperCase() === presetColor ? "is-active" : ""}
              style={{ background: presetColor }}
              aria-label={t("roleEditor.colorAria", { color: presetColor })}
              onClick={() => onColorChange(presetColor)}
            />
          ))}
        </div>
      </div>
    </section>
  )
}
