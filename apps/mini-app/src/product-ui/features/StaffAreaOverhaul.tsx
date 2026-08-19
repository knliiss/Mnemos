import type { Route } from "../model"
import { BetAdminOverhaul } from "./BetAdminOverhaul"
import { PersonalizationReviewOverhaul } from "./PersonalizationReviewOverhaul"
import { StaffFeatureFinal } from "./StaffFeatureFinal"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

export function StaffAreaOverhaul(props: Props) {
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

  return <StaffFeatureFinal {...props} />
}
