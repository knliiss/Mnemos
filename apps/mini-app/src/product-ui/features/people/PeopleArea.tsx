import { lazy, Suspense } from "react"
import type { Account, Route } from "../../model"
import { FeatureLoading } from "../FeatureLoading"
import { PublicProfilePage } from "./PublicProfilePage"
import { SearchPage } from "./SearchPage"
import "./public-profile.css"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account | null
  navigate: (route: Route) => void
  back: () => void
}

const UserPrimeWallet = lazy(() =>
  import("../UserPrimeWallet").then((module) => ({
    default: module.UserPrimeWallet,
  })),
)

export function PeopleArea(props: Props) {
  if (props.routeId === "user-wallet") {
    return (
      <Suspense fallback={<FeatureLoading />}>
        <UserPrimeWallet navigate={props.navigate} back={props.back} />
      </Suspense>
    )
  }

  if (props.routeId === "public-profile" && props.activeAccount) {
    return (
      <PublicProfilePage
        activeAccount={props.activeAccount}
        accountId={props.params?.accountId ?? ""}
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  return <SearchPage navigate={props.navigate} />
}
