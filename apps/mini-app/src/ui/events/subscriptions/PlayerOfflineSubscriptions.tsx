import type { FormEvent, ReactNode } from "react"
import { useState } from "react"
import { useTranslation } from "react-i18next"
import type {
  PlayerPresenceStatus,
  PresenceSubscriptionResponse,
} from "@/shared/api/playerDirectoryApi"
import { CristalixHead } from "../../components/profile/CristalixHead"
import { profileSkinUrl } from "../../people/profile/profilePresentation"
import { Glyph } from "../../shared/MnemosIcon"
import { FeatureLoading } from "../../system/FeatureLoading"
import { usePlayerOfflineSubscriptions } from "./usePlayerOfflineSubscriptions"

export function PlayerOfflineSubscriptions() {
  const { t } = useTranslation()
  const [query, setQuery] = useState("")
  const directory = usePlayerOfflineSubscriptions()
  const player = directory.resolvedPlayer
  const subscribed = player
    ? directory.subscriptions.some((item) => item.playerId === player.playerId)
    : false

  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    void directory.resolveNickname(query)
  }

  function changeQuery(value: string) {
    setQuery(value)

    if (directory.resolvedPlayer || directory.searchFailed) {
      directory.clearResolvedPlayer()
    }
  }

  return (
    <section className="notification-settings-group notification-settings-presence">
      <header>
        <span>
          <small>{t("events.subscriptions.offlinePlayers.eyebrow")}</small>
          <strong>{t("events.subscriptions.offlinePlayers.title")}</strong>
          <p>{t("events.subscriptions.offlinePlayers.copy")}</p>
        </span>
        <b>{directory.subscriptions.length}</b>
      </header>

      <form className="notification-settings-presence-search" onSubmit={submit}>
        <label>
          <Glyph name="search" size={16} />
          <input
            value={query}
            maxLength={64}
            autoCapitalize="off"
            autoCorrect="off"
            aria-label={t("events.subscriptions.offlinePlayers.searchAria")}
            placeholder={t("events.subscriptions.offlinePlayers.searchPlaceholder")}
            onChange={(event) => changeQuery(event.target.value)}
          />
          {query ? (
            <button
              type="button"
              className="notification-settings-presence-search__clear"
              aria-label={t("common.clear")}
              onClick={() => changeQuery("")}
            >
              <Glyph name="close" size={13} />
            </button>
          ) : null}
        </label>

        <button
          type="submit"
          className="notification-settings-presence-search__submit"
          disabled={!query.trim() || directory.searching}
        >
          <Glyph name="search" size={14} />
          <span>
            {directory.searching
              ? t("events.subscriptions.offlinePlayers.searching")
              : t("events.subscriptions.offlinePlayers.search")}
          </span>
        </button>
      </form>

      {directory.searchFailed ? (
        <p className="notification-settings-presence-hint" role="status">
          {t("events.subscriptions.offlinePlayers.searchFailed")}
        </p>
      ) : null}

      {player ? (
        <PlayerPresenceRow
          nickname={player.nickname}
          playerId={player.playerId}
          presenceStatus={player.presenceStatus}
          realm={player.realm}
          action={
            <button
              type="button"
              className={subscribed ? "is-remove" : "is-add"}
              disabled={directory.busyPlayerId !== null}
              onClick={() => {
                if (subscribed) {
                  void directory.unsubscribe(player.playerId)
                } else {
                  void directory.subscribe(player.playerId)
                }
              }}
            >
              {directory.busyPlayerId === player.playerId
                ? t("events.subscriptions.offlinePlayers.saving")
                : subscribed
                  ? t("events.subscriptions.offlinePlayers.unsubscribe")
                  : t("events.subscriptions.offlinePlayers.subscribe")}
            </button>
          }
        />
      ) : null}

      <div className="notification-settings-presence-list">
        <div className="notification-settings-presence-list__head">
          <strong>{t("events.subscriptions.offlinePlayers.listTitle")}</strong>
          <small>{t("events.subscriptions.offlinePlayers.listCopy")}</small>
        </div>

        {directory.loading && directory.subscriptions.length === 0 ? (
          <FeatureLoading label={t("events.subscriptions.offlinePlayers.loading")} />
        ) : null}

        {directory.loadFailed ? (
          <div className="notification-settings-presence-error" role="status">
            <span>{t("events.subscriptions.offlinePlayers.loadFailed")}</span>
            <button type="button" onClick={() => void directory.refresh()}>
              {t("common.retry")}
            </button>
          </div>
        ) : null}

        {!directory.loading && !directory.loadFailed && directory.subscriptions.length === 0 ? (
          <p className="notification-settings-presence-empty">
            {t("events.subscriptions.offlinePlayers.empty")}
          </p>
        ) : null}

        {directory.subscriptions.map((subscription) => (
          <PresenceSubscriptionRow
            key={subscription.playerId}
            subscription={subscription}
            busy={directory.busyPlayerId === subscription.playerId}
            disabled={directory.busyPlayerId !== null}
            onRemove={() => void directory.unsubscribe(subscription.playerId)}
          />
        ))}
      </div>
    </section>
  )
}

function PresenceSubscriptionRow({
  subscription,
  busy,
  disabled,
  onRemove,
}: {
  subscription: PresenceSubscriptionResponse
  busy: boolean
  disabled: boolean
  onRemove(): void
}) {
  const { t } = useTranslation()

  return (
    <PlayerPresenceRow
      nickname={subscription.nickname ?? t("events.subscriptions.offlinePlayers.unknownPlayer")}
      playerId={subscription.playerId}
      presenceStatus={subscription.presenceStatus ?? "UNKNOWN"}
      realm={subscription.realm}
      action={
        <button type="button" className="is-remove" disabled={disabled} onClick={onRemove}>
          {busy
            ? t("events.subscriptions.offlinePlayers.saving")
            : t("events.subscriptions.offlinePlayers.unsubscribe")}
        </button>
      }
    />
  )
}

function PlayerPresenceRow({
  nickname,
  playerId,
  presenceStatus,
  realm,
  action,
}: {
  nickname: string
  playerId: string
  presenceStatus: PlayerPresenceStatus
  realm: string | null
  action: ReactNode
}) {
  const { t } = useTranslation()

  return (
    <article className="notification-settings-presence-player">
      <CristalixHead skinUrl={profileSkinUrl(playerId)} size={44} />
      <span className="notification-settings-presence-player__identity">
        <strong>{nickname}</strong>
        <small>{t("events.subscriptions.offlinePlayers.uuid", { id: playerId.slice(0, 8) })}</small>
        <span className="notification-settings-presence-player__badges">
          <i className={`is-${presenceStatus.toLowerCase()}`}>
            {t(`events.subscriptions.offlinePlayers.presence.${presenceStatus}`)}
          </i>
          {realm ? <i>{t("events.subscriptions.offlinePlayers.realm", { realm })}</i> : null}
        </span>
      </span>
      <span className="notification-settings-presence-player__action">{action}</span>
    </article>
  )
}
