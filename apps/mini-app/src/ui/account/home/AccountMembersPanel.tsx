import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import type { AccountRole, Member, Route } from "../../model"
import { RoleSigil } from "../../shared/RoleSigil"
import { formatMemberDate } from "../accountPresentation"

type AccountMembersPanelProps = {
  members: Member[]
  roles: AccountRole[]
  maxMembers: number
  locale: string
  navigate: (route: Route) => void
}

export function AccountMembersPanel({
  members,
  roles,
  maxMembers,
  locale,
  navigate,
}: AccountMembersPanelProps) {
  const { t } = useTranslation()
  const roleById = new Map(roles.map((role) => [role.id, role]))

  return (
    <section className="account-home-panel account-home-members">
      <header className="account-home-panel__head account-home-members__head">
        <span>
          <small>{t("accountHome.team")}</small>
          <strong>{t("accountHome.members")}</strong>
          <em>
            {t("accountHome.membersCount", {
              count: members.length,
              max: maxMembers,
            })}
          </em>
        </span>
        <img src="/ui-illustrations/team-roster.svg" alt="" aria-hidden="true" />
      </header>

      <div className="account-home-member-list">
        {members.map((member) => {
          const highestRole = member.highestRoleId ? (roleById.get(member.highestRoleId) ?? null) : null
          const extraRoleCount = Math.max(0, member.roleIds.length - 1)

          return (
            <motion.button
              type="button"
              className="account-home-member"
              key={member.id}
              whileTap={{ scale: 0.992 }}
              onClick={() => navigate({ id: "member", params: { memberId: member.id } })}
            >
              <span className="account-home-member__avatar" aria-hidden="true" />

              <span className="account-home-member__copy">
                <span className="account-home-member__name">
                  <strong>{member.name}</strong>
                  {member.owner ? (
                    <img
                      className="account-home-owner-crown"
                      src="/ui-illustrations/account-owner-crown.svg"
                      alt={t("accountHome.owner")}
                    />
                  ) : null}
                </span>

                {highestRole ? (
                  <span className="account-home-member__role">
                    <RoleSigil color={highestRole.color} size={21} compact />
                    <span style={{ color: highestRole.color }}>{highestRole.name}</span>
                    {extraRoleCount > 0 ? <small>+{extraRoleCount}</small> : null}
                  </span>
                ) : (
                  <small className="account-home-member__no-role">{t("accountHome.noRole")}</small>
                )}
              </span>

              <time>{formatMemberDate(member.joinedAt, locale)}</time>
            </motion.button>
          )
        })}
      </div>
    </section>
  )
}
