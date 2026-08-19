import { lazy, Suspense } from "react"
import type { Route } from "../model"
import { FeatureLoading } from "./FeatureLoading"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

const PersonalizationFeatureProduction = lazy(() =>
  import("./PersonalizationFeatureProduction").then((module) => ({
    default: module.PersonalizationFeatureProduction,
  })),
)

export function PersonalizationFeatureFinal(props: Props) {
  return (
    <Suspense fallback={<FeatureLoading />}>
      <PersonalizationFeatureProduction {...props} />
    </Suspense>
  )
}
