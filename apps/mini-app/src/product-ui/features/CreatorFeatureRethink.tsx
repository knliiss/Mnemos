import { lazy, Suspense } from "react"
import type { Route } from "../model"
import { FeatureLoading } from "./FeatureLoading"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

const CreatorFeatureProduction = lazy(() =>
  import("./CreatorFeatureProduction").then((module) => ({
    default: module.CreatorFeatureProduction,
  })),
)

export function CreatorFeature(props: Props) {
  return (
    <Suspense fallback={<FeatureLoading />}>
      <CreatorFeatureProduction {...props} />
    </Suspense>
  )
}
