import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { EventArt } from "../../../components/events/EventArt"
import { eventVisualTone } from "../../../eventVisualTone"
import type { FeedEvent, Route } from "../../../model"
import { formatOccurredAt } from "../../../timePresentation"
import { rarityLabel } from "../profilePresentation"

type PublicProfileDropsSectionProps = {
  drops: FeedEvent[]
  locale: string
  navigate: (route: Route) => void
}

export function PublicProfileDropsSection({
  drops,
  locale,
  navigate,
}: PublicProfileDropsSectionProps) {
  const { t } = useTranslation()

  return (
    <section className="public-profile-section">
      <header className="public-profile-section-head">
        <span>
          <small>{t("people.profile.activity")}</small>
          <strong>{t("people.profile.latestDrops")}</strong>
        </span>
        <b>{drops.length}</b>
      </header>

      <div className="public-profile-drops">
        {drops.map((drop, index) => (
          <motion.button
            key={drop.id}
            type="button"
            className={`public-profile-drop event-surface--${eventVisualTone(drop)}`}
            initial={{ opacity: 0, y: 6 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.045 }}
            whileTap={{ scale: 0.992 }}
            onClick={() =>
              navigate({
                id: "event-detail",
                params: { eventId: drop.id },
              })
            }
          >
            <span className="public-profile-drop__art">
              <EventArt event={drop} size={46} />
            </span>

            <span className="public-profile-drop__copy">
              <small>{rarityLabel(drop.rarity, t)}</small>
              <strong>{drop.title}</strong>
              <em>{formatOccurredAt(drop.occurredAt, locale)}</em>
            </span>

            <span className="public-profile-drop__order">0{index + 1}</span>
            <span className="public-profile-drop__open" aria-hidden="true">
              ›
            </span>
          </motion.button>
        ))}

        {!drops.length ? (
          <div className="public-profile-state public-profile-state--compact public-profile-drops-empty">
            <img src="/ui-illustrations/account-activity.svg" alt="" aria-hidden="true" />
            <strong>{t("people.profile.noDrops")}</strong>
          </div>
        ) : null}
      </div>
    </section>
  )
}
