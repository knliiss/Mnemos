import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createDropSpotlight,
  getDropSpotlight,
  getDropSpotlightOffer,
  type DropSpotlightOfferResponse,
  type DropSpotlightResponse,
} from "@/shared/api/spotlightApi"
import { ApiError } from "@/shared/api/httpClient"
import { handleError } from "@/shared/errors/errorHandler"
import { EventArt } from "../components/events/EventArt"
import { eventVisualTone } from "../eventVisualTone"
import type { Account, FeedEvent } from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { eventTitle, eventTypeLabel, rarityLabel } from "./eventPresentation"

const SPOTLIGHT_POLL_INTERVAL_MS = 700
const SPOTLIGHT_POLL_ATTEMPTS = 12

type Props = {
  event: FeedEvent
  activeAccount: Account
  locale: string
}

export function DropSpotlightPurchase({ event, activeAccount, locale }: Props) {
  const { t } = useTranslation()
  const [offer, setOffer] = useState<DropSpotlightOfferResponse | null>(null)
  const [offerUnavailable, setOfferUnavailable] = useState(false)
  const [purchaseWindowOpen, setPurchaseWindowOpen] = useState(true)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [busy, setBusy] = useState(false)
  const [purchase, setPurchase] = useState<DropSpotlightResponse | null>(null)
  const [failureKey, setFailureKey] = useState<string | null>(null)

  useEffect(() => {
    let cancelled = false

    void getDropSpotlightOffer()
      .then((response) => {
        if (cancelled) {
          return
        }

        const occurredAt = Date.parse(event.occurredAt)
        const withinPurchaseWindow =
          Number.isFinite(occurredAt) &&
          occurredAt + response.purchaseWindowSeconds * 1_000 > Date.now()

        setOffer(response)
        setOfferUnavailable(false)
        setPurchaseWindowOpen(withinPurchaseWindow)
      })
      .catch(() => {
        if (!cancelled) {
          setOfferUnavailable(true)
        }
      })

    return () => {
      cancelled = true
    }
  }, [event.occurredAt])

  const status = purchase?.status ?? null
  const purchased = status === "QUEUED" || status === "PUBLISHED" || status === "PENDING_PAYMENT"
  const canOpen = Boolean(offer && purchaseWindowOpen && !offerUnavailable && !busy && !purchased)
  const tone = eventVisualTone(event)
  const price = offer?.pricePrime ?? purchase?.pricePrime
  const rarity = rarityLabel(t, event.rarity)
  const itemType = eventTypeLabel(t, event)

  const purchaseSpotlight = async () => {
    if (!offer || busy) {
      return
    }

    setBusy(true)
    setFailureKey(null)
    setPurchase(null)

    try {
      const accepted = await createDropSpotlight(event.id)
      setPurchase(accepted)

      try {
        const resolved = await waitForSpotlight(accepted)
        setPurchase(resolved)

        if (resolved.status === "PAYMENT_FAILED") {
          setFailureKey("events.detail.spotlight.errors.paymentFailed")
        }
      } catch (pollError) {
        handleError(pollError)
      }
    } catch (error) {
      const expectedFailure = spotlightFailureKey(error)

      if (expectedFailure) {
        setFailureKey(expectedFailure)
      } else {
        setFailureKey("events.detail.spotlight.errors.purchaseFailed")
        handleError(error)
      }
    } finally {
      setBusy(false)
    }
  }

  const actionLabel = (() => {
    if (busy) {
      return t("events.detail.spotlight.processing")
    }

    if (status === "PUBLISHED") {
      return t("events.detail.spotlight.published")
    }

    if (status === "QUEUED") {
      return t("events.detail.spotlight.queued")
    }

    if (status === "PENDING_PAYMENT") {
      return t("events.detail.spotlight.paymentPending")
    }

    if (status === "PAYMENT_FAILED") {
      return t("events.detail.spotlight.retry")
    }

    if (!purchaseWindowOpen) {
      return t("events.detail.spotlight.expired")
    }

    return t("events.detail.spotlight.open")
  })()

  const purchaseStatusLabel = (() => {
    if (status === "PUBLISHED") {
      return t("events.detail.spotlight.published")
    }

    if (status === "QUEUED") {
      return t("events.detail.spotlight.queued")
    }

    if (status === "PAYMENT_FAILED") {
      return t("events.detail.spotlight.errors.paymentFailed")
    }

    return t("events.detail.spotlight.paymentPending")
  })()

  return (
    <>
      <section className={`event-detail-spotlight event-surface--${tone}`}>
        <span className="event-detail-spotlight__beacon" aria-hidden="true">
          <i />
          <i />
          <i />
        </span>

        <span className="event-detail-spotlight__copy">
          <small>{t("events.detail.spotlight.eyebrow")}</small>
          <strong>{t("events.detail.spotlight.title")}</strong>
          <em>
            {offerUnavailable
              ? t("events.detail.spotlight.unavailable")
              : offer
                ? t("events.detail.spotlight.copy", {
                    hours: Math.max(1, Math.round(offer.purchaseWindowSeconds / 3600)),
                  })
                : t("events.detail.spotlight.loading")}
          </em>
        </span>

        {price !== undefined ? (
          <span className="event-detail-spotlight__price">
            <CurrencyMark currency="PRIME" size={14} />
            <b>{price}</b>
          </span>
        ) : null}

        <motion.button
          type="button"
          whileTap={canOpen ? { scale: 0.97 } : undefined}
          disabled={!canOpen}
          onClick={() => {
            setFailureKey(null)
            setDialogOpen(true)
          }}
        >
          {actionLabel}
        </motion.button>
      </section>

      <AnimatePresence>
        {dialogOpen ? (
          <motion.div
            className="spotlight-purchase-backdrop"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onMouseDown={(pointerEvent) => {
              if (pointerEvent.target === pointerEvent.currentTarget && !busy) {
                setDialogOpen(false)
              }
            }}
          >
            <motion.section
              className="spotlight-purchase-dialog"
              role="dialog"
              aria-modal="true"
              aria-labelledby="spotlight-purchase-title"
              initial={{ opacity: 0, y: 14, scale: 0.985 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, y: 8, scale: 0.99 }}
            >
              <header className="spotlight-purchase-dialog__head">
                <span>
                  <small>{t("events.detail.spotlight.previewEyebrow")}</small>
                  <strong id="spotlight-purchase-title">
                    {t("events.detail.spotlight.previewTitle")}
                  </strong>
                </span>

                <button
                  type="button"
                  disabled={busy}
                  onClick={() => setDialogOpen(false)}
                  aria-label={t("common.close")}
                >
                  <Glyph name="close" size={16} />
                </button>
              </header>

              <div className={`spotlight-purchase-preview event-surface--${tone}`}>
                <span className="spotlight-purchase-preview__art" aria-hidden="true">
                  <EventArt event={event} size={68} />
                </span>

                <span className="spotlight-purchase-preview__copy">
                  <small>
                    <i /> SPOTLIGHT{rarity ? ` · ${rarity}` : ""}
                  </small>
                  <strong>{eventTitle(t, event)}</strong>
                  <em>
                    {activeAccount.nickname}
                    {itemType ? ` · ${itemType}` : ""}
                  </em>
                </span>

                <span className="spotlight-purchase-preview__signal" aria-hidden="true">
                  <i />
                  <i />
                  <i />
                </span>
              </div>

              <p className="spotlight-purchase-dialog__hint">
                {t("events.detail.spotlight.previewHint")}
              </p>

              {failureKey ? (
                <p className="spotlight-purchase-dialog__error">{t(failureKey)}</p>
              ) : null}

              {purchase && purchase.status !== "PAYMENT_FAILED" ? (
                <div className="spotlight-purchase-dialog__status">
                  <span>
                    <small>{t("events.detail.spotlight.status")}</small>
                    <strong>{purchaseStatusLabel}</strong>
                  </span>
                  <span>
                    <small>{t("events.detail.spotlight.scheduled")}</small>
                    <strong>{formatScheduledAt(purchase.scheduledAt, locale)}</strong>
                  </span>
                </div>
              ) : null}

              <footer className="spotlight-purchase-dialog__actions">
                <button type="button" disabled={busy} onClick={() => setDialogOpen(false)}>
                  {purchase && purchase.status !== "PAYMENT_FAILED"
                    ? t("events.detail.spotlight.done")
                    : t("common.cancel")}
                </button>

                {!purchase || purchase.status === "PAYMENT_FAILED" ? (
                  <motion.button
                    type="button"
                    className="is-primary"
                    whileTap={{ scale: 0.97 }}
                    disabled={busy || !offer}
                    onClick={() => void purchaseSpotlight()}
                  >
                    {busy ? (
                      t("events.detail.spotlight.processing")
                    ) : (
                      <>
                        {t("events.detail.spotlight.confirm")}
                        {offer ? (
                          <span>
                            <CurrencyMark currency="PRIME" size={13} />
                            {offer.pricePrime}
                          </span>
                        ) : null}
                      </>
                    )}
                  </motion.button>
                ) : null}
              </footer>
            </motion.section>
          </motion.div>
        ) : null}
      </AnimatePresence>
    </>
  )
}

