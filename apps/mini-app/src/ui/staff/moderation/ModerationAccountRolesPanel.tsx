import { useCallback, useEffect, useMemo, useState } from "react"
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

  const refresh = useCallback(async () => {
    setLoading(true)

    try {
      const loadedRoles = await getAdminAccountRoles(details.accountId)
      const assignments = await Promise.all(
        details.members.map(async (member) => [
          member.memberId,
          await getAdminMemberRoles(details.accountId, member.memberId),
        ] as const),
      )

      setRoles(loadedRoles)
      setMemberRoles(
        Object.fromEntries(
          assignments.map(([memberId, assignedRoles]) => [
            memberId,
            new Set(assignedRoles.map((role) => role.id)),
          ]),
        ),
      )
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [details.accountId, details.members])

  useEffect(() => {
    void refresh()
  }, [refresh])

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

    if (!name || busyKey) {
      return
    }

    await runMutation("create", async () => {
      await createAdminRole(details.accountId, {
        name,
        color: newRoleColor,
        permissions: [...newPermissions],
      })
      setNewRoleName("")
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
      <div className="moderation-admin-roles__heading">
        <span>
          <small>{t("rolesOverview.hierarchyEyebrow")}</small>
          <strong>{t("rolesOverview.title")}</strong>
        </span>
        <em>{sortedRoles.length}</em>
      </div>

      <div className="moderation-admin-role-create">
        <input
          aria-label={t("roleEditor.name")}
          className="moderation-admin-input"
          maxLength={32}
          placeholder={t("roleEditor.namePlaceholder")}
          value={newRoleName}
          onChange={(event) => setNewRoleName(event.target.value)}
        />
        <input
          aria-label={t("roleEditor.color")}
          className="moderation-admin-color"
          type="color"
          value={newRoleColor}
          onChange={(event) => setNewRoleColor(event.target.value.toUpperCase())}
        />
        <button
          type="button"
          className="moderation-control-button moderation-control-button--positive"
          disabled={!newRoleName.trim() || Boolean(busyKey)}
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
        <div className="moderation-admin-role-empty">{t("rolesOverview.emptyTitle")}</div>
      ) : (
        <div className="moderation-admin-role-list">
          {sortedRoles.map((role, index) => (
            <AdminRoleCard
              key={role.id}
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
                <small>{member.owner ? "OWNER" : member.memberId}</small>
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

  useEffect(() => {
    setName(role.name)
    setColor(role.color.slice(0, 7))
  }, [role.color, role.name])

  return (
    <article className="moderation-admin-role-card">
      <header>
        <span className="moderation-admin-role-swatch" style={{ background: role.color }} />
        <span>
          <strong>{role.name}</strong>
          <small>{t("rolesOverview.permissionSummary", {
            count: Object.values(role.permissions).filter(Boolean).length,
            rank: role.hierarchyRank,
          })}</small>
        </span>
        {isBaseRole ? <em>BASE</em> : null}
      </header>

      <div className="moderation-admin-role-details">
        <input
          className="moderation-admin-input"
          maxLength={32}
          value={name}
          onChange={(event) => setName(event.target.value)}
        />
        <input
          className="moderation-admin-color"
          type="color"
          value={color}
          onChange={(event) => setColor(event.target.value.toUpperCase())}
        />
        <button
          type="button"
          className="moderation-control-button moderation-control-button--neutral"
          disabled={busy || !name.trim()}
          onClick={() => onSaveDetails(name.trim(), color)}
        >
          {t("roleEditor.save")}
        </button>
      </div>

      <div className="moderation-admin-role-actions">
        <button type="button" disabled={busy || index === 0} onClick={() => onMove(-1)}>↑</button>
        <button type="button" disabled={busy || index === roleCount - 1} onClick={() => onMove(1)}>↓</button>
        <button type="button" className={isBaseRole ? "is-active" : ""} disabled={busy} onClick={onToggleBase}>
          BASE
        </button>
        <button type="button" className="is-danger" disabled={busy} onClick={onDelete}>
          {t("roleEditor.delete")}
        </button>
      </div>

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
