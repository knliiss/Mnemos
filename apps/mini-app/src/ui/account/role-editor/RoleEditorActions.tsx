import { useState } from "react"
import { useTranslation } from "react-i18next"
import { Glyph } from "../../shared/MnemosIcon"

type RoleEditorActionsProps = {
  existing: boolean
  busy: boolean
  saveDisabled: boolean
  canSaveRole: boolean
  canDeleteRole: boolean
  accessMessage: string
  isNew: boolean
  onSave(): void
  onDelete(): void
}

export function RoleEditorActions({
  existing,
  busy,
  saveDisabled,
  canSaveRole,
  canDeleteRole,
  accessMessage,
  isNew,
  onSave,
  onDelete,
}: RoleEditorActionsProps) {
  const { t } = useTranslation()
  const [confirmDelete, setConfirmDelete] = useState(false)

  return (
    <div className="role-editor-actions">
      <button
        type="button"
        className="role-editor-save"
        disabled={saveDisabled}
        title={!canSaveRole ? accessMessage : undefined}
        onClick={onSave}
      >
        <Glyph name="check" size={16} />
        {busy ? t("roleEditor.saving") : isNew ? t("roleEditor.create") : t("roleEditor.save")}
      </button>

      {existing ? (
        <section className="role-editor-danger">
          <span>
            <strong>{t("roleEditor.deleteTitle")}</strong>
            <small>{t("roleEditor.deleteCopy")}</small>
          </span>
          {confirmDelete ? (
            <div>
              <button type="button" disabled={busy} onClick={() => setConfirmDelete(false)}>
                {t("roleEditor.cancel")}
              </button>
              <button
                type="button"
                disabled={busy || !canDeleteRole}
                title={!canDeleteRole ? accessMessage : undefined}
                className="is-danger"
                onClick={onDelete}
              >
                {t("roleEditor.confirmDelete")}
              </button>
            </div>
          ) : (
            <button
              type="button"
              disabled={busy || !canDeleteRole}
              title={!canDeleteRole ? accessMessage : undefined}
              className="is-danger"
              onClick={() => setConfirmDelete(true)}
            >
              {t("roleEditor.delete")}
            </button>
          )}
        </section>
      ) : null}
    </div>
  )
}
