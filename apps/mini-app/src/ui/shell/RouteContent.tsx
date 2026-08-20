import { AccountArea } from "../account/AccountArea"
import { CreatorArea } from "../creator/CreatorArea"
import { EventsArea } from "../events/EventsArea"
import type { Account, RootRoute, Route } from "../model"
import { featureArea } from "../navigation/routeRegistry"
import { PeopleArea } from "../people/PeopleArea"
import { PersonalizationArea } from "../personalization/PersonalizationArea"
import { AdministrationHome } from "../staff/administration/AdministrationHome"
import { ModerationArea } from "../staff/moderation/ModerationArea"
import { StaffArea } from "../staff/StaffArea"
import "../staff/staff.css"
import "../staff/staff-typography.css"
import { NotFoundFeature } from "../system/NotFoundFeature"
import { AccountAccessLanding } from "./AccountAccessLanding"
import { canRenderWithoutActiveAccount } from "./navigation"

type RouteContentProps = {
  current: Route
  activeAccount: Account | null
  navigate(route: Route): void
  back(): void
  switchRoot(route: RootRoute): void
}

export function RouteContent({
  current,
  activeAccount,
  navigate,
  back,
  switchRoot,
}: RouteContentProps) {
  const notFound = current.id === "account" && current.params?.notFound === "true"

  if (notFound) {
    return (
      <NotFoundFeature requested={current.params?.requested} onHome={() => switchRoot("account")} />
    )
  }

  if (!activeAccount && !canRenderWithoutActiveAccount(current)) {
    return <AccountAccessLanding route={current} navigate={navigate} back={back} />
  }

  const area = featureArea(current.id)

  if (area === "events" && activeAccount) {
    return (
      <EventsArea
        routeId={current.id}
        params={current.params}
        activeAccount={activeAccount}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (area === "account") {
    return (
      <AccountArea routeId={current.id} params={current.params} navigate={navigate} back={back} />
    )
  }

  if (area === "people") {
    return (
      <PeopleArea
        routeId={current.id}
        params={current.params}
        activeAccount={activeAccount}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (area === "personalization") {
    return (
      <PersonalizationArea
        routeId={current.id}
        params={current.params}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (area === "creator") {
    return (
      <CreatorArea routeId={current.id} params={current.params} navigate={navigate} back={back} />
    )
  }

  if (current.id === "moderation") {
    return <ModerationArea navigate={navigate} back={back} />
  }

  if (current.id === "administration") {
    return <AdministrationHome navigate={navigate} back={back} />
  }

  return <StaffArea routeId={current.id} params={current.params} navigate={navigate} back={back} />
}
