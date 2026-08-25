import { useCallback, useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  approvePersonalization,
  listManagedPersonalizations,
  listPersonalizationReviewQueue,
  rejectPersonalization,
  relistPersonalization,
  unlistPersonalization,
} from "@/shared/api/personalizationModerationApi"
import { handleError } from "@/shared/errors/errorHandler"
import {
  PersonalizationDetails,
  PersonalizationVisual,
} from "../../components/personalization/PersonalizationPreview"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { Personalization } from "../../model"
import { NotificationPackSpotlightPreview } from "../../personalization/components/NotificationPackSpotlightPreview"
import { NotificationPackTelegramPreview } from "../../personalization/components/NotificationPackTelegramPreview"
import { toPersonalization } from "../../personalization/data/personalizationMappers"
import { CurrencyMark, Glyph } from "../../shared/MnemosIcon"
import { Field, PageHeader } from "../../shared/Primitives"
import { FeatureEmpty } from "../../system/FeatureEmpty"
import { FeatureLoading } from "../../system/FeatureLoading"

type ModerationView = "REVIEW" | "PUBLISHED" | "UNLISTED"

type Props = {
  personalizationId: string
  view: ModerationView
  back: () => void
}

export function PersonalizationReviewDetail({ personalizationId, view, back }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [item, setItem] = useState<Personalization | null>(null)
  const [price, setPrice] = useState("")
  const [reason, setReason] = useState("")
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [busy, setBusy] = useState(false)
  const mountedRef = useRef(true)
  const busyRef = useRef(false)

  const load = useCallback(async () => {
    setLoading(true)
    setLoadError(false)

    try {
      const responses =
        view === "REVIEW"
          ? await listPersonalizationReviewQueue()
          : await listManagedPersonalizations()
      const response = responses.find((candidate) => candidate.id === personalizationId) ?? null
      const next = response
        ? toPersonalization(response, null, data.user.id, data.user.username)
        : null

      if (!mountedRef.current) {
        return
      }

      setItem(next)
      setPrice(next ? String(next.price) : "")
      setReason("")
    } catch (error) {
      if (mountedRef.current) {
        setLoadError(true)
        handleError(error)
      }
    } finally {
      if (mountedRef.current) {
        setLoading(false)
      }
    }
  }, [data.user.id, data.user.username, personalizationId, view])

  useEffect(() => {
    mountedRef.current = true
    const frame = window.requestAnimationFrame(() => {
      void load()
    })

    return () => {
      mountedRef.current = false
      window.cancelAnimationFrame(frame)
    }
  }, [load])

  const numericPrice = Number(price)
  const priceValid = Number.isInteger(numericPrice) && numericPrice > 0

  const decide = async (decision: "APPROVE" | "REJECT") => {
    if (!item || item.status !== "IN_REVIEW" || busyRef.current) {
      return
    }

    if (decision === "APPROVE" && !priceValid) {
      return
    }

    if (decision === "REJECT" && !reason.trim()) {
      return
    }

    busyRef.current = true
    setBusy(true)

    try {
      if (decision === "APPROVE") {
        await approvePersonalization(item.id, item.currency, numericPrice)
      } else {
        await rejectPersonalization(item.id, reason.trim())
      }

      if (mountedRef.current) {
        back()
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

  const changeListing = async () => {
    if (!item || busyRef.current) {
      return
    }

    if (item.status !== "PUBLISHED" && item.status !== "UNLISTED") {
      return
    }

    busyRef.current = true
    setBusy(true)

    try {
      if (item.status === "PUBLISHED") {
        await unlistPersonalization(item.id)
      } else {
        await relistPersonalization(item.id)
      }

      if (mountedRef.current) {
        back()
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

  if (loading) {
    return (
      <div className="screen personalization-review-screen">
        <PageHeader title={t("personalization.review.detailTitle")} back={back} />
        <FeatureLoading />
      </div>
    )
  }

  if (loadError) {
    return (
      <div className="screen personalization-review-screen">
        <PageHeader title={t("personalization.review.detailTitle")} back={back} />
        <FeatureEmpty
          icon="creator"
          title={t("common.loadErrorTitle")}
          copy={t("common.loadErrorCopy")}
        >
          <button type="button" onClick={() => void load()}>
            {t("common.retry")}
          </button>
        </FeatureEmpty>
      </div>
    )
  }

  if (!item) {
    return (
      <div className="screen personalization-review-screen">
        <PageHeader title={t("personalization.review.detailTitle")} back={back} />
        <FeatureEmpty
          icon="creator"
          title={t("personalization.review.notFound")}
          copy={t("personalization.review.notFoundCopy")}
        />
      </div>
    )
  }

  return (
    <div className="screen personalization-review-screen personalization-review-detail-screen">
      <PageHeader title={item.name} back={back} />

      <section className="review-workbench review-workbench--standalone">
        <header>
          <span>
            <small>{t("personalization.review.detailEyebrow")}</small>
            <strong>{item.name}</strong>
            <em>
              {t(`personalization.types.${item.type}.title`)} · {t(`personalization.target.${item.target}`)}
            </em>
          </span>
        </header>

        {item.content?.kind === "NOTIFICATION_PACK" ? (
          <>
            <div className="review-workbench__spotlight">
              <NotificationPackSpotlightPreview item={item} size="DETAIL" />
            </div>
            <NotificationPackTelegramPreview content={item.content} />
          </>
        ) : (
          <>
            <PersonalizationVisual item={item} detail />
            <PersonalizationDetails item={item} />
          </>
        )}

        <div className="review-metadata">
          <article>
            <small>{t("personalization.review.author")}</small>
            <strong>{item.creator}</strong>
          </article>
          <article>
            <small>{t("personalization.review.scope")}</small>
            <strong>{t(`personalization.target.${item.target}`)}</strong>
          </article>
          <article>
            <small>{t("personalization.review.status")}</small>
            <strong>{t(`creator.status.${item.status}`)}</strong>
          </article>
        </div>

        {item.status === "IN_REVIEW" ? (
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
                    <CurrencyMark currency={item.currency} size={18} />
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
        ) : (
          <section className="review-approve-panel">
            <button
              type="button"
              className={item.status === "PUBLISHED" ? "destructive-action" : "primary-action"}
              disabled={busy}
              onClick={() => void changeListing()}
            >
              <Glyph name={item.status === "PUBLISHED" ? "close" : "check"} size={16} />
              {busy
                ? t("personalization.review.saving")
                : item.status === "PUBLISHED"
                  ? t("personalization.review.unlist")
                  : t("personalization.review.relist")}
            </button>
          </section>
        )}
      </section>
    </div>
  )
}
