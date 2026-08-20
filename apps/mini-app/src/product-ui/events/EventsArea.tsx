import { lazy, Suspense } from "react"
import type { Account, Route } from "../model"
import { FeatureLoading } from "../system/FeatureLoading"
import { NotFoundFeature } from "../system/NotFoundFeature"
import "./events.css"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

const Bets = lazy(() =>
  import("../bets/Bets").then((module) => ({
    default: module.Bets,
  })),
)

const EventDetail = lazy(() =>
  import("./EventDetail").then((module) => ({
    default: module.EventDetail,
  })),
)

const EventsFeed = lazy(() =>
  import("./feed/EventsFeed").then((module) => ({
    default: module.EventsFeed,
  })),
)

const Subscriptions = lazy(() =>
  import("./subscriptions/Subscriptions").then((module) => ({
    default: module.Subscriptions,
  })),
)

export function EventsArea(props: Props) {
  let content

  if (props.routeId === "bets") {
    content = <Bets activeAccount={props.activeAccount} navigate={props.navigate} />
  } else if (props.routeId === "events") {
    content = <EventsFeed activeAccount={props.activeAccount} navigate={props.navigate} />
  } else if (props.routeId === "event-detail") {
    content = (
      <EventDetail
        eventId={props.params?.eventId}
        activeAccount={props.activeAccount}
        navigate={props.navigate}
        back={props.back}
      />
    )
  } else if (props.routeId === "subscriptions") {
    content = <Subscriptions back={props.back} />
  } else {
    return (
      <NotFoundFeature
        requested={props.routeId}
        onHome={() => props.navigate({ id: "events" })}
      />
    )
  }

  return <Suspense fallback={<FeatureLoading />}>{content}</Suspense>
}
