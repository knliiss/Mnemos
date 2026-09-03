import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  assignAdminMemberRole,
  clearAdminBaseRole,
  createAdminRole,
  deleteAdminRole,
  revokeAdminMemberRole,
  updateAdminBaseRole,
  updateAdminRoleDetails,
  updateAdminRoleHierarchy,
  updateAdminRolePermissions,
} from "@/shared/api/adminRoleApi"
import {
  getAdminAccountRoles,
  getAdminMemberRoles,
  type AdminAccountDetailsResponse,
  type AdminAccountRoleResponse,
} from "@/shared/api/moderationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { allRolePermissions } from "../../rolePermissions"
import { AdminRoleColorControl } from "./AdminRoleColorControl"
import "./moderation-admin-role-polish.css"

type Props = {
  details: AdminAccountDetailsResponse
  onChanged: () => Promise<void>
}

type MemberRoleState = Record<string, Set<string>>

export function ModerationAccountRolesPanel({ details, onChanged }: Props) {
  const { t } = useTranslation()
  const [roles, setRoles] = useState<AdminAccountRoleResponse[]>([])
  const [memberRoles, setMemberRoles] = useState<MemberRoleState>({})
  const [loading, setLoading] = useState(true)
  const [busyKey, setBusyKey] = useState<string | null>(null)
  const [newRoleName, setNewRoleName] = useState("")
  const [newRoleColor, setNewRoleColor] = useState("#4BAF8A")
  const [newPermissions, setNewPermissions] = useState<Set<string>>(new Set())

  const sortedRoles = useMemo(
    () => [...roles].sort((left, right) => left.hierarchyRank - right.hierarchyRank),
    [roles],
  )

  const loadRoleState = useCallback(async () => {
    const loadedRoles = await getAdminAccountRoles(details.accountId)
    const assignments = await Promise.all(
      details.members.map(async (member) => [
        member.memberId,
        await getAdminMemberRoles(details.accountId, member.memberId),
      ] as const),
    )

    return {
      roles: loadedRoles,
      memberRoles: Object.fromEntries(
        assignments.map(([memberId, assignedRoles]) => [
          memberId,
          new Set(assignedRoles.map((role) => role.id)),
        ]),
      ) as MemberRoleState,
    }
  }, [details.accountId, details.members])

  const refresh = useCallback(async () => {
    setLoading(true)

    try {
      const state = await loadRoleState()
      setRoles(state.roles)
      setMemberRoles(state.memberRoles)
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [loadRoleState])

  useEffect(() => {
    let active = true

    void loadRoleState()
      .then((state) => {
        if (active) {
          setRoles(state.roles)
          setMemberRoles(state.memberRoles)
        }
      })
      .catch((error: unknown) => {
        if (active) {
          handleError(error)
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false)
        }
      })

    return () => {
      active = false
    }
  }, [loadRoleState])

  const runMutation = async (key: string, mutation: () => Promise<void>) => {
    if (busyKey) {
      return
    }

    setBusyKey(key)

    try {
      await mutation()
      await Promise.all([refresh(), onChanged()])
    } catch (error) {
      handleError(error)
    } finally {
      setBusyKey(null)
    }
  }

  const submitNewRole = async () => {
    const name = newRoleName.trim()

    if (!name || !isRoleColor(newRoleColor) || busyKey) {
      return
    }

    await runMutation("create", async () => {
      await createAdminRole(details.accountId, {
        name,
        color: newRoleColor,
        permissions: [...newPermissions],
      })
      setNewRoleName("")
      setNewRoleColor("#4BAF8A")
      setNewPermissions(new Set())
    })
  }

  const moveRole = async (roleId: string, direction: -1 | 1) => {
    const currentIndex = sortedRoles.findIndex((role) => role.id === roleId)
    const targetIndex = currentIndex + direction

    if (currentIndex < 0 || targetIndex < 0 || targetIndex >= sortedRoles.length) {
      return
    }

    const ordered = [...sortedRoles]
    const [moved] = ordered.splice(currentIndex, 1)
    ordered.splice(targetIndex, 0, moved)

    await runMutation(`hierarchy:${roleId}`, async () => {
      await updateAdminRoleHierarchy(
        details.accountId,
        ordered.map((role) => role.id),
      )
    })
  }

  const togglePermission = async (role: AdminAccountRoleResponse, permission: string) => {
    await runMutation(`permission:${role.id}:${permission}`, async () => {
      await updateAdminRolePermissions(details.accountId, role.id, {
        permissions: {
          [permission]: !role.permissions[permission],
        },
      })
    })
  }

  const toggleMemberRole = async (memberId: string, roleId: string) => {
    const assigned = memberRoles[memberId]?.has(roleId) ?? false

    await runMutation(`member:${memberId}:${roleId}`, async () => {
      if (assigned) {
        await revokeAdminMemberRole(details.accountId, memberId, roleId)
      } else {
        await assignAdminMemberRole(details.accountId, memberId, roleId)
      }
    })
  }

  if (details.status === "DELETED") {
    return null
  }

  return (
    <section className="moderation-admin-roles">
      <div className="moderation-admin-roles__heading moderation-admin-roles__heading--illustrated">
        <span className="moderation-admin-roles__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-event.svg" alt="" />
        </span>
        <span>
          <small>{t("rolesOverview.hierarchyEyebrow")}</small>
          <strong>{t("rolesOverview.title")}</strong>
        </span>
        <em>{sortedRoles.length}</em>
      </div>

      <div className="moderation-admin-role-create moderation-admin-role-create--custom">
        <label className="moderation-admin-role-field">
          <span>{t("roleEditor.name")}</span>
          <input
            aria-label={t("roleEditor.name")}
            className="moderation-admin-input"
            maxLength={32}
            placeholder={t("roleEditor.namePlaceholder")}
            value={newRoleName}
            onChange={(event) => setNewRoleName(event.target.value)}
          />
        </label>

        <label className="moderation-admin-role-field">
          <span>{t("roleEditor.color")}</span>
          <AdminRoleColorControl value={newRoleColor} onChange={setNewRoleColor} />
        </label>

        <button
          type="button"
          className="moderation-control-button moderation-control-button--positive"
          disabled={!newRoleName.trim() || !isRoleColor(newRoleColor) || Boolean(busyKey)}
          onClick={() => void submitNewRole()}
        >
          {busyKey === "create" ? t("roleEditor.saving") : t("roleEditor.create")}
        </button>
      </div>

      <div className="moderation-admin-permission-picker">
        {allRolePermissions.map((permission) => (
          <button
            key={permission}
            type="button"
            className={newPermissions.has(permission) ? "is-active" : ""}
            onClick={() =>
              setNewPermissions((current) => {
                const next = new Set(current)

                if (next.has(permission)) {
                  next.delete(permission)
                } else {
                  next.add(permission)
                }

                return next
              })
            }
          >
            {t(`roleEditor.permissions.${permission}`)}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="moderation-admin-role-empty">{t("common.loading")}</div>
      ) : sortedRoles.length === 0 ? (
        <div className="moderation-admin-role-empty moderation-admin-role-empty--illustrated">
          <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
          <strong>{t("rolesOverview.emptyTitle")}</strong>
        </div>
      ) : (
        <div className="moderation-admin-role-list">
          {sortedRoles.map((role, index) => (
            <AdminRoleCard
              key={`${role.id}:${role.updatedAt}`}
              role={role}
              index={index}
              roleCount={sortedRoles.length}
              isBaseRole={details.baseRoleId === role.id}
              busy={Boolean(busyKey)}
              onMove={(direction) => void moveRole(role.id, direction)}
              onTogglePermission={(permission) => void togglePermission(role, permission)}
              onSaveDetails={(name, color) =>
                void runMutation(`details:${role.id}`, async () => {
                  await updateAdminRoleDetails(details.accountId, role.id, { name, color })
                })
              }
              onToggleBase={() =>
                void runMutation(`base:${role.id}`, async () => {
                  if (details.baseRoleId === role.id) {
                    await clearAdminBaseRole(details.accountId)
                  } else {
                    await updateAdminBaseRole(details.accountId, role.id)
                  }
                })
              }
              onDelete={() =>
                void runMutation(`delete:${role.id}`, async () => {
                  await deleteAdminRole(details.accountId, role.id)
                })
              }
            />
          ))}
        </div>
      )}

      {sortedRoles.length > 0 ? (
        <div className="moderation-admin-member-roles">
          <div className="moderation-admin-roles__heading">
            <span>
              <small>{t("roleEditor.groups.members")}</small>
              <strong>{t("roleEditor.permissions.ASSIGN_MEMBER_ROLES")}</strong>
            </span>
          </div>

          {details.members.map((member) => (
            <article key={member.memberId} className="moderation-admin-member-role-row">
              <span>
                <strong>@{member.username}</strong>
                <small>{member.owner ? t("moderation.owner") : member.memberId}</small>
              </span>
              <div>
                {sortedRoles.map((role) => {
                  const assigned = memberRoles[member.memberId]?.has(role.id) ?? false

                  return (
                    <button
                      key={role.id}
                      type="button"
                      className={assigned ? "is-active" : ""}
                      disabled={Boolean(busyKey)}
                      onClick={() => void toggleMemberRole(member.memberId, role.id)}
                    >
                      <i style={{ background: role.color }} />
                      {role.name}
                    </button>
                  )
                })}
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  )
}

type AdminRoleCardProps = {
  role: AdminAccountRoleResponse
  index: number
  roleCount: number
  isBaseRole: boolean
  busy: boolean
  onMove: (direction: -1 | 1) => void
  onTogglePermission: (permission: string) => void
  onSaveDetails: (name: string, color: string) => void
  onToggleBase: () => void
  onDelete: () => void
}

function AdminRoleCard({
  role,
  index,
  roleCount,
  isBaseRole,
  busy,
  onMove,
  onTogglePermission,
  onSaveDetails,
  onToggleBase,
  onDelete,
}: AdminRoleCardProps) {
  const { t } = useTranslation()
  const [name, setName] = useState(role.name)
  const [color, setColor] = useState(role.color.slice(0, 7))
  const [baseConfirmationVisible, setBaseConfirmationVisible] = useState(false)
  const [deleteConfirmationVisible, setDeleteConfirmationVisible] = useState(false)
  const baseConfirmRef = useRef<HTMLButtonElement>(null)
  const deleteConfirmRef = useRef<HTMLButtonElement>(null)
  const baseConfirmationTitle = isBaseRole
    ? t("roleEditor.confirmClearBaseRole", "Clear base role?")
    : t("roleEditor.confirmSetBaseRole", "Set as base role?")
  const deleteConfirmationTitle = t("roleEditor.confirmDelete", "Delete this role?")

  useEffect(() => {
    if (!baseConfirmationVisible) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      baseConfirmRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [baseConfirmationVisible])

  useEffect(() => {
    if (!deleteConfirmationVisible) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      deleteConfirmRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [deleteConfirmationVisible])

  const dismissBaseConfirmation = () => {
    if (!busy) {
      setBaseConfirmationVisible(false)
    }
  }

  const dismissDeleteConfirmation = () => {
    if (!busy) {
      setDeleteConfirmationVisible(false)
    }
  }

  return (
    <article className="moderation-admin-role-card">
      <header>
        <span className="moderation-admin-role-swatch" style={{ background: role.color }} />
        <span>
          <strong>{role.name}</strong>
          <small>
            {t("rolesOverview.permissionSummary", {
              count: Object.values(role.permissions).filter(Boolean).length,
              rank: role.hierarchyRank,
            })}
          </small>
        </span>
        {isBaseRole ? <em>{t("roleEditor.baseRole", "BASE")}</em> : null}
      </header>

      <div className="moderation-admin-role-details moderation-admin-role-details--custom">
        <label className="moderation-admin-role-field">
          <span>{t("roleEditor.name")}</span>
          <input
            className="moderation-admin-input"
            maxLength={32}
            value={name}
            onChange={(event) => setName(event.target.value)}
          />
        </label>

        <label className="moderation-admin-role-field">
          <span>{t("roleEditor.color")}</span>
          <AdminRoleColorControl compact value={color} disabled={busy} onChange={setColor} />
        </label>

        <button
          type="button"
          className="moderation-control-button moderation-control-button--neutral"
          disabled={busy || !name.trim() || !isRoleColor(color)}
          onClick={() => onSaveDetails(name.trim(), color)}
        >
          {t("roleEditor.save")}
        </button>
      </div>

      <div className="moderation-admin-role-actions">
        <button type="button" disabled={busy || index === 0} onClick={() => onMove(-1)}>
          ↑
        </button>
        <button type="button" disabled={busy || index === roleCount - 1} onClick={() => onMove(1)}>
          ↓
        </button>
        <button
          type="button"
          className={isBaseRole ? "is-active" : ""}
          disabled={busy}
          onClick={() => {
            setDeleteConfirmationVisible(false)
            setBaseConfirmationVisible(true)
          }}
        >
          {t("roleEditor.baseRole", "BASE")}
        </button>
        <button
          type="button"
          className="is-danger"
          disabled={busy}
          onClick={() => {
            setBaseConfirmationVisible(false)
            setDeleteConfirmationVisible(true)
          }}
        >
          {t("roleEditor.delete")}
        </button>
      </div>

      {baseConfirmationVisible ? (
        <div
          className="moderation-admin-role-base-confirmation"
          role="alertdialog"
          aria-label={baseConfirmationTitle}
          onKeyDown={(event) => {
            if (event.key === "Escape") {
              event.preventDefault()
              dismissBaseConfirmation()
            }
          }}
        >
          <span>
            <strong>{baseConfirmationTitle}</strong>
            <small>
              {isBaseRole
                ? t(
                    "roleEditor.confirmClearBaseRoleHint",
                    "New members will no longer receive this role automatically.",
                  )
                : t(
                    "roleEditor.confirmSetBaseRoleHint",
                    "New members will receive this role automatically. Existing assignments stay unchanged.",
                  )}
            </small>
          </span>
          <div>
            <button
              ref={baseConfirmRef}
              type="button"
              className="moderation-control-button moderation-control-button--primary"
              disabled={busy}
              onClick={() => {
                setBaseConfirmationVisible(false)
                onToggleBase()
              }}
            >
              {isBaseRole
                ? t("roleEditor.clearBaseRole", "Clear base role")
                : t("roleEditor.setBaseRole", "Set base role")}
            </button>
            <button
              type="button"
              className="moderation-control-button moderation-control-button--neutral"
              disabled={busy}
              onClick={dismissBaseConfirmation}
            >
              {t("common.cancel", "Cancel")}
            </button>
          </div>
        </div>
      ) : null}

      {deleteConfirmationVisible ? (
        <div
          className="moderation-admin-role-delete-confirmation"
          role="alertdialog"
          aria-label={deleteConfirmationTitle}
          onKeyDown={(event) => {
            if (event.key === "Escape") {
              event.preventDefault()
              dismissDeleteConfirmation()
            }
          }}
        >
          <span>
            <strong>{deleteConfirmationTitle}</strong>
            <small>{t("roleEditor.confirmDeleteHint", "Role assignments will be removed with it.")}</small>
          </span>
          <div>
            <button
              ref={deleteConfirmRef}
              type="button"
              className="moderation-control-button moderation-control-button--danger"
              disabled={busy}
              onClick={() => {
                setDeleteConfirmationVisible(false)
                onDelete()
              }}
            >
              {t("common.confirm", "Confirm")}
            </button>
            <button
              type="button"
              className="moderation-control-button moderation-control-button--neutral"
              disabled={busy}
              onClick={dismissDeleteConfirmation}
            >
              {t("common.cancel", "Cancel")}
            </button>
          </div>
        </div>
      ) : null}

      <div className="moderation-admin-role-permissions">
        {allRolePermissions.map((permission) => (
          <button
            key={permission}
            type="button"
            className={role.permissions[permission] ? "is-active" : ""}
            disabled={busy}
            onClick={() => onTogglePermission(permission)}
          >
            {t(`roleEditor.permissions.${permission}`)}
          </button>
        ))}
      </div>
    </article>
  )
}

function isRoleColor(value: string): boolean {
  return /^#[0-9A-F]{6}$/.test(value.trim().toUpperCase())
}
