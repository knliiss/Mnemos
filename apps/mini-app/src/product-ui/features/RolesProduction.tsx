import { Reorder, useDragControls } from "framer-motion"
import { useMemo, useState } from "react"
import {
  createAccountRole,
  deleteAccountRole,
  updateAccountRoleDetails,
  updateAccountRoleHierarchy,
  updateAccountRolePermissions,
} from "@/shared/api/productApi"
import { handleError } from "@/shared/errors/errorHandler"
import { permissionGroups } from "../mockData"
import { useProductData } from "../data/ProductDataProvider"
import type { AccountRole, RolePermission, Route } from "../model"
import { permissionLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"

const roleColors = [
  "#A985A8",
  "#CFFF35",
  "#E9AD43",
  "#D7A0DE",
  "#8BB9E8",
  "#E88787",
  "#8A9283",
] as const

const allPermissions = permissionGroups.flatMap((group) => group.permissions)

export function RolesOverviewProduction({ navigate, back }: {
  navigate: (route: Route) => void
  back: () => void
}) {
  const product = useProductData()
  const activeAccount = product.activeAccount
  const roles = useMemo(
    () => [...product.roles].sort((left, right) => left.hierarchyRank - right.hierarchyRank),
    [product.roles],
  )

  if (!activeAccount) return null

  const memberCounts = new Map<string, number>()
  product.members.forEach((member) => {
    if (!member.highestRoleId) return
    memberCounts.set(member.highestRoleId, (memberCounts.get(member.highestRoleId) ?? 0) + 1)
  })

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
          <strong>{roles.length} ролей · {allPermissions.length} можливих дозволів</strong>
          <em>Порядок ролей зберігається на сервері. Вищі ролі мають більший пріоритет.</em>
        </span>
      </section>

      <section className="roles-v2-guide">
        <span className="roles-v2-guide__handle" aria-hidden="true">⠿</span>
        <span>
          <strong>Порядок змінюється перетягуванням</strong>
          <small>Затисни ручку праворуч. Новий порядок збережеться після завершення перетягування.</small>
        </span>
      </section>

      <RoleReorderList
        key={roles.map((role) => `${role.id}:${role.hierarchyRank}`).join("|")}
        accountId={activeAccount.id}
        initialRoles={roles}
        memberCounts={memberCounts}
        navigate={navigate}
        refresh={product.refreshAccount}
      />

      <button
        className="roles-v2-create-card"
        onClick={() => navigate({ id: "role-editor", params: { roleId: "new" } })}
      >
        <span><Glyph name="plus" size={20} /></span>
        <span>
          <strong>Створити нову роль</strong>
          <small>Задай назву, колір і точний набір дозволів.</small>
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
  const [saving, setSaving] = useState(false)

  const persist = async () => {
    if (saving) return

    setSaving(true)

    try {
      await updateAccountRoleHierarchy(accountId, roles.map((role) => role.id))
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className={`roles-v2-stack ${saving ? "is-saving" : ""}`}>
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
  navigate,
  onDragEnd,
}: {
  role: AccountRole
  memberCount: number
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
        aria-label={`Перетягнути роль ${role.name}`}
        title="Перетягнути"
        onPointerDown={(event) => dragControls.start(event)}
      >
        ⠿
      </button>
    </Reorder.Item>
  )
}

export function RoleEditorProduction({ params, navigate, back }: {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}) {
  const product = useProductData()
  const activeAccount = product.activeAccount
  const existing = product.roles.find((role) => role.id === params?.roleId) ?? null
  const isNew = params?.roleId === "new" || !existing
  const [name, setName] = useState(existing?.name ?? "")
  const [color, setColor] = useState(existing?.color ?? roleColors[0])
  const [permissions, setPermissions] = useState<Record<RolePermission, boolean>>(
    () => createPermissionState(existing),
  )
  const [query, setQuery] = useState("")
  const [saving, setSaving] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState(false)

  const selectedCount = Object.values(permissions).filter(Boolean).length
  const normalizedQuery = query.trim().toLowerCase()
  const visibleGroups = useMemo(
    () => permissionGroups
      .map((group) => ({
        ...group,
        permissions: group.permissions.filter((permission) =>
          !normalizedQuery || permissionLabels[permission].toLowerCase().includes(normalizedQuery),
        ),
      }))
      .filter((group) => group.permissions.length),
    [normalizedQuery],
  )

  if (!activeAccount) return null

  const togglePermission = (permission: RolePermission) => {
    setPermissions((current) => ({
      ...current,
      [permission]: !current[permission],
    }))
  }

  const toggleGroup = (groupPermissions: RolePermission[]) => {
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
    setPermissions(Object.fromEntries(
      allPermissions.map((permission) => [permission, checked]),
    ) as Record<RolePermission, boolean>)
  }

  const save = async () => {
    const cleanName = name.trim()
    if (!cleanName || saving) return

    setSaving(true)

    try {
      if (existing) {
        await updateAccountRoleDetails(activeAccount.id, existing.id, cleanName, color)
        await updateAccountRolePermissions(activeAccount.id, existing.id, permissions)
      } else {
        const enabled = allPermissions.filter((permission) => permissions[permission])
        await createAccountRole(activeAccount.id, cleanName, color, enabled)
      }

      await product.refreshAccount()
      back()
    } catch (error) {
      handleError(error)
    } finally {
      setSaving(false)
    }
  }

  const remove = async () => {
    if (!existing || saving) return

    setSaving(true)

    try {
      await deleteAccountRole(activeAccount.id, existing.id)
      await product.refreshAccount()
      navigate({ id: "roles" })
    } catch (error) {
      handleError(error)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="screen role-editor-v2">
      <PageHeader
        title={isNew ? "Нова роль" : existing?.name ?? "Роль"}
        back={back}
        actions={!isNew ? (
          <button className="role-editor-v2-back-to-list" onClick={() => navigate({ id: "roles" })}>
            Усі ролі
          </button>
        ) : undefined}
      />

      <section className="role-editor-v2-hero">
        <RoleSigil color={color} size={48} />
        <span className="role-editor-v2-hero__copy">
          <small>{isNew ? "НОВА РОЛЬ" : "НАЛАШТУВАННЯ РОЛІ"}</small>
          <strong style={{ color }}>{name.trim() || "Назва ролі"}</strong>
          <em>{selectedCount} із {allPermissions.length} дозволів увімкнено</em>
        </span>
        <div className="role-editor-v2-progress" aria-hidden="true">
          <i style={{ width: `${(selectedCount / Math.max(1, allPermissions.length)) * 100}%`, background: color }} />
        </div>
      </section>

      <section className="role-identity-panel">
        <header>
          <span><small>ВИГЛЯД</small><strong>Назва та колір</strong></span>
        </header>

        <label className="role-editor-field">
          <span>Назва</span>
          <input
            value={name}
            maxLength={32}
            onChange={(event) => setName(event.target.value)}
            placeholder="Наприклад, Рейд-менеджер"
          />
          <small>{name.length}/32</small>
        </label>

        <div className="role-color-editor">
          <label>
            <span>Колір</span>
            <span className="role-color-input">
              <input
                type="color"
                value={color.slice(0, 7)}
                onChange={(event) => setColor(event.target.value.toUpperCase())}
              />
              <code>{color}</code>
            </span>
          </label>

          <div className="role-color-presets" aria-label="Швидкі кольори">
            {roleColors.map((presetColor) => (
              <button
                key={presetColor}
                type="button"
                className={color.toUpperCase() === presetColor ? "is-active" : ""}
                style={{ background: presetColor }}
                aria-label={`Колір ${presetColor}`}
                onClick={() => setColor(presetColor)}
              />
            ))}
          </div>
        </div>
      </section>

      <section className="role-permissions-panel">
        <header className="role-permissions-panel__head">
          <span><small>ДОЗВОЛИ</small><strong>{selectedCount} увімкнено</strong></span>
          <div>
            <button onClick={() => setAllPermissions(true)}>Усі</button>
            <button onClick={() => setAllPermissions(false)}>Очистити</button>
          </div>
        </header>

        <label className="role-permission-search">
          <Glyph name="search" size={16} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Знайти дозвіл"
          />
          {query ? (
            <button onClick={() => setQuery("")} aria-label="Очистити пошук">
              <Glyph name="close" size={14} />
            </button>
          ) : null}
        </label>

        <div className="role-permission-groups-v2">
          {visibleGroups.map((group) => {
            const enabledInGroup = group.permissions.filter((permission) => permissions[permission]).length
            const allEnabled = enabledInGroup === group.permissions.length

            return (
              <section key={group.label}>
                <header>
                  <span>
                    <strong>{group.label}</strong>
                    <small>{enabledInGroup}/{group.permissions.length}</small>
                  </span>
                  <button
                    className={allEnabled ? "is-active" : ""}
                    onClick={() => toggleGroup(group.permissions)}
                  >
                    {allEnabled ? "Вимкнути" : "Увімкнути групу"}
                  </button>
                </header>

                <div>
                  {group.permissions.map((permission) => (
                    <button
                      key={permission}
                      type="button"
                      className={`role-permission-row-v2 ${permissions[permission] ? "is-on" : ""}`}
                      onClick={() => togglePermission(permission)}
                    >
                      <span>
                        <strong>{permissionLabels[permission]}</strong>
                        <small>{permission}</small>
                      </span>
                      <i aria-hidden="true"><b /></i>
                    </button>
                  ))}
                </div>
              </section>
            )
          })}
        </div>
      </section>

      <div className="role-editor-actions-v2">
        <button className="role-editor-save-v2" disabled={!name.trim() || saving} onClick={() => void save()}>
          <Glyph name="check" size={16} />
          {saving ? "Зберігаємо…" : isNew ? "Створити роль" : "Зберегти зміни"}
        </button>

        {existing ? (
          <section className="role-editor-danger-v2">
            <span>
              <strong>Видалити роль</strong>
              <small>Учасники втратять цю роль. Сервер перевірить права та ієрархію перед видаленням.</small>
            </span>
            {confirmDelete ? (
              <div>
                <button disabled={saving} onClick={() => setConfirmDelete(false)}>Скасувати</button>
                <button className="is-danger" disabled={saving} onClick={() => void remove()}>Так, видалити</button>
              </div>
            ) : (
              <button className="is-danger" disabled={saving} onClick={() => setConfirmDelete(true)}>Видалити</button>
            )}
          </section>
        ) : null}
      </div>
    </div>
  )
}

function createPermissionState(role: AccountRole | null): Record<RolePermission, boolean> {
  return Object.fromEntries(
    allPermissions.map((permission) => [permission, Boolean(role?.permissions[permission])]),
  ) as Record<RolePermission, boolean>
}

function enabledPermissions(role: AccountRole) {
  return Object.values(role.permissions).filter(Boolean).length
}
