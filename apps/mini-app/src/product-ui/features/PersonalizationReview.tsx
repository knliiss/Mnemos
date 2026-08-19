import { useCallback, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  approvePersonalization,
  listPersonalizationReviewQueue,
  rejectPersonalization,
} from "@/shared/api/personalizationModerationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { PersonalizationDetails, PersonalizationVisual } from "../components/PersonalizationPreview"
import { useProductData } from "../data/ProductDataProvider"
import { toProductPersonalization } from "../data/personalizationMappers"
import type { Personalization, PersonalizationTarget } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, Field, PageHeader } from "../shared/Primitives"

type ReviewScope = "ALL" | PersonalizationTarget

const reviewScopes: ReviewScope[] = ["ALL", "USER", "ACCOUNT"]

export function PersonalizationReview({ back }: { back: () => void }) {
  const { t } = useTranslation()
  const product = useProductData()
  const [items, setItems] = useState<Personalization[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [price, setPrice] = useState("")
  const [reason, setReason] = useState("")
  const [query, setQuery] = useState("")
  const [scope, setScope] = useState<ReviewScope>("ALL")
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)

  const refresh = useCallback(async () => {
    try {
      const responses = await listPersonalizationReviewQueue()
      const next = responses.map((response) =>
        toProductPersonalization(response, null, product.user.id, product.user.username),
      )

      setItems(next)
      setSelectedId((current) =>
        current && next.some((item) => item.id === current) ? current : (next[0]?.id ?? null),
      )
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [product.user.id, product.user.username])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => window.cancelAnimationFrame(frame)
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
    if (!selected || busy) {
      return
    }
    if (decision === "APPROVE" && !priceValid) {
      return
    }
    if (decision === "REJECT" && !reason.trim()) {
      return
    }

    setBusy(true)

    try {
      if (decision === "APPROVE") {
        await approvePersonalization(selected.id, selected.currency, numericPrice)
      } else {
        await rejectPersonalization(selected.id, reason.trim())
      }

      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="screen review-overhaul-screen">
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
        <LoadingState label={t("personalization.loading")} />
      ) : (
        <div className="review-queue-grid">
          {visible.map((item) => (
            <button
              key={item.id}
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

      {selected ? (
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
              aria-label={t("personalization.review.close")}
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
      ) : (
        <section className="review-empty-workbench">
          <Glyph name="creator" size={22} />
          <span>
            <strong>{t("personalization.review.selectApplication")}</strong>
            <small>{t("personalization.review.selectApplicationCopy")}</small>
          </span>
        </section>
      )}
    </div>
  )
}

function LoadingState({ label }: { label: string }) {
  return (
    <div className="events-r2-loading" aria-label={label}>
      <i />
      <i />
      <i />
    </div>
  )
}
