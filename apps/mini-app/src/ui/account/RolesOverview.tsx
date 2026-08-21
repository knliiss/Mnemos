import { Reorder, useDragControls } from "framer-motion"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { updateRoleHierarchy } from "@/shared/api/roleApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useMnemosData } from "../data/MnemosDataProvider"
import type { AccountRole, Route } from "../model"
import { allRolePermissions } from "../rolePermissions"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"
import { FeatureEmpty } from "../system/FeatureEmpty"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function RolesOverview({ navigate, back }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const accountId = data.activeAccount?.id ?? null
  const canViewRoles = data.hasPermission("VIEW_ROLES")
  const canViewMembers = data.hasPermission("VIEW_MEMBERS")
  const canCreateRoles = data.hasPermission("CREATE_ROLES")
  const canManageHierarchy = data.hasPermission("MANAGE_ROLE_HIERARCHY")
  const accessMessage = t("errorNotifications.tones.access.message")
  const roles = useMemo(
    () => [...data.roles].sort((left, right) => left.hierarchyRank - right.hierarchyRank),
    [data.roles],
  )
  const totalPermissions = allRolePermissions.length

  return (
    <div className="screen roles-screen">
      <PageHeader
        title={t("rolesOverview.title")}
        back={back}
        actions={
          <button
            type="button"
            className="roles-create"
            disabled={!accountId || !canCreateRoles}
            title={!canCreateRoles ? accessMessage : undefined}
            onClick={() => navigate({ id: "role-editor", params: { roleId: "new" } })}
          >
            <Glyph name="plus" size={16} />
            {t("rolesOverview.new")}
          </button>
        }
      />

      <section className="roles-hero">
        <span className="roles-hero__icon">
          <Glyph name="roles" size={23} />
        </span>
        <span className="roles-hero__copy">
          <small>{t("rolesOverview.hierarchyEyebrow")}</small>
          <strong>
            {t("rolesOverview.hierarchySummary", {
              roles: canViewRoles ? roles.length : "—",
              permissions: totalPermissions,
            })}
          </strong>
          <em>{t("rolesOverview.hierarchyNote")}</em>
        </span>
      </section>

      <section className="roles-guide">
        <span className="roles-guide__handle" aria-hidden="true">
          ⠿
        </span>
        <span>
          <strong>{t("rolesOverview.dragTitle")}</strong>
          <small>{t("rolesOverview.dragNote")}</small>
        </span>
      </section>

      {canViewRoles && accountId && roles.length ? (
        <RoleReorderList
          key={roles.map((role) => `${role.id}:${role.hierarchyRank}`).join("|")}
          accountId={accountId}
          initialRoles={roles}
          memberCounts={
            canViewMembers
              ? new Map(
                  roles.map((role) => [
                    role.id,
                    data.members.filter((member) => member.highestRoleId === role.id).length,
                  ]),
                )
              : null
          }
          canManageHierarchy={canManageHierarchy}
          navigate={navigate}
          refresh={data.refreshAccount}
        />
      ) : null}

      {!canViewRoles ? (
        <FeatureEmpty
          icon="roles"
          title={t("errorNotifications.tones.access.title")}
          copy={accessMessage}
        />
      ) : null}

      {canViewRoles && !roles.length && !data.loadingAccount ? (
        <FeatureEmpty
          icon="roles"
          title={t("rolesOverview.emptyTitle")}
          copy={t("rolesOverview.emptyCopy")}
        />
      ) : null}

      <button
        type="button"
        className="roles-create-card"
        disabled={!accountId || !canCreateRoles}
        title={!canCreateRoles ? accessMessage : undefined}
        onClick={() => navigate({ id: "role-editor", params: { roleId: "new" } })}
      >
        <span>
          <Glyph name="plus" size={20} />
        </span>
        <span>
          <strong>{t("rolesOverview.createTitle")}</strong>
          <small>{t("rolesOverview.createCopy")}</small>
        </span>
        <b>›</b>
      </button>
    </div>
  )
}

function RoleReorderList({
  accountId,
  initialRoles,
  memberCounts,
  canManageHierarchy,
  navigate,
  refresh,
}: {
  accountId: string
  initialRoles: AccountRole[]
  memberCounts: Map<string, number> | null
  canManageHierarchy: boolean
  navigate: (route: Route) => void
  refresh: () => Promise<void>
}) {
  const [roles, setRoles] = useState(initialRoles)
  const [savingOrder, setSavingOrder] = useState(false)

  const persist = async () => {
    if (savingOrder || !canManageHierarchy) {
      return
    }

    setSavingOrder(true)

    try {
      await updateRoleHierarchy(
        accountId,
        roles.map((role) => role.id),
      )
      await refresh()
    } catch (error) {
      handleError(error)
      await refresh()
    } finally {
      setSavingOrder(false)
    }
  }

  return (
    <section className="roles-stack">
      <Reorder.Group axis="y" values={roles} onReorder={setRoles} className="role-reorder">
        {roles.map((role) => (
          <SortableRoleRow
            key={role.id}
            role={role}
            memberCount={memberCounts?.get(role.id) ?? null}
            disabled={savingOrder || !canManageHierarchy}
            navigate={navigate}
            onDragEnd={() => void persist()}
          />
        ))}
      </Reorder.Group>
    </section>
  )
}

function SortableRoleRow({
  role,
  memberCount,
  disabled,
  navigate,
  onDragEnd,
}: {
  role: AccountRole
  memberCount: number | null
  disabled: boolean
  navigate: (route: Route) => void
  onDragEnd: () => void
}) {
  const { t } = useTranslation()
  const dragControls = useDragControls()

  return (
    <Reorder.Item
      value={role}
      dragListener={false}
      dragControls={dragControls}
      className="role-row"
      whileDrag={{ scale: 1.02, zIndex: 5 }}
      onDragEnd={onDragEnd}
    >
      <button
        type="button"
        className="role-row__main"
        onClick={() => navigate({ id: "role-editor", params: { roleId: role.id } })}
      >
        <RoleSigil color={role.color} size={40} />
        <span className="role-row__copy">
          <span>
            <strong>{role.name}</strong>
            <i>
              {memberCount === null
                ? "—"
                : t("rolesOverview.primaryMembers", { count: memberCount })}
            </i>
          </span>
          <small>
            {t("rolesOverview.permissionSummary", {
              count: enabledPermissions(role),
              rank: role.hierarchyRank,
            })}
          </small>
        </span>
        <b>›</b>
      </button>

      <button
        type="button"
        className="role-drag-handle"
        disabled={disabled}
        aria-label={t("rolesOverview.dragAria", { name: role.name })}
        title={disabled ? t("errorNotifications.tones.access.message") : t("rolesOverview.dragAction")}
        onPointerDown={(event) => {
          if (!disabled) {
            dragControls.start(event)
          }
        }}
      >
        ⠿
      </button>
    </Reorder.Item>
  )
}

function enabledPermissions(role: AccountRole) {
  return Object.values(role.permissions).filter(Boolean).length
}
