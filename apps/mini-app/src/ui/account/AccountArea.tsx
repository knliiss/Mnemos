import type { ReactNode } from "react"
import type { Badge } from "@/shared/api/badgeApi"
import "../events/event-surfaces.css"
import type { Route } from "../model"
import { useMnemosData } from "../data/MnemosDataProvider"
import { canAccessGuides } from "../guides/guideAccess"
import { GuidesPage } from "../guides/GuidesPage"
import { LeaderboardPage } from "../leaderboards/LeaderboardPage"
import { FeatureError } from "../system/FeatureError"
import { NotFoundFeature } from "../system/NotFoundFeature"
import { AccountSettings } from "./AccountSettings"
import "./role-shared.css"
import "./role-editor/role-editor.css"
import "./roles-overview.css"
import { AccountHome } from "./home/AccountHome"
import { AccountInvites } from "./invites/AccountInvites"
import { AccountCreate } from "./onboarding/AccountCreate"
import { AccountJoin } from "./onboarding/AccountJoin"
import { RoleEditor } from "./RoleEditor"
import { RolesOverview } from "./RolesOverview"
import { AccountWallet } from "./wallet/AccountWallet"

type Props = {
  navigate: (route: Route) => void
  back: () => void
  routeId: Route["id"]
  params?: Record<string, string>
  rootUserAccess?: ReactNode
  activeUserBadge: Badge | null
}

export function AccountArea(props: Props) {
  const product = useMnemosData()
  const requiresAccountData = [
    "account",
    "member",
    "roles",
    "role-editor",
    "account-settings",
  ].includes(props.routeId)

  if (requiresAccountData && product.accountError) {
    return <FeatureError back={props.back} onRetry={() => void product.refreshAccount()} />
  }

  if (props.routeId === "account" || props.routeId === "member") {
    return (
      <AccountHome
        navigate={props.navigate}
        back={props.back}
        activeUserBadge={props.activeUserBadge}
        rootUserAccess={props.routeId === "account" ? props.rootUserAccess : null}
        initialMemberId={props.routeId === "member" ? (props.params?.memberId ?? null) : null}
      />
    )
  }

  if (props.routeId === "leaderboards") {
    return <LeaderboardPage navigate={props.navigate} back={props.back} />
  }

  if (props.routeId === "guides") {
    if (!canAccessGuides(product.user.systemRoles)) {
      return (
        <NotFoundFeature
          requested={props.routeId}
          onHome={() => props.navigate({ id: "account" })}
        />
      )
    }

    return <GuidesPage back={props.back} />
  }

  if (props.routeId === "account-wallet") {
    return <AccountWallet back={props.back} />
  }

  if (props.routeId === "roles") {
    return <RolesOverview navigate={props.navigate} back={props.back} />
  }

  if (props.routeId === "role-editor") {
    return (
      <RoleEditor
        key={props.params?.roleId ?? "missing"}
        params={props.params}
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  if (props.routeId === "account-settings") {
    return <AccountSettings navigate={props.navigate} back={props.back} />
  }

  if (props.routeId === "account-invites") {
    return <AccountInvites back={props.back} />
  }

  if (props.routeId === "account-create") {
    return <AccountCreate back={props.back} />
  }

  if (props.routeId === "account-join") {
    return <AccountJoin back={props.back} />
  }

  return (
    <NotFoundFeature requested={props.routeId} onHome={() => props.navigate({ id: "account" })} />
  )
}
