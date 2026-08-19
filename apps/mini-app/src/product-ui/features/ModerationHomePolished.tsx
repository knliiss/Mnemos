import { lazy, Suspense } from "react"
import type { Route } from "../model"
import { FeatureLoading } from "./FeatureLoading"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

const ModerationHomeProduction = lazy(() =>
  import("./ModerationFeatureProduction").then((module) => ({
    default: module.ModerationHomeProduction,
  })),
)

export function ModerationHomePolished(props: Props) {
  return (
    <Suspense fallback={<FeatureLoading />}>
      <ModerationHomeProduction {...props} />
    </Suspense>
  )
}
