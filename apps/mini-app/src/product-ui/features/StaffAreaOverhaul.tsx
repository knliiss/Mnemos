import type { Route } from "../model"
import { BadgeAdminProduction } from "./BadgeAdminProduction"
import { BetAdminOverhaul } from "./BetAdminOverhaul"
import { ModerationUserProduction } from "./ModerationFeatureProduction"
import { NotFoundFeature } from "./NotFoundFeature"
import { PersonalizationReviewOverhaul } from "./PersonalizationReviewOverhaul"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

export function StaffAreaOverhaul(props: Props) {
  if (props.routeId === "moderation-user") {
    return (
      <ModerationUserProduction
        params={props.params}
        back={props.back}
      />
    )
  }

  if (props.routeId === "personalization-review") {
    return <PersonalizationReviewOverhaul back={props.back} />
  }

  if (props.routeId === "bet-admin") {
    return (
      <BetAdminOverhaul
        params={props.params}
        navigate={props.navigate}
        back={props.back}
      />
    )
  }

  if (props.routeId === "badge-admin") {
    return <BadgeAdminProduction back={props.back} />
  }

  return (
    <NotFoundFeature
      requested={props.routeId}
      onHome={() => props.navigate({ id: "account" })}
    />
  )
}
