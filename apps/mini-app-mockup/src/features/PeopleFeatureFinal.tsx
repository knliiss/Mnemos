import type { Account, Route } from "../model"
import { PeopleFeature } from "./PeopleFeature"
import { PublicProfilePolished } from "./PublicProfilePolished"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

const memberProfileIds: Record<string, string> = {
  "member-1": "46d7a80e-74f5-11eb-acca-1cb72caa35fd",
  "member-2": "profile-north",
  "member-3": "profile-vex",
}

export function PeopleFeatureFinal(props: Props) {
  if (props.routeId === "public-profile" && props.params?.accountId) {
    const accountId = memberProfileIds[props.params.accountId] ?? props.params.accountId

    return (
      <PublicProfilePolished
        activeAccount={props.activeAccount}
        navigate={props.navigate}
        back={props.back}
        params={{
          ...props.params,
          accountId,
        }}
      />
    )
  }

  return <PeopleFeature {...props} />
}
