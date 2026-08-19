import { currentUser } from "../mockData"
import type { Account, Route } from "../model"
import { AccountFeatureFinal } from "./AccountFeatureFinal"
import { AccountHomePolished } from "./AccountHomePolished"
import { AccountInvitesFinal } from "./AccountInviteFlowsFinal"
import { AccountCreateOnboarding, AccountJoinOnboarding } from "./AccountOnboardingFinal"
import { AccountSettingsFinal } from "./AccountSettingsFinal"
import { AccountWalletPolished } from "./AccountWalletPolished"
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
      <AccountHomePolished
        activeAccount={props.activeAccount}
        navigate={props.navigate}
        back={props.back}
        initialMemberId={props.routeId === "member" ? props.params?.memberId ?? null : null}
        onRewardClaimed={(coin, prime) => {
          const updatedAccount = {
            ...props.activeAccount,
            coin: props.activeAccount.coin + coin,
          }

          props.setActiveAccount(updatedAccount)
          props.setAccounts(props.accounts.map((account) => (
            account.id === updatedAccount.id ? updatedAccount : account
          )))
          currentUser.prime += prime
        }}
      />
    )
  }

  if (props.routeId === "account-wallet") {
    return (
      <AccountWalletPolished
        activeAccount={props.activeAccount}
        setActiveAccount={props.setActiveAccount}
        accounts={props.accounts}
        setAccounts={props.setAccounts}
        back={props.back}
      />
    )
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
    return (
      <AccountCreateOnboarding
        activeAccount={props.activeAccount}
        setActiveAccount={props.setActiveAccount}
        accounts={props.accounts}
        setAccounts={props.setAccounts}
        back={props.back}
      />
    )
  }

  if (props.routeId === "account-join") {
    return (
      <AccountJoinOnboarding
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
