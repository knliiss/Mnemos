import { lazy, Suspense } from "react"
import type { Account, Route } from "../model"
import { FeatureLoading } from "./FeatureLoading"
import { NotFoundFeature } from "./NotFoundFeature"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

const BetsProduction = lazy(() =>
  import("./BetsProduction").then((module) => ({
    default: module.BetsProduction,
  })),
)

const EventDetailProduction = lazy(() =>
  import("./EventDetailProduction").then((module) => ({
    default: module.EventDetailProduction,
  })),
)

const EventsFeedProduction = lazy(() =>
  import("./EventsFeedProduction").then((module) => ({
    default: module.EventsFeedProduction,
  })),
)

const SubscriptionsProduction = lazy(() =>
  import("./SubscriptionsProduction").then((module) => ({
    default: module.SubscriptionsProduction,
  })),
)

export function EventsArea(props: Props) {
  let content

  if (props.routeId === "bets") {
    content = (
      <BetsProduction
        activeAccount={props.activeAccount}
        navigate={props.navigate}
      />
    )
  } else if (props.routeId === "events") {
    content = (
      <EventsFeedProduction
        activeAccount={props.activeAccount}
        navigate={props.navigate}
      />
    )
  } else if (props.routeId === "event-detail") {
    content = (
      <EventDetailProduction
        eventId={props.params?.eventId}
        activeAccount={props.activeAccount}
        navigate={props.navigate}
        back={props.back}
      />
    )
  } else if (props.routeId === "subscriptions") {
    content = <SubscriptionsProduction back={props.back} />
  } else {
    return (
      <NotFoundFeature
        requested={props.routeId}
        onHome={() => props.navigate({ id: "events" })}
      />
    )
  }

  return (
    <Suspense fallback={<FeatureLoading />}>
      {content}
    </Suspense>
  )
}
