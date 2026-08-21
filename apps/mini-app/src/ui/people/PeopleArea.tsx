import { lazy, Suspense } from "react"
import "../events/event-surfaces.css"
import type { Account, Route } from "../model"
import { FeatureLoading } from "../system/FeatureLoading"
import "./people-wallet.css"
import { PublicProfilePage } from "./profile/PublicProfilePage"
import { SearchPage } from "./profile/SearchPage"
import "./profile/profile-state.css"
import "./profile/public-profile.css"
import "./profile/search.css"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account | null
  navigate: (route: Route) => void
  back: () => void
}

const UserPrimeWallet = lazy(() =>
  import("./UserPrimeWallet").then((module) => ({
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
        key={props.params?.accountId ?? "missing"}
        activeAccount={props.activeAccount}
        accountId={props.params?.accountId ?? ""}
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  return <SearchPage navigate={props.navigate} />
}
