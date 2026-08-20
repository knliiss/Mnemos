import { useMemo, useState } from "react"
import { initialRoles, permissionGroups } from "../mockData"
import type { AccountRole, RolePermission, Route } from "../model"
import { permissionLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"
import {
  createEmptyPermissions,
  loadMockRoles,
  normalizeRoles,
  persistMockRoles,
  roleColors,
  rolePresets,
  type RolePreset,
} from "./roleMockStore"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

export function RoleEditorOverhaul({ params, navigate, back }: Props) {
  const [roles, setRoles] = useState<AccountRole[]>(() => normalizeRoles(loadMockRoles()))
  const existing = roles.find((role) => role.id === params?.roleId)
  const isNew = params?.roleId === "new" || !existing
  const readOnly = existing?.locked === true
  const [name, setName] = useState(existing?.name ?? "")
  const [color, setColor] = useState(existing?.color ?? "#A985A8")
  const [permissions, setPermissions] = useState<Record<RolePermission, boolean>>(
    existing?.permissions ?? createEmptyPermissions(),
  )
  const [query, setQuery] = useState("")
  const [confirmDelete, setConfirmDelete] = useState(false)

  const selectedCount = Object.values(permissions).filter(Boolean).length
  const allPermissionCount = Object.keys(permissions).length
  const normalizedQuery = query.trim().toLowerCase()

  const visibleGroups = useMemo(
    () => permissionGroups
      .map((group) => ({
        ...group,
        permissions: group.permissions.filter((permission) =>
          !normalizedQuery
          || permissionLabels[permission].toLowerCase().includes(normalizedQuery),
        ),
      }))
      .filter((group) => group.permissions.length),
    [normalizedQuery],
  )

  const updateStoredRoles = (next: AccountRole[]) => {
    const normalized = persistMockRoles(next)
    setRoles(normalized)
    return normalized
  }

  const applyPreset = (preset: RolePreset) => {
    if (readOnly) return

    const source = initialRoles.find((role) => role.id === preset.sourceRoleId)
    if (!source) return

    setPermissions({ ...source.permissions })
    setColor(source.color)

    if (!name.trim()) {
      setName(preset.title)
    }
  }

  const togglePermission = (permission: RolePermission) => {
    if (readOnly) return

    setPermissions((current) => ({
      ...current,
      [permission]: !current[permission],
    }))
  }

  const toggleGroup = (groupPermissions: RolePermission[]) => {
    if (readOnly) return

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
    if (readOnly) return

    setPermissions((current) => Object.fromEntries(
      Object.keys(current).map((permission) => [permission, checked]),
    ) as Record<RolePermission, boolean>)
  }

  const save = () => {
    if (readOnly || !name.trim()) return

    if (existing) {
      updateStoredRoles(roles.map((role) => role.id === existing.id
        ? {
            ...role,
            name: name.trim(),
            color,
            permissions,
          }
        : role))
    } else {
      updateStoredRoles([
        ...roles,
        {
          id: `role-${Date.now()}`,
          name: name.trim(),
          color,
          hierarchyRank: 20,
          permissions,
        },
      ])
    }

    back()
  }

  const remove = () => {
    if (!existing || existing.locked) return

    updateStoredRoles(roles.filter((role) => role.id !== existing.id))
    back()
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
          <small>{readOnly ? "СИСТЕМНА РОЛЬ" : isNew ? "НОВА РОЛЬ" : "НАЛАШТУВАННЯ РОЛІ"}</small>
          <strong style={{ color }}>{name.trim() || "Назва ролі"}</strong>
          <em>{selectedCount} із {allPermissionCount} дозволів увімкнено</em>
        </span>
        <div className="role-editor-v2-progress" aria-hidden="true">
          <i style={{ width: `${(selectedCount / Math.max(1, allPermissionCount)) * 100}%`, background: color }} />
        </div>
      </section>

      {readOnly ? (
        <section className="role-editor-readonly">
          <span><Glyph name="check" size={17} /></span>
          <span>
            <strong>Системна роль захищена</strong>
            <small>Її можна переглядати, але назва, колір і дозволи не редагуються.</small>
          </span>
        </section>
      ) : null}

      {isNew ? (
        <section className="role-preset-panel">
          <header>
            <span>
              <small>ШВИДКИЙ СТАРТ</small>
              <strong>Почати з шаблону</strong>
            </span>
          </header>
          <div className="role-preset-grid">
            {rolePresets.map((preset) => {
              const source = initialRoles.find((role) => role.id === preset.sourceRoleId)

              return (
                <button key={preset.id} onClick={() => applyPreset(preset)}>
                  <RoleSigil color={source?.color ?? "#8A877F"} size={34} compact />
                  <strong>{preset.title}</strong>
                  <small>{preset.note}</small>
                  <b>›</b>
                </button>
              )
            })}
          </div>
        </section>
      ) : null}

      <section className="role-identity-panel">
        <header>
          <span>
            <small>ВИГЛЯД</small>
            <strong>Назва та колір</strong>
          </span>
        </header>

        <label className="role-editor-field">
          <span>Назва</span>
          <input
            value={name}
            maxLength={32}
            disabled={readOnly}
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
                disabled={readOnly}
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
                disabled={readOnly}
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
          <span>
            <small>ДОЗВОЛИ</small>
            <strong>{selectedCount} увімкнено</strong>
          </span>
          {!readOnly ? (
            <div>
              <button onClick={() => setAllPermissions(true)}>Усі</button>
              <button onClick={() => setAllPermissions(false)}>Очистити</button>
            </div>
          ) : null}
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
                  {!readOnly ? (
                    <button
                      className={allEnabled ? "is-active" : ""}
                      onClick={() => toggleGroup(group.permissions)}
                    >
                      {allEnabled ? "Вимкнути" : "Увімкнути групу"}
                    </button>
                  ) : null}
                </header>

                <div>
                  {group.permissions.map((permission) => (
                    <button
                      key={permission}
                      type="button"
                      className={`role-permission-row-v2 ${permissions[permission] ? "is-on" : ""}`}
                      disabled={readOnly}
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

      {!readOnly ? (
        <div className="role-editor-actions-v2">
          <button className="role-editor-save-v2" disabled={!name.trim()} onClick={save}>
            <Glyph name="check" size={16} />
            {isNew ? "Створити роль" : "Зберегти зміни"}
          </button>

          {existing && !existing.locked ? (
            <section className="role-editor-danger-v2">
              <span>
                <strong>Видалити роль</strong>
                <small>Учасники втратять цю роль. Дія в мокапі застосовується одразу.</small>
              </span>
              {confirmDelete ? (
                <div>
                  <button onClick={() => setConfirmDelete(false)}>Скасувати</button>
                  <button className="is-danger" onClick={remove}>Так, видалити</button>
                </div>
              ) : (
                <button className="is-danger" onClick={() => setConfirmDelete(true)}>Видалити</button>
              )}
            </section>
          ) : null}
        </div>
      ) : null}
    </div>
  )
}
