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
import type { AccountRole, Member } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { RoleSigil } from "../shared/RoleSigil"
import "./account-member.css"
import { formatMemberDate, localeForLanguage } from "./accountPresentation"

type AccountMemberManagementProps = {
  member: Member
  roles: AccountRole[]
  accountId: string
  canViewRoles: boolean
  refresh(): Promise<void>
  back(): void
}

export function AccountMemberManagement({
  member,
  roles,
  accountId,
  canViewRoles,
  refresh,
  back,
}: AccountMemberManagementProps) {
  const { t, i18n } = useTranslation()
  const [roleIds, setRoleIds] = useState<string[]>(member.roleIds)
  const [roleIdsScope, setRoleIdsScope] = useState(`${accountId}:${member.id}`)
  const [busyRole, setBusyRole] = useState<{ scope: string; roleId: string } | null>(null)
  const [kickBusyScope, setKickBusyScope] = useState<string | null>(null)
  const busyRoleRef = useRef<{ scope: string; roleId: string } | null>(null)
  const kickBusyScopeRef = useRef<string | null>(null)
  const scope = `${accountId}:${member.id}`
  const scopeRef = useRef(scope)

  const highestRole = roles.find((role) => role.id === member.highestRoleId) ?? null
  const locale = localeForLanguage(i18n.resolvedLanguage)

  useLayoutEffect(() => {
    scopeRef.current = scope
  }, [scope])

  useEffect(() => {
    if (!canViewRoles) {
      return
    }

    let cancelled = false

    void getMemberRoles(accountId, member.id)
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
  }, [accountId, canViewRoles, member.id, scope])

  const visibleRoleIds = roleIdsScope === scope ? roleIds : member.roleIds
  const busyRoleId = busyRole?.scope === scope ? busyRole.roleId : null
  const kickBusy = kickBusyScope === scope

  const toggleRole = async (role: AccountRole) => {
    if (!canViewRoles || member.owner || busyRoleRef.current?.scope === scope) {
      return
    }

    const requestedScope = scope
    const assigned = visibleRoleIds.includes(role.id)

    busyRoleRef.current = { scope: requestedScope, roleId: role.id }
    setBusyRole({ scope: requestedScope, roleId: role.id })

    try {
      if (assigned) {
        await revokeMemberRole(accountId, member.id, role.id)
      } else {
        await assignMemberRole(accountId, member.id, role.id)
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
    if (member.owner || kickBusyScopeRef.current === scope) {
      return
    }

    const requestedScope = scope

    kickBusyScopeRef.current = requestedScope
    setKickBusyScope(requestedScope)

    try {
      await kickAccountMember(accountId, member.id)
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

        {canViewRoles && highestRole ? <RoleSigil color={highestRole.color} size={42} /> : null}
      </section>

      <section className="account-member__roles">
        <header>
          <span>
            <small>{t("accountMember.access")}</small>
            <strong>{t("accountMember.roles")}</strong>
          </span>

          <em>{canViewRoles ? t("accountMember.activeRoles", { count: visibleRoleIds.length }) : "—"}</em>
        </header>

        <div>
          {canViewRoles ? (
            roles.map((role) => {
              const assigned = visibleRoleIds.includes(role.id)
              const locked = member.owner

              return (
                <motion.button
                  type="button"
                  key={role.id}
                  className={assigned ? "is-active" : ""}
                  disabled={locked || busyRoleId !== null}
                  whileTap={locked ? undefined : { scale: 0.985 }}
                  onClick={() => void toggleRole(role)}
                >
                  <RoleSigil color={role.color} size={34} compact />

                  <span>
                    <strong style={{ color: assigned ? role.color : undefined }}>{role.name}</strong>
                    <small>
                      {t("accountMember.permissionCount", {
                        count: enabledPermissions(role),
                      })}
                    </small>
                  </span>

                  <i aria-hidden="true">
                    <b />
                  </i>
                </motion.button>
              )
            })
          ) : (
            <p className="inline-hint inline-hint--left">
              {t("errorNotifications.tones.access.message")}
            </p>
          )}
        </div>
      </section>

      {!member.owner ? (
        <section className="account-member__danger">
          <span>
            <Glyph name="close" size={18} />
          </span>

          <span>
            <strong>{t("accountMember.removeTitle")}</strong>
            <small>{t("accountMember.removeCopy")}</small>
          </span>

          <button type="button" disabled={kickBusy} onClick={() => void kickMember()}>
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
