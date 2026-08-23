import type { TFunction } from "i18next"
import { motion } from "framer-motion"
import type {
  EventBetRoundResponse,
  ItemRaceParticipantResponse,
  ItemRaceResponse,
} from "@/shared/api/betApi"
import { GlobalEventArt, ItemTypeArt } from "../components/events/EventArt"
import { globalEventVisualTone } from "../eventVisualTone"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue } from "../shared/Primitives"
import {
  betAmount,
  formatBetMoment,
  formatOdds,
  globalEventLabel,
  itemTypeLabel,
  racePositionAmount,
  raceStatusLabel,
  roundStatusLabel,
} from "./betPresentation"

type BetRoundCardProps = {
  round: EventBetRoundResponse
  locale: string
  t: TFunction
  onClick: () => void
}

export function BetRoundCard({ round, locale, t, onClick }: BetRoundCardProps) {
  return (
    <motion.button
      type="button"
      className={`bets-card event-surface--${globalEventVisualTone(round.eventType)} ${round.status !== "OPEN" ? "is-locked" : ""}`}
      whileTap={{ scale: 0.992 }}
      onClick={onClick}
    >
      <span className="bets-card__art">
        <GlobalEventArt eventType={round.eventType} size={52} />
      </span>

      <span className="bets-card__copy">
        <small>{globalEventLabel(t, round.eventType)}</small>
        <strong>{roundStatusLabel(t, round.status)}</strong>
        <em>{formatBetMoment(round.closesAt, locale)}</em>
      </span>

      <span className="bets-card__odds">
        <b>{formatOdds(round.yesOdds, locale)}</b>
        <i />
        <b>{formatOdds(round.noOdds, locale)}</b>
      </span>

      {round.myBet ? (
        <span className="bets-card__mine">
          <CurrencyMark currency="COIN" size={12} />
          {betAmount(round.myBet)}
        </span>
      ) : null}
    </motion.button>
  )
}

type BetRaceCardProps = {
  race: ItemRaceResponse
  position: ItemRaceParticipantResponse | null
  locale: string
  t: TFunction
  onClick: () => void
}

export function BetRaceCard({ race, position, locale, t, onClick }: BetRaceCardProps) {
  return (
    <motion.button
      type="button"
      className="bets-race"
      whileTap={{ scale: 0.992 }}
      onClick={onClick}
    >
      <span className="bets-race__art">
        <ItemTypeArt itemType={race.itemType} size={54} />
      </span>

      <span className="bets-race__copy">
        <small>{itemTypeLabel(t, race.itemType)}</small>
        <strong>{race.itemName}</strong>
        <em>
          {raceStatusLabel(t, race.status)} ·{" "}
          {t("bets.sheet.until", {
            time: formatBetMoment(race.registrationClosesAt, locale),
          })}
        </em>
      </span>

      <span className="bets-race__entry">
        <small>{position ? t("bets.race.yours") : t("bets.race.entryUpper")}</small>
        <CurrencyValue
          currency="COIN"
          value={position ? racePositionAmount(position) : race.entryAmount}
          compact
        />
      </span>
    </motion.button>
  )
}

type EmptyBetsProps = {
  title: string
  note: string
  illustration?: "race"
}

export function EmptyBets({ title, note, illustration }: EmptyBetsProps) {
  return (
    <div className={`bets-empty ${illustration ? `bets-empty--${illustration}` : ""}`.trim()}>
      {illustration === "race" ? (
        <img
          className="bets-empty__illustration"
          src="/ui-illustrations/mnemos-mascot-race.svg"
          alt=""
          aria-hidden="true"
        />
      ) : (
        <Glyph name="bet" size={23} />
      )}

      <strong>{title}</strong>
      <small>{note}</small>
    </div>
  )
}
