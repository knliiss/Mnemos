import { motion } from "framer-motion"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  assignMemberRole,
  getMemberRoles,
  kickAccountMember,
  revokeMemberRole,
} from "@/shared/api/productApi"
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
  refresh(): Promise<void>
  back(): void
}

export function AccountMemberManagement({
  member,
  roles,
  accountId,
  refresh,
  back,
}: AccountMemberManagementProps) {
  const { t, i18n } = useTranslation()
  const [roleIds, setRoleIds] = useState<string[]>(member.roleIds)
  const [busyRoleId, setBusyRoleId] = useState<string | null>(null)
  const [kickBusy, setKickBusy] = useState(false)

  const highestRole = roles.find((role) => role.id === member.highestRoleId) ?? null
  const locale = localeForLanguage(i18n.resolvedLanguage)

  useEffect(() => {
    void getMemberRoles(accountId, member.id)
      .then((memberRoles) => setRoleIds(memberRoles.map((role) => role.id)))
      .catch(handleError)
  }, [accountId, member.id])

  const toggleRole = async (role: AccountRole) => {
    if (member.owner || busyRoleId) {
      return
    }

    const assigned = roleIds.includes(role.id)

    setBusyRoleId(role.id)

    try {
      if (assigned) {
        await revokeMemberRole(accountId, member.id, role.id)
        setRoleIds((current) => current.filter((id) => id !== role.id))
      } else {
        await assignMemberRole(accountId, member.id, role.id)
        setRoleIds((current) => [...current, role.id])
      }

      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setBusyRoleId(null)
    }
  }

  const kickMember = async () => {
    if (member.owner || kickBusy) {
      return
    }

    setKickBusy(true)

    try {
      await kickAccountMember(accountId, member.id)
      await refresh()
      back()
    } catch (error) {
      handleError(error)
      setKickBusy(false)
    }
  }

  return (
    <div className="screen account-home-member-detail">
      <PageHeader title={t("accountMember.title")} back={back} />

      <section className="account-member__hero">
        <span className="account-member__avatar" aria-hidden="true" />

        <span className="account-member__identity">
          <small>ACCOUNT MEMBER</small>
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

          <em>{t("accountMember.activeRoles", { count: roleIds.length })}</em>
        </header>

        <div>
          {roles.map((role) => {
            const assigned = roleIds.includes(role.id)
            const locked = member.owner

            return (
              <motion.button
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
            <small>{t("accountMember.removeCopy")}</small>
          </span>

          <button disabled={kickBusy} onClick={() => void kickMember()}>
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
