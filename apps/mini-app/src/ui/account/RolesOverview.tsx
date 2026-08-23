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
import {
  resolveActorMember,
  resolveHierarchyBoundary,
  roleAccessReasonTranslationKey,
} from "./roleAccessPolicy"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function RolesOverview({ navigate, back }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const account = data.activeAccount
  const accountId = account?.id ?? null
  const canManageRoles = data.hasPermission("MANAGE_ROLES")
  const canManageHierarchy = data.hasPermission("MANAGE_ROLE_HIERARCHY")
  const accessMessage = t("errorNotifications.tones.access.message")
  const roles = useMemo(
    () => [...data.roles].sort((left, right) => left.hierarchyRank - right.hierarchyRank),
    [data.roles],
  )
  const actorMember = account ? resolveActorMember(account, data.members) : null
  const hierarchyBoundary = account
    ? resolveHierarchyBoundary(
        {
          account,
          actorMember,
          roles,
        },
        canManageHierarchy,
      )
    : null
  const totalPermissions = allRolePermissions.length
  const memberCounts = useMemo(
    () =>
      new Map(
        roles.map((role) => [
          role.id,
          data.members.filter((member) => member.highestRoleId === role.id).length,
        ]),
      ),
    [data.members, roles],
  )

  return (
    <div className="screen roles-screen">
      <PageHeader
        title={t("rolesOverview.title")}
        back={back}
        actions={
          <button
            type="button"
            className="roles-create"
            disabled={!accountId || !canManageRoles}
            title={!canManageRoles ? accessMessage : undefined}
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
              roles: roles.length,
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
          <small>
            {hierarchyBoundary === null
              ? accessMessage
              : hierarchyBoundary === Number.NEGATIVE_INFINITY
                ? t("rolesOverview.dragNote")
                : t("rolesOverview.delegatedDragNote")}
          </small>
        </span>
      </section>

      {accountId && roles.length ? (
        <RoleReorderList
          key={roles.map((role) => `${role.id}:${role.hierarchyRank}`).join("|")}
          accountId={accountId}
          initialRoles={roles}
          hierarchyBoundary={hierarchyBoundary}
          memberCounts={memberCounts}
          navigate={navigate}
          refresh={data.refreshAccount}
        />
      ) : null}

      {!roles.length && !data.loadingAccount ? (
        <FeatureEmpty
          icon="roles"
          title={t("rolesOverview.emptyTitle")}
          copy={t("rolesOverview.emptyCopy")}
        />
      ) : null}

      <button
        type="button"
        className="roles-create-card"
        disabled={!accountId || !canManageRoles}
        title={!canManageRoles ? accessMessage : undefined}
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
  hierarchyBoundary,
  memberCounts,
  navigate,
  refresh,
}: {
  accountId: string
  initialRoles: AccountRole[]
  hierarchyBoundary: number | null
  memberCounts: Map<string, number>
  navigate: (route: Route) => void
  refresh: () => Promise<void>
}) {
  const { t } = useTranslation()
  const protectedRoles =
    hierarchyBoundary === Number.NEGATIVE_INFINITY
      ? []
      : hierarchyBoundary === null
        ? initialRoles
        : initialRoles.filter((role) => role.hierarchyRank <= hierarchyBoundary)
  const initialMovableRoles = initialRoles.filter((role) => !protectedRoles.includes(role))
  const [movableRoles, setMovableRoles] = useState(initialMovableRoles)
  const [savingOrder, setSavingOrder] = useState(false)
  const priorityByRoleId = new Map(initialRoles.map((role, index) => [role.id, index + 1]))

  const persist = async () => {
    if (savingOrder || hierarchyBoundary === null || movableRoles.length < 2) {
      return
    }

    setSavingOrder(true)

    try {
      await updateRoleHierarchy(accountId, [
        ...protectedRoles.map((role) => role.id),
        ...movableRoles.map((role) => role.id),
      ])
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
      {protectedRoles.map((role) => (
        <StaticRoleRow
          key={role.id}
          role={role}
          priority={priorityByRoleId.get(role.id) ?? 0}
          memberCount={memberCounts.get(role.id) ?? 0}
          lockedMessage={
            hierarchyBoundary === null
              ? t("errorNotifications.tones.access.message")
              : t(roleAccessReasonTranslationKey("ROLE_NOT_LOWER"))
          }
          navigate={navigate}
        />
      ))}

      {movableRoles.length ? (
        <Reorder.Group
          axis="y"
          values={movableRoles}
          onReorder={setMovableRoles}
          className="role-reorder"
        >
          {movableRoles.map((role) => (
            <SortableRoleRow
              key={role.id}
              role={role}
              priority={priorityByRoleId.get(role.id) ?? 0}
              memberCount={memberCounts.get(role.id) ?? 0}
              savingOrder={savingOrder}
              navigate={navigate}
              onDragEnd={() => void persist()}
            />
          ))}
        </Reorder.Group>
      ) : null}
    </section>
  )
}

function StaticRoleRow({
  role,
  priority,
  memberCount,
  lockedMessage,
  navigate,
}: {
  role: AccountRole
  priority: number
  memberCount: number
  lockedMessage: string
  navigate: (route: Route) => void
}) {
  return (
    <article className="role-row is-protected">
      <RoleRowMain
        role={role}
        priority={priority}
        memberCount={memberCount}
        navigate={navigate}
      />
      <button type="button" className="role-drag-handle" disabled title={lockedMessage}>
        ⠿
      </button>
    </article>
  )
}

function SortableRoleRow({
  role,
  priority,
  memberCount,
  savingOrder,
  navigate,
  onDragEnd,
}: {
  role: AccountRole
  priority: number
  memberCount: number
  savingOrder: boolean
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
      <RoleRowMain
        role={role}
        priority={priority}
        memberCount={memberCount}
        navigate={navigate}
      />

      <button
        type="button"
        className="role-drag-handle"
        disabled={savingOrder}
        aria-label={t("rolesOverview.dragAria", { name: role.name })}
        title={t("rolesOverview.dragAction")}
        onPointerDown={(event) => {
          if (!savingOrder) {
            dragControls.start(event)
          }
        }}
      >
        ⠿
      </button>
    </Reorder.Item>
  )
}

function RoleRowMain({
  role,
  priority,
  memberCount,
  navigate,
}: {
  role: AccountRole
  priority: number
  memberCount: number
  navigate: (route: Route) => void
}) {
  const { t } = useTranslation()

  return (
    <button
      type="button"
      className="role-row__main"
      onClick={() => navigate({ id: "role-editor", params: { roleId: role.id } })}
    >
      <RoleSigil color={role.color} size={40} />
      <span className="role-row__copy">
        <span>
          <strong>{role.name}</strong>
          <i>{t("rolesOverview.primaryMembers", { count: memberCount })}</i>
        </span>
        <small>
          {t("rolesOverview.permissionSummary", {
            count: enabledPermissions(role),
            rank: priority,
          })}
        </small>
      </span>
      <b>›</b>
    </button>
  )
}

function enabledPermissions(role: AccountRole) {
  return Object.values(role.permissions).filter(Boolean).length
}
