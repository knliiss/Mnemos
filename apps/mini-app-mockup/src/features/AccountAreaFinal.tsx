import type { Account, Route } from "../model"
import { AccountFeatureFinal } from "./AccountFeatureFinal"
import { AccountHomePolished } from "./AccountHomePolished"
import { AccountInvitesFinal, AccountJoinFinal } from "./AccountInviteFlowsFinal"
import { AccountSettingsFinal } from "./AccountSettingsFinal"

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
      <AccountHomePolished
        activeAccount={props.activeAccount}
        navigate={props.navigate}
        initialMemberId={props.routeId === "member" ? props.params?.memberId ?? null : null}
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

  if (props.routeId === "account-join") {
    return (
      <AccountJoinFinal
        activeAccount={props.activeAccount}
        setActiveAccount={props.setActiveAccount}
        accounts={props.accounts}
        setAccounts={props.setAccounts}
        back={props.back}
      />
    )
  }

  return <AccountFeatureFinal {...props} />
}
