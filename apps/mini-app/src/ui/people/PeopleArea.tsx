import { lazy, Suspense } from "react"
import type { Account, Route } from "../model"
import type { UserBadgesController } from "../shell/badges/useUserBadges"
import { FeatureLoading } from "../system/FeatureLoading"
import { UserPersonalizationSettings } from "./personalization/UserPersonalizationSettings"
import { PublicProfileRoute } from "./profile/PublicProfileRoute"
import { SearchPage } from "./profile/SearchPage"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account | null
  userBadges: UserBadgesController
  navigate: (route: Route) => void
  back: () => void
}

const UserPrimeWallet = lazy(() =>
  import("./wallet/UserPrimeWallet").then((module) => ({
    default: module.UserPrimeWallet,
  })),
)

export function PeopleArea(props: Props) {
  if (props.routeId === "user-personalization") {
    return <UserPersonalizationSettings badges={props.userBadges} back={props.back} />
  }

  if (props.routeId === "user-wallet") {
    return (
      <Suspense fallback={<FeatureLoading />}>
        <UserPrimeWallet navigate={props.navigate} back={props.back} />
      </Suspense>
    )
  }

  if (props.routeId === "public-profile" && props.activeAccount) {
    return (
      <PublicProfileRoute
        activeAccount={props.activeAccount}
        accountId={props.params?.accountId}
        nickname={props.params?.nickname}
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  return <SearchPage navigate={props.navigate} />
}
