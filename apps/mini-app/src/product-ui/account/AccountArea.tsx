import type { Route } from "../model"
import { NotFoundFeature } from "../system/NotFoundFeature"
import { AccountHomePage } from "./AccountHomePage"
import { AccountInvites } from "./AccountInvites"
import { AccountCreate, AccountJoin } from "./AccountOnboarding"
import { AccountSettings } from "./AccountSettings"
import { AccountWallet } from "./AccountWallet"
import { RoleEditor } from "./RoleEditor"
import { RolesOverview } from "./RolesOverview"

type Props = {
  navigate: (route: Route) => void
  back: () => void
  routeId: Route["id"]
  params?: Record<string, string>
}

export function AccountArea(props: Props) {
  if (props.routeId === "account" || props.routeId === "member") {
    return (
      <AccountHomePage
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
    return <RoleEditor params={props.params} navigate={props.navigate} back={props.back} />
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
    <NotFoundFeature
      requested={props.routeId}
      onHome={() => props.navigate({ id: "account" })}
    />
  )
}
