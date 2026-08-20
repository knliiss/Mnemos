import { lazy, Suspense } from "react"
import type { Route } from "../../model"
import { FeatureLoading } from "../FeatureLoading"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

const PersonalizationFeature = lazy(() =>
  import("../PersonalizationFeature").then((module) => ({
    default: module.PersonalizationFeature,
  })),
)

export function PersonalizationArea(props: Props) {
  return (
    <Suspense fallback={<FeatureLoading />}>
      <PersonalizationFeature {...props} />
    </Suspense>
  )
}
