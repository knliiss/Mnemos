import { lazy, Suspense } from "react"
import type { Account, Route } from "../model"
import { FeatureLoading } from "./FeatureLoading"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

const PeopleFeature = lazy(() =>
  import("./PeopleFeature").then((module) => ({
    default: module.PeopleFeature,
  })),
)

const UserPrimeWallet = lazy(() =>
  import("./UserPrimeWallet").then((module) => ({
    default: module.UserPrimeWallet,
  })),
)

export function PeopleArea(props: Props) {
  return (
    <Suspense fallback={<FeatureLoading />}>
      {props.routeId === "user-wallet" ? (
        <UserPrimeWallet navigate={props.navigate} back={props.back} />
      ) : (
        <PeopleFeature {...props} />
      )}
    </Suspense>
  )
}
