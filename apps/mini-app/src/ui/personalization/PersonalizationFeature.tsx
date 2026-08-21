import { useMnemosData } from "../data/MnemosDataProvider"
import type { Route } from "../model"
import { FeatureError } from "../system/FeatureError"
import { usePersonalizations } from "./data/usePersonalizations"
import { PersonalizationDetail } from "./detail/PersonalizationDetail"
import { OwnedPersonalizations } from "./owned/OwnedPersonalizations"
import "./owned/owned.css"
import "./personalization.css"
import { PersonalizationStore } from "./store/PersonalizationStore"
import { PersonalizationStoreCategory } from "./store/PersonalizationStoreCategory"
import "./store/store.css"

type PersonalizationFeatureProps = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

export function PersonalizationFeature({
  routeId,
  params,
  navigate,
  back,
}: PersonalizationFeatureProps) {
  const product = useMnemosData()
  const personalization = usePersonalizations()
  const canBuyAccountPersonalizations = Boolean(
    product.activeAccount && product.hasPermission("BUY_ACCOUNT_PERSONALIZATIONS"),
  )
  const canApplyAccountPersonalizations = Boolean(
    product.activeAccount && product.hasPermission("APPLY_ACCOUNT_PERSONALIZATIONS"),
  )

  if (personalization.error) {
    return <FeatureError back={back} onRetry={() => void personalization.refresh()} />
  }

  if (routeId === "personalization-owned") {
    return (
      <OwnedPersonalizations
        params={params}
        items={personalization.items}
        loading={personalization.loading}
        busyId={personalization.busyId}
        canApplyAccountPersonalizations={canApplyAccountPersonalizations}
        onToggleActive={personalization.toggleActive}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (routeId === "personalization-store-category") {
    return (
      <PersonalizationStoreCategory
        params={params}
        items={personalization.items}
        loading={personalization.loading}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (routeId === "personalization-detail") {
    return (
      <PersonalizationDetail
        params={params}
        items={personalization.items}
        loading={personalization.loading}
        busyId={personalization.busyId}
        pendingPurchases={personalization.pendingPurchases}
        canBuyAccountPersonalizations={canBuyAccountPersonalizations}
        canApplyAccountPersonalizations={canApplyAccountPersonalizations}
        onPurchase={personalization.purchase}
        onToggleActive={personalization.toggleActive}
        back={back}
      />
    )
  }

  return (
    <PersonalizationStore
      items={personalization.items}
      loading={personalization.loading}
      prime={product.user.prime}
      navigate={navigate}
    />
  )
}
