import type { ReactNode } from "react"
import type { Badge } from "@/shared/api/badgeApi"
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
import { CurrentGlobalEventsPanel } from "./CurrentGlobalEventsPanel"
import "./current-global-events.css"

type Props = {
  navigate: (route: Route) => void
  back: () => void
  activeUserBadge: Badge | null
  rootUserAccess?: ReactNode
  initialMemberId?: string | null
}

export function AccountHome({
  navigate,
  back,
  activeUserBadge,
  rootUserAccess,
  initialMemberId,
}: Props) {
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

      <CurrentGlobalEventsPanel navigate={navigate} />

      <AccountMembersPanel
        members={data.members}
        roles={data.roles}
        currentMemberId={activeAccount.currentMemberId}
        activeUserBadge={activeUserBadge}
        maxMembers={activeAccount.maxMembers}
        locale={locale}
        navigate={navigate}
      />

      <AccountDropsPanel drops={drops} locale={locale} navigate={navigate} />
    </div>
  )
}
