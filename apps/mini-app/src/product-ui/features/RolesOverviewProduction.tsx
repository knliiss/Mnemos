import { Reorder, useDragControls } from "framer-motion"
import { useEffect, useMemo, useState } from "react"
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
  const [roles, setRoles] = useState<AccountRole[]>([])
  const [savingOrder, setSavingOrder] = useState(false)

  useEffect(() => {
    setRoles([...product.roles].sort((left, right) => left.hierarchyRank - right.hierarchyRank))
  }, [product.roles])

  const totalPermissions = useMemo(
    () => new Set(permissionGroups.flatMap((group) => group.permissions)).size,
    [],
  )

  const reorder = async (nextRoles: AccountRole[]) => {
    if (!accountId || savingOrder) return

    setRoles(nextRoles)
    setSavingOrder(true)

    try {
      await updateRoleHierarchy(accountId, nextRoles.map((role) => role.id))
      await product.refreshAccount()
    } catch (error) {
      handleError(error)
      await product.refreshAccount()
    } finally {
      setSavingOrder(false)
    }
  }

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
          <strong>{savingOrder ? "Зберігаємо порядок…" : "Порядок змінюється перетягуванням"}</strong>
          <small>Затисни ручку ⠿ праворуч і перетягни роль вище або нижче.</small>
        </span>
      </section>

      <section className="roles-v2-stack">
        <Reorder.Group
          axis="y"
          values={roles}
          onReorder={(nextRoles) => void reorder(nextRoles)}
          className="role-v2-reorder"
        >
          {roles.map((role) => (
            <SortableRoleRow
              key={role.id}
              role={role}
              memberCount={product.members.filter((member) => member.highestRoleId === role.id).length}
              disabled={savingOrder}
              navigate={navigate}
            />
          ))}
        </Reorder.Group>
      </section>

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

function SortableRoleRow({
  role,
  memberCount,
  disabled,
  navigate,
}: {
  role: AccountRole
  memberCount: number
  disabled: boolean
  navigate: (route: Route) => void
}) {
  const dragControls = useDragControls()

  return (
    <Reorder.Item
      value={role}
      dragListener={false}
      dragControls={dragControls}
      className="role-v2-row"
      whileDrag={{ scale: 1.02, zIndex: 5 }}
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