async function waitForSpotlight(initial: DropSpotlightResponse): Promise<DropSpotlightResponse> {
  let current = initial

  for (let attempt = 0; attempt < SPOTLIGHT_POLL_ATTEMPTS; attempt += 1) {
    if (current.status !== "PENDING_PAYMENT") {
      return current
    }

    await delay(SPOTLIGHT_POLL_INTERVAL_MS)
    current = await getDropSpotlight(current.id)
  }

  return current
}

function delay(milliseconds: number): Promise<void> {
  return new Promise((resolve) => window.setTimeout(resolve, milliseconds))
}

function spotlightFailureKey(error: unknown): string | null {
  if (!(error instanceof ApiError) || typeof error.body === "string") {
    return null
  }

  if (error.body.code === "SPOTLIGHT_PURCHASE_WINDOW_CLOSED") {
    return "events.detail.spotlight.errors.expired"
  }

  if (error.body.code === "ITEM_DROP_ALREADY_SPOTLIGHTED") {
    return "events.detail.spotlight.errors.alreadyUsed"
  }

  if (error.body.code === "SPOTLIGHT_BUYER_COOLDOWN_ACTIVE") {
    return "events.detail.spotlight.errors.cooldown"
  }

  if (error.body.code === "SPOTLIGHT_QUEUE_LIMIT_EXCEEDED") {
    return "events.detail.spotlight.errors.queueFull"
  }

  return null
}

function formatScheduledAt(value: string, locale: string): string {
  const date = new Date(value)

  if (Number.isNaN(date.getTime())) {
    return "—"
  }

  return new Intl.DateTimeFormat(locale, {
    hour: "2-digit",
    minute: "2-digit",
  }).format(date)
}
