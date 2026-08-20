import { useTranslation } from "react-i18next"
import {
  PersonalizationDetails,
  PersonalizationVisual,
} from "../../components/personalization/PersonalizationPreview"
import type { Personalization } from "../../model"
import { CurrencyValue, PageHeader } from "../../shared/Primitives"
import { PersonalizationLoadingState } from "../components/PersonalizationLoadingState"
import { personalizationTypeTitle } from "../personalizationPresentation"
import "./personalization-detail.css"

type PersonalizationDetailProps = {
  params?: Record<string, string>
  items: Personalization[]
  loading: boolean
  busyId: string | null
  pendingPurchases: Record<string, string>
  onPurchase: (item: Personalization) => Promise<void>
  onToggleActive: (item: Personalization) => Promise<void>
  back: () => void
}

export function PersonalizationDetail({
  params,
  items,
  loading,
  busyId,
  pendingPurchases,
  onPurchase,
  onToggleActive,
  back,
}: PersonalizationDetailProps) {
  const { t } = useTranslation()
  const item = items.find((candidate) => candidate.id === params?.personalizationId) ?? null

  if (loading) {
    return (
      <div className="screen">
        <PageHeader title={t("personalization.detail.title")} back={back} />
        <PersonalizationLoadingState />
      </div>
    )
  }

  if (!item) {
    return (
      <div className="screen">
        <PageHeader title={t("personalization.detail.title")} back={back} />
        <p className="inline-hint">{t("personalization.detail.notFound")}</p>
      </div>
    )
  }

  const pending = Boolean(pendingPurchases[item.id])
  const busy = busyId === item.id
  const actionClassName = `personalization-detail-purchase__action ${
    item.owned && item.active ? "is-active" : ""
  }`.trim()

  return (
    <div className="screen personalization-detail-screen">
      <PageHeader title={item.name} back={back} />

      <section className="personalization-detail-hero">
        <span className="personalization-detail-hero__copy">
          <small>
            {personalizationTypeTitle(t, item.type)} · {t(`personalization.target.${item.target}`)}
          </small>
          <strong>{item.name}</strong>
          <em>{item.creator}</em>
        </span>

        <PersonalizationVisual item={item} detail />
      </section>

      <PersonalizationDetails item={item} />

      <section className="personalization-detail-purchase">
        <span className="personalization-detail-purchase__summary">
          <small>
            {item.owned
              ? t("personalization.detail.inCollection")
              : t("personalization.detail.price")}
          </small>

          {item.owned ? (
            <strong>
              {item.active
                ? t("personalization.detail.active")
                : t("personalization.detail.purchased")}
            </strong>
          ) : (
            <CurrencyValue currency={item.currency} value={item.price} />
          )}
        </span>

        {item.owned ? (
          <button
            className={actionClassName}
            disabled={busy}
            onClick={() => void onToggleActive(item)}
          >
            {busy
              ? t("personalization.detail.saving")
              : item.active
                ? t("personalization.detail.disable")
                : t("personalization.detail.enable")}
          </button>
        ) : (
          <button
            className={actionClassName}
            disabled={busy || pending}
            onClick={() => void onPurchase(item)}
          >
            {pending
              ? t("personalization.detail.processing")
              : busy
                ? t("personalization.detail.sending")
                : t("personalization.detail.buy")}
          </button>
        )}
      </section>
    </div>
  )
}
