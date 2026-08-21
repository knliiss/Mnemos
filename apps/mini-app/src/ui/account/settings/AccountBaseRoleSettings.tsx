import { useTranslation } from "react-i18next"
import type { AccountRole } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { RoleSigil } from "../../shared/RoleSigil"
import { countEnabledPermissions, NO_BASE_ROLE } from "../accountSettingsPresentation"

type Props = {
  availableRoles: AccountRole[]
  baseRoleId: string
  savedRoleId: string
  busy: boolean
  editable: boolean
  onBaseRoleChange: (roleId: string) => void
  onSave: () => void
}

export function AccountBaseRoleSettings({
  availableRoles,
  baseRoleId,
  savedRoleId,
  busy,
  editable,
  onBaseRoleChange,
  onSave,
}: Props) {
  const { t } = useTranslation()
  const baseRole = availableRoles.find((role) => role.id === baseRoleId) ?? null
  const roleChanged = baseRoleId !== savedRoleId

  return (
    <section className="account-settings-panel">
      <header>
        <span>
          <small>{t("accountSettings.accessEyebrow")}</small>
          <strong>{t("accountSettings.defaultRoleTitle")}</strong>
          <em>{t("accountSettings.defaultRoleCopy")}</em>
        </span>

        {roleChanged ? (
          <b className="is-unsaved">{t("accountSettings.unsaved")}</b>
        ) : (
          <b className="is-saved">
            <Glyph name="check" size={12} />
            {t("accountSettings.saved")}
          </b>
        )}
      </header>

      <div className="account-default-role-grid">
        <button
          type="button"
          className={baseRoleId === NO_BASE_ROLE ? "is-selected" : ""}
          disabled={!editable || busy}
          onClick={() => onBaseRoleChange(NO_BASE_ROLE)}
        >
          <span className="account-base-role-empty">
            <Glyph name="close" size={16} />
          </span>
          <strong>{t("accountSettings.noRole")}</strong>
          <small>{t("accountSettings.permissionsCount", { count: 0 })}</small>
        </button>

        {availableRoles.map((role) => (
          <RoleOption
            key={role.id}
            role={role}
            selected={role.id === baseRoleId}
            disabled={!editable || busy}
            onClick={() => onBaseRoleChange(role.id)}
          />
        ))}
      </div>

      {!editable ? (
        <p className="inline-hint inline-hint--left">{t("accountSettings.baseRoleUnavailable")}</p>
      ) : null}

      <div className="account-role-summary">
        {baseRole ? (
          <RoleSigil color={baseRole.color} size={42} />
        ) : (
          <span className="account-base-role-empty account-base-role-empty--summary">
            <Glyph name="close" size={18} />
          </span>
        )}

        <span>
          <small>{t("accountSettings.baseRoleSummary")}</small>
          <strong>{baseRole?.name ?? t("accountSettings.noRole")}</strong>
          <em>
            {t("accountSettings.permissionsCount", {
              count: countEnabledPermissions(baseRole),
            })}
          </em>
        </span>

        <button type="button" disabled={!editable || !roleChanged || busy} onClick={onSave}>
          {busy ? "…" : t("accountSettings.save")}
        </button>
      </div>
    </section>
  )
}

type RoleOptionProps = {
  role: AccountRole
  selected: boolean
  disabled: boolean
  onClick: () => void
}

function RoleOption({ role, selected, disabled, onClick }: RoleOptionProps) {
  const { t } = useTranslation()

  return (
    <button
      type="button"
      className={selected ? "is-selected" : ""}
      disabled={disabled}
      onClick={onClick}
    >
      <RoleSigil color={role.color} size={40} />
      <strong>{role.name}</strong>
      <small>
        {t("accountSettings.permissionsCount", {
          count: countEnabledPermissions(role),
        })}
      </small>
    </button>
  )
}
