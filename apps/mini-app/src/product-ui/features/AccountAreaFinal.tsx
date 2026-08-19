import type { Account, Route } from "../model"
import { AccountFeatureFinal } from "./AccountFeatureFinal"
import { AccountHomeProduction } from "./AccountHomeProduction"
import { AccountInvitesFinal } from "./AccountInviteFlowsFinal"
import { AccountCreateProduction, AccountJoinProduction } from "./AccountOnboardingProduction"
import { AccountSettingsFinal } from "./AccountSettingsFinal"
import { AccountWalletProduction } from "./AccountWalletProduction"
import { RoleEditorOverhaul } from "./RoleEditorOverhaul"
import { RolesOverviewOverhaul } from "./RolesOverviewOverhaul"

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
      <RolesOverviewOverhaul
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  if (props.routeId === "role-editor") {
    return (
      <RoleEditorOverhaul
        params={props.params}
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  if (props.routeId === "account-settings") {
    return (
      <AccountSettingsFinal
        activeAccount={props.activeAccount}
        setActiveAccount={props.setActiveAccount}
        accounts={props.accounts}
        setAccounts={props.setAccounts}
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  if (props.routeId === "account-invites") {
    return <AccountInvitesFinal back={props.back} />
  }

  if (props.routeId === "account-create") {
    return <AccountCreateProduction back={props.back} />
  }

  if (props.routeId === "account-join") {
    return <AccountJoinProduction back={props.back} />
  }

  return <AccountFeatureFinal {...props} />
}
