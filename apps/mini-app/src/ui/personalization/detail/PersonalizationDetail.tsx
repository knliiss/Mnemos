import { useTranslation } from "react-i18next"
import {
  PersonalizationDetails,
  PersonalizationVisual,
} from "../../components/personalization/PersonalizationPreview"
import type { Account, Personalization, Route } from "../../model"
import { CurrencyValue, PageHeader } from "../../shared/Primitives"
import { NotificationPackContextPreview } from "../components/NotificationPackContextPreview"
import { NotificationPackTelegramPreview } from "../components/NotificationPackTelegramPreview"
import { PersonalizationBalanceBar } from "../components/PersonalizationBalanceBar"
import { PersonalizationLoadingState } from "../components/PersonalizationLoadingState"
import { personalizationTypeTitle } from "../personalizationPresentation"
import "./personalization-detail.css"

type PersonalizationDetailProps = {
  params?: Record<string, string>
  items: Personalization[]
  loading: boolean
  busyId: string | null
  pendingPurchases: Record<string, string>
  activeAccount: Account | null
  prime: number
  canBuyAccountPersonalizations: boolean
  canApplyAccountPersonalizations: boolean
  onPurchase: (item: Personalization) => Promise<void>
  onToggleActive: (item: Personalization) => Promise<void>
  navigate: (route: Route) => void
  back: () => void
}

export function PersonalizationDetail({
  params,
  items,
  loading,
  busyId,
  pendingPurchases,
  activeAccount,
  prime,
  canBuyAccountPersonalizations,
  canApplyAccountPersonalizations,
  onPurchase,
  onToggleActive,
  navigate,
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
  const canPurchase =
    !item.createdByCurrentUser && (item.target === "USER" || canBuyAccountPersonalizations)
  const canToggleActive = item.target === "USER" || canApplyAccountPersonalizations
  const accessMessage = t("errorNotifications.tones.access.message")
  const actionClassName = `personalization-detail-purchase__action ${
    item.owned && item.active ? "is-active" : ""
  }`.trim()
  const notificationPackContent = item.content?.kind === "NOTIFICATION_PACK" ? item.content : null

  return (
    <div className="screen personalization-detail-screen">
      <PageHeader title={item.name} back={back} />

      <PersonalizationBalanceBar activeAccount={activeAccount} prime={prime} navigate={navigate} />

      <section className="personalization-detail-hero">
        <span className="personalization-detail-hero__copy">
          <small>
            {personalizationTypeTitle(t, item.type)} · {t(`personalization.target.${item.target}`)}
          </small>
          <strong>{item.name}</strong>
          <em>{item.creator}</em>
        </span>

        {item.type === "NOTIFICATION_PACK" ? (
          <NotificationPackContextPreview item={item} size="DETAIL" />
        ) : (
          <PersonalizationVisual item={item} detail />
        )}
      </section>

      {notificationPackContent ? (
        <NotificationPackTelegramPreview content={notificationPackContent} />
      ) : (
        <PersonalizationDetails item={item} />
      )}

      <section className="personalization-detail-purchase">
        <span className="personalization-detail-purchase__summary">
          <small>
            {item.owned
              ? t("personalization.detail.inCollection")
              : item.createdByCurrentUser
                ? t("personalization.detail.yourWork")
                : t("personalization.detail.price")}
          </small>

          {item.owned ? (
            <strong>
              {item.active
                ? t("personalization.detail.active")
                : t("personalization.detail.purchased")}
            </strong>
          ) : item.createdByCurrentUser ? (
            <strong>{t("personalization.detail.selfPurchaseUnavailable")}</strong>
          ) : (
            <CurrencyValue currency={item.currency} value={item.price} />
          )}
        </span>

        {item.owned ? (
          <button
            type="button"
            className={actionClassName}
            disabled={busy || !canToggleActive}
            title={!canToggleActive ? accessMessage : undefined}
            onClick={() => void onToggleActive(item)}
          >
            {busy
              ? t("personalization.detail.saving")
              : item.active
                ? t("personalization.detail.disable")
                : t("personalization.detail.enable")}
          </button>
        ) : item.createdByCurrentUser ? (
          <button type="button" className={actionClassName} disabled>
            {t("personalization.detail.ownWork")}
          </button>
        ) : (
          <button
            type="button"
            className={actionClassName}
            disabled={busy || pending || !canPurchase}
            title={!canPurchase ? accessMessage : undefined}
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
