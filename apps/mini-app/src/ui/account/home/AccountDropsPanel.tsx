import { motion } from "framer-motion"
import { useTranslation } from "react-i18next"
import { EventArt } from "../../components/events/EventArt"
import { eventVisualTone } from "../../eventVisualTone"
import type { FeedEvent, Route } from "../../model"
import { formatOccurredAt } from "../../timePresentation"

type AccountDropsPanelProps = {
  accountId: string
  accountNickname: string
  drops: FeedEvent[]
  locale: string
  navigate: (route: Route) => void
}

export function AccountDropsPanel({
  accountId,
  accountNickname,
  drops,
  locale,
  navigate,
}: AccountDropsPanelProps) {
  const { t } = useTranslation()

  const openActivity = () => {
    navigate({
      id: "public-profile",
      params: {
        accountId,
        nickname: accountNickname,
        section: "activity",
      },
    })
  }

  return (
    <section className="account-home-panel account-home-drops">
      <header className="account-home-panel__head account-home-drops__head">
        <span className="account-home-drops__title-icon">
          <img src="/ui-icons/drop-stack.svg" alt="" aria-hidden="true" />
        </span>
        <span>
          <small>{t("accountHome.activity")}</small>
          <strong>{t("accountHome.latestDrops")}</strong>
        </span>

        <button type="button" className="account-home-panel__more" onClick={openActivity}>
          {t("accountHome.viewAll")}
          <span aria-hidden="true">›</span>
        </button>

        <img
          className="account-home-drops__activity-art"
          src="/ui-illustrations/account-activity.svg"
          alt=""
          aria-hidden="true"
        />
      </header>

      <div className="account-home-drop-list">
        {drops.length ? (
          drops.map((drop) => {
            const tone = eventVisualTone(drop)

            return (
              <motion.button
                type="button"
                className={`account-home-drop event-surface--${tone}`}
                key={drop.id}
                whileTap={{ scale: 0.992 }}
                onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}
              >
                <EventArt event={drop} size={42} />
                <span className="account-home-drop__copy">
                  <strong>{drop.title}</strong>
                  <small>{drop.meta}</small>
                </span>
                <time>{formatOccurredAt(drop.occurredAt, locale)}</time>
              </motion.button>
            )
          })
        ) : (
          <button type="button" className="account-home-drops__empty" onClick={openActivity}>
            <span>{t("accountHome.noDrops")}</span>
            <small>{t("accountHome.viewAll")} ›</small>
          </button>
        )}
      </div>
    </section>
  )
}
