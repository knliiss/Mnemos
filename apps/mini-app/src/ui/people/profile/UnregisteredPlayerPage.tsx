import { motion } from "framer-motion"
import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { getEventFeed } from "@/shared/api/eventApi"
import type { PlayerResponse } from "@/shared/api/playerDirectoryApi"
import { handleError } from "@/shared/errors/errorHandler"
import { EventArt } from "../../components/events/EventArt"
import { CristalixHead } from "../../components/profile/CristalixHead"
import { toFeedEvent } from "../../data/dataMappers"
import type { FeedEvent, Route } from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import { PageHeader } from "../../shared/Primitives"
import { formatOccurredAt } from "../../timePresentation"
import { eventTitle, localeForLanguage } from "../../events/eventPresentation"
import { usePresenceSubscription } from "../presence/usePresenceSubscription"
import {
  canSubscribeToPresence,
  formatPresenceFreshness,
  formatPresenceTimestamp,
  presenceStatusClassName,
} from "../presence/presencePresentation"
import "../presence/presence.css"
import "./unregistered-player.css"

const SKIN_BASE_URL = "https://webdata.c7x.dev/textures/skin"

type Props = {
  player: PlayerResponse
  navigate: (route: Route) => void
  back: () => void
}

type DropsState =
  | { key: string; status: "loading" }
  | { key: string; status: "ready"; items: FeedEvent[] }
  | { key: string; status: "error" }

