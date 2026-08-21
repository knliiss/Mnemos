import { useEffect, useState, type ReactNode } from "react"
import { useTranslation } from "react-i18next"
import { errorEvents } from "@/shared/errors/errorEventBus"
import { notifications } from "@/shared/notifications/notificationCenter"
import { realtimeClient } from "@/shared/realtime/realtimeClient"
import { realtimeBroadcasts, realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import type { DropSpotlightBroadcast, RealtimeEvent } from "@/shared/realtime/realtime.types"
import { Glyph } from "@/ui/shared/MnemosIcon"
import "@/ui/system/system.css"

const FULL_SPOTLIGHT_DURATION_MS = 7_000
const COMPACT_SPOTLIGHT_DURATION_MS = 4_500

export function RealtimeProvider({ children }: { children: ReactNode }) {
  const { t } = useTranslation()
  const [spotlight, setSpotlight] = useState<DropSpotlightBroadcast | null>(null)

  useEffect(() => {
    const stop = realtimeClient.start()
    const unsubscribe = realtimeEvents.subscribe((event) => {
      notifyAboutOperation(event, (key) => t(key))
    })

    return () => {
      unsubscribe()
      stop()
    }
  }, [t])

  useEffect(() => {
    const queue: DropSpotlightBroadcast[] = []
    const seenIds = new Set<string>()
    let currentSpotlight: DropSpotlightBroadcast | null = null
    let lastPresentedAt = 0
    let gapTimer: number | null = null
    let hideTimer: number | null = null
    let disposed = false

    const presentNext = () => {
      if (disposed || currentSpotlight || gapTimer !== null) {
        return
      }

      const now = Date.now()

      while (queue.length > 0 && Date.parse(queue[0].expiresAt) <= now) {
        queue.shift()
      }

      const next = queue[0]

      if (!next) {
        return
      }

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

    const unsubscribe = realtimeBroadcasts.subscribe((event) => {
      if (
        event.type !== "drop-spotlight" ||
        seenIds.has(event.id) ||
        Date.parse(event.expiresAt) <= Date.now()
      ) {
        return
      }

      seenIds.add(event.id)
      queue.push(event)
      queue.sort((left, right) => left.sequence - right.sequence)
      presentNext()
    })

    return () => {
      disposed = true
      unsubscribe()

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
      {spotlight ? <DropSpotlightAlert spotlight={spotlight} /> : null}
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

  return (
    <aside
      className={`realtime-spotlight ${compact ? "is-compact" : "is-full"}`}
      role="status"
      aria-live="polite"
      aria-atomic="true"
    >
      <span className="realtime-spotlight__mark" aria-hidden="true">
        {template?.emoji ? template.emoji : <Glyph name="item-drop" size={compact ? 18 : 22} />}
      </span>

      <span className="realtime-spotlight__copy">
        <small>
          SPOTLIGHT · {spotlight.payload.itemRarity}
        </small>
        <strong>{title}</strong>
        {compact ? <em>{body}</em> : <p>{body}</p>}
      </span>

      <span className="realtime-spotlight__pulse" aria-hidden="true">
        <i />
        <i />
        <i />
      </span>
    </aside>
  )
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
