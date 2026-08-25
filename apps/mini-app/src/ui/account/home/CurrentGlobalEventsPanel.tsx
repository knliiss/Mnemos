import { motion } from "framer-motion"
import { useCallback, useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { getActiveGlobalEvents } from "@/shared/api/eventApi"
import {
  reconcileGlobalEventPresence,
  useActiveGlobalEvents,
} from "@/shared/realtime/globalEventPresenceStore"
import { realtimeBroadcasts, realtimeConnectionEvents } from "@/shared/realtime/realtimeEventBus"
import { GlobalEventArt } from "@/ui/components/events/EventArt"
import type { Route } from "@/ui/model"
import "./current-global-events-state.css"

type CurrentGlobalEventsPanelProps = {
  navigate: (route: Route) => void
}

type PresenceSyncState = "LOADING" | "READY" | "ERROR"

export function CurrentGlobalEventsPanel({ navigate }: CurrentGlobalEventsPanelProps) {
  const { t } = useTranslation()
  const events = useActiveGlobalEvents()
  const [syncState, setSyncState] = useState<PresenceSyncState>("LOADING")
  const [now, setNow] = useState(() => Date.now())
  const requestRevision = useRef(0)

  const refresh = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision

    try {
      const response = await getActiveGlobalEvents()

      if (revision !== requestRevision.current) {
        return
      }

      reconcileGlobalEventPresence(
        response.map((event) => ({
          eventId: event.id,
          type: event.type,
          startedAt: event.startedAt,
          endsAt: event.endsAt,
        })),
      )
      setSyncState("READY")
    } catch {
      if (revision === requestRevision.current) {
        setSyncState("ERROR")
      }
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })
    const unsubscribeConnection = realtimeConnectionEvents.subscribe((state) => {
      if (state === "CONNECTED") {
        void refresh()
      }
    })
    const unsubscribeBroadcasts = realtimeBroadcasts.subscribe((event) => {
      if (event.type === "game-event" && event.payload.eventType === "GLOBAL_STARTED") {
        void refresh()
      }
    })
    const refreshWhenVisible = () => {
      if (document.visibilityState === "visible") {
        void refresh()
      }
    }
    const refreshOnFocus = () => {
      void refresh()
    }

    document.addEventListener("visibilitychange", refreshWhenVisible)
    window.addEventListener("focus", refreshOnFocus)

    return () => {
      requestRevision.current += 1
      window.cancelAnimationFrame(frame)
      unsubscribeConnection()
      unsubscribeBroadcasts()
      document.removeEventListener("visibilitychange", refreshWhenVisible)
      window.removeEventListener("focus", refreshOnFocus)
    }
  }, [refresh])

  useEffect(() => {
    if (!events.length) {
      return
    }

    setNow(Date.now())
    const timer = window.setInterval(() => setNow(Date.now()), 1_000)

    return () => window.clearInterval(timer)
  }, [events.length])

  if (!events.length && syncState === "LOADING") {
    return (
      <section className="account-home-global-events is-loading" aria-busy="true">
        <span className="account-home-global-events__loading-art" aria-hidden="true">
          <i />
          <i />
          <i />
        </span>
        <span className="account-home-global-events__loading-copy" aria-hidden="true">
          <i />
          <i />
          <i />
        </span>
      </section>
    )
  }

  if (!events.length && syncState === "ERROR") {
    return (
      <section className="account-home-global-events is-empty is-error">
        <span className="account-home-global-events__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
        </span>

        <span className="account-home-global-events__empty-copy">
          <small>{t("accountHome.globalEventsEyebrow")}</small>
          <strong>{t("common.loadErrorTitle")}</strong>
          <em>{t("common.loadErrorCopy")}</em>
          <button type="button" onClick={() => void refresh()}>
            {t("common.retry")}
          </button>
        </span>
      </section>
    )
  }

  if (!events.length) {
    return (
      <section className="account-home-global-events is-empty">
        <span className="account-home-global-events__mascot" aria-hidden="true">
          <img src="/ui-illustrations/mnemos-mascot-cat.svg" alt="" />
          <i />
        </span>

        <span className="account-home-global-events__empty-copy">
          <small>{t("accountHome.globalEventsEyebrow")}</small>
          <strong>{t("accountHome.globalEventsEmptyTitle")}</strong>
          <em>{t("accountHome.globalEventsEmptyCopy")}</em>
        </span>
      </section>
    )
  }

  return (
    <section className="account-home-global-events is-active">
      <header className="account-home-global-events__header">
        <span>
          <small>{t("accountHome.globalEventsEyebrow")}</small>
          <strong>{t("accountHome.globalEventsActiveTitle")}</strong>
        </span>
        <em>{t("accountHome.globalEventsCount", { count: events.length })}</em>
      </header>

      <div
        className={`account-home-global-events__grid account-home-global-events__grid--${events.length}`}
      >
        {events.map((event) => {
          const remaining = formatRemainingTime(event.endsAt, now)

          return (
            <motion.button
              type="button"
              key={event.eventId}
              className="account-home-global-event"
              data-event-type={event.type.toLowerCase()}
              whileTap={{ scale: 0.985 }}
              onClick={() =>
                navigate({
                  id: "event-detail",
                  params: { eventId: event.eventId },
                })
              }
            >
              <span className="account-home-global-event__art" aria-hidden="true">
                <GlobalEventArt eventType={event.type} size={events.length === 1 ? 70 : 52} />
              </span>

              <span className="account-home-global-event__copy">
                <small>
                  <i /> {t("accountHome.globalEventLive")}
                </small>
                <strong>{t(`events.globals.${event.type}`)}</strong>
                <em className="account-home-global-event__remaining">
                  {t("accountHome.globalEventRemaining", { time: remaining })}
                </em>
              </span>
            </motion.button>
          )
        })}
      </div>
    </section>
  )
}

export function formatRemainingTime(endsAt: string, now: number): string {
  const remainingSeconds = Math.max(0, Math.ceil((Date.parse(endsAt) - now) / 1_000))
  const hours = Math.floor(remainingSeconds / 3_600)
  const minutes = Math.floor((remainingSeconds % 3_600) / 60)
  const seconds = remainingSeconds % 60

  if (hours > 0) {
    return `${hours}:${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`
  }

  return `${String(minutes).padStart(2, "0")}:${String(seconds).padStart(2, "0")}`
}
