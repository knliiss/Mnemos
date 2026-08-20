import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  approvePersonalization,
  listPersonalizationReviewQueue,
  rejectPersonalization,
} from "@/shared/api/personalizationModerationApi"
import { handleError } from "@/shared/errors/errorHandler"
import {
  PersonalizationDetails,
  PersonalizationVisual,
} from "../../components/personalization/PersonalizationPreview"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { Personalization, PersonalizationTarget } from "../../model"
import { toPersonalization } from "../../personalization/data/personalizationMappers"
import { CurrencyMark, Glyph } from "../../shared/MnemosIcon"
import { CurrencyValue, Field, PageHeader } from "../../shared/Primitives"
import { FeatureEmpty } from "../../system/FeatureEmpty"
import { FeatureLoading } from "../../system/FeatureLoading"
import "./personalization-review.css"

type ReviewScope = "ALL" | PersonalizationTarget

const reviewScopes: ReviewScope[] = ["ALL", "USER", "ACCOUNT"]

export function PersonalizationReview({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [items, setItems] = useState<Personalization[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [price, setPrice] = useState("")
  const [reason, setReason] = useState("")
  const [query, setQuery] = useState("")
  const [scope, setScope] = useState<ReviewScope>("ALL")
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [busy, setBusy] = useState(false)
  const requestRevision = useRef(0)
  const busyRef = useRef(false)
  const mountedRef = useRef(true)

  const refresh = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision
    setLoading(true)
    setLoadError(false)

    try {
      const responses = await listPersonalizationReviewQueue()
      const next = responses.map((response) =>
        toPersonalization(response, null, data.user.id, data.user.username),
      )

      if (revision === requestRevision.current) {
        setItems(next)
        setSelectedId((current) =>
          current && next.some((item) => item.id === current) ? current : (next[0]?.id ?? null),
        )
      }
    } catch (error) {
      if (revision === requestRevision.current) {
        setItems([])
        setSelectedId(null)
        setLoadError(true)
        handleError(error)
      }
    } finally {
      if (revision === requestRevision.current) {
        setLoading(false)
      }
    }
  }, [data.user.id, data.user.username])

  useEffect(() => {
    mountedRef.current = true
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => {
      requestRevision.current += 1
      mountedRef.current = false
      window.cancelAnimationFrame(frame)
    }
  }, [refresh])

  const selected = items.find((item) => item.id === selectedId) ?? null

  useEffect(() => {
    if (!selected) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      setPrice(String(selected.price))
      setReason("")
    })

    return () => window.cancelAnimationFrame(frame)
  }, [selected])

  const numericPrice = Number(price)
  const priceValid = Number.isInteger(numericPrice) && numericPrice > 0
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

  const decide = async (decision: "APPROVE" | "REJECT") => {
    if (!selected || busyRef.current) {
      return
    }

    if (decision === "APPROVE" && !priceValid) {
      return
    }

    if (decision === "REJECT" && !reason.trim()) {
      return
    }

    const selectedItem = selected

    busyRef.current = true
    setBusy(true)

    try {
      if (decision === "APPROVE") {
        await approvePersonalization(selectedItem.id, selectedItem.currency, numericPrice)
      } else {
        await rejectPersonalization(selectedItem.id, reason.trim())
      }

      if (mountedRef.current) {
        await refresh()
      }
    } catch (error) {
      if (mountedRef.current) {
        handleError(error)
      }
    } finally {
      busyRef.current = false
      if (mountedRef.current) {
        setBusy(false)
      }
    }
  }

  return (
    <div className="screen personalization-review-screen">
      <PageHeader title={t("personalization.review.title")} back={back} />

      <section className="review-overview">
        <span className="review-overview__mark">
          <Glyph name="creator" size={20} />
        </span>
        <span>
          <small>{t("personalization.review.queue")}</small>
          <strong>{t("personalization.review.pendingCount", { count: items.length })}</strong>
          <em>{t("personalization.review.description")}</em>
        </span>
      </section>

      <div className="review-toolbar">
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
              disabled={busy}
              className={
                selectedId === item.id ? "review-queue-card is-selected" : "review-queue-card"
              }
              onClick={() => setSelectedId(item.id)}
            >
              <PersonalizationVisual item={item} small />
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
            <p className="inline-hint">{t("personalization.review.emptyQueue")}</p>
          ) : null}
        </div>
      )}

      {selected && !loadError ? (
        <section className="review-workbench">
          <header>
            <span>
              <small>{t("personalization.review.selected")}</small>
              <strong>{selected.name}</strong>
              <em>
                {t(`personalization.types.${selected.type}.title`)} ·{" "}
                {t(`personalization.target.${selected.target}`)}
              </em>
            </span>
            <button
              type="button"
              aria-label={t("personalization.review.close")}
              disabled={busy}
              onClick={() => setSelectedId(null)}
            >
              <Glyph name="close" size={16} />
            </button>
          </header>

          <PersonalizationVisual item={selected} detail />
          <PersonalizationDetails item={selected} />

          <div className="review-metadata">
            <article>
              <small>{t("personalization.review.author")}</small>
              <strong>{selected.creator}</strong>
            </article>
            <article>
              <small>{t("personalization.review.scope")}</small>
              <strong>{t(`personalization.target.${selected.target}`)}</strong>
            </article>
            <article>
              <small>{t("personalization.review.status")}</small>
              <strong>{t(`creator.status.${selected.status}`)}</strong>
            </article>
          </div>

          <div className="review-decision-grid">
            <section className="review-approve-panel">
              <Field
                label={t("personalization.review.approvedPrice")}
                hint={
                  !priceValid && price.length
                    ? t("personalization.review.positiveInteger")
                    : undefined
                }
              >
                <div className="amount-input">
                  <span>
                    <CurrencyMark currency={selected.currency} size={18} />
                  </span>
                  <input
                    type="number"
                    min={1}
                    step={1}
                    value={price}
                    disabled={busy}
                    onChange={(event) => setPrice(event.target.value)}
                  />
                </div>
              </Field>
              <button
                type="button"
                className="primary-action"
                disabled={!priceValid || busy}
                onClick={() => void decide("APPROVE")}
              >
                <Glyph name="check" size={16} />
                {busy ? t("personalization.review.saving") : t("personalization.review.approve")}
              </button>
            </section>

            <section className="review-reject-panel">
              <Field label={t("personalization.review.rejectionReason")}>
                <textarea
                  value={reason}
                  maxLength={500}
                  disabled={busy}
                  onChange={(event) => setReason(event.target.value)}
                  placeholder={t("personalization.review.rejectionPlaceholder")}
                />
              </Field>
              <button
                type="button"
                className="destructive-action"
                disabled={!reason.trim() || busy}
                onClick={() => void decide("REJECT")}
              >
                <Glyph name="close" size={16} />
                {busy ? t("personalization.review.saving") : t("personalization.review.reject")}
              </button>
            </section>
          </div>
        </section>
      ) : !loadError ? (
        <section className="review-empty-workbench">
          <Glyph name="creator" size={22} />
          <span>
            <strong>{t("personalization.review.selectApplication")}</strong>
            <small>{t("personalization.review.selectApplicationCopy")}</small>
          </span>
        </section>
      ) : null}
    </div>
  )
}
