import { useTranslation } from "react-i18next"
import type {
  EventBetRoundResponse,
  ItemRaceParticipantResponse,
  ItemRaceResponse,
} from "@/shared/api/betApi"
import { GlobalEventArt, ItemTypeArt } from "../components/events/EventArt"
import { BET_SIDES } from "../contracts"
import type { BetSide } from "../model"
import { CurrencyMark } from "../shared/MnemosIcon"
import { BottomSheet, CurrencyValue, Field } from "../shared/Primitives"
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

type RoundBetSheetProps = {
  round: EventBetRoundResponse
  side: BetSide
  amount: string
  amountValid: boolean
  busy: boolean
  locale: string
  onClose: () => void
  onSideChange: (side: BetSide) => void
  onAmountChange: (amount: string) => void
  onSubmit: () => void
}

export function RoundBetSheet({
  round,
  side,
  amount,
  amountValid,
  busy,
  locale,
  onClose,
  onSideChange,
  onAmountChange,
  onSubmit,
}: RoundBetSheetProps) {
  const { t } = useTranslation()

  return (
    <BottomSheet title={globalEventLabel(t, round.eventType)} onClose={onClose}>
      <div className="bets-sheet">
        <div className="bets-sheet__event">
          <GlobalEventArt eventType={round.eventType} size={56} />
          <span>
            <small>{roundStatusLabel(t, round.status)}</small>
            <strong>{formatBetMoment(round.scheduledAt, locale)}</strong>
            <em>
              {t("productBets.sheet.until", {
                time: formatBetMoment(round.closesAt, locale),
              })}
            </em>
          </span>
        </div>

        {!round.myBet ? (
          <div className="bets-side-choice">
            {BET_SIDES.map((value) => (
              <button
                key={value}
                disabled={busy}
                className={side === value ? "is-active" : ""}
                onClick={() => onSideChange(value)}
              >
                <span>{value === "YES" ? t("productBets.yes") : t("productBets.no")}</span>
                <strong>
                  × {formatOdds(value === "YES" ? round.yesOdds : round.noOdds, locale)}
                </strong>
              </button>
            ))}
          </div>
        ) : (
          <div className="bets-current-position">
            <span>
              <small>{t("productBets.sheet.yourPosition")}</small>
              <strong>
                {round.myBet.side === "YES" ? t("productBets.yes") : t("productBets.no")}
              </strong>
            </span>
            <CurrencyValue currency="COIN" value={betAmount(round.myBet)} compact />
          </div>
        )}

        <Field
          label={round.myBet ? t("productBets.sheet.increaseBy") : t("productBets.sheet.amount")}
        >
          <div className="bets-amount">
            <CurrencyMark currency="COIN" size={16} />
            <input
              type="number"
              min={round.myBet ? 1 : 100}
              max={round.myBet ? undefined : 5000}
              value={amount}
              disabled={busy}
              onChange={(event) => onAmountChange(event.target.value)}
            />
          </div>
        </Field>

        <button
          className="bets-submit"
          disabled={!amountValid || round.status !== "OPEN" || busy}
          onClick={onSubmit}
        >
          {busy
            ? t("productBets.sheet.sending")
            : round.myBet
              ? t("productBets.sheet.increase")
              : t("productBets.sheet.place")}
        </button>
      </div>
    </BottomSheet>
  )
}

type RaceBetSheetProps = {
  race: ItemRaceResponse
  position: ItemRaceParticipantResponse | null
  amount: string
  amountValid: boolean
  busy: boolean
  locale: string
  onClose: () => void
  onAmountChange: (amount: string) => void
  onSubmit: () => void
}

export function RaceBetSheet({
  race,
  position,
  amount,
  amountValid,
  busy,
  locale,
  onClose,
  onAmountChange,
  onSubmit,
}: RaceBetSheetProps) {
  const { t } = useTranslation()

  return (
    <BottomSheet title={race.itemName} onClose={onClose}>
      <div className="bets-sheet">
        <div className="bets-sheet__event">
          <ItemTypeArt itemType={race.itemType} size={56} />
          <span>
            <small>{itemTypeLabel(t, race.itemType)}</small>
            <strong>{raceStatusLabel(t, race.status)}</strong>
            <em>
              {t("productBets.race.registrationUntil", {
                time: formatBetMoment(race.registrationClosesAt, locale),
              })}
            </em>
          </span>
        </div>

        <div className="bets-current-position">
          <span>
            <small>
              {position ? t("productBets.race.yourAmount") : t("productBets.race.entry")}
            </small>
            <strong>
              {position
                ? t("productBets.race.activeParticipation")
                : t("productBets.race.fee", { percent: race.feePercent })}
            </strong>
          </span>
          <CurrencyValue
            currency="COIN"
            value={position ? racePositionAmount(position) : race.entryAmount}
            compact
          />
        </div>

        {position ? (
          <Field label={t("productBets.sheet.increaseBy")}>
            <div className="bets-amount">
              <CurrencyMark currency="COIN" size={16} />
              <input
                type="number"
                min={1}
                value={amount}
                disabled={busy}
                onChange={(event) => onAmountChange(event.target.value)}
              />
            </div>
          </Field>
        ) : null}

        <button
          className="bets-submit"
          disabled={busy || race.status !== "REGISTRATION" || (Boolean(position) && !amountValid)}
          onClick={onSubmit}
        >
          {busy
            ? t("productBets.sheet.sending")
            : position
              ? t("productBets.sheet.increase")
              : t("productBets.race.join")}
        </button>
      </div>
    </BottomSheet>
  )
}
