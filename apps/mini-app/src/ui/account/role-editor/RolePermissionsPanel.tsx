import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type { RolePermission } from "../../model"
import { rolePermissionGroups } from "../../rolePermissions"
import { Glyph } from "../../shared/MnemosIcon"

type RolePermissionsPanelProps = {
  permissions: Record<RolePermission, boolean>
  busy: boolean
  editable: boolean
  accessMessage: string
  canEnablePermission(permission: RolePermission): boolean
  onTogglePermission(permission: RolePermission): void
  onToggleGroup(permissions: RolePermission[]): void
  onSetAllPermissions(checked: boolean): void
}

export function RolePermissionsPanel({
  permissions,
  busy,
  editable,
  accessMessage,
  canEnablePermission,
  onTogglePermission,
  onToggleGroup,
  onSetAllPermissions,
}: RolePermissionsPanelProps) {
  const { t } = useTranslation()
  const [query, setQuery] = useState("")
  const selectedCount = Object.values(permissions).filter(Boolean).length
  const normalizedQuery = query.trim().toLowerCase()
  const hasMutablePermissions =
    editable &&
    rolePermissionGroups.some((group) =>
      group.permissions.some(
        (permission) => permissions[permission] || canEnablePermission(permission),
      ),
    )

  const visibleGroups = useMemo(
    () =>
      rolePermissionGroups
        .map((group) => ({
          ...group,
          permissions: group.permissions.filter(
            (permission) =>
              !normalizedQuery ||
              t(`roleEditor.permissions.${permission}`).toLowerCase().includes(normalizedQuery) ||
              permission.toLowerCase().includes(normalizedQuery),
          ),
        }))
        .filter((group) => group.permissions.length),
    [normalizedQuery, t],
  )

  return (
    <section className="role-permissions-panel">
      <header className="role-permissions-panel__head">
        <span>
          <small>{t("roleEditor.permissionsEyebrow")}</small>
          <strong>{t("roleEditor.enabledCount", { count: selectedCount })}</strong>
        </span>
        <div>
          <button
            type="button"
            disabled={busy || !hasMutablePermissions}
            title={!hasMutablePermissions ? accessMessage : undefined}
            onClick={() => onSetAllPermissions(true)}
          >
            {t("roleEditor.all")}
          </button>
          <button
            type="button"
            disabled={busy || !hasMutablePermissions}
            title={!hasMutablePermissions ? accessMessage : undefined}
            onClick={() => onSetAllPermissions(false)}
          >
            {t("roleEditor.clear")}
          </button>
        </div>
      </header>

      <label className="role-permission-search">
        <Glyph name="search" size={16} />
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={t("roleEditor.searchPlaceholder")}
        />
        {query ? (
          <button
            type="button"
            onClick={() => setQuery("")}
            aria-label={t("roleEditor.clearSearch")}
          >
            <Glyph name="close" size={14} />
          </button>
        ) : null}
      </label>

      <div className="role-permission-groups">
        {visibleGroups.map((group) => {
          const enabledInGroup = group.permissions.filter(
            (permission) => permissions[permission],
          ).length
          const mutablePermissions = group.permissions.filter(
            (permission) => permissions[permission] || canEnablePermission(permission),
          )
          const allMutableEnabled =
            mutablePermissions.length > 0 &&
            mutablePermissions.every((permission) => permissions[permission])
          const groupLocked = !editable || !mutablePermissions.length

          return (
            <section key={group.key}>
              <header>
                <span>
                  <strong>{t(`roleEditor.groups.${group.key}`)}</strong>
                  <small>
                    {enabledInGroup}/{group.permissions.length}
                  </small>
                </span>
                <button
                  type="button"
                  disabled={busy || groupLocked}
                  title={groupLocked ? accessMessage : undefined}
                  className={allMutableEnabled ? "is-active" : ""}
                  onClick={() => onToggleGroup(mutablePermissions)}
                >
                  {allMutableEnabled ? t("roleEditor.disableGroup") : t("roleEditor.enableGroup")}
                </button>
              </header>

              <div>
                {group.permissions.map((permission) => {
                  const permissionLocked =
                    !editable || (!permissions[permission] && !canEnablePermission(permission))

                  return (
                    <button
                      key={permission}
                      type="button"
                      className={`role-permission-row ${permissions[permission] ? "is-on" : ""}`}
                      disabled={busy || permissionLocked}
                      title={permissionLocked ? accessMessage : undefined}
                      onClick={() => onTogglePermission(permission)}
                    >
                      <span>
                        <strong>{t(`roleEditor.permissions.${permission}`)}</strong>
                        <small>{permission}</small>
                      </span>
                      <i aria-hidden="true">
                        <b />
                      </i>
                    </button>
                  )
                })}
              </div>
            </section>
          )
        })}
      </div>
    </section>
  )
}
