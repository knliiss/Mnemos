import { Reorder, useDragControls } from "framer-motion"
import { useState } from "react"
import { permissionGroups } from "../mockData"
import type { AccountRole, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import {
  enabledPermissions,
  loadMockRoles,
  memberCount,
  normalizeRoles,
  persistMockRoles,
} from "./roleMockStore"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function RolesOverviewOverhaul({ navigate, back }: Props) {
  const [roles, setRoles] = useState<AccountRole[]>(() => normalizeRoles(loadMockRoles()))
  const lockedRoles = roles.filter((role) => role.locked)
  const editableRoles = roles.filter((role) => !role.locked)
  const totalPermissions = new Set(
    permissionGroups.flatMap((group) => group.permissions),
  ).size

  const reorder = (nextEditable: AccountRole[]) => {
    setRoles(persistMockRoles([...lockedRoles, ...nextEditable]))
  }

  return (
    <div className="screen roles-v2">
      <PageHeader
        title="Ролі"
        back={back}
        actions={(
          <button
            className="roles-v2-create"
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
          <em>Вища роль має більший пріоритет. Системна роль власника завжди закріплена зверху.</em>
        </span>
      </section>

      <section className="roles-v2-guide">
        <span className="roles-v2-guide__handle" aria-hidden="true">⠿</span>
        <span>
          <strong>Порядок змінюється перетягуванням</strong>
          <small>Затисни ручку ⠿ праворуч і перетягни роль вище або нижче.</small>
        </span>
      </section>

      <section className="roles-v2-stack">
        {lockedRoles.map((role) => (
          <article className="role-v2-row is-system" key={role.id}>
            <button
              className="role-v2-row__main"
              onClick={() => navigate({ id: "role-editor", params: { roleId: role.id } })}
            >
              <span className="role-v2-row__swatch" style={{ background: role.color }} />
              <span className="role-v2-row__copy">
                <span>
                  <strong>{role.name}</strong>
                  <i>Системна</i>
                </span>
                <small>{enabledPermissions(role)} дозволів · {memberCount(role)} учасник</small>
              </span>
              <span className="role-v2-row__rank">#{role.hierarchyRank}</span>
              <b>›</b>
            </button>
            <span className="role-v2-row__pin" title="Закріплена роль">
              <Glyph name="check" size={14} />
            </span>
          </article>
        ))}

        <Reorder.Group
          axis="y"
          values={editableRoles}
          onReorder={reorder}
          className="role-v2-reorder"
        >
          {editableRoles.map((role) => (
            <SortableRoleRow
              key={role.id}
              role={role}
              navigate={navigate}
            />
          ))}
        </Reorder.Group>
      </section>

      <button
        className="roles-v2-create-card"
        onClick={() => navigate({ id: "role-editor", params: { roleId: "new" } })}
      >
        <span><Glyph name="plus" size={20} /></span>
        <span>
          <strong>Створити нову роль</strong>
          <small>Можна почати з шаблону або налаштувати дозволи вручну.</small>
        </span>
        <b>›</b>
      </button>
    </div>
  )
}

function SortableRoleRow({ role, navigate }: {
  role: AccountRole
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
        <span className="role-v2-row__swatch" style={{ background: role.color }} />
        <span className="role-v2-row__copy">
          <span>
            <strong>{role.name}</strong>
            <i>{memberCount(role)} уч.</i>
          </span>
          <small>{enabledPermissions(role)} дозволів · пріоритет {role.hierarchyRank}</small>
        </span>
        <b>›</b>
      </button>

      <button
        type="button"
        className="role-v2-drag-handle"
        aria-label={`Перетягнути роль ${role.name}`}
        title="Перетягнути"
        onPointerDown={(event) => dragControls.start(event)}
      >
        ⠿
      </button>
    </Reorder.Item>
  )
}
