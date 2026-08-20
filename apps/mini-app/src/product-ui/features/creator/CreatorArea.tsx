import { lazy, Suspense } from "react"
import type { Route } from "../../model"
import { FeatureLoading } from "../FeatureLoading"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

const CreatorFeature = lazy(() =>
  import("../CreatorFeature").then((module) => ({
    default: module.CreatorFeature,
  })),
)

export function CreatorArea(props: Props) {
  return (
    <Suspense fallback={<FeatureLoading />}>
      <CreatorFeature {...props} />
    </Suspense>
  )
}
