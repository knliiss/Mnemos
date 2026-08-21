import { useTranslation } from "react-i18next"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { Route } from "../../model"
import { AccountMemberManagement } from "../AccountMemberManagement"
import { localeForLanguage } from "../accountPresentation"
import { useLatestAccountDrops } from "../data/useLatestAccountDrops"
import "./account-home.css"
import { AccountDropsPanel } from "./AccountDropsPanel"
import { AccountMembersPanel } from "./AccountMembersPanel"
import { AccountOverview } from "./AccountOverview"

type Props = {
  navigate: (route: Route) => void
  back: () => void
  initialMemberId?: string | null
}

export function AccountHome({ navigate, back, initialMemberId }: Props) {
  const { i18n } = useTranslation()
  const data = useMnemosData()
  const activeAccount = data.activeAccount
  const selectedMember = data.members.find((member) => member.id === initialMemberId) ?? null
  const showingMemberDetail = Boolean(initialMemberId && selectedMember)
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const canAssignRoles = data.hasPermission("ASSIGN_MEMBER_ROLES")
  const canManageMembers = data.hasPermission("MANAGE_MEMBERS")
  const { drops } = useLatestAccountDrops(activeAccount?.nickname ?? null, !showingMemberDetail)

  if (!activeAccount) {
    return null
  }

  if (initialMemberId && selectedMember) {
    return (
      <AccountMemberManagement
        member={selectedMember}
        roles={data.roles}
        accountId={activeAccount.id}
        canAssignRoles={canAssignRoles}
        canKickMembers={canManageMembers}
        refresh={data.refreshAccount}
        back={back}
      />
    )
  }

  return (
    <div className="screen account-home-screen root-screen">
      <AccountOverview account={activeAccount} locale={locale} navigate={navigate} />

      <AccountMembersPanel
        members={data.members}
        roles={data.roles}
        maxMembers={activeAccount.maxMembers}
        locale={locale}
        navigate={navigate}
      />

      <AccountDropsPanel drops={drops} locale={locale} navigate={navigate} />
    </div>
  )
}
