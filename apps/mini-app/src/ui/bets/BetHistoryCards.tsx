import type { TFunction } from "i18next"
import type {
  EventBetResponse,
  EventBetRoundResponse,
  ItemRaceParticipantResponse,
  ItemRaceResponse,
} from "@/shared/api/betApi"
import { GlobalEventArt, ItemTypeArt } from "../components/events/EventArt"
import { globalEventVisualTone } from "../eventVisualTone"
import { CurrencyValue } from "../shared/Primitives"
import {
  betAmount,
  eventBetStatusLabel,
  eventOutcomeLabel,
  formatBetMoment,
  globalEventLabel,
  itemTypeLabel,
  raceParticipantStatusLabel,
  racePositionAmount,
} from "./betPresentation"
import "./bets-history.css"

type EventBetHistoryCardProps = {
  round: EventBetRoundResponse
  bet: EventBetResponse
  locale: string
  t: TFunction
}

export function EventBetHistoryCard({ round, bet, locale, t }: EventBetHistoryCardProps) {
  const outcome = round.outcome ? eventOutcomeLabel(t, round.outcome) : null
  const tone = globalEventVisualTone(round.eventType)

  return (
    <article className={`bets-card bets-history-card event-surface--${tone}`}>
      <span className="bets-card__art">
        <GlobalEventArt eventType={round.eventType} size={52} />
      </span>

      <span className="bets-card__copy">
        <small>{globalEventLabel(t, round.eventType)}</small>
        <strong>{eventBetStatusLabel(t, bet.status)}</strong>
        <em>
          {formatBetMoment(round.scheduledAt, locale)}
          {outcome ? ` · ${t("bets.history.result", { outcome })}` : ""}
        </em>
      </span>

      <span className="bets-history-card__position">
        <small>{bet.side === "YES" ? t("bets.yes") : t("bets.no")}</small>
        <CurrencyValue currency="COIN" value={betAmount(bet)} compact />
      </span>
    </article>
  )
}

type ItemRaceHistoryCardProps = {
  race: ItemRaceResponse
  position: ItemRaceParticipantResponse
  locale: string
  t: TFunction
}

export function ItemRaceHistoryCard({ race, position, locale, t }: ItemRaceHistoryCardProps) {
  const status = raceParticipantStatusLabel(t, position.status)
  const startedAt = formatBetMoment(race.startsAt, locale)

  return (
    <article className="bets-race bets-history-card">
      <span className="bets-race__art">
        <ItemTypeArt itemType={race.itemType} size={54} />
      </span>

      <span className="bets-race__copy">
        <small>{itemTypeLabel(t, race.itemType)}</small>
        <strong>{race.itemName}</strong>
        <em>{`${status} · ${startedAt}`}</em>
      </span>

      <span className="bets-history-card__position">
        <small>{t("bets.sheet.amount")}</small>
        <CurrencyValue currency="COIN" value={racePositionAmount(position)} compact />
      </span>
    </article>
  )
}
