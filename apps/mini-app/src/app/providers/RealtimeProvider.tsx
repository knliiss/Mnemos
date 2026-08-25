import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useState, type ReactNode } from "react"
import { useTranslation } from "react-i18next"
import { errorEvents } from "@/shared/errors/errorEventBus"
import { notifications } from "@/shared/notifications/notificationCenter"
import { realtimeClient } from "@/shared/realtime/realtimeClient"
import { realtimeBroadcasts, realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import { spotlightPreviews } from "@/shared/realtime/spotlightPreviewBus"
import type { DropSpotlightBroadcast, RealtimeEvent } from "@/shared/realtime/realtime.types"
import { ItemTypeArt } from "@/ui/components/events/EventArt"
import type { BetItemType } from "@/ui/model"
import { Glyph } from "@/ui/shared/MnemosIcon"
import "@/ui/system/system.css"
import "@/ui/system/spotlight-position.css"

const FULL_SPOTLIGHT_DURATION_MS = 7_000
const COMPACT_SPOTLIGHT_DURATION_MS = 4_500
const supportedSpotlightItemTypes: BetItemType[] = ["SWORD", "AURA", "PET", "BOOK", "JEWELRY"]

type QueuedSpotlight = {
  spotlight: DropSpotlightBroadcast
  requiresAcknowledgement: boolean
}

export function RealtimeProvider({ children }: { children: ReactNode }) {
  const { t } = useTranslation()
  const [spotlight, setSpotlight] = useState<DropSpotlightBroadcast | null>(null)

  useEffect(() => {
    const stop = realtimeClient.start()

    return () => {
      stop()
    }
  }, [])

  useEffect(() => {
    const unsubscribe = realtimeEvents.subscribe((event) => {
      notifyAboutOperation(event, (key) => t(key))
    })

    return () => {
      unsubscribe()
    }
  }, [t])

  useEffect(() => {
    const queue: QueuedSpotlight[] = []
    const seenIds = new Set<string>()
    let currentSpotlight: DropSpotlightBroadcast | null = null
    let lastPresentedAt = 0
    let gapTimer: number | null = null
    let hideTimer: number | null = null
    let disposed = false

    const acknowledge = (broadcast: DropSpotlightBroadcast) => {
      void realtimeClient.acknowledgeBroadcast(broadcast.sequence).catch(() => {
        // Replay remains safe: the in-memory set suppresses duplicates for this open session,
        // while a later reconnect can retry acknowledgement for the same broadcast.
      })
    }

    const presentNext = () => {
      if (disposed || currentSpotlight || gapTimer !== null) {
        return
      }

      const now = Date.now()

      while (queue.length > 0 && Date.parse(queue[0].spotlight.expiresAt) <= now) {
        queue.shift()
      }

      const nextQueued = queue[0]

      if (!nextQueued) {
        return
      }

      const next = nextQueued.spotlight
      const expiresAt = Date.parse(next.expiresAt)
      const minimumGapMs = next.payload.minimumPresentationGapSeconds * 1_000
      const earliestPresentationAt = lastPresentedAt + minimumGapMs
      const wakeAt = Math.min(earliestPresentationAt, expiresAt)
      const delayMs = Math.max(0, wakeAt - now)

      if (lastPresentedAt > 0 && delayMs > 0) {
        gapTimer = window.setTimeout(() => {
          gapTimer = null
          presentNext()
        }, delayMs)
        return
      }

      queue.shift()
      currentSpotlight = next
      lastPresentedAt = now

      if (nextQueued.requiresAcknowledgement) {
        acknowledge(next)
      }

      setSpotlight(next)

      const requestedDuration =
        next.presentationMode === "COMPACT"
          ? COMPACT_SPOTLIGHT_DURATION_MS
          : FULL_SPOTLIGHT_DURATION_MS
      const remainingLifetime = Math.max(0, expiresAt - now)
      const visibleDuration = Math.min(requestedDuration, remainingLifetime)

      hideTimer = window.setTimeout(() => {
        hideTimer = null
        currentSpotlight = null
        setSpotlight((visible) => (visible?.id === next.id ? null : visible))
        presentNext()
      }, visibleDuration)
    }

    const enqueueSpotlight = (
      event: DropSpotlightBroadcast,
      requiresAcknowledgement: boolean,
    ) => {
      if (seenIds.has(event.id) || Date.parse(event.expiresAt) <= Date.now()) {
        return
      }

      seenIds.add(event.id)
      queue.push({
        spotlight: event,
        requiresAcknowledgement,
      })
      queue.sort((left, right) => left.spotlight.sequence - right.spotlight.sequence)
      presentNext()
    }

    const unsubscribeBroadcasts = realtimeBroadcasts.subscribe((event) => {
      if (event.type !== "drop-spotlight") {
        return
      }

      enqueueSpotlight(event, true)
    })

    const unsubscribePreviews = spotlightPreviews.subscribe((event) => {
      enqueueSpotlight(event, false)
    })

    return () => {
      disposed = true
      unsubscribeBroadcasts()
      unsubscribePreviews()

      if (gapTimer !== null) {
        window.clearTimeout(gapTimer)
      }

      if (hideTimer !== null) {
        window.clearTimeout(hideTimer)
      }
    }
  }, [])

  return (
    <>
      {children}
      <AnimatePresence mode="wait">
        {spotlight ? <DropSpotlightAlert key={spotlight.id} spotlight={spotlight} /> : null}
      </AnimatePresence>
    </>
  )
}

function DropSpotlightAlert({ spotlight }: { spotlight: DropSpotlightBroadcast }) {
  const template = spotlight.payload.notificationPack?.templates.find(
    (candidate) => candidate.type === "ITEM_DROP",
  )
  const values = {
    player: spotlight.payload.droppedFor,
    item: spotlight.payload.itemName,
    rarity: spotlight.payload.itemRarity,
    itemType: spotlight.payload.itemType,
  }
  const fallbackBody = `${spotlight.payload.droppedFor} · ${spotlight.payload.itemRarity} · ${spotlight.payload.itemType}`
  const title = renderedTemplate(template?.soloTitle, values) || spotlight.payload.itemName
  const bodyTemplate =
    spotlight.presentationMode === "COMPACT" ? template?.compact : template?.soloBody
  const body = renderedTemplate(bodyTemplate, values) || fallbackBody
  const compact = spotlight.presentationMode === "COMPACT"
  const itemType = resolveSpotlightItemType(spotlight.payload.itemType)

  return (
    <motion.div
      className="realtime-spotlight-host"
      initial={{ opacity: 0, y: -24, scale: 0.955, filter: "blur(10px)" }}
      animate={{ opacity: 1, y: 0, scale: 1, filter: "blur(0px)" }}
      exit={{ opacity: 0, y: -13, scale: 0.985, filter: "blur(6px)" }}
      transition={{ duration: 0.34, ease: [0.22, 1, 0.36, 1] }}
    >
      <aside
        className={`realtime-spotlight ${compact ? "is-compact" : "is-full"}`}
        data-rarity={spotlight.payload.itemRarity.toLowerCase()}
        role="status"
        aria-live="polite"
        aria-atomic="true"
      >
        <span className="realtime-spotlight__scan" aria-hidden="true" />
        <span className="realtime-spotlight__corners" aria-hidden="true">
          <i />
          <i />
          <i />
          <i />
        </span>

        <motion.span
          className="realtime-spotlight__visual"
          aria-hidden="true"
          animate={{ y: [0, -3, 0], rotate: [0, -0.7, 0.7, 0] }}
          transition={{ duration: 2.8, repeat: Infinity, ease: "easeInOut" }}
        >
          {itemType ? (
            <ItemTypeArt itemType={itemType} size={compact ? 40 : 78} />
          ) : (
            <Glyph name="item-drop" size={compact ? 20 : 32} />
          )}
          {template?.emoji ? <b>{template.emoji}</b> : null}
        </motion.span>

        <span className="realtime-spotlight__copy">
          <small>
            <i /> MNEMOS SPOTLIGHT · {spotlight.payload.itemRarity}
          </small>
          <strong>{title}</strong>
          {compact ? <em>{body}</em> : <p>{body}</p>}
          {!compact ? (
            <span className="realtime-spotlight__meta">
              <b>{spotlight.payload.droppedFor}</b>
              <i />
              <em>{spotlight.payload.itemType}</em>
            </span>
          ) : null}
        </span>

        <span className="realtime-spotlight__signal" aria-hidden="true">
          <i />
          <i />
          <i />
        </span>
        <span className="realtime-spotlight__lifetime" aria-hidden="true" />
      </aside>
    </motion.div>
  )
}

function resolveSpotlightItemType(value: string): BetItemType | null {
  return supportedSpotlightItemTypes.includes(value as BetItemType) ? (value as BetItemType) : null
}

function renderedTemplate(template: string | undefined, values: Record<string, string>): string {
  if (!template) {
    return ""
  }

  return template
    .replace(/\{([a-zA-Z][a-zA-Z0-9_]*)}/g, (_, key: string) => values[key] ?? `{${key}}`)
    .trim()
}

function notifyAboutOperation(event: RealtimeEvent, translate: (key: string) => string) {
  if (event.type === "wallet.transaction.failed") {
    if (event.payload.source !== "BET_PLACE") {
      errorEvents.publish({
        code: "WALLET_TRANSACTION_FAILED",
        message: translate("realtime.transactionFailed"),
        statusCode: 409,
        details: {
          reason: event.payload.reason,
          source: event.payload.source,
        },
        requestId: event.payload.requestId,
      })
    }

    return
  }

  if (event.type !== "bet.operation.completed") {
    return
  }

  const status = event.payload.status

  if (status === "CONFIRMED") {
    notifications.success(translate("realtime.betConfirmed"))
    return
  }

  if (status === "REJECTED") {
    notifications.warning(translate("realtime.betRejected"))
    return
  }

  errorEvents.publish({
    code: "BET_OPERATION_FAILED",
    message: event.payload.failureReason ?? translate("realtime.betFailed"),
    statusCode: 500,
    details: {
      resourceType: event.payload.resourceType,
      resourceId: event.payload.resourceId,
    },
  })
}
