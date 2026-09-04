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
import { NotificationPackTelegramPreview } from "../../personalization/components/NotificationPackTelegramPreview"
import { toPersonalization } from "../../personalization/data/personalizationMappers"
import { personalizationIllustrations } from "../../personalization/personalizationPresentation"
import { CurrencyMark, Glyph } from "../../shared/MnemosIcon"
import { Field, PageHeader } from "../../shared/Primitives"
import { FeatureEmpty } from "../../system/FeatureEmpty"
import { FeatureLoading } from "../../system/FeatureLoading"

type ModerationView = "REVIEW" | "PUBLISHED" | "UNLISTED"
type MarketplaceAction = "RELIST" | "UNLIST"

type ReviewDecisionConfirmation =
  | {
      decision: "APPROVE"
      price: number
    }
  | {
      decision: "REJECT"
      reason: string
    }

type Props = {
  personalizationId: string
  view: ModerationView
  back: () => void
}

export function PersonalizationReviewDetail({ personalizationId, view, back }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const [item, setItem] = useState<Personalization | null>(null)
  const [systemManaged, setSystemManaged] = useState(false)
  const [price, setPrice] = useState("")
  const [reason, setReason] = useState("")
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState(false)
  const [busy, setBusy] = useState(false)
  const [decisionConfirmation, setDecisionConfirmation] =
    useState<ReviewDecisionConfirmation | null>(null)
  const [marketplaceConfirmation, setMarketplaceConfirmation] = useState<MarketplaceAction | null>(
    null,
  )
  const mountedRef = useRef(true)
  const busyRef = useRef(false)
  const decisionConfirmRef = useRef<HTMLButtonElement>(null)
  const marketplaceConfirmRef = useRef<HTMLButtonElement>(null)

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
      setSystemManaged(response?.origin === "SYSTEM")
      setPrice(next ? String(next.price) : "")
      setReason("")
      setDecisionConfirmation(null)
      setMarketplaceConfirmation(null)
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

  useEffect(() => {
    if (!decisionConfirmation) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      decisionConfirmRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [decisionConfirmation])

  useEffect(() => {
    if (!marketplaceConfirmation) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      marketplaceConfirmRef.current?.focus()
    })

    return () => {
      window.cancelAnimationFrame(frame)
    }
  }, [marketplaceConfirmation])

  const numericPrice = Number(price)
  const priceValid = Number.isInteger(numericPrice) && numericPrice > 0

  const prepareDecision = (decision: "APPROVE" | "REJECT") => {
    if (!item || item.status !== "IN_REVIEW" || busyRef.current) {
      return
    }

    if (decision === "APPROVE") {
      if (!priceValid) {
        return
      }

      setDecisionConfirmation({ decision, price: numericPrice })
      return
    }

    const trimmedReason = reason.trim()

    if (!trimmedReason) {
      return
    }

    setDecisionConfirmation({ decision, reason: trimmedReason })
  }

  const applyDecision = async () => {
    const confirmation = decisionConfirmation

    if (!item || item.status !== "IN_REVIEW" || !confirmation || busyRef.current) {
      return
    }

    busyRef.current = true
    setBusy(true)

    try {
      if (confirmation.decision === "APPROVE") {
        await approvePersonalization(item.id, item.currency, confirmation.price)
      } else {
        await rejectPersonalization(item.id, confirmation.reason)
      }

      if (mountedRef.current) {
        setDecisionConfirmation(null)
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

  const prepareMarketplaceChange = () => {
    if (!item || systemManaged || busyRef.current) {
      return
    }

    if (item.status === "PUBLISHED") {
      setMarketplaceConfirmation("UNLIST")
    } else if (item.status === "UNLISTED") {
      setMarketplaceConfirmation("RELIST")
    }
  }

  const applyMarketplaceChange = async () => {
    const confirmation = marketplaceConfirmation

    if (!item || systemManaged || !confirmation || busyRef.current) {
      return
    }

    if (
      (confirmation === "UNLIST" && item.status !== "PUBLISHED") ||
      (confirmation === "RELIST" && item.status !== "UNLISTED")
    ) {
      return
    }

    busyRef.current = true
    setBusy(true)

    try {
      if (confirmation === "UNLIST") {
        await unlistPersonalization(item.id)
      } else {
        await relistPersonalization(item.id)
      }

      if (mountedRef.current) {
        setMarketplaceConfirmation(null)
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

  const decisionIsApprove = decisionConfirmation?.decision === "APPROVE"
  const marketplaceIsUnlist = marketplaceConfirmation === "UNLIST"

  return (
    <div className="screen personalization-review-screen personalization-review-detail-screen">
      <PageHeader title={item.name} back={back} />

      <section className="review-workbench review-workbench--standalone">
        <header>
          <span>
            <small>{t("personalization.review.detailEyebrow")}</small>
            <strong>{item.name}</strong>
            <em>
              {t(`personalization.types.${item.type}.title`)} ·{" "}
              {t(`personalization.target.${item.target}`)}
            </em>
          </span>
        </header>

        {item.content?.kind === "NOTIFICATION_PACK" ? (
          <>
            <div className="review-workbench__notification-art" aria-hidden="true">
              <img src={personalizationIllustrations.NOTIFICATION_PACK} alt="" />
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
          <>
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
                      disabled={busy || Boolean(decisionConfirmation)}
                      onChange={(event) => setPrice(event.target.value)}
                    />
                  </div>
                </Field>
                <button
                  type="button"
                  className="primary-action"
                  disabled={!priceValid || busy || Boolean(decisionConfirmation)}
                  onClick={() => prepareDecision("APPROVE")}
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
                    disabled={busy || Boolean(decisionConfirmation)}
                    onChange={(event) => setReason(event.target.value)}
                    placeholder={t("personalization.review.rejectionPlaceholder")}
                  />
                </Field>
                <button
                  type="button"
                  className="destructive-action"
                  disabled={!reason.trim() || busy || Boolean(decisionConfirmation)}
                  onClick={() => prepareDecision("REJECT")}
                >
                  <Glyph name="close" size={16} />
                  {busy ? t("personalization.review.saving") : t("personalization.review.reject")}
                </button>
              </section>
            </div>

            {decisionConfirmation ? (
              <div
                className={
                  decisionIsApprove
                    ? "review-decision-confirmation is-approve"
                    : "review-decision-confirmation is-reject"
                }
                role="alertdialog"
                aria-label={t("personalization.review.decisionConfirmTitle")}
                onKeyDown={(event) => {
                  if (event.key === "Escape" && !busy) {
                    event.preventDefault()
                    setDecisionConfirmation(null)
                  }
                }}
              >
                <span className="review-decision-confirmation__art" aria-hidden="true">
                  <img
                    src={
                      decisionIsApprove
                        ? "/ui-illustrations/mnemos-mascot-event.svg"
                        : "/ui-illustrations/mnemos-mascot-search.svg"
                    }
                    alt=""
                  />
                </span>
                <span className="review-decision-confirmation__copy">
                  <small>
                    {decisionIsApprove
                      ? t("personalization.review.approveConfirmEyebrow")
                      : t("personalization.review.rejectConfirmEyebrow")}
                  </small>
                  <strong>
                    {decisionIsApprove
                      ? t("personalization.review.approveConfirmTitle")
                      : t("personalization.review.rejectConfirmTitle")}
                  </strong>
                  <p>
                    {decisionIsApprove
                      ? t("personalization.review.approveConfirmCopy", {
                          price: decisionConfirmation.price,
                          currency: item.currency,
                        })
                      : t("personalization.review.rejectConfirmCopy", {
                          reason: decisionConfirmation.reason,
                        })}
                  </p>
                </span>
                <div className="review-decision-confirmation__actions">
                  <button
                    ref={decisionConfirmRef}
                    type="button"
                    className={decisionIsApprove ? "primary-action" : "destructive-action"}
                    disabled={busy}
                    onClick={() => void applyDecision()}
                  >
                    <Glyph name={decisionIsApprove ? "check" : "close"} size={16} />
                    {busy
                      ? t("personalization.review.saving")
                      : decisionIsApprove
                        ? t("personalization.review.confirmApprove")
                        : t("personalization.review.confirmReject")}
                  </button>
                  <button
                    type="button"
                    className="review-listing-cancel"
                    disabled={busy}
                    onClick={() => setDecisionConfirmation(null)}
                  >
                    {t("common.cancel")}
                  </button>
                </div>
              </div>
            ) : null}
          </>
        ) : systemManaged ? null : (
          <section className="review-approve-panel review-listing-panel">
            {marketplaceConfirmation ? (
              <div
                className={
                  marketplaceIsUnlist
                    ? "review-listing-confirmation is-unlist"
                    : "review-listing-confirmation is-relist"
                }
                role="alertdialog"
                aria-label={
                  marketplaceIsUnlist
                    ? t("personalization.review.unlistConfirmTitle")
                    : t("personalization.review.relistConfirmTitle")
                }
                onKeyDown={(event) => {
                  if (event.key === "Escape" && !busy) {
                    event.preventDefault()
                    setMarketplaceConfirmation(null)
                  }
                }}
              >
                <span className="review-listing-confirmation__art" aria-hidden="true">
                  <img
                    src={
                      marketplaceIsUnlist
                        ? "/ui-illustrations/mnemos-mascot-event.svg"
                        : "/ui-illustrations/mnemos-mascot-cat.svg"
                    }
                    alt=""
                  />
                </span>
                <span className="review-listing-confirmation__copy">
                  <small>{t("personalization.review.unlistConfirmEyebrow")}</small>
                  <strong>
                    {marketplaceIsUnlist
                      ? t("personalization.review.unlistConfirmTitle")
                      : t("personalization.review.relistConfirmTitle")}
                  </strong>
                  <p>
                    {marketplaceIsUnlist
                      ? t("personalization.review.unlistConfirmCopy")
                      : t("personalization.review.relistConfirmCopy")}
                  </p>
                </span>
                <div className="review-listing-confirmation__actions">
                  <button
                    ref={marketplaceConfirmRef}
                    type="button"
                    className={marketplaceIsUnlist ? "destructive-action" : "primary-action"}
                    disabled={busy}
                    onClick={() => void applyMarketplaceChange()}
                  >
                    <Glyph name={marketplaceIsUnlist ? "close" : "check"} size={16} />
                    {busy
                      ? t("personalization.review.saving")
                      : marketplaceIsUnlist
                        ? t("personalization.review.unlist")
                        : t("personalization.review.relist")}
                  </button>
                  <button
                    type="button"
                    className="review-listing-cancel"
                    disabled={busy}
                    onClick={() => setMarketplaceConfirmation(null)}
                  >
                    {t("common.cancel")}
                  </button>
                </div>
              </div>
            ) : (
              <button
                type="button"
                className={item.status === "PUBLISHED" ? "destructive-action" : "primary-action"}
                disabled={busy}
                onClick={prepareMarketplaceChange}
              >
                <Glyph name={item.status === "PUBLISHED" ? "close" : "check"} size={16} />
                {busy
                  ? t("personalization.review.saving")
                  : item.status === "PUBLISHED"
                    ? t("personalization.review.unlist")
                    : t("personalization.review.relist")}
              </button>
            )}
          </section>
        )}
      </section>
    </div>
  )
}
