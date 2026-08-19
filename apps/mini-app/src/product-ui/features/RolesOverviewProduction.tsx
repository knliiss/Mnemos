import { Reorder, useDragControls } from "framer-motion"
import { useMemo, useRef, useState } from "react"
import { updateRoleHierarchy } from "@/shared/api/roleApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useProductData } from "../data/ProductDataProvider"
import { permissionGroups } from "../mockData"
import type { AccountRole, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function RolesOverviewProduction({ navigate, back }: Props) {
  const product = useProductData()
  const accountId = product.activeAccount?.id ?? null
  const roles = useMemo(
    () => [...product.roles].sort((left, right) => left.hierarchyRank - right.hierarchyRank),
    [product.roles],
  )
  const totalPermissions = useMemo(
    () => new Set(permissionGroups.flatMap((group) => group.permissions)).size,
    [],
  )

  return (
    <div className="screen roles-v2">
      <PageHeader
        title="Ролі"
        back={back}
        actions={(
          <button
            className="roles-v2-create"
            disabled={!accountId}
            onClick={() => navigate({ id: "role-editor", params: { roleId: "new" } })}
          >
            <Glyph name="plus" size={16} />
            Нова
          </button>
        )}
      />

      <section className="roles-v2-hero">
        <span className="roles-v2-hero__icon"><Glyph name="roles" size={23} /></span>
        <span className="roles-v2-hero__copy">
          <small>ІЄРАРХІЯ ДОСТУПУ</small>
          <strong>{roles.length} ролі · {totalPermissions} можливих дозволів</strong>
          <em>Вища позиція має більший пріоритет. Власник аккаунта залишається над ролями.</em>
        </span>
      </section>

      <section className="roles-v2-guide">
        <span className="roles-v2-guide__handle" aria-hidden="true">⠿</span>
        <span>
          <strong>Порядок змінюється перетягуванням</strong>
          <small>Затисни ручку ⠿ праворуч. Порядок відправиться на backend після завершення перетягування.</small>
        </span>
      </section>

      {accountId && roles.length ? (
        <RoleReorderList
          key={roles.map((role) => `${role.id}:${role.hierarchyRank}`).join("|")}
          accountId={accountId}
          initialRoles={roles}
          memberCounts={new Map(roles.map((role) => [
            role.id,
            product.members.filter((member) => member.highestRoleId === role.id).length,
          ]))}
          navigate={navigate}
          refresh={product.refreshAccount}
        />
      ) : null}

      {!roles.length && !product.loadingAccount ? (
        <section className="events-r2-empty">
          <Glyph name="roles" size={24} />
          <strong>Ролей ще немає</strong>
          <small>Створи першу роль і налаштуй її дозволи.</small>
        </section>
      ) : null}

      <button
        className="roles-v2-create-card"
        disabled={!accountId}
        onClick={() => navigate({ id: "role-editor", params: { roleId: "new" } })}
      >
        <span><Glyph name="plus" size={20} /></span>
        <span>
          <strong>Створити нову роль</strong>
          <small>Назва, колір і набір дозволів зберігаються на backend.</small>
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
  navigate,
  refresh,
}: {
  accountId: string
  initialRoles: AccountRole[]
  memberCounts: Map<string, number>
  navigate: (route: Route) => void
  refresh: () => Promise<void>
}) {
  const [roles, setRoles] = useState(initialRoles)
  const [savingOrder, setSavingOrder] = useState(false)
  const rolesRef = useRef(roles)
  rolesRef.current = roles

  const persist = async () => {
    if (savingOrder) return

    setSavingOrder(true)

    try {
      await updateRoleHierarchy(accountId, rolesRef.current.map((role) => role.id))
      await refresh()
    } catch (error) {
      handleError(error)
      await refresh()
    } finally {
      setSavingOrder(false)
    }
  }

  return (
    <section className="roles-v2-stack">
      <Reorder.Group
        axis="y"
        values={roles}
        onReorder={setRoles}
        className="role-v2-reorder"
      >
        {roles.map((role) => (
          <SortableRoleRow
            key={role.id}
            role={role}
            memberCount={memberCounts.get(role.id) ?? 0}
            disabled={savingOrder}
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
  memberCount: number
  disabled: boolean
  navigate: (route: Route) => void
  onDragEnd: () => void
}) {
  const dragControls = useDragControls()

  return (
    <Reorder.Item
      value={role}
      dragListener={false}
      dragControls={dragControls}
      className="role-v2-row"
      whileDrag={{ scale: 1.02, zIndex: 5 }}
      onDragEnd={onDragEnd}
    >
      <button
        className="role-v2-row__main"
        onClick={() => navigate({ id: "role-editor", params: { roleId: role.id } })}
      >
        <RoleSigil color={role.color} size={40} />
        <span className="role-v2-row__copy">
          <span>
            <strong>{role.name}</strong>
            <i>{memberCount} осн. уч.</i>
          </span>
          <small>{enabledPermissions(role)} дозволів · пріоритет {role.hierarchyRank}</small>
        </span>
        <b>›</b>
      </button>

      <button
        type="button"
        className="role-v2-drag-handle"
        disabled={disabled}
        aria-label={`Перетягнути роль ${role.name}`}
        title="Перетягнути"
        onPointerDown={(event) => {
          if (!disabled) dragControls.start(event)
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
