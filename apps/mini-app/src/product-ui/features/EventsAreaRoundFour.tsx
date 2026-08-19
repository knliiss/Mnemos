import type { Account, Route } from "../model"
import { EventDetailRoundFour } from "./EventDetailRoundFour"
import { EventsArea as EventsAreaRethink } from "./EventsAreaRethink"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

export function EventsArea(props: Props) {
  if (props.routeId === "event-detail") {
    return (
      <EventDetailRoundFour
        eventId={props.params?.eventId}
        activeAccount={props.activeAccount}
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  return <EventsAreaRethink {...props} />
}
