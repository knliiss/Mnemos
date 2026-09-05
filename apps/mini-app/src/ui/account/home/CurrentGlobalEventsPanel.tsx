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
import { formatGlobalEventRemainingTime } from "./globalEventCountdown"

type CurrentGlobalEventsPanelProps = {
  navigate: (route: Route) => void
}

export function CurrentGlobalEventsPanel({ navigate }: CurrentGlobalEventsPanelProps) {
  const { t } = useTranslation()
  const events = useActiveGlobalEvents()
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
    } catch {
      // Global events are contextual dashboard content. Keep the dashboard quiet when sync fails.
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

    const timer = window.setInterval(() => setNow(Date.now()), 1_000)

    return () => window.clearInterval(timer)
  }, [events.length])

  if (!events.length) {
    return null
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
          const remaining = formatGlobalEventRemainingTime(event.endsAt, now)

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
