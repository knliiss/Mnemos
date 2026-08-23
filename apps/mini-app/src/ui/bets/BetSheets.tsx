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

const EVENT_BET_MAX = 5_000

type RoundBetSheetProps = {
  round: EventBetRoundResponse
  side: BetSide
  amount: string
  amountValid: boolean
  amountHint: string | null
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
  amountHint,
  busy,
  locale,
  onClose,
  onSideChange,
  onAmountChange,
  onSubmit,
}: RoundBetSheetProps) {
  const { t } = useTranslation()
  const currentAmount = round.myBet ? betAmount(round.myBet) : 0
  const remainingAmount = Math.max(EVENT_BET_MAX - currentAmount, 0)
  const atMaximum = Boolean(round.myBet && remainingAmount === 0)
  const numericAmount = Number(amount)
  const exceedsPositionMaximum =
    Boolean(round.myBet) && Number.isFinite(numericAmount) && numericAmount > remainingAmount
  const effectiveAmountValid = amountValid && !atMaximum && !exceedsPositionMaximum
  const effectiveAmountHint =
    atMaximum || exceedsPositionMaximum ? t("bets.sheet.amountMaximum") : amountHint

  return (
    <BottomSheet title={globalEventLabel(t, round.eventType)} onClose={onClose}>
      <div className="bets-sheet">
        <div className="bets-sheet__event">
          <GlobalEventArt eventType={round.eventType} size={56} />
          <span>
            <small>{roundStatusLabel(t, round.status)}</small>
            <strong>{formatBetMoment(round.scheduledAt, locale)}</strong>
            <em>
              {t("bets.sheet.until", {
                time: formatBetMoment(round.closesAt, locale),
              })}
            </em>
          </span>
        </div>

        {!round.myBet ? (
          <div className="bets-side-choice">
            {BET_SIDES.map((value) => (
              <button
                type="button"
                key={value}
                disabled={busy}
                className={side === value ? "is-active" : ""}
                onClick={() => onSideChange(value)}
              >
                <span>{value === "YES" ? t("bets.yes") : t("bets.no")}</span>
                <strong>
                  × {formatOdds(value === "YES" ? round.yesOdds : round.noOdds, locale)}
                </strong>
              </button>
            ))}
          </div>
        ) : (
          <div className="bets-current-position">
            <span>
              <small>{t("bets.sheet.yourPosition")}</small>
              <strong>{round.myBet.side === "YES" ? t("bets.yes") : t("bets.no")}</strong>
            </span>
            <CurrencyValue currency="COIN" value={currentAmount} compact />
          </div>
        )}

        <Field label={round.myBet ? t("bets.sheet.increaseBy") : t("bets.sheet.amount")}>
          <div className="bets-amount">
            <CurrencyMark currency="COIN" size={16} />
            <input
              type="number"
              min={round.myBet ? 1 : 500}
              max={round.myBet ? remainingAmount : EVENT_BET_MAX}
              value={amount}
              disabled={busy || atMaximum}
              aria-invalid={Boolean(effectiveAmountHint)}
              onChange={(event) => onAmountChange(event.target.value)}
            />
          </div>
        </Field>

        {effectiveAmountHint ? (
          <p className="inline-hint inline-hint--left">{effectiveAmountHint}</p>
        ) : null}

        <button
          type="button"
          className="bets-submit"
          disabled={!effectiveAmountValid || round.status !== "OPEN" || busy}
          onClick={onSubmit}
        >
          {busy
            ? t("bets.sheet.sending")
            : round.myBet
              ? t("bets.sheet.increase")
              : t("bets.sheet.place")}
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
              {t("bets.race.registrationUntil", {
                time: formatBetMoment(race.registrationClosesAt, locale),
              })}
            </em>
          </span>
        </div>

        <div className="bets-current-position">
          <span>
            <small>{position ? t("bets.race.yourAmount") : t("bets.race.entry")}</small>
            <strong>
              {position
                ? t("bets.race.activeParticipation")
                : t("bets.race.fee", { percent: race.feePercent })}
            </strong>
          </span>
          <CurrencyValue
            currency="COIN"
            value={position ? racePositionAmount(position) : race.entryAmount}
            compact
          />
        </div>

        {position ? (
          <Field label={t("bets.sheet.increaseBy")}>
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
          type="button"
          className="bets-submit"
          disabled={busy || race.status !== "REGISTRATION" || !amountValid}
          onClick={onSubmit}
        >
          {busy
            ? t("bets.sheet.sending")
            : position
              ? t("bets.sheet.increase")
              : t("bets.race.join")}
        </button>
      </div>
    </BottomSheet>
  )
}
