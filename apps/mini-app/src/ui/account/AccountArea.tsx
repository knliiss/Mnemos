import type { Route } from "../model"
import { useMnemosData } from "../data/MnemosDataProvider"
import { FeatureError } from "../system/FeatureError"
import { NotFoundFeature } from "../system/NotFoundFeature"
import { AccountHome } from "./AccountHome"
import { AccountInvites } from "./AccountInvites"
import { AccountCreate, AccountJoin } from "./AccountOnboarding"
import { AccountSettings } from "./AccountSettings"
import { AccountWallet } from "./AccountWallet"
import "./account-management-typography.css"
import { RoleEditor } from "./RoleEditor"
import { RolesOverview } from "./RolesOverview"

type Props = {
  navigate: (route: Route) => void
  back: () => void
  routeId: Route["id"]
  params?: Record<string, string>
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
        initialMemberId={props.routeId === "member" ? (props.params?.memberId ?? null) : null}
      />
    )
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
