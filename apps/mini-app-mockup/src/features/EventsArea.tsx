import type { Route } from "../model"
import { BetsPage } from "./BetsFeature"
import { EventDetail, EventsHome } from "./EventsHomeFeature"
import { SubscriptionsPage } from "./SubscriptionsFeature"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

export function EventsArea({ routeId, params, navigate, back }: Props) {
  if (routeId === "bets") return <BetsPage />
  if (routeId === "event-detail") return <EventDetail eventId={params?.eventId} back={back} />
  if (routeId === "subscriptions") return <SubscriptionsPage back={back} />

  return <EventsHome navigate={navigate} />
}
