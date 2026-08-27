import { useState } from "react"
import { useTranslation } from "react-i18next"
import type { SaoRealmResponse } from "@/shared/api/playerDirectoryApi"
import type { Route } from "../../model"
import { SaoRealmPlayerRow } from "./SaoRealmPlayerRow"

const COLLAPSED_PLAYERS = 6

type Props = {
  realm: SaoRealmResponse
  forceExpanded?: boolean
  navigate: (route: Route) => void
}

export function SaoRealmCard({ realm, forceExpanded = false, navigate }: Props) {
  const { t } = useTranslation()
  const [expanded, setExpanded] = useState(false)
  const showAll = forceExpanded || expanded
  const visiblePlayers = showAll ? realm.players : realm.players.slice(0, COLLAPSED_PLAYERS)
  const remaining = Math.max(0, realm.players.length - visiblePlayers.length)

  return (
    <article className="sao-realm-card">
      <header>
        <span>
          <small>{t("people.realms.realm")}</small>
          <strong>{realm.name}</strong>
        </span>
        <b>{realm.playerCount}</b>
      </header>

      <div className="sao-realm-card__players">
        {visiblePlayers.map((player) => (
          <SaoRealmPlayerRow key={player.playerId} player={player} navigate={navigate} />
        ))}
      </div>

      {!forceExpanded && realm.players.length > COLLAPSED_PLAYERS ? (
        <button
          type="button"
          className="sao-realm-card__expand"
          onClick={() => setExpanded((value) => !value)}
        >
          {expanded
            ? t("people.realms.collapse")
            : t("people.realms.morePlayers", { count: remaining })}
        </button>
      ) : null}
    </article>
  )
}
