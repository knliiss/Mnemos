import { lazy, Suspense } from "react"
import type { Route } from "../model"
import { FeatureLoading } from "../system/FeatureLoading"
import { NotFoundFeature } from "../system/NotFoundFeature"
import "./badges/badge-admin.css"
import "./collectors/collector-admin.css"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

const BadgeAdmin = lazy(() =>
  import("./badges/BadgeAdmin").then((module) => ({
    default: module.BadgeAdmin,
  })),
)

const BetAdmin = lazy(() =>
  import("./bets/BetAdmin").then((module) => ({
    default: module.BetAdmin,
  })),
)

const CollectorAdmin = lazy(() =>
  import("./collectors/CollectorAdmin").then((module) => ({
    default: module.CollectorAdmin,
  })),
)

const ModerationUser = lazy(() =>
  import("./moderation/ModerationUser").then((module) => ({
    default: module.ModerationUser,
  })),
)

const PersonalizationReview = lazy(() =>
  import("./personalization/PersonalizationReview").then((module) => ({
    default: module.PersonalizationReview,
  })),
)

const WipeAdmin = lazy(() =>
  import("./wipes/WipeAdmin").then((module) => ({
    default: module.WipeAdmin,
  })),
)

export function StaffArea(props: Props) {
  let content

  if (props.routeId === "moderation-user") {
    const userId = props.params?.userId ?? ""

    content = <ModerationUser key={userId} params={props.params} back={props.back} />
  } else if (props.routeId === "personalization-review") {
    content = (
      <PersonalizationReview params={props.params} navigate={props.navigate} back={props.back} />
    )
  } else if (props.routeId === "bet-admin") {
    content = <BetAdmin params={props.params} navigate={props.navigate} back={props.back} />
  } else if (props.routeId === "badge-admin") {
    content = <BadgeAdmin back={props.back} />
  } else if (props.routeId === "collector-admin") {
    content = <CollectorAdmin back={props.back} />
  } else if (props.routeId === "wipe-admin") {
    content = <WipeAdmin back={props.back} />
  } else {
    return (
      <NotFoundFeature requested={props.routeId} onHome={() => props.navigate({ id: "account" })} />
    )
  }

  return <Suspense fallback={<FeatureLoading />}>{content}</Suspense>
}
