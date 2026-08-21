import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createRole,
  deleteRole,
  updateRoleDetails,
  updateRolePermissions,
} from "@/shared/api/roleApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useMnemosData } from "../data/MnemosDataProvider"
import type { RolePermission, Route } from "../model"
import { allRolePermissions, rolePermissionGroups } from "../rolePermissions"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"
import { FeatureEmpty } from "../system/FeatureEmpty"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

const roleColors = [
  "#A985A8",
  "#D8A64A",
  "#CFFF35",
  "#67B7DC",
  "#E57C73",
  "#8E9AE6",
  "#A9B1A4",
] as const

function emptyPermissions(): Record<RolePermission, boolean> {
  return Object.fromEntries(allRolePermissions.map((permission) => [permission, false])) as Record<
    RolePermission,
    boolean
  >
}

export function RoleEditor({ params, navigate, back }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const accountId = data.activeAccount?.id ?? null
  const canViewRoles = data.hasPermission("VIEW_ROLES")
  const existing = data.roles.find((role) => role.id === params?.roleId) ?? null
  const isNew = params?.roleId === "new"
  const initialPermissions = existing
    ? { ...emptyPermissions(), ...existing.permissions }
    : emptyPermissions()
  const [name, setName] = useState(existing?.name ?? "")
  const [color, setColor] = useState(existing?.color ?? "#A985A8")
  const [permissions, setPermissions] =
    useState<Record<RolePermission, boolean>>(initialPermissions)
  const [query, setQuery] = useState("")
  const [busy, setBusy] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState(false)
  const busyRef = useRef(false)
  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  const selectedCount = Object.values(permissions).filter(Boolean).length
  const normalizedQuery = query.trim().toLowerCase()

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

  const togglePermission = (permission: RolePermission) => {
    if (busyRef.current) {
      return
    }

    setPermissions((current) => ({
      ...current,
      [permission]: !current[permission],
    }))
  }

  const toggleGroup = (groupPermissions: RolePermission[]) => {
    if (busyRef.current) {
      return
    }

    const allEnabled = groupPermissions.every((permission) => permissions[permission])

    setPermissions((current) => {
      const next = { ...current }

      groupPermissions.forEach((permission) => {
        next[permission] = !allEnabled
      })

      return next
    })
  }

  const setAllPermissions = (checked: boolean) => {
    if (busyRef.current) {
      return
    }

    setPermissions(
      Object.fromEntries(allRolePermissions.map((permission) => [permission, checked])) as Record<
        RolePermission,
        boolean
      >,
    )
  }

  const save = async () => {
    const cleanName = name.trim()
    if (!accountId || !cleanName || busyRef.current) {
      return
    }

    busyRef.current = true
    setBusy(true)

    try {
      if (existing) {
        await updateRoleDetails(accountId, existing.id, cleanName, color)
        await updateRolePermissions(accountId, existing.id, permissions)
      } else {
        await createRole(
          accountId,
          cleanName,
          color,
          allRolePermissions.filter((permission) => permissions[permission]),
        )
      }

      await data.refreshAccount()

      if (mountedRef.current) {
        back()
      }
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      busyRef.current = false
      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  const remove = async () => {
    if (!accountId || !existing || busyRef.current) {
      return
    }

    busyRef.current = true
    setBusy(true)

    try {
      await deleteRole(accountId, existing.id)
      await data.refreshAccount()

      if (mountedRef.current) {
        back()
      }
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      busyRef.current = false
      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  if (!isNew && !canViewRoles) {
    return (
      <div className="screen role-editor-screen">
        <PageHeader title={t("roleEditor.role")} back={back} />
        <FeatureEmpty
          icon="roles"
          title={t("errorNotifications.tones.access.title")}
          copy={t("errorNotifications.tones.access.message")}
        />
      </div>
    )
  }

  if (!isNew && !existing && !data.loadingAccount) {
    return (
      <div className="screen role-editor-screen">
        <PageHeader title={t("roleEditor.role")} back={back} />
        <FeatureEmpty
          icon="roles"
          title={t("roleEditor.notFoundTitle")}
          copy={t("roleEditor.notFoundCopy")}
        />
      </div>
    )
  }

  return (
    <div className="screen role-editor-screen">
      <PageHeader
        title={isNew ? t("roleEditor.newRole") : (existing?.name ?? t("roleEditor.role"))}
        back={back}
        actions={
          !isNew ? (
            <button
              type="button"
              className="role-editor-back-to-list"
              onClick={() => navigate({ id: "roles" })}
            >
              {t("roleEditor.allRoles")}
            </button>
          ) : undefined
        }
      />

      <section className="role-editor-hero">
        <RoleSigil color={color} size={48} />
        <span className="role-editor-hero__copy">
          <small>{isNew ? t("roleEditor.newEyebrow") : t("roleEditor.editEyebrow")}</small>
          <strong style={{ color }}>{name.trim() || t("roleEditor.roleNameFallback")}</strong>
          <em>
            {t("roleEditor.enabledSummary", {
              selected: selectedCount,
              total: allRolePermissions.length,
            })}
          </em>
        </span>
        <div className="role-editor-progress" aria-hidden="true">
          <i
            style={{
              width: `${(selectedCount / Math.max(1, allRolePermissions.length)) * 100}%`,
              background: color,
            }}
          />
        </div>
      </section>

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
            disabled={busy}
            onChange={(event) => setName(event.target.value)}
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
                disabled={busy}
                onChange={(event) => setColor(event.target.value.toUpperCase())}
              />
              <code>{color}</code>
            </span>
          </label>

          <div className="role-color-presets" aria-label={t("roleEditor.quickColors")}>
            {roleColors.map((presetColor) => (
              <button
                key={presetColor}
                type="button"
                disabled={busy}
                className={color.toUpperCase() === presetColor ? "is-active" : ""}
                style={{ background: presetColor }}
                aria-label={t("roleEditor.colorAria", { color: presetColor })}
                onClick={() => setColor(presetColor)}
              />
            ))}
          </div>
        </div>
      </section>

      <section className="role-permissions-panel">
        <header className="role-permissions-panel__head">
          <span>
            <small>{t("roleEditor.permissionsEyebrow")}</small>
            <strong>{t("roleEditor.enabledCount", { count: selectedCount })}</strong>
          </span>
          <div>
            <button type="button" disabled={busy} onClick={() => setAllPermissions(true)}>
              {t("roleEditor.all")}
            </button>
            <button type="button" disabled={busy} onClick={() => setAllPermissions(false)}>
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
            const allEnabled = enabledInGroup === group.permissions.length

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
                    disabled={busy}
                    className={allEnabled ? "is-active" : ""}
                    onClick={() => toggleGroup(group.permissions)}
                  >
                    {allEnabled ? t("roleEditor.disableGroup") : t("roleEditor.enableGroup")}
                  </button>
                </header>

                <div>
                  {group.permissions.map((permission) => (
                    <button
                      key={permission}
                      type="button"
                      className={`role-permission-row ${permissions[permission] ? "is-on" : ""}`}
                      disabled={busy}
                      onClick={() => togglePermission(permission)}
                    >
                      <span>
                        <strong>{t(`roleEditor.permissions.${permission}`)}</strong>
                        <small>{permission}</small>
                      </span>
                      <i aria-hidden="true">
                        <b />
                      </i>
                    </button>
                  ))}
                </div>
              </section>
            )
          })}
        </div>
      </section>

      <div className="role-editor-actions">
        <button
          type="button"
          className="role-editor-save"
          disabled={!name.trim() || busy}
          onClick={() => void save()}
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
                  disabled={busy}
                  className="is-danger"
                  onClick={() => void remove()}
                >
                  {t("roleEditor.confirmDelete")}
                </button>
              </div>
            ) : (
              <button
                type="button"
                disabled={busy}
                className="is-danger"
                onClick={() => setConfirmDelete(true)}
              >
                {t("roleEditor.delete")}
              </button>
            )}
          </section>
        ) : null}
      </div>
    </div>
  )
}
