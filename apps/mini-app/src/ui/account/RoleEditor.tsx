import { useEffect, useRef, useState } from "react"
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
import { allRolePermissions } from "../rolePermissions"
import { PageHeader } from "../shared/Primitives"
import { FeatureEmpty } from "../system/FeatureEmpty"
import { RoleEditorActions } from "./role-editor/RoleEditorActions"
import { RoleEditorHero } from "./role-editor/RoleEditorHero"
import { RoleIdentityPanel } from "./role-editor/RoleIdentityPanel"
import { RolePermissionsPanel } from "./role-editor/RolePermissionsPanel"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

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
  const canManageRoles = data.hasPermission("MANAGE_ROLES")
  const existing = data.roles.find((role) => role.id === params?.roleId) ?? null
  const isNew = params?.roleId === "new"
  const accessMessage = t("errorNotifications.tones.access.message")
  const initialPermissions = existing
    ? { ...emptyPermissions(), ...existing.permissions }
    : emptyPermissions()
  const [name, setName] = useState(existing?.name ?? "")
  const [color, setColor] = useState(existing?.color ?? "#A985A8")
  const [permissions, setPermissions] =
    useState<Record<RolePermission, boolean>>(initialPermissions)
  const [busy, setBusy] = useState(false)
  const busyRef = useRef(false)
  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  const canEnablePermission = (permission: RolePermission) => data.hasPermission(permission)
  const selectedCount = Object.values(permissions).filter(Boolean).length
  const cleanName = name.trim()
  const detailsChanged = Boolean(
    existing &&
      (cleanName !== existing.name || color.toUpperCase() !== existing.color.toUpperCase()),
  )
  const permissionsChanged = Boolean(
    existing &&
      allRolePermissions.some(
        (permission) => permissions[permission] !== existing.permissions[permission],
      ),
  )
  const hasEditableChanges = existing
    ? (canManageRoles && detailsChanged) || (canManageRoles && permissionsChanged)
    : canManageRoles
  const saveDisabled = !accountId || !cleanName || busy || !hasEditableChanges

  const togglePermission = (permission: RolePermission) => {
    if (busyRef.current || !canManageRoles) {
      return
    }

    if (!permissions[permission] && !canEnablePermission(permission)) {
      return
    }

    setPermissions((current) => ({
      ...current,
      [permission]: !current[permission],
    }))
  }

  const toggleGroup = (groupPermissions: RolePermission[]) => {
    if (busyRef.current || !canManageRoles || !groupPermissions.length) {
      return
    }

    const allEnabled = groupPermissions.every((permission) => permissions[permission])

    setPermissions((current) => {
      const next = { ...current }

      groupPermissions.forEach((permission) => {
        if (allEnabled) {
          next[permission] = false
          return
        }

        if (canEnablePermission(permission)) {
          next[permission] = true
        }
      })

      return next
    })
  }

  const setAllPermissions = (checked: boolean) => {
    if (busyRef.current || !canManageRoles) {
      return
    }

    setPermissions((current) =>
      Object.fromEntries(
        allRolePermissions.map((permission) => [
          permission,
          checked ? current[permission] || canEnablePermission(permission) : false,
        ]),
      ) as Record<RolePermission, boolean>,
    )
  }

  const save = async () => {
    if (!accountId || !cleanName || busyRef.current || saveDisabled) {
      return
    }

    busyRef.current = true
    setBusy(true)

    try {
      if (existing) {
        if (detailsChanged) {
          await updateRoleDetails(accountId, existing.id, cleanName, color)
        }

        if (permissionsChanged) {
          await updateRolePermissions(accountId, existing.id, permissions)
        }
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
    if (!accountId || !existing || !canManageRoles || busyRef.current) {
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

      <RoleEditorHero
        color={color}
        name={cleanName}
        selectedCount={selectedCount}
        totalPermissions={allRolePermissions.length}
        isNew={isNew}
      />

      <RoleIdentityPanel
        name={name}
        color={color}
        busy={busy}
        editable={canManageRoles}
        accessMessage={accessMessage}
        onNameChange={setName}
        onColorChange={setColor}
      />

      <RolePermissionsPanel
        permissions={permissions}
        busy={busy}
        editable={canManageRoles}
        accessMessage={accessMessage}
        canEnablePermission={canEnablePermission}
        onTogglePermission={togglePermission}
        onToggleGroup={toggleGroup}
        onSetAllPermissions={setAllPermissions}
      />

      <RoleEditorActions
        existing={Boolean(existing)}
        busy={busy}
        saveDisabled={saveDisabled}
        canSaveRole={canManageRoles}
        canDeleteRole={canManageRoles}
        accessMessage={accessMessage}
        isNew={isNew}
        onSave={() => void save()}
        onDelete={() => void remove()}
      />
    </div>
  )
}
