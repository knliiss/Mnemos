import type { ReactNode } from "react"
import { useTranslation } from "react-i18next"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { Route } from "../../model"
import { AccountMemberManagement } from "../AccountMemberManagement"
import { localeForLanguage } from "../accountPresentation"
import { resolveActorMember } from "../roleAccessPolicy"
import { useLatestAccountDrops } from "../data/useLatestAccountDrops"
import "./account-home.css"
import "./account-home-visuals.css"
import { AccountDropsPanel } from "./AccountDropsPanel"
import { AccountMembersPanel } from "./AccountMembersPanel"
import { AccountOverview } from "./AccountOverview"

type Props = {
  navigate: (route: Route) => void
  back: () => void
  rootUserAccess?: ReactNode
  initialMemberId?: string | null
}

export function AccountHome({ navigate, back, rootUserAccess, initialMemberId }: Props) {
  const { i18n } = useTranslation()
  const data = useMnemosData()
  const activeAccount = data.activeAccount
  const selectedMember = data.members.find((member) => member.id === initialMemberId) ?? null
  const showingMemberDetail = Boolean(initialMemberId && selectedMember)
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const actorMember = activeAccount ? resolveActorMember(activeAccount, data.members) : null
  const { drops } = useLatestAccountDrops(activeAccount?.nickname ?? null, !showingMemberDetail)

  if (!activeAccount) {
    return null
  }

  if (initialMemberId && selectedMember) {
    return (
      <AccountMemberManagement
        member={selectedMember}
        actorMember={actorMember}
        roles={data.roles}
        account={activeAccount}
        hasPermission={data.hasPermission}
        refresh={data.refreshAccount}
        back={back}
      />
    )
  }

  return (
    <div className="screen account-home-screen root-screen">
      <AccountOverview
        account={activeAccount}
        locale={locale}
        userHubTrigger={rootUserAccess}
        navigate={navigate}
      />

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
