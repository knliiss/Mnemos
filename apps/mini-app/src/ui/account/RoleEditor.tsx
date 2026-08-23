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
import {
  resolveActorMember,
  resolveRoleManagementAccess,
  resolveRolePermissionMutationAccess,
  roleAccessReasonTranslationKey,
} from "./roleAccessPolicy"

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
  const account = data.activeAccount
  const accountId = account?.id ?? null
  const canManageRoles = data.hasPermission("MANAGE_ROLES")
  const existing = data.roles.find((role) => role.id === params?.roleId) ?? null
  const isNew = params?.roleId === "new"
  const actorMember = account ? resolveActorMember(account, data.members) : null
  const roleAccessContext = account
    ? {
        account,
        actorMember,
        roles: data.roles,
      }
    : null
  const managementAccess =
    existing && roleAccessContext
      ? resolveRoleManagementAccess(roleAccessContext, canManageRoles, existing)
      : canManageRoles
        ? "ALLOWED"
        : "MISSING_PERMISSION"
  const permissionMutationAccess =
    existing && roleAccessContext
      ? resolveRolePermissionMutationAccess(
          roleAccessContext,
          canManageRoles,
          existing,
          actorMember?.roleIds ?? [],
        )
      : managementAccess
  const detailsEditable = isNew ? canManageRoles : managementAccess === "ALLOWED"
  const permissionsEditable = isNew ? canManageRoles : permissionMutationAccess === "ALLOWED"
  const detailsAccessMessage =
    managementAccess === "ALLOWED"
      ? t("errorNotifications.tones.access.message")
      : t(roleAccessReasonTranslationKey(managementAccess))
  const permissionsAccessMessage =
    permissionMutationAccess === "ALLOWED"
      ? t("errorNotifications.tones.access.message")
      : t(roleAccessReasonTranslationKey(permissionMutationAccess))
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

  const canEnablePermission = (permission: RolePermission) =>
    Boolean(account?.owner || data.hasPermission(permission))
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
    ? (detailsEditable && detailsChanged) || (permissionsEditable && permissionsChanged)
    : canManageRoles
  const saveDisabled = !accountId || !cleanName || busy || !hasEditableChanges

  const togglePermission = (permission: RolePermission) => {
    if (busyRef.current || !permissionsEditable) {
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
    if (busyRef.current || !permissionsEditable || !groupPermissions.length) {
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
    if (busyRef.current || !permissionsEditable) {
      return
    }

    setPermissions(
      (current) =>
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
        if (detailsEditable && detailsChanged) {
          await updateRoleDetails(accountId, existing.id, cleanName, color)
        }

        if (permissionsEditable && permissionsChanged) {
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
    if (!accountId || !existing || managementAccess !== "ALLOWED" || busyRef.current) {
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
        editable={detailsEditable}
        accessMessage={detailsAccessMessage}
        onNameChange={setName}
        onColorChange={setColor}
      />

      <RolePermissionsPanel
        permissions={permissions}
        busy={busy}
        editable={permissionsEditable}
        accessMessage={permissionsAccessMessage}
        canEnablePermission={canEnablePermission}
        onTogglePermission={togglePermission}
        onToggleGroup={toggleGroup}
        onSetAllPermissions={setAllPermissions}
      />

      <RoleEditorActions
        existing={Boolean(existing)}
        busy={busy}
        saveDisabled={saveDisabled}
        canSaveRole={isNew ? canManageRoles : detailsEditable || permissionsEditable}
        canDeleteRole={managementAccess === "ALLOWED"}
        accessMessage={detailsAccessMessage}
        isNew={isNew}
        onSave={() => void save()}
        onDelete={() => void remove()}
      />
    </div>
  )
}
