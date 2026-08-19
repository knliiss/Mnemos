import type { Account, Route } from "../model"
import { PeopleFeatureProduction } from "./PeopleFeatureProduction"
import { UserPrimeWalletProduction } from "./UserPrimeWalletProduction"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

export function PeopleFeatureFinal(props: Props) {
  if (props.routeId === "user-wallet") {
    return (
      <UserPrimeWalletProduction
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  return <PeopleFeatureProduction {...props} />
}
