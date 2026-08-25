import { motion } from "framer-motion"
import { useCallback, useEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import { getActiveGlobalEvents } from "@/shared/api/eventApi"
import {
  replaceGlobalEventPresence,
  useActiveGlobalEvents,
} from "@/shared/realtime/globalEventPresenceStore"
import { realtimeBroadcasts, realtimeConnectionEvents } from "@/shared/realtime/realtimeEventBus"
import { GlobalEventArt } from "@/ui/components/events/EventArt"
import type { Route } from "@/ui/model"
import { localeForLanguage } from "../accountPresentation"
import "./current-global-events-state.css"

type CurrentGlobalEventsPanelProps = {
  navigate: (route: Route) => void
}

type PresenceSyncState = "LOADING" | "READY" | "ERROR"

export function CurrentGlobalEventsPanel({ navigate }: CurrentGlobalEventsPanelProps) {
  const { t, i18n } = useTranslation()
  const events = useActiveGlobalEvents()
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const [syncState, setSyncState] = useState<PresenceSyncState>("LOADING")
  const requestRevision = useRef(0)

  const refresh = useCallback(async () => {
    const revision = requestRevision.current + 1

    requestRevision.current = revision

    try {
      const response = await getActiveGlobalEvents()

      if (revision !== requestRevision.current) {
        return
      }

      replaceGlobalEventPresence(
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
          const startedAt = new Date(event.startedAt).toLocaleTimeString(locale, {
            hour: "2-digit",
            minute: "2-digit",
          })

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
                <em>{t("accountHome.globalEventStartedAt", { time: startedAt })}</em>
              </span>
            </motion.button>
          )
        })}
      </div>
    </section>
  )
}
