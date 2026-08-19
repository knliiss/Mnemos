import type { Account, Route } from "../model"
import { AccountHomeProduction } from "./AccountHomeProduction"
import { AccountInvitesProduction } from "./AccountInvitesProduction"
import { AccountCreateProduction, AccountJoinProduction } from "./AccountOnboardingProduction"
import { AccountSettingsProduction } from "./AccountSettingsProduction"
import { AccountWalletProduction } from "./AccountWalletProduction"
import { NotFoundFeature } from "./NotFoundFeature"
import { RoleEditorProduction } from "./RoleEditorProduction"
import { RolesOverviewProduction } from "./RolesOverviewProduction"

type Props = {
  activeAccount: Account
  setActiveAccount: (account: Account) => void
  accounts: Account[]
  setAccounts: (accounts: Account[]) => void
  navigate: (route: Route) => void
  back: () => void
  routeId: Route["id"]
  params?: Record<string, string>
}

export function AccountAreaFinal(props: Props) {
  if (props.routeId === "account" || props.routeId === "member") {
    return (
      <AccountHomeProduction
        navigate={props.navigate}
        back={props.back}
        initialMemberId={props.routeId === "member" ? props.params?.memberId ?? null : null}
      />
    )
  }

  if (props.routeId === "account-wallet") {
    return <AccountWalletProduction back={props.back} />
  }

  if (props.routeId === "roles") {
    return (
      <RolesOverviewProduction
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  if (props.routeId === "role-editor") {
    return (
      <RoleEditorProduction
        params={props.params}
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  if (props.routeId === "account-settings") {
    return (
      <AccountSettingsProduction
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  if (props.routeId === "account-invites") {
    return <AccountInvitesProduction back={props.back} />
  }

  if (props.routeId === "account-create") {
    return <AccountCreateProduction back={props.back} />
  }

  if (props.routeId === "account-join") {
    return <AccountJoinProduction back={props.back} />
  }

  return (
    <NotFoundFeature
      requested={props.routeId}
      onHome={() => props.navigate({ id: "account" })}
    />
  )
}
