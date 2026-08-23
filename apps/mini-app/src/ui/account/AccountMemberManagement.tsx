import { motion } from "framer-motion"
import { useEffect, useLayoutEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  assignMemberRole,
  getMemberRoles,
  kickAccountMember,
  revokeMemberRole,
} from "@/shared/api/accountApi"
import { handleError } from "@/shared/errors/errorHandler"
import type { Account, AccountRole, Member, RolePermission } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"
import "./account-member.css"
import { formatMemberDate, localeForLanguage } from "./accountPresentation"
import {
  resolveKickMemberAccess,
  resolveRoleAssignmentAccess,
  roleAccessReasonTranslationKey,
} from "./roleAccessPolicy"

type AccountMemberManagementProps = {
  member: Member
  actorMember: Member | null
  roles: AccountRole[]
  account: Account
  hasPermission(permission: RolePermission): boolean
  refresh(): Promise<void>
  back(): void
}

export function AccountMemberManagement({
  member,
  actorMember,
  roles,
  account,
  hasPermission,
  refresh,
  back,
}: AccountMemberManagementProps) {
  const { t, i18n } = useTranslation()
  const [roleIds, setRoleIds] = useState<string[]>(member.roleIds)
  const [roleIdsScope, setRoleIdsScope] = useState(`${account.id}:${member.id}`)
  const [busyRole, setBusyRole] = useState<{ scope: string; roleId: string } | null>(null)
  const [kickBusyScope, setKickBusyScope] = useState<string | null>(null)
  const busyRoleRef = useRef<{ scope: string; roleId: string } | null>(null)
  const kickBusyScopeRef = useRef<string | null>(null)
  const scope = `${account.id}:${member.id}`
  const scopeRef = useRef(scope)
  const canAssignRoles = hasPermission("ASSIGN_MEMBER_ROLES")
  const canManageMembers = hasPermission("MANAGE_MEMBERS")
  const highestRole = roles.find((role) => role.id === member.highestRoleId) ?? null
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const kickAccess = resolveKickMemberAccess(
    {
      account,
      actorMember,
      targetMember: member,
      roles,
    },
    canManageMembers,
  )

  useLayoutEffect(() => {
    scopeRef.current = scope
  }, [scope])

  useEffect(() => {
    let cancelled = false

    void getMemberRoles(account.id, member.id)
      .then((memberRoles) => {
        if (!cancelled) {
          setRoleIds(memberRoles.map((role) => role.id))
          setRoleIdsScope(scope)
        }
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
        }
      })

    return () => {
      cancelled = true
    }
  }, [account.id, member.id, scope])

  const visibleRoleIds = roleIdsScope === scope ? roleIds : member.roleIds
  const busyRoleId = busyRole?.scope === scope ? busyRole.roleId : null
  const kickBusy = kickBusyScope === scope

  const toggleRole = async (role: AccountRole) => {
    const assigned = visibleRoleIds.includes(role.id)
    const access = resolveRoleAssignmentAccess(
      {
        account,
        actorMember,
        targetMember: member,
        role,
        roles,
        hasPermission,
      },
      canAssignRoles,
      !assigned,
    )

    if (access !== "ALLOWED" || busyRoleRef.current?.scope === scope) {
      return
    }

    const requestedScope = scope

    busyRoleRef.current = { scope: requestedScope, roleId: role.id }
    setBusyRole({ scope: requestedScope, roleId: role.id })

    try {
      if (assigned) {
        await revokeMemberRole(account.id, member.id, role.id)
      } else {
        await assignMemberRole(account.id, member.id, role.id)
      }

      if (scopeRef.current === requestedScope) {
        setRoleIds((current) =>
          assigned ? current.filter((id) => id !== role.id) : [...current, role.id],
        )
        setRoleIdsScope(requestedScope)
        await refresh()
      }
    } catch (error) {
      if (scopeRef.current === requestedScope) {
        handleError(error)
      }
    } finally {
      if (busyRoleRef.current?.scope === requestedScope && busyRoleRef.current.roleId === role.id) {
        busyRoleRef.current = null
        setBusyRole(null)
      }
    }
  }

  const kickMember = async () => {
    if (kickAccess !== "ALLOWED" || kickBusyScopeRef.current === scope) {
      return
    }

    const requestedScope = scope

    kickBusyScopeRef.current = requestedScope
    setKickBusyScope(requestedScope)

    try {
      await kickAccountMember(account.id, member.id)

      if (scopeRef.current === requestedScope) {
        await refresh()
        back()
      }
    } catch (error) {
      if (scopeRef.current === requestedScope) {
        handleError(error)
      }
    } finally {
      if (kickBusyScopeRef.current === requestedScope) {
        kickBusyScopeRef.current = null
        setKickBusyScope(null)
      }
    }
  }

  return (
    <div className="screen account-home-member-detail">
      <PageHeader title={t("accountMember.title")} back={back} />

      <section className="account-member__hero">
        <span className="account-member__avatar" aria-hidden="true" />

        <span className="account-member__identity">
          <small>{t("accountMember.eyebrow")}</small>
          <strong>{member.name}</strong>
          <em>{t("accountMember.since", { date: formatMemberDate(member.joinedAt, locale) })}</em>
        </span>

        {highestRole ? <RoleSigil color={highestRole.color} size={42} /> : null}
      </section>

      <section className="account-member__roles">
        <header>
          <span>
            <small>{t("accountMember.access")}</small>
            <strong>{t("accountMember.roles")}</strong>
          </span>

          <em>{t("accountMember.activeRoles", { count: visibleRoleIds.length })}</em>
        </header>

        <div>
          {roles.map((role) => {
            const assigned = visibleRoleIds.includes(role.id)
            const access = resolveRoleAssignmentAccess(
              {
                account,
                actorMember,
                targetMember: member,
                role,
                roles,
                hasPermission,
              },
              canAssignRoles,
              !assigned,
            )
            const locked = access !== "ALLOWED"
            const accessMessage = locked ? t(roleAccessReasonTranslationKey(access)) : null

            return (
              <motion.button
                type="button"
                key={role.id}
                className={assigned ? "is-active" : ""}
                disabled={locked || busyRoleId !== null}
                title={accessMessage ?? undefined}
                whileTap={locked ? undefined : { scale: 0.985 }}
                onClick={() => void toggleRole(role)}
              >
                <RoleSigil color={role.color} size={34} compact />

                <span>
                  <strong style={{ color: assigned ? role.color : undefined }}>{role.name}</strong>
                  <small className={locked ? "is-access-note" : undefined}>
                    {accessMessage ??
                      t("accountMember.permissionCount", {
                        count: enabledPermissions(role),
                      })}
                  </small>
                </span>

                <i aria-hidden="true">
                  <b />
                </i>
              </motion.button>
            )
          })}
        </div>
      </section>

      {!member.owner ? (
        <section className="account-member__danger">
          <span>
            <Glyph name="close" size={18} />
          </span>

          <span>
            <strong>{t("accountMember.removeTitle")}</strong>
            <small>
              {kickAccess === "ALLOWED"
                ? t("accountMember.removeCopy")
                : t(roleAccessReasonTranslationKey(kickAccess))}
            </small>
          </span>

          <button
            type="button"
            disabled={kickBusy || kickAccess !== "ALLOWED"}
            title={
              kickAccess === "ALLOWED"
                ? undefined
                : t(roleAccessReasonTranslationKey(kickAccess))
            }
            onClick={() => void kickMember()}
          >
            {kickBusy ? "…" : t("accountMember.kick")}
          </button>
        </section>
      ) : null}
    </div>
  )
}

function enabledPermissions(role: AccountRole) {
  return Object.values(role.permissions).filter(Boolean).length
}
