import type { Route } from "../model"
import { CreatorRevenue } from "./CreatorRevenue"
import { CreatorStudio } from "./CreatorStudio"
import "./creator.css"
import { CreatorEditor } from "./editor/CreatorEditor"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

export function CreatorFeature(props: Props) {
  if (props.routeId === "creator-revenue") {
    return <CreatorRevenue back={props.back} />
  }

  if (props.routeId === "personalization-editor") {
    return <CreatorEditor params={props.params} back={props.back} />
  }

  return <CreatorStudio navigate={props.navigate} back={props.back} />
}
