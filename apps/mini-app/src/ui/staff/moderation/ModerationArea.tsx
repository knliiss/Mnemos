import { lazy, Suspense } from "react"
import type { Route } from "../../model"
import { FeatureLoading } from "../../system/FeatureLoading"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

const ModerationHome = lazy(() =>
  import("./ModerationHome").then((module) => ({
    default: module.ModerationHome,
  })),
)

export function ModerationArea(props: Props) {
  return (
    <Suspense fallback={<FeatureLoading />}>
      <ModerationHome {...props} />
    </Suspense>
  )
}
