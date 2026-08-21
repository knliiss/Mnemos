import { useTranslation } from "react-i18next"
import { RoleSigil } from "../../shared/RoleSigil"

type RoleEditorHeroProps = {
  color: string
  name: string
  selectedCount: number
  totalPermissions: number
  isNew: boolean
}

export function RoleEditorHero({
  color,
  name,
  selectedCount,
  totalPermissions,
  isNew,
}: RoleEditorHeroProps) {
  const { t } = useTranslation()

  return (
    <section className="role-editor-hero">
      <RoleSigil color={color} size={48} />
      <span className="role-editor-hero__copy">
        <small>{isNew ? t("roleEditor.newEyebrow") : t("roleEditor.editEyebrow")}</small>
        <strong style={{ color }}>{name || t("roleEditor.roleNameFallback")}</strong>
        <em>
          {t("roleEditor.enabledSummary", {
            selected: selectedCount,
            total: totalPermissions,
          })}
        </em>
      </span>
      <div className="role-editor-progress" aria-hidden="true">
        <i
          style={{
            width: `${(selectedCount / Math.max(1, totalPermissions)) * 100}%`,
            background: color,
          }}
        />
      </div>
    </section>
  )
}
