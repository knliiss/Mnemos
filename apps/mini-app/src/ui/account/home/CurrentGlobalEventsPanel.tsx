import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { useActiveGlobalEvents } from "@/shared/realtime/globalEventPresenceStore"
import { GlobalEventArt } from "@/ui/components/events/EventArt"
import type { Route } from "@/ui/model"
import { localeForLanguage } from "../accountPresentation"

type CurrentGlobalEventsPanelProps = {
  navigate: (route: Route) => void
}

export function CurrentGlobalEventsPanel({ navigate }: CurrentGlobalEventsPanelProps) {
  const { t, i18n } = useTranslation()
  const events = useActiveGlobalEvents()
  const locale = localeForLanguage(i18n.resolvedLanguage)

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
