import { type RefObject, useCallback, useEffect, useMemo, useRef, useState } from "react"
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
  type AdminAccountMemberResponse,
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

type Workspace = "roles" | "members"
type MemberRoleState = Record<string, Set<string>>

type Confirmation =
  | {
      type: "base"
      roleId: string
      clear: boolean
    }
  | {
      type: "delete"
      roleId: string
    }

export function ModerationAccountRolesPanel({ details, onChanged }: Props) {
  const { t } = useTranslation()
  const [roles, setRoles] = useState<AdminAccountRoleResponse[]>([])
  const [memberRoles, setMemberRoles] = useState<MemberRoleState>({})
  const [workspace, setWorkspace] = useState<Workspace>("roles")
  const [selectedRoleId, setSelectedRoleId] = useState<string | null>(null)
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(
    details.members[0]?.memberId ?? null,
  )
  const [creating, setCreating] = useState(false)
  const [loading, setLoading] = useState(true)
  const [loadingMemberId, setLoadingMemberId] = useState<string | null>(null)
  const [busyKey, setBusyKey] = useState<string | null>(null)
  const [newRoleName, setNewRoleName] = useState("")
  const [newRoleColor, setNewRoleColor] = useState("#4BAF8A")
  const [newPermissions, setNewPermissions] = useState<Set<string>>(new Set())
  const [confirmation, setConfirmation] = useState<Confirmation | null>(null)
  const confirmationRef = useRef<HTMLButtonElement>(null)

  const sortedRoles = useMemo(
    () => [...roles].sort((left, right) => left.hierarchyRank - right.hierarchyRank),
    [roles],
  )

  const selectedRole =
    sortedRoles.find((role) => role.id === selectedRoleId) ?? sortedRoles[0] ?? null
  const selectedMember =
    details.members.find((member) => member.memberId === selectedMemberId) ??
    details.members[0] ??
    null

  const loadRoles = useCallback(async () => {
    const loaded = await getAdminAccountRoles(details.accountId)
    const sorted = [...loaded].sort((left, right) => left.hierarchyRank - right.hierarchyRank)

    setRoles(sorted)
    setSelectedRoleId((current) => {
      if (current && sorted.some((role) => role.id === current)) {
        return current
      }

      return sorted[0]?.id ?? null
    })
  }, [details.accountId])

  const loadMemberRoles = useCallback(
    async (memberId: string) => {
      setLoadingMemberId(memberId)

      try {
        const assigned = await getAdminMemberRoles(details.accountId, memberId)

        setMemberRoles((current) => ({
          ...current,
          [memberId]: new Set(assigned.map((role) => role.id)),
        }))
      } finally {
        setLoadingMemberId((current) => (current === memberId ? null : current))
      }
    },
    [details.accountId],
  )

  useEffect(() => {
    let active = true

    void getAdminAccountRoles(details.accountId)
      .then((loaded) => {
        if (!active) {
          return
        }

        const sorted = [...loaded].sort((left, right) => left.hierarchyRank - right.hierarchyRank)

        setRoles(sorted)
        setSelectedRoleId(sorted[0]?.id ?? null)
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
  }, [details.accountId])

  useEffect(() => {
    if (workspace !== "members" || !selectedMemberId || memberRoles[selectedMemberId]) {
      return
    }

    void Promise.resolve()
      .then(() => loadMemberRoles(selectedMemberId))
      .catch(handleError)
  }, [loadMemberRoles, memberRoles, selectedMemberId, workspace])

  useEffect(() => {
    if (!confirmation) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      confirmationRef.current?.focus()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [confirmation])

  const runMutation = async (
    key: string,
    mutation: () => Promise<void>,
    memberId?: string | null,
  ) => {
    if (busyKey) {
      return
    }

    setBusyKey(key)

    try {
      await mutation()
      await loadRoles()

      if (memberId) {
        await loadMemberRoles(memberId)
      }

      await onChanged()
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
      const created = await createAdminRole(details.accountId, {
        name,
        color: newRoleColor,
        permissions: [...newPermissions],
      })

      setSelectedRoleId(created.id)
      setCreating(false)
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

  const toggleMemberRole = async (
    member: AdminAccountMemberResponse,
    role: AdminAccountRoleResponse,
  ) => {
    if (member.owner || busyKey) {
      return
    }

    const assigned = memberRoles[member.memberId]?.has(role.id) ?? false

    await runMutation(
      `member:${member.memberId}:${role.id}`,
      async () => {
        if (assigned) {
          await revokeAdminMemberRole(details.accountId, member.memberId, role.id)
        } else {
          await assignAdminMemberRole(details.accountId, member.memberId, role.id)
        }
      },
      member.memberId,
    )
  }

  const applyConfirmation = async () => {
    const pending = confirmation

    if (!pending || busyKey) {
      return
    }

    setConfirmation(null)

    if (pending.type === "base") {
      await runMutation(`base:${pending.roleId}`, async () => {
        if (pending.clear) {
          await clearAdminBaseRole(details.accountId)
        } else {
          await updateAdminBaseRole(details.accountId, pending.roleId)
        }
      })
      return
    }

    await runMutation(`delete:${pending.roleId}`, async () => {
      await deleteAdminRole(details.accountId, pending.roleId)
    })
  }

  if (details.status === "DELETED") {
    return null
  }

  return (
    <section className="moderation-admin-roles moderation-admin-roles--workspace">
      <header className="moderation-admin-roles__hero">
        <span className="moderation-admin-roles__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-event.svg" alt="" />
        </span>
        <span>
          <small>{t("rolesOverview.hierarchyEyebrow")}</small>
          <strong>{t("rolesOverview.title")}</strong>
          <p>
            {t("rolesOverview.hierarchySummary", {
              roles: sortedRoles.length,
              permissions: allRolePermissions.length,
            })}
          </p>
        </span>
        <button
          type="button"
          className="moderation-control-button moderation-control-button--positive"
          disabled={Boolean(busyKey)}
          onClick={() => {
            setWorkspace("roles")
            setCreating((current) => !current)
          }}
        >
          + {t("rolesOverview.new")}
        </button>
      </header>

      <nav className="moderation-admin-role-tabs" aria-label={t("rolesOverview.title")}>
        <button
          type="button"
          className={workspace === "roles" ? "is-active" : ""}
          onClick={() => setWorkspace("roles")}
        >
          <span>{t("rolesOverview.title")}</span>
          <em>{sortedRoles.length}</em>
        </button>
        <button
          type="button"
          className={workspace === "members" ? "is-active" : ""}
          onClick={() => setWorkspace("members")}
        >
          <span>{t("roleEditor.groups.members")}</span>
          <em>{details.members.length}</em>
        </button>
      </nav>

      {creating ? (
        <CreateRolePanel
          name={newRoleName}
          color={newRoleColor}
          permissions={newPermissions}
          busy={Boolean(busyKey)}
          onNameChange={setNewRoleName}
          onColorChange={setNewRoleColor}
          onPermissionsChange={setNewPermissions}
          onCancel={() => setCreating(false)}
          onSubmit={() => void submitNewRole()}
        />
      ) : null}

      {workspace === "roles" ? (
        <div className="moderation-admin-role-workspace">
          <aside className="moderation-admin-role-directory">
            {loading ? (
              <div className="moderation-admin-role-empty">{t("common.loading")}</div>
            ) : sortedRoles.length === 0 ? (
              <div className="moderation-admin-role-empty moderation-admin-role-empty--illustrated">
                <img src="/ui-illustrations/mnemos-mascot-search.svg" alt="" aria-hidden="true" />
                <strong>{t("rolesOverview.emptyTitle")}</strong>
                <small>{t("rolesOverview.emptyCopy")}</small>
              </div>
            ) : (
              sortedRoles.map((role, index) => (
                <button
                  key={role.id}
                  type="button"
                  className={`moderation-admin-role-directory__item ${
                    selectedRole?.id === role.id ? "is-active" : ""
                  }`}
                  onClick={() => setSelectedRoleId(role.id)}
                >
                  <i style={{ background: role.color }} />
                  <span>
                    <strong>{role.name}</strong>
                    <small>
                      {t("rolesOverview.permissionSummary", {
                        count: enabledPermissionCount(role),
                        rank: role.hierarchyRank,
                      })}
                    </small>
                  </span>
                  <span className="moderation-admin-role-directory__badges">
                    {details.baseRoleId === role.id ? <em>BASE</em> : null}
                    <b>{index + 1}</b>
                  </span>
                </button>
              ))
            )}
          </aside>

          <main className="moderation-admin-role-editor-stage">
            {selectedRole ? (
              <AdminRoleEditor
                key={`${selectedRole.id}:${selectedRole.updatedAt}`}
                role={selectedRole}
                index={sortedRoles.findIndex((role) => role.id === selectedRole.id)}
                roleCount={sortedRoles.length}
                isBaseRole={details.baseRoleId === selectedRole.id}
                busy={Boolean(busyKey)}
                onMove={(direction) => void moveRole(selectedRole.id, direction)}
                onSavePermissions={(permissions) =>
                  void runMutation(`permissions:${selectedRole.id}`, async () => {
                    await updateAdminRolePermissions(details.accountId, selectedRole.id, {
                      permissions,
                    })
                  })
                }
                onSaveDetails={(name, color) =>
                  void runMutation(`details:${selectedRole.id}`, async () => {
                    await updateAdminRoleDetails(details.accountId, selectedRole.id, {
                      name,
                      color,
                    })
                  })
                }
                onToggleBase={() =>
                  setConfirmation({
                    type: "base",
                    roleId: selectedRole.id,
                    clear: details.baseRoleId === selectedRole.id,
                  })
                }
                onDelete={() =>
                  setConfirmation({
                    type: "delete",
                    roleId: selectedRole.id,
                  })
                }
              />
            ) : null}
          </main>
        </div>
      ) : (
        <MemberRoleWorkspace
          members={details.members}
          roles={sortedRoles}
          selectedMember={selectedMember}
          memberRoles={memberRoles}
          loadingMemberId={loadingMemberId}
          busy={Boolean(busyKey)}
          onSelectMember={setSelectedMemberId}
          onToggleRole={(member, role) => void toggleMemberRole(member, role)}
        />
      )}

      {confirmation ? (
        <RoleConfirmation
          confirmation={confirmation}
          busy={Boolean(busyKey)}
          confirmRef={confirmationRef}
          onCancel={() => setConfirmation(null)}
          onConfirm={() => void applyConfirmation()}
        />
      ) : null}
    </section>
  )
}

type CreateRolePanelProps = {
  name: string
  color: string
  permissions: Set<string>
  busy: boolean
  onNameChange: (value: string) => void
  onColorChange: (value: string) => void
  onPermissionsChange: (value: Set<string>) => void
  onCancel: () => void
  onSubmit: () => void
}

function CreateRolePanel({
  name,
  color,
  permissions,
  busy,
  onNameChange,
  onColorChange,
  onPermissionsChange,
  onCancel,
  onSubmit,
}: CreateRolePanelProps) {
  const { t } = useTranslation()

  return (
    <section className="moderation-admin-role-create-panel">
      <header>
        <span>
          <small>{t("roleEditor.newEyebrow")}</small>
          <strong>{t("rolesOverview.createTitle")}</strong>
          <p>{t("rolesOverview.createCopy")}</p>
        </span>
        <button type="button" disabled={busy} onClick={onCancel} aria-label={t("common.cancel")}>
          ×
        </button>
      </header>

      <div className="moderation-admin-role-create-panel__fields">
        <label className="moderation-admin-role-field">
          <span>{t("roleEditor.name")}</span>
          <input
            aria-label={t("roleEditor.name")}
            className="moderation-admin-input"
            maxLength={32}
            placeholder={t("roleEditor.namePlaceholder")}
            value={name}
            disabled={busy}
            onChange={(event) => onNameChange(event.target.value)}
          />
        </label>

        <div className="moderation-admin-role-field">
          <span>{t("roleEditor.color")}</span>
          <AdminRoleColorControl value={color} disabled={busy} onChange={onColorChange} />
        </div>
      </div>

      <PermissionPicker
        values={Object.fromEntries(
          allRolePermissions.map((permission) => [permission, permissions.has(permission)]),
        )}
        disabled={busy}
        onToggle={(permission) => {
          const next = new Set(permissions)

          if (next.has(permission)) {
            next.delete(permission)
          } else {
            next.add(permission)
          }

          onPermissionsChange(next)
        }}
      />

      <footer>
        <span>
          {t("roleEditor.enabledCount", { count: permissions.size })} / {allRolePermissions.length}
        </span>
        <div>
          <button
            type="button"
            className="moderation-control-button moderation-control-button--neutral"
            disabled={busy}
            onClick={onCancel}
          >
            {t("common.cancel")}
          </button>
          <button
            type="button"
            className="moderation-control-button moderation-control-button--positive"
            disabled={busy || !name.trim() || !isRoleColor(color)}
            onClick={onSubmit}
          >
            {busy ? t("roleEditor.saving") : t("roleEditor.create")}
          </button>
        </div>
      </footer>
    </section>
  )
}

type AdminRoleEditorProps = {
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

function AdminRoleEditor({
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
}: AdminRoleEditorProps) {
  const { t } = useTranslation()
  const [name, setName] = useState(role.name)
  const [color, setColor] = useState(role.color.slice(0, 7))
  const [permissionDraft, setPermissionDraft] = useState<Record<string, boolean>>(() =>
    rolePermissionState(role),
  )
  const permissionChanges = rolePermissionChanges(role, permissionDraft)
  const permissionChangesCount = Object.keys(permissionChanges).length
  const detailsChanged =
    name.trim() !== role.name || color.toUpperCase() !== role.color.toUpperCase()

  return (
    <article className="moderation-admin-role-editor-card">
      <header className="moderation-admin-role-editor-card__header">
        <span
          className="moderation-admin-role-editor-card__swatch"
          style={{ background: role.color }}
        />
        <span>
          <small>{t("roleEditor.editEyebrow")}</small>
          <strong>{role.name}</strong>
          <p>
            {t("rolesOverview.permissionSummary", {
              count: enabledPermissionCount(role),
              rank: role.hierarchyRank,
            })}
          </p>
        </span>
        {isBaseRole ? <em>{t("roleEditor.baseRole")}</em> : null}
      </header>

      <section className="moderation-admin-role-editor-card__section">
        <div className="moderation-admin-role-editor-card__section-heading">
          <span>
            <small>{t("roleEditor.appearance")}</small>
            <strong>{t("roleEditor.nameAndColor")}</strong>
          </span>
        </div>

        <div className="moderation-admin-role-details moderation-admin-role-details--custom">
          <label className="moderation-admin-role-field">
            <span>{t("roleEditor.name")}</span>
            <input
              className="moderation-admin-input"
              maxLength={32}
              value={name}
              disabled={busy}
              onChange={(event) => setName(event.target.value)}
            />
          </label>

          <div className="moderation-admin-role-field">
            <span>{t("roleEditor.color")}</span>
            <AdminRoleColorControl compact value={color} disabled={busy} onChange={setColor} />
          </div>

          <button
            type="button"
            className="moderation-control-button moderation-control-button--primary"
            disabled={busy || !detailsChanged || !name.trim() || !isRoleColor(color)}
            onClick={() => onSaveDetails(name.trim(), color)}
          >
            {t("roleEditor.save")}
          </button>
        </div>
      </section>

      <section className="moderation-admin-role-editor-card__section">
        <div className="moderation-admin-role-editor-card__section-heading">
          <span>
            <small>{t("rolesOverview.hierarchyEyebrow")}</small>
            <strong>{t("rolesOverview.title")}</strong>
          </span>
          <div className="moderation-admin-role-priority-actions">
            <button type="button" disabled={busy || index === 0} onClick={() => onMove(-1)}>
              ↑
            </button>
            <button
              type="button"
              disabled={busy || index === roleCount - 1}
              onClick={() => onMove(1)}
            >
              ↓
            </button>
          </div>
        </div>

        <div className="moderation-admin-role-structural-actions">
          <button
            type="button"
            className={isBaseRole ? "is-active" : ""}
            disabled={busy}
            onClick={onToggleBase}
          >
            <span>{t("roleEditor.baseRole")}</span>
            <small>
              {isBaseRole
                ? t("roleEditor.confirmClearBaseRoleHint")
                : t("roleEditor.confirmSetBaseRoleHint")}
            </small>
          </button>
          <button type="button" className="is-danger" disabled={busy} onClick={onDelete}>
            <span>{t("roleEditor.deleteTitle")}</span>
            <small>{t("roleEditor.deleteCopy")}</small>
          </button>
        </div>
      </section>

      <section className="moderation-admin-role-editor-card__section">
        <div className="moderation-admin-role-editor-card__section-heading">
          <span>
            <small>{t("roleEditor.permissionsEyebrow")}</small>
            <strong>
              {t("roleEditor.enabledSummary", {
                selected: Object.values(permissionDraft).filter(Boolean).length,
                total: allRolePermissions.length,
              })}
            </strong>
          </span>
        </div>

        <PermissionPicker
          values={permissionDraft}
          disabled={busy}
          onToggle={(permission) =>
            setPermissionDraft((current) => ({
              ...current,
              [permission]: !(current[permission] ?? false),
            }))
          }
        />

        {permissionChangesCount > 0 ? (
          <div className="moderation-admin-role-permission-actions" role="status">
            <span>
              <strong>
                {t("roleEditor.permissionChanges", { count: permissionChangesCount })}
              </strong>
              <small>{t("roleEditor.permissionChangesHint")}</small>
            </span>
            <div>
              <button
                type="button"
                className="moderation-control-button moderation-control-button--neutral"
                disabled={busy}
                onClick={() => setPermissionDraft(rolePermissionState(role))}
              >
                {t("roleEditor.discardPermissionChanges")}
              </button>
              <button
                type="button"
                className="moderation-control-button moderation-control-button--primary"
                disabled={busy}
                onClick={() => onSavePermissions(permissionChanges)}
              >
                {t("roleEditor.applyPermissionChanges")}
              </button>
            </div>
          </div>
        ) : null}
      </section>
    </article>
  )
}

type PermissionPickerProps = {
  values: Record<string, boolean>
  disabled: boolean
  onToggle: (permission: string) => void
}

function PermissionPicker({ values, disabled, onToggle }: PermissionPickerProps) {
  const { t } = useTranslation()

  return (
    <div className="moderation-admin-permission-picker moderation-admin-permission-picker--focused">
      {allRolePermissions.map((permission) => {
        const enabled = values[permission] ?? false

        return (
          <button
            key={permission}
            type="button"
            className={enabled ? "is-active" : ""}
            aria-pressed={enabled}
            disabled={disabled}
            onClick={() => onToggle(permission)}
          >
            <i aria-hidden="true">{enabled ? "✓" : "+"}</i>
            <span>{t(`roleEditor.permissions.${permission}`)}</span>
          </button>
        )
      })}
    </div>
  )
}

type MemberRoleWorkspaceProps = {
  members: AdminAccountMemberResponse[]
  roles: AdminAccountRoleResponse[]
  selectedMember: AdminAccountMemberResponse | null
  memberRoles: MemberRoleState
  loadingMemberId: string | null
  busy: boolean
  onSelectMember: (memberId: string) => void
  onToggleRole: (member: AdminAccountMemberResponse, role: AdminAccountRoleResponse) => void
}

function MemberRoleWorkspace({
  members,
  roles,
  selectedMember,
  memberRoles,
  loadingMemberId,
  busy,
  onSelectMember,
  onToggleRole,
}: MemberRoleWorkspaceProps) {
  const { t } = useTranslation()
  const assigned = selectedMember
    ? (memberRoles[selectedMember.memberId] ?? new Set<string>())
    : new Set<string>()

  return (
    <div className="moderation-admin-member-workspace">
      <aside className="moderation-admin-member-directory">
        {members.map((member) => (
          <button
            key={member.memberId}
            type="button"
            className={selectedMember?.memberId === member.memberId ? "is-active" : ""}
            onClick={() => onSelectMember(member.memberId)}
          >
            <span className="moderation-admin-member-avatar" aria-hidden="true">
              {member.username.slice(0, 1).toUpperCase()}
            </span>
            <span>
              <strong>@{member.username}</strong>
              <small>{member.owner ? t("moderation.owner") : member.memberId}</small>
            </span>
            <em>{member.owner ? "★" : "→"}</em>
          </button>
        ))}
      </aside>

      <main className="moderation-admin-member-role-editor">
        {selectedMember ? (
          <>
            <header>
              <span className="moderation-admin-member-avatar moderation-admin-member-avatar--large">
                {selectedMember.username.slice(0, 1).toUpperCase()}
              </span>
              <span>
                <small>{t("roleEditor.groups.members")}</small>
                <strong>@{selectedMember.username}</strong>
                <p>
                  {selectedMember.owner
                    ? t("moderation.owner")
                    : `${assigned.size} / ${roles.length}`}
                </p>
              </span>
            </header>

            {selectedMember.owner ? (
              <div className="moderation-admin-member-role-owner-note">
                <strong>{t("moderation.owner")}</strong>
                <small>{t("rolesOverview.hierarchyNote")}</small>
              </div>
            ) : loadingMemberId === selectedMember.memberId ? (
              <div className="moderation-admin-role-empty">{t("common.loading")}</div>
            ) : roles.length === 0 ? (
              <div className="moderation-admin-role-empty">{t("rolesOverview.emptyTitle")}</div>
            ) : (
              <div className="moderation-admin-member-role-choices">
                {roles.map((role) => {
                  const active = assigned.has(role.id)

                  return (
                    <button
                      key={role.id}
                      type="button"
                      className={active ? "is-active" : ""}
                      aria-pressed={active}
                      disabled={busy}
                      onClick={() => onToggleRole(selectedMember, role)}
                    >
                      <i style={{ background: role.color }} />
                      <span>
                        <strong>{role.name}</strong>
                        <small>
                          {t("rolesOverview.permissionSummary", {
                            count: enabledPermissionCount(role),
                            rank: role.hierarchyRank,
                          })}
                        </small>
                      </span>
                      <em>{active ? "✓" : "+"}</em>
                    </button>
                  )
                })}
              </div>
            )}
          </>
        ) : null}
      </main>
    </div>
  )
}

type RoleConfirmationProps = {
  confirmation: Confirmation
  busy: boolean
  confirmRef: RefObject<HTMLButtonElement | null>
  onCancel: () => void
  onConfirm: () => void
}

function RoleConfirmation({
  confirmation,
  busy,
  confirmRef,
  onCancel,
  onConfirm,
}: RoleConfirmationProps) {
  const { t } = useTranslation()
  const isDelete = confirmation.type === "delete"
  const clearBase = confirmation.type === "base" && confirmation.clear
  const title = isDelete
    ? t("roleEditor.confirmDelete", "Delete this role?")
    : clearBase
      ? t("roleEditor.confirmClearBaseRole", "Clear base role?")
      : t("roleEditor.confirmSetBaseRole", "Set as base role?")
  const hint = isDelete
    ? t("roleEditor.confirmDeleteHint")
    : clearBase
      ? t("roleEditor.confirmClearBaseRoleHint")
      : t("roleEditor.confirmSetBaseRoleHint")

  return (
    <div
      className={`moderation-admin-role-confirmation ${isDelete ? "is-danger" : ""}`}
      role="alertdialog"
      aria-label={title}
      onKeyDown={(event) => {
        if (event.key === "Escape" && !busy) {
          event.preventDefault()
          onCancel()
        }
      }}
    >
      <span className="moderation-admin-role-confirmation__art" aria-hidden="true">
        <img
          src={
            isDelete
              ? "/ui-illustrations/mnemos-mascot-search.svg"
              : "/ui-illustrations/mnemos-mascot-event.svg"
          }
          alt=""
        />
      </span>
      <span className="moderation-admin-role-confirmation__copy">
        <strong>{title}</strong>
        <small>{hint}</small>
      </span>
      <div className="moderation-admin-role-confirmation__actions">
        <button
          ref={confirmRef}
          type="button"
          className={
            isDelete
              ? "moderation-control-button moderation-control-button--danger"
              : "moderation-control-button moderation-control-button--primary"
          }
          disabled={busy}
          onClick={onConfirm}
        >
          {isDelete
            ? t("common.confirm", "Confirm")
            : clearBase
              ? t("roleEditor.clearBaseRole")
              : t("roleEditor.setBaseRole")}
        </button>
        <button
          type="button"
          className="moderation-control-button moderation-control-button--neutral"
          disabled={busy}
          onClick={onCancel}
        >
          {t("common.cancel")}
        </button>
      </div>
    </div>
  )
}

function enabledPermissionCount(role: AdminAccountRoleResponse): number {
  return Object.values(role.permissions).filter(Boolean).length
}

function rolePermissionState(role: AdminAccountRoleResponse): Record<string, boolean> {
  return Object.fromEntries(
    allRolePermissions.map((permission) => [permission, role.permissions[permission] === true]),
  )
}

function rolePermissionChanges(
  role: AdminAccountRoleResponse,
  draft: Record<string, boolean>,
): Record<string, boolean> {
  return Object.fromEntries(
    allRolePermissions
      .filter(
        (permission) => (draft[permission] ?? false) !== (role.permissions[permission] === true),
      )
      .map((permission) => [permission, draft[permission] ?? false]),
  )
}

function isRoleColor(value: string): boolean {
  return /^#[0-9A-Fa-f]{6}$/.test(value)
}
