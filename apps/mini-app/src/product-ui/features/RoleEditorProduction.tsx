import { useMemo, useState } from "react"
import {
  createRole,
  deleteRole,
  updateRoleDetails,
  updateRolePermissions,
} from "@/shared/api/roleApi"
import { handleError } from "@/shared/errors/errorHandler"
import { useProductData } from "../data/ProductDataProvider"
import { permissionGroups } from "../mockData"
import type { RolePermission, Route } from "../model"
import { permissionLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"

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

const allPermissions = Array.from(
  new Set(permissionGroups.flatMap((group) => group.permissions)),
)

function emptyPermissions(): Record<RolePermission, boolean> {
  return Object.fromEntries(allPermissions.map((permission) => [permission, false])) as Record<RolePermission, boolean>
}

export function RoleEditorProduction({ params, navigate, back }: Props) {
  const product = useProductData()
  const accountId = product.activeAccount?.id ?? null
  const existing = product.roles.find((role) => role.id === params?.roleId) ?? null
  const isNew = params?.roleId === "new"
  const initialPermissions = existing
    ? { ...emptyPermissions(), ...existing.permissions }
    : emptyPermissions()
  const [name, setName] = useState(existing?.name ?? "")
  const [color, setColor] = useState(existing?.color ?? "#A985A8")
  const [permissions, setPermissions] = useState<Record<RolePermission, boolean>>(initialPermissions)
  const [query, setQuery] = useState("")
  const [busy, setBusy] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState(false)

  const selectedCount = Object.values(permissions).filter(Boolean).length
  const normalizedQuery = query.trim().toLowerCase()

  const visibleGroups = useMemo(
    () => permissionGroups
      .map((group) => ({
        ...group,
        permissions: group.permissions.filter((permission) =>
          !normalizedQuery
          || permissionLabels[permission].toLowerCase().includes(normalizedQuery)
          || permission.toLowerCase().includes(normalizedQuery),
        ),
      }))
      .filter((group) => group.permissions.length),
    [normalizedQuery],
  )

  const togglePermission = (permission: RolePermission) => {
    if (busy) return

    setPermissions((current) => ({
      ...current,
      [permission]: !current[permission],
    }))
  }

  const toggleGroup = (groupPermissions: RolePermission[]) => {
    if (busy) return

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
    if (busy) return

    setPermissions(Object.fromEntries(
      allPermissions.map((permission) => [permission, checked]),
    ) as Record<RolePermission, boolean>)
  }

  const save = async () => {
    const cleanName = name.trim()
    if (!accountId || !cleanName || busy) return

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
          allPermissions.filter((permission) => permissions[permission]),
        )
      }

      await product.refreshAccount()
      back()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const remove = async () => {
    if (!accountId || !existing || busy) return

    setBusy(true)

    try {
      await deleteRole(accountId, existing.id)
      await product.refreshAccount()
      back()
    } catch (error) {
      handleError(error)
      setBusy(false)
    }
  }

  if (!isNew && !existing && !product.loadingAccount) {
    return (
      <div className="screen role-editor-v2">
        <PageHeader title="Роль" back={back} />
        <section className="events-r2-empty">
          <Glyph name="roles" size={24} />
          <strong>Роль не знайдено</strong>
          <small>Список ролей міг змінитися.</small>
        </section>
      </div>
    )
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
            disabled={busy}
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
                disabled={busy}
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
                disabled={busy}
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
            <button disabled={busy} onClick={() => setAllPermissions(true)}>Усі</button>
            <button disabled={busy} onClick={() => setAllPermissions(false)}>Очистити</button>
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
                  <span><strong>{group.label}</strong><small>{enabledInGroup}/{group.permissions.length}</small></span>
                  <button
                    disabled={busy}
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
                      disabled={busy}
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
        <button className="role-editor-save-v2" disabled={!name.trim() || busy} onClick={() => void save()}>
          <Glyph name="check" size={16} />
          {busy ? "Зберігаємо…" : isNew ? "Створити роль" : "Зберегти зміни"}
        </button>

        {existing ? (
          <section className="role-editor-danger-v2">
            <span>
              <strong>Видалити роль</strong>
              <small>Учасники втратять цю роль після підтвердження backend.</small>
            </span>
            {confirmDelete ? (
              <div>
                <button disabled={busy} onClick={() => setConfirmDelete(false)}>Скасувати</button>
                <button disabled={busy} className="is-danger" onClick={() => void remove()}>Так, видалити</button>
              </div>
            ) : (
              <button disabled={busy} className="is-danger" onClick={() => setConfirmDelete(true)}>Видалити</button>
            )}
          </section>
        ) : null}
      </div>
    </div>
  )
}
