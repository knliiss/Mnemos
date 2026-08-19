import { lazy, Suspense } from "react"
import type { Route } from "../model"
import { FeatureLoading } from "./FeatureLoading"
import { NotFoundFeature } from "./NotFoundFeature"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

const BadgeAdminProduction = lazy(() =>
  import("./BadgeAdminProduction").then((module) => ({
    default: module.BadgeAdminProduction,
  })),
)

const BetAdminProduction = lazy(() =>
  import("./BetAdminProduction").then((module) => ({
    default: module.BetAdminProduction,
  })),
)

const ModerationUserProduction = lazy(() =>
  import("./ModerationFeatureProduction").then((module) => ({
    default: module.ModerationUserProduction,
  })),
)

const PersonalizationReviewProduction = lazy(() =>
  import("./PersonalizationReviewProduction").then((module) => ({
    default: module.PersonalizationReviewProduction,
  })),
)

export function StaffAreaOverhaul(props: Props) {
  let content

  if (props.routeId === "moderation-user") {
    content = (
      <ModerationUserProduction
        params={props.params}
        back={props.back}
      />
    )
  } else if (props.routeId === "personalization-review") {
    content = <PersonalizationReviewProduction back={props.back} />
  } else if (props.routeId === "bet-admin") {
    content = (
      <BetAdminProduction
        params={props.params}
        navigate={props.navigate}
        back={props.back}
      />
    )
  } else if (props.routeId === "badge-admin") {
    content = <BadgeAdminProduction back={props.back} />
  } else {
    return (
      <NotFoundFeature
        requested={props.routeId}
        onHome={() => props.navigate({ id: "account" })}
      />
    )
  }

  return (
    <Suspense fallback={<FeatureLoading />}>
      {content}
    </Suspense>
  )
}
