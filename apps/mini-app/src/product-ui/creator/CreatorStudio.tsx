import { AnimatePresence, motion } from "framer-motion"
import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  getCreatorRevenueSummary,
  listCreatorPersonalizations,
  type CreatorRevenueSummaryResponse,
} from "@/shared/api/creatorPersonalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { PersonalizationVisual } from "../components/personalization/PersonalizationPreview"
import { useProductData } from "../data/ProductDataProvider"
import type { Personalization, Route } from "../model"
import { toProductPersonalization } from "../personalization/data/personalizationMappers"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { PageHeader } from "../shared/Primitives"
import { FeatureEmpty } from "../system/FeatureEmpty"
import { CreatorLoadingState } from "./CreatorLoadingState"
import {
  creatorStatusLabel,
  creatorTypeLabel,
  type StudioFilter,
} from "./creatorPresentation"

type Props = {
  navigate: (route: Route) => void
  back: () => void
}

export function CreatorStudio({ navigate, back }: Props) {
  const { t } = useTranslation()
  const product = useProductData()
  const [items, setItems] = useState<Personalization[]>([])
  const [filter, setFilter] = useState<StudioFilter>("ALL")
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [revenue, setRevenue] = useState<CreatorRevenueSummaryResponse | null>(null)
  const requestRevision = useRef(0)

  const refresh = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)
    setLoadError(false)

    try {
      const [responses, summary] = await Promise.all([
        listCreatorPersonalizations(),
        getCreatorRevenueSummary(),
      ])

      if (revision === requestRevision.current) {
        setItems(
          responses.map((response) =>
            toProductPersonalization(response, null, product.user.id, product.user.username),
          ),
        )
        setRevenue(summary)
      }
    } catch (error) {
      if (revision === requestRevision.current) {
        setLoadError(true)
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current) {
        setLoading(false)
      }
    }
  }, [product.user.id, product.user.username])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  const counts = useMemo(
    () => ({
      draft: items.filter((item) => item.status === "DRAFT" || item.status === "REJECTED").length,
      review: items.filter((item) => item.status === "IN_REVIEW" || item.status === "APPROVED")
        .length,
      published: items.filter((item) => item.status === "PUBLISHED" || item.status === "UNLISTED")
        .length,
    }),
    [items],
  )

  const visible = items.filter((item) => {
    if (filter === "ALL") {
      return true
    }

    if (filter === "DRAFT") {
      return item.status === "DRAFT" || item.status === "REJECTED"
    }

    if (filter === "REVIEW") {
      return item.status === "IN_REVIEW" || item.status === "APPROVED"
    }

    return item.status === "PUBLISHED" || item.status === "UNLISTED"
  })

  return (
    <div className="screen creator-screen">
      <PageHeader title={t("creator.title")} back={back} />

      <section className="creator-hero">
        <div className="creator-hero__copy">
          <small>{t("creator.eyebrow")}</small>
          <strong>{t("creator.workshopTitle")}</strong>
          <p>{t("creator.workshopCopy")}</p>
        </div>

        <div className="creator-hero__art" aria-hidden="true" />

        <div className="creator-hero__actions">
          <button type="button"
            onClick={() =>
              navigate({ id: "personalization-editor", params: { personalizationId: "new" } })
            }
          >
            <Glyph name="plus" size={16} />
            <span>{t("creator.newWork")}</span>
          </button>

          <button type="button" className="is-revenue" onClick={() => navigate({ id: "creator-revenue" })}>
            <CurrencyMark currency="PRIME" size={15} />
            <span>{revenue?.confirmedPrime ?? 0}</span>
            <small>{t("creator.revenue")}</small>
          </button>
        </div>
      </section>

      <section className="creator-dashboard">
        <button type="button"
          className={filter === "DRAFT" ? "is-active" : ""}
          onClick={() => setFilter(filter === "DRAFT" ? "ALL" : "DRAFT")}
        >
          <i className="is-draft" />
          <strong>{counts.draft}</strong>
          <span>{t("creator.drafts")}</span>
        </button>

        <button type="button"
          className={filter === "REVIEW" ? "is-active" : ""}
          onClick={() => setFilter(filter === "REVIEW" ? "ALL" : "REVIEW")}
        >
          <i className="is-review" />
          <strong>{counts.review}</strong>
          <span>{t("creator.review")}</span>
        </button>

        <button type="button"
          className={filter === "PUBLISHED" ? "is-active" : ""}
          onClick={() => setFilter(filter === "PUBLISHED" ? "ALL" : "PUBLISHED")}
        >
          <i className="is-published" />
          <strong>{counts.published}</strong>
          <span>{t("creator.published")}</span>
        </button>
      </section>

      <header className="creator-section-title">
        <span>
          <small>{filter === "ALL" ? t("creator.allWorks") : t("creator.filtered")}</small>
          <strong>{t("creator.personalizations")}</strong>
        </span>
        <b>{visible.length}</b>
      </header>

      {loading ? (
        <CreatorLoadingState />
      ) : loadError && !items.length ? (
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
        <section className="creator-gallery">
          <AnimatePresence initial={false}>
            {visible.map((item, index) => (
              <motion.button type="button"
                layout
                key={item.id}
                className={`creator-card creator-card--${item.type.toLowerCase()}`}
                initial={{ opacity: 0, y: 6 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.98 }}
                transition={{ delay: Math.min(index * 0.035, 0.12) }}
                onClick={() =>
                  navigate({ id: "personalization-editor", params: { personalizationId: item.id } })
                }
              >
                <span className="creator-card__visual">
                  <PersonalizationVisual item={item} small />
                </span>

                <span className="creator-card__copy">
                  <small>{creatorTypeLabel(t, item.type)}</small>
                  <strong>{item.name}</strong>
                  <em>{item.target === "USER" ? t("creator.forUser") : t("creator.forAccount")}</em>
                </span>

                <span className={`creator-status is-${item.status.toLowerCase()}`}>
                  {creatorStatusLabel(t, item.status)}
                </span>
              </motion.button>
            ))}
          </AnimatePresence>

          {!visible.length ? <p className="inline-hint">{t("creator.emptySection")}</p> : null}
        </section>
      )}
    </div>
  )
}
