import type { Account, Route } from "../model"
import { BetsPage } from "./BetsFeature"
import { EventDetail, EventsHome } from "./EventsHomeFeature"
import { SubscriptionsPage } from "./SubscriptionsFeature"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

export function EventsArea({ routeId, params, activeAccount, navigate, back }: Props) {
  if (routeId === "bets") return <BetsPage activeAccount={activeAccount} />
  if (routeId === "event-detail") return <EventDetail eventId={params?.eventId} activeAccount={activeAccount} back={back} />
  if (routeId === "subscriptions") return <SubscriptionsPage back={back} />

  return <EventsHome activeAccount={activeAccount} navigate={navigate} />
}
