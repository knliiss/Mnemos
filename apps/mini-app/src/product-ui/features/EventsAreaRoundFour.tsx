import type { Account, Route } from "../model"
import { BetsProduction } from "./BetsProduction"
import { EventDetailProduction } from "./EventDetailProduction"
import { EventsFeedProduction } from "./EventsFeedProduction"
import { NotFoundFeature } from "./NotFoundFeature"
import { SubscriptionsProduction } from "./SubscriptionsProduction"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

export function EventsArea(props: Props) {
  if (props.routeId === "bets") {
    return (
      <BetsProduction
        activeAccount={props.activeAccount}
        navigate={props.navigate}
      />
    )
  }

  if (props.routeId === "events") {
    return (
      <EventsFeedProduction
        activeAccount={props.activeAccount}
        navigate={props.navigate}
      />
    )
  }

  if (props.routeId === "event-detail") {
    return (
      <EventDetailProduction
        eventId={props.params?.eventId}
        activeAccount={props.activeAccount}
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  if (props.routeId === "subscriptions") {
    return <SubscriptionsProduction back={props.back} />
  }

  return (
    <NotFoundFeature
      requested={props.routeId}
      onHome={() => props.navigate({ id: "events" })}
    />
  )
}
