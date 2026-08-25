import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  listManagedPersonalizations,
  listPersonalizationReviewQueue,
} from "@/shared/api/personalizationModerationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { PersonalizationVisual } from "../../components/personalization/PersonalizationPreview"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { Personalization, PersonalizationTarget, Route } from "../../model"
import { toPersonalization } from "../../personalization/data/personalizationMappers"
import { personalizationIllustrations } from "../../personalization/personalizationPresentation"
import { Glyph } from "../../shared/MnemosIcon"
import { CurrencyValue, PageHeader } from "../../shared/Primitives"
import { FeatureEmpty } from "../../system/FeatureEmpty"
import { FeatureLoading } from "../../system/FeatureLoading"
import { PersonalizationReviewDetail } from "./PersonalizationReviewDetail"
import "./personalization-review.css"

type ReviewScope = "ALL" | PersonalizationTarget
type ModerationView = "REVIEW" | "PUBLISHED" | "UNLISTED"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

const reviewScopes: ReviewScope[] = ["ALL", "USER", "ACCOUNT"]
const moderationViews: ModerationView[] = ["REVIEW", "PUBLISHED", "UNLISTED"]

export function PersonalizationReview({ params, navigate, back }: Props) {
  const detailId = params?.personalizationId
  const detailView = readModerationView(params?.view)

  if (detailId) {
    return (
      <PersonalizationReviewDetail personalizationId={detailId} view={detailView} back={back} />
    )
  }

  return <PersonalizationReviewList initialView={detailView} navigate={navigate} back={back} />
}

function PersonalizationReviewList({
  initialView,
  navigate,
  back,
}: {
  initialView: ModerationView
  navigate: (route: Route) => void
  back: () => void
}) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [items, setItems] = useState<Personalization[]>([])
  const [query, setQuery] = useState("")
  const [scope, setScope] = useState<ReviewScope>("ALL")
  const [view, setView] = useState<ModerationView>(initialView)
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const requestRevision = useRef(0)

  const refresh = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)
    setLoadError(false)

    try {
      const responses =
        view === "REVIEW"
          ? await listPersonalizationReviewQueue()
          : (await listManagedPersonalizations()).filter((response) => response.status === view)
      const next = responses.map((response) =>
        toPersonalization(response, null, data.user.id, data.user.username),
      )

      if (revision === requestRevision.current) {
        setItems(next)
      }
    } catch (error) {
      if (revision === requestRevision.current) {
        setItems([])
        setLoadError(true)
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current) {
        setLoading(false)
      }
    }
  }, [data.user.id, data.user.username, view])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  const visible = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    return items.filter((item) => {
      const scopeMatches = scope === "ALL" || item.target === scope
      const typeLabel = t(`personalization.types.${item.type}.title`).toLowerCase()
      const queryMatches =
        !normalized ||
        item.name.toLowerCase().includes(normalized) ||
        item.creator.toLowerCase().includes(normalized) ||
        typeLabel.includes(normalized)

      return scopeMatches && queryMatches
    })
  }, [items, query, scope, t])

  const viewLabel = (value: ModerationView) =>
    value === "REVIEW" ? t("personalization.review.reviewView") : t(`creator.status.${value}`)

  return (
    <div className="screen personalization-review-screen">
      <PageHeader title={t("personalization.review.title")} back={back} />

      <section className="review-overview">
        <span className="review-overview__mark">
          <Glyph name="creator" size={20} />
        </span>
        <span>
          <small>
            {view === "REVIEW"
              ? t("personalization.review.queue")
              : t("personalization.review.management")}
          </small>
          <strong>
            {view === "REVIEW"
              ? t("personalization.review.pendingCount", { count: items.length })
              : t("personalization.review.managedCount", {
                  count: items.length,
                  status: viewLabel(view),
                })}
          </strong>
          <em>{t("personalization.review.listDescription")}</em>
        </span>
      </section>

      <div className="review-toolbar">
        <div className="review-scope" aria-label={t("personalization.review.mode")}>
          {moderationViews.map((value) => (
            <button
              type="button"
              key={value}
              className={view === value ? "is-active" : ""}
              onClick={() => setView(value)}
            >
              {viewLabel(value)}
            </button>
          ))}
        </div>

        <label>
          <Glyph name="search" size={16} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder={t("personalization.review.searchPlaceholder")}
          />
        </label>

        <div className="review-scope">
          {reviewScopes.map((value) => (
            <button
              type="button"
              key={value}
              className={scope === value ? "is-active" : ""}
              onClick={() => setScope(value)}
            >
              {t(`personalization.review.scopes.${value}`)}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <FeatureLoading />
      ) : loadError ? (
        <FeatureEmpty
          icon="creator"
          title={t("common.loadErrorTitle")}
          copy={t("common.loadErrorCopy")}
        >
          <button type="button" onClick={() => void refresh()}>
            {t("common.retry")}
          </button>
        </FeatureEmpty>
      ) : (
        <div className="review-queue-grid">
          {visible.map((item) => (
            <button
              type="button"
              key={item.id}
              className="review-queue-card"
              onClick={() =>
                navigate({
                  id: "personalization-review",
                  params: {
                    personalizationId: item.id,
                    view,
                  },
                })
              }
            >
              {item.type === "NOTIFICATION_PACK" ? (
                <span className="review-queue-card__illustration" aria-hidden="true">
                  <img src={personalizationIllustrations.NOTIFICATION_PACK} alt="" />
                </span>
              ) : (
                <PersonalizationVisual item={item} small />
              )}

              <span className="review-queue-card__copy">
                <span className="review-queue-card__meta">
                  <small>{t(`personalization.types.${item.type}.title`)}</small>
                  <i className={`is-${item.target.toLowerCase()}`}>
                    {t(`personalization.target.${item.target}`)}
                  </i>
                </span>
                <strong>{item.name}</strong>
                <em>{t("personalization.review.by", { creator: item.creator })}</em>
              </span>

              <span className="review-queue-card__side">
                <CurrencyValue currency={item.currency} value={item.price} compact />
                <b className={`status-${item.status.toLowerCase()}`}>
                  {t(`creator.status.${item.status}`)}
                </b>
              </span>
            </button>
          ))}

          {!visible.length ? (
            <p className="inline-hint">
              {view === "REVIEW"
                ? t("personalization.review.emptyQueue")
                : t("personalization.review.emptyManagement")}
            </p>
          ) : null}
        </div>
      )}
    </div>
  )
}

function readModerationView(value: string | undefined): ModerationView {
  if (value === "PUBLISHED" || value === "UNLISTED") {
    return value
  }

  return "REVIEW"
}
