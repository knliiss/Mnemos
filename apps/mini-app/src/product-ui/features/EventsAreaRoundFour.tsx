import type { Account, Route } from "../model"
import { EventDetailProduction } from "./EventDetailProduction"
import { EventsArea as EventsAreaRethink } from "./EventsAreaRethink"
import { EventsFeedProduction } from "./EventsFeedProduction"
import { SubscriptionsProduction } from "./SubscriptionsProduction"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

export function EventsArea(props: Props) {
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

  return <EventsAreaRethink {...props} />
}
