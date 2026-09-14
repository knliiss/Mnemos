import { lazy, Suspense } from "react"
import type { Account, Route } from "../model"
import { FeatureLoading } from "../system/FeatureLoading"
import { NotFoundFeature } from "../system/NotFoundFeature"
import "./event-detail.css"
import "./event-surfaces.css"
import "./events.css"
import "./feed/events-feed-visuals.css"
import "./feed/feed.css"
import "./subscriptions/subscriptions.css"
import "./event-polish.css"

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

const NotificationSettingsPage = lazy(() =>
  import("./subscriptions/NotificationSettingsPage").then((module) => ({
    default: module.NotificationSettingsPage,
  })),
)

export function EventsArea(props: Props) {
  let content

  if (props.routeId === "bets") {
    content = <Bets activeAccount={props.activeAccount} navigate={props.navigate} />
  } else if (props.routeId === "events") {
    content = (
      <EventsFeed
        key={JSON.stringify(props.params ?? {})}
        params={props.params}
        activeAccount={props.activeAccount}
        navigate={props.navigate}
      />
    )
  } else if (props.routeId === "event-detail") {
    content = (
      <EventDetail
        key={props.params?.eventId ?? "missing"}
        eventId={props.params?.eventId}
        activeAccount={props.activeAccount}
        navigate={props.navigate}
        back={props.back}
      />
    )
  } else if (props.routeId === "subscriptions") {
    content = <NotificationSettingsPage back={props.back} />
  } else {
    return (
      <NotFoundFeature requested={props.routeId} onHome={() => props.navigate({ id: "events" })} />
    )
  }

  return <Suspense fallback={<FeatureLoading />}>{content}</Suspense>
}
