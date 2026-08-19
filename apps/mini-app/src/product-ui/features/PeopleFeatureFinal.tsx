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

const PeopleFeatureProduction = lazy(() =>
  import("./PeopleFeatureProduction").then((module) => ({
    default: module.PeopleFeatureProduction,
  })),
)

const UserPrimeWalletProduction = lazy(() =>
  import("./UserPrimeWalletProduction").then((module) => ({
    default: module.UserPrimeWalletProduction,
  })),
)

export function PeopleFeatureFinal(props: Props) {
  return (
    <Suspense fallback={<FeatureLoading />}>
      {props.routeId === "user-wallet" ? (
        <UserPrimeWalletProduction
          navigate={props.navigate}
          back={props.back}
        />
      ) : (
        <PeopleFeatureProduction {...props} />
      )}
    </Suspense>
  )
}
