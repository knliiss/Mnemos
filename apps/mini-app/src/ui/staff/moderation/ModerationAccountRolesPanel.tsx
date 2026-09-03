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

type HierarchyConfirmation = {
  roleId: string
  roleName: string
  direction: -1 | 1
}

type MemberRoleConfirmation = {
  memberId: string
  username: string
  roleId: string
  roleName: string
  assigned: boolean
}

export function ModerationAccountRolesPanel({ details, onChanged }: Props) {
  const { t } = useTranslation()
  const [roles, setRoles] = useState<AdminAccountRoleResponse[]>([])
  const [memberRoles, setMemberRoles] = useState<MemberRoleState>({})
  const [loading, setLoading] = useState(true)
  const [busyKey, setBusyKey] = useState<string | null>(null)
  const [newRoleName, setNewRoleName] = useState("")
  const [newRoleColor, setNewRoleColor] = useState("#4BAF8A")
  const [newPermissions, setNewPermissions] = useState<Set<string>>(new Set())
  const [hierarchyConfirmation, setHierarchyConfirmation] = useState<HierarchyConfirmation | null>(
    null,
  )
  const [memberRoleConfirmation, setMemberRoleConfirmation] = useState<MemberRoleConfirmation | null>(
    null,
  )
  const hierarchyConfirmRef = useRef<HTMLButtonElement>(null)
  const memberRoleConfirmRef = useRef<HTMLButtonElement>(null)

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

  useEffect(() => {
    if (!hierarchyConfirmation) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      hierarchyConfirmRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [hierarchyConfirmation])

  useEffect(() => {
    if (!memberRoleConfirmation) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      memberRoleConfirmRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [memberRoleConfirmation])

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

  const applyHierarchyChange = async () => {
    const confirmation = hierarchyConfirmation

    if (!confirmation || busyKey) {
      return
    }

    setHierarchyConfirmation(null)
    await moveRole(confirmation.roleId, confirmation.direction)
  }

  const applyMemberRoleChange = async () => {
    const confirmation = memberRoleConfirmation

    if (!confirmation || busyKey) {
      return
    }

    setMemberRoleConfirmation(null)

    await runMutation(`member:${confirmation.memberId}:${confirmation.roleId}`, async () => {
      if (confirmation.assigned) {
        await revokeAdminMemberRole(details.accountId, confirmation.memberId, confirmation.roleId)
      } else {
        await assignAdminMemberRole(details.accountId, confirmation.memberId, confirmation.roleId)
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

        <div className="moderation-admin-role-field">
          <span>{t("roleEditor.color")}</span>
          <AdminRoleColorControl value={newRoleColor} onChange={setNewRoleColor} />
        </div>

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
            aria-pressed={newPermissions.has(permission)}
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
              onMove={(direction) => {
                setMemberRoleConfirmation(null)
                setHierarchyConfirmation({
                  roleId: role.id,
                  roleName: role.name,
                  direction,
                })
              }}
              onSavePermissions={(permissions) =>
                void runMutation(`permissions:${role.id}`, async () => {
                  await updateAdminRolePermissions(details.accountId, role.id, { permissions })
                })
              }
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

      {hierarchyConfirmation ? (
        <div
          className="moderation-admin-role-hierarchy-confirmation"
          role="alertdialog"
          aria-label={t("roleEditor.confirmHierarchyChange")}
          onKeyDown={(event) => {
            if (event.key === "Escape" && !busyKey) {
              event.preventDefault()
              setHierarchyConfirmation(null)
            }
          }}
        >
          <span className="moderation-admin-role-confirmation__art" aria-hidden="true">
            <img src="/ui-illustrations/mnemos-mascot-event.svg" alt="" />
          </span>
          <span className="moderation-admin-role-confirmation__copy">
            <strong>
              {t(
                hierarchyConfirmation.direction === -1
                  ? "roleEditor.moveRoleUp"
                  : "roleEditor.moveRoleDown",
                { name: hierarchyConfirmation.roleName },
              )}
            </strong>
            <small>{t("roleEditor.confirmHierarchyChangeHint")}</small>
          </span>
          <div className="moderation-admin-role-confirmation__actions">
            <button
              ref={hierarchyConfirmRef}
              type="button"
              className="moderation-control-button moderation-control-button--primary"
              disabled={Boolean(busyKey)}
              onClick={() => void applyHierarchyChange()}
            >
              {t("roleEditor.confirmHierarchyChangeAction")}
            </button>
            <button
              type="button"
              className="moderation-control-button moderation-control-button--neutral"
              disabled={Boolean(busyKey)}
              onClick={() => setHierarchyConfirmation(null)}
            >
              {t("common.cancel")}
            </button>
          </div>
        </div>
      ) : null}

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
                      aria-pressed={assigned}
                      aria-haspopup="dialog"
                      disabled={Boolean(busyKey)}
                      onClick={() => {
                        setHierarchyConfirmation(null)
                        setMemberRoleConfirmation({
                          memberId: member.memberId,
                          username: member.username,
                          roleId: role.id,
                          roleName: role.name,
                          assigned,
                        })
                      }}
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

      {memberRoleConfirmation ? (
        <div
          className="moderation-admin-member-role-confirmation"
          role="alertdialog"
          aria-label={t("roleEditor.confirmMemberRoleChange")}
          onKeyDown={(event) => {
            if (event.key === "Escape" && !busyKey) {
              event.preventDefault()
              setMemberRoleConfirmation(null)
            }
          }}
        >
          <span className="moderation-admin-role-confirmation__art" aria-hidden="true">
            <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
          </span>
          <span className="moderation-admin-role-confirmation__copy">
            <strong>
              {memberRoleConfirmation.assigned
                ? t("roleEditor.confirmRevokeMemberRole")
                : t("roleEditor.confirmAssignMemberRole")}
            </strong>
            <small>
              {t("roleEditor.memberRoleChangeHint", {
                username: memberRoleConfirmation.username,
                role: memberRoleConfirmation.roleName,
              })}
            </small>
          </span>
          <div className="moderation-admin-role-confirmation__actions">
            <button
              ref={memberRoleConfirmRef}
              type="button"
              className={
                memberRoleConfirmation.assigned
                  ? "moderation-control-button moderation-control-button--danger"
                  : "moderation-control-button moderation-control-button--primary"
              }
              disabled={Boolean(busyKey)}
              onClick={() => void applyMemberRoleChange()}
            >
              {memberRoleConfirmation.assigned
                ? t("roleEditor.revokeMemberRole")
                : t("roleEditor.assignMemberRole")}
            </button>
            <button
              type="button"
              className="moderation-control-button moderation-control-button--neutral"
              disabled={Boolean(busyKey)}
              onClick={() => setMemberRoleConfirmation(null)}
            >
              {t("common.cancel")}
            </button>
          </div>
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
  onSavePermissions: (permissions: Record<string, boolean>) => void
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
  onSavePermissions,
  onSaveDetails,
  onToggleBase,
  onDelete,
}: AdminRoleCardProps) {
  const { t } = useTranslation()
  const [name, setName] = useState(role.name)
  const [color, setColor] = useState(role.color.slice(0, 7))
  const [permissionDraft, setPermissionDraft] = useState<Record<string, boolean>>(() =>
    rolePermissionState(role),
  )
  const [baseConfirmationVisible, setBaseConfirmationVisible] = useState(false)
  const [deleteConfirmationVisible, setDeleteConfirmationVisible] = useState(false)
  const baseConfirmRef = useRef<HTMLButtonElement>(null)
  const deleteConfirmRef = useRef<HTMLButtonElement>(null)
  const baseConfirmationTitle = isBaseRole
    ? t("roleEditor.confirmClearBaseRole", "Clear base role?")
    : t("roleEditor.confirmSetBaseRole", "Set as base role?")
  const deleteConfirmationTitle = t("roleEditor.confirmDelete", "Delete this role?")
  const permissionChanges = rolePermissionChanges(role, permissionDraft)
  const permissionChangesCount = Object.keys(permissionChanges).length

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

        <div className="moderation-admin-role-field">
          <span>{t("roleEditor.color")}</span>
          <AdminRoleColorControl compact value={color} disabled={busy} onChange={setColor} />
        </div>

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
            <small>
              {t(
                "roleEditor.confirmDeleteHint",
                "Deleting this role changes the account access configuration. Review the target role before continuing.",
              )}
            </small>
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
        {allRolePermissions.map((permission) => {
          const enabled = permissionDraft[permission] ?? false

          return (
            <button
              key={permission}
              type="button"
              className={enabled ? "is-active" : ""}
              aria-pressed={enabled}
              disabled={busy}
              onClick={() =>
                setPermissionDraft((current) => ({
                  ...current,
                  [permission]: !enabled,
                }))
              }
            >
              {t(`roleEditor.permissions.${permission}`)}
            </button>
          )
        })}
      </div>

      {permissionChangesCount > 0 ? (
        <div className="moderation-admin-role-permission-actions" role="status">
          <span>
            <strong>{t("roleEditor.permissionChanges", { count: permissionChangesCount })}</strong>
            <small>{t("roleEditor.permissionChangesHint")}</small>
          </span>
          <div>
            <button
              type="button"
              className="moderation-control-button moderation-control-button--primary"
              disabled={busy}
              onClick={() => onSavePermissions(permissionChanges)}
            >
              {t("roleEditor.applyPermissionChanges")}
            </button>
            <button
              type="button"
              className="moderation-control-button moderation-control-button--neutral"
              disabled={busy}
              onClick={() => setPermissionDraft(rolePermissionState(role))}
            >
              {t("roleEditor.discardPermissionChanges")}
            </button>
          </div>
        </div>
      ) : null}
    </article>
  )
}

function rolePermissionState(role: AdminAccountRoleResponse): Record<string, boolean> {
  return Object.fromEntries(
    allRolePermissions.map((permission) => [permission, Boolean(role.permissions[permission])]),
  ) as Record<string, boolean>
}

function rolePermissionChanges(
  role: AdminAccountRoleResponse,
  draft: Record<string, boolean>,
): Record<string, boolean> {
  return allRolePermissions.reduce<Record<string, boolean>>((changes, permission) => {
    const current = Boolean(role.permissions[permission])
    const next = Boolean(draft[permission])

    if (current !== next) {
      changes[permission] = next
    }

    return changes
  }, {})
}

function isRoleColor(value: string): boolean {
  return /^#[0-9A-F]{6}$/.test(value.trim().toUpperCase())
}