export function UnregisteredPlayerPage({ player, navigate, back }: Props) {
  const { t, i18n } = useTranslation()
  const [dropsRevision, setDropsRevision] = useState(0)
  const requestKey = `${player.playerId}:${player.nickname.toLocaleLowerCase()}:${dropsRevision}`
  const [dropsState, setDropsState] = useState<DropsState>({ key: requestKey, status: "loading" })
  const activeDropsState: DropsState =
    dropsState.key === requestKey ? dropsState : { key: requestKey, status: "loading" }
  const drops = activeDropsState.status === "ready" ? activeDropsState.items : []
  const dropsLoading = activeDropsState.status === "loading"
  const dropsFailed = activeDropsState.status === "error"
  const subscription = usePresenceSubscription(player.playerId)
  const canSubscribe = canSubscribeToPresence(player.presenceStatus)
  const locale = localeForLanguage(i18n.resolvedLanguage)
  const lastSeen = formatPresenceTimestamp(player.lastSeenOnline, locale)
  const freshness = formatPresenceFreshness(player.observedAt)

  useEffect(() => {
    let cancelled = false

    void getEventFeed({
      types: ["ITEM_DROP"],
      droppedFor: player.nickname,
      sort: "NEWEST",
      limit: 6,
    })
      .then((response) => {
        if (!cancelled) {
          setDropsState({
            key: requestKey,
            status: "ready",
            items: response.items.map(toFeedEvent),
          })
        }
      })
      .catch((error) => {
        if (!cancelled) {
          handleError(error)
          setDropsState({ key: requestKey, status: "error" })
        }
      })

    return () => {
      cancelled = true
    }
  }, [player.nickname, requestKey])

  const openDropFeed = () => {
    navigate({
      id: "events",
      params: { filter: "ITEM_DROP", query: player.nickname },
    })
  }

  return (
    <div className="screen public-profile-screen unregistered-player-screen">
      <PageHeader
        title={player.nickname}
        eyebrow={t("people.profile.externalEyebrow")}
        back={back}
      />

      <motion.section
        className="unregistered-player-hero"
        initial={{ opacity: 0, y: 6 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <span className="unregistered-player-hero__head">
          <CristalixHead skinUrl={`${SKIN_BASE_URL}/${player.playerId}`} size={72} />
        </span>

        <span className="unregistered-player-hero__copy">
          <small>{t("people.profile.externalBadge")}</small>
          <strong>{t("people.profile.externalTitle")}</strong>
          <p>{t("people.profile.externalCopy")}</p>
        </span>

        <span className="unregistered-player-hero__signal" aria-hidden="true">
          <i />
          <i />
          <i />
        </span>
      </motion.section>

      <section className="public-profile-presence-panel unregistered-player-presence">
        <div className="public-profile-presence-panel__state">
          <span className={presenceStatusClassName(player.presenceStatus)}>
            <i />
            {t(`people.presence.status.${player.presenceStatus}`)}
          </span>

          <span>
            <small>{t("people.presence.currentRealm")}</small>
            <strong>
              {player.presenceStatus === "ONLINE" && player.realm ? player.realm : "—"}
            </strong>
          </span>

          <span>
            <small>{t("people.presence.lastSeen")}</small>
            <strong>{lastSeen ?? t("people.presence.unavailable")}</strong>
          </span>
        </div>

        <div className="public-profile-presence-panel__footer">
          <small>
            {freshness === null
              ? t("people.presence.notVerified")
              : t("people.presence.verifiedAgo", { seconds: freshness })}
          </small>

          <button
            type="button"
            disabled={!canSubscribe || subscription.loading || subscription.busy}
            onClick={() => void subscription.toggle()}
          >
            {subscription.busy
              ? t("people.presence.saving")
              : subscription.subscribed
                ? t("people.presence.unsubscribe")
                : t("people.presence.subscribe")}
          </button>
        </div>

        {!canSubscribe ? (
          <p>
            {player.presenceStatus === "HIDDEN"
              ? t("people.presence.hiddenSubscription")
              : t("people.presence.unverifiedSubscription")}
          </p>
        ) : null}
      </section>

      <section className="unregistered-player-drops">
        <header>
          <span>
            <small>{t("people.profile.externalObservedData")}</small>
            <strong>
              {t("people.profile.latestDrops")}
              {!dropsLoading && !dropsFailed && drops.length ? <b>{drops.length}</b> : null}
            </strong>
          </span>

          <button type="button" onClick={openDropFeed}>
            {t("people.profile.openInFeed")}
            <span aria-hidden="true">›</span>
          </button>
        </header>

        {dropsLoading ? (
          <div className="unregistered-player-drops__state" aria-label={t("common.loading")}>
            <span className="unregistered-player-drops__pulse" />
            <span className="unregistered-player-drops__pulse" />
            <span className="unregistered-player-drops__pulse" />
          </div>
        ) : dropsFailed ? (
          <div className="unregistered-player-drops__state is-message">
            <Glyph name="events" size={22} />
            <strong>{t("common.loadErrorTitle")}</strong>
            <small>{t("common.loadErrorCopy")}</small>
            <button type="button" onClick={() => setDropsRevision((value) => value + 1)}>
              {t("common.retry")}
            </button>
          </div>
        ) : drops.length ? (
          <div className="unregistered-player-drops__list">
            {drops.map((drop) => (
              <motion.button
                type="button"
                key={drop.id}
                whileTap={{ scale: 0.985 }}
                onClick={() => navigate({ id: "event-detail", params: { eventId: drop.id } })}
              >
                <EventArt event={drop} size={42} />
                <span>
                  <small>{formatOccurredAt(drop.occurredAt, locale)}</small>
                  <strong>{eventTitle(t, drop)}</strong>
                  <em>
                    {drop.rarity ? t(`events.rarities.${drop.rarity}`) : t("people.profile.drop")}
                  </em>
                </span>
                <b aria-hidden="true">›</b>
              </motion.button>
            ))}
          </div>
        ) : (
          <div className="unregistered-player-drops__state is-message">
            <Glyph name="events" size={22} />
            <strong>{t("people.profile.noDrops")}</strong>
            <small>{t("people.profile.externalNoDropsCopy")}</small>
            <button type="button" onClick={openDropFeed}>
              {t("people.profile.openInFeed")}
            </button>
          </div>
        )}
      </section>

      <section className="unregistered-player-footer-note">
        <Glyph name="profile-mark" size={20} />
        <span>
          <strong>{t("people.profile.externalMnemosTitle")}</strong>
          <small>{t("people.profile.externalMnemosCopy")}</small>
        </span>
      </section>
    </div>
  )
}
