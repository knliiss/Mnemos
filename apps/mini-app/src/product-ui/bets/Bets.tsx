import { AnimatePresence, motion } from "framer-motion"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  increaseEventBet,
  increaseItemRaceBet,
  joinItemRace,
  placeEventBet,
  type EventBetRoundResponse,
  type ItemRaceParticipantResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { handleError } from "@/shared/errors/errorHandler"
import { GlobalEventArt } from "../components/events/EventArt"
import { useProductData } from "../data/ProductDataProvider"
import { globalEventVisualTone } from "../eventVisualTone"
import type { Account, BetSide, Route } from "../model"
import { CurrencyMark } from "../shared/MnemosIcon"
import { CurrencyValue } from "../shared/Primitives"
import { FeatureLoading } from "../system/FeatureLoading"
import { BetRaceCard, BetRoundCard, EmptyBets } from "./BetCards"
import { RaceBetSheet, RoundBetSheet } from "./BetSheets"
import {
  betAmount,
  betLocale,
  betModes,
  formatBetMoment,
  formatOdds,
  globalEventLabel,
  roundStatusLabel,
  type BetMode,
} from "./betPresentation"
import { useBetsData } from "./data/useBetsData"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
}

type SelectedRace = {
  race: ItemRaceResponse
  position: ItemRaceParticipantResponse | null
}

export function Bets({ activeAccount, navigate }: Props) {
  const { t, i18n } = useTranslation()
  const product = useProductData()
  const { rounds, races, positions, loading, refresh } = useBetsData(product.refreshAccount)
  const [mode, setMode] = useState<BetMode>("EVENTS")
  const [selectedRound, setSelectedRound] = useState<EventBetRoundResponse | null>(null)
  const [selectedRace, setSelectedRace] = useState<SelectedRace | null>(null)
  const [side, setSide] = useState<BetSide>("YES")
  const [amount, setAmount] = useState("500")
  const [busy, setBusy] = useState(false)
  const locale = betLocale(i18n.resolvedLanguage)

  const positionsByRace = useMemo(
    () => new Map(positions.map((position) => [position.raceId, position])),
    [positions],
  )
  const featured = rounds.find((round) => round.status === "OPEN") ?? rounds[0] ?? null
  const eventRounds = featured ? rounds.filter((round) => round.id !== featured.id) : rounds
  const myRounds = rounds.filter((round) => round.myBet)
  const myRaces = races.filter((race) => positionsByRace.has(race.id))
  const numericAmount = Number(amount)
  const selectedHasBet = Boolean(selectedRound?.myBet)
  const roundAmountValid =
    Number.isInteger(numericAmount) &&
    numericAmount >= (selectedHasBet ? 1 : 100) &&
    (selectedHasBet || numericAmount <= 5_000)
  const raceAmountValid = Number.isInteger(numericAmount) && numericAmount > 0

  const openRound = (round: EventBetRoundResponse) => {
    setSelectedRound(round)
    setSide(round.myBet?.side ?? "YES")
    setAmount(round.myBet ? "100" : "500")
  }

  const openRace = (race: ItemRaceResponse) => {
    const position = positionsByRace.get(race.id) ?? null

    setSelectedRace({ race, position })
    setAmount(position ? "100" : String(race.entryAmount))
  }

  const submitRound = async () => {
    if (!selectedRound || !roundAmountValid || selectedRound.status !== "OPEN" || busy) {
      return
    }

    setBusy(true)

    try {
      if (selectedRound.myBet) {
        await increaseEventBet(selectedRound.id, numericAmount)
      } else {
        await placeEventBet(selectedRound.id, side, numericAmount)
      }

      setSelectedRound(null)
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const submitRace = async () => {
    if (!selectedRace || busy || selectedRace.race.status !== "REGISTRATION") {
      return
    }

    if (selectedRace.position && !raceAmountValid) {
      return
    }

    setBusy(true)

    try {
      if (selectedRace.position) {
        await increaseItemRaceBet(selectedRace.race.id, numericAmount)
      } else {
        await joinItemRace(selectedRace.race.id)
      }

      setSelectedRace(null)
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="screen bets-r2-screen root-screen">
      <header className="root-title-block bets-r2-title">
        <span>
          <small>MNEMOS BETS</small>
          <h1>{t("productBets.title")}</h1>
          <p>{activeAccount.nickname}</p>
        </span>

        <button className="bets-r2-wallet" onClick={() => navigate({ id: "account-wallet" })}>
          <CurrencyValue currency="COIN" value={activeAccount.coin} compact />
        </button>
      </header>

      <nav className="bets-r2-mode">
        {betModes.map((value) => (
          <button
            key={value}
            className={mode === value ? "is-active" : ""}
            onClick={() => setMode(value)}
          >
            <span>{t(`productBets.modes.${value}`)}</span>
            {value === "MY" && myRounds.length + myRaces.length ? (
              <b>{myRounds.length + myRaces.length}</b>
            ) : null}
          </button>
        ))}
      </nav>

      {loading ? <FeatureLoading label={t("productBets.loading")} /> : null}

      {!loading && mode === "EVENTS" ? (
        <>
          {featured ? (
            <motion.button
              className={`bets-r2-feature event-surface--${globalEventVisualTone(featured.eventType)}`}
              whileTap={{ scale: 0.992 }}
              onClick={() => openRound(featured)}
            >
              <span className="bets-r2-feature__art">
                <GlobalEventArt eventType={featured.eventType} size={82} />
              </span>

              <span className="bets-r2-feature__copy">
                <small>
                  {featured.status === "OPEN"
                    ? t("productBets.nextRound")
                    : roundStatusLabel(t, featured.status)}
                </small>
                <strong>{globalEventLabel(t, featured.eventType)}</strong>
                <em>
                  {t("productBets.closes", {
                    time: formatBetMoment(featured.closesAt, locale),
                  })}
                </em>
              </span>

              <span className="bets-r2-feature__odds">
                <span>
                  <small>{t("productBets.yesUpper")}</small>
                  <strong>{formatOdds(featured.yesOdds, locale)}</strong>
                </span>
                <i />
                <span>
                  <small>{t("productBets.noUpper")}</small>
                  <strong>{formatOdds(featured.noOdds, locale)}</strong>
                </span>
              </span>

              {featured.myBet ? (
                <span className="bets-r2-feature__mine">
                  <CurrencyMark currency="COIN" size={13} />
                  {betAmount(featured.myBet)}
                </span>
              ) : null}
            </motion.button>
          ) : (
            <EmptyBets
              title={t("productBets.empty.roundsTitle")}
              note={t("productBets.empty.roundsCopy")}
            />
          )}

          {eventRounds.length ? (
            <>
              <header className="bets-r2-section-head">
                <span>
                  <small>{t("productBets.moreRounds")}</small>
                  <strong>{eventRounds.length}</strong>
                </span>
              </header>

              <section className="bets-r2-rounds">
                {eventRounds.map((round) => (
                  <BetRoundCard
                    key={round.id}
                    round={round}
                    locale={locale}
                    t={t}
                    onClick={() => openRound(round)}
                  />
                ))}
              </section>
            </>
          ) : null}
        </>
      ) : null}

      {!loading && mode === "MY" ? (
        <section className="bets-r2-rounds">
          {myRounds.map((round) => (
            <BetRoundCard
              key={round.id}
              round={round}
              locale={locale}
              t={t}
              onClick={() => openRound(round)}
            />
          ))}

          {myRaces.map((race) => (
            <BetRaceCard
              key={race.id}
              race={race}
              position={positionsByRace.get(race.id) ?? null}
              locale={locale}
              t={t}
              onClick={() => openRace(race)}
            />
          ))}

          {!myRounds.length && !myRaces.length ? (
            <EmptyBets
              title={t("productBets.empty.myTitle")}
              note={t("productBets.empty.myCopy")}
            />
          ) : null}
        </section>
      ) : null}

      {!loading && mode === "RACES" ? (
        <section className="bets-r2-races">
          {races.map((race) => (
            <BetRaceCard
              key={race.id}
              race={race}
              position={positionsByRace.get(race.id) ?? null}
              locale={locale}
              t={t}
              onClick={() => openRace(race)}
            />
          ))}

          {!races.length ? (
            <EmptyBets
              title={t("productBets.empty.racesTitle")}
              note={t("productBets.empty.racesCopy")}
            />
          ) : null}
        </section>
      ) : null}

      <AnimatePresence>
        {selectedRound ? (
          <RoundBetSheet
            round={selectedRound}
            side={side}
            amount={amount}
            amountValid={roundAmountValid}
            busy={busy}
            locale={locale}
            onClose={() => !busy && setSelectedRound(null)}
            onSideChange={setSide}
            onAmountChange={setAmount}
            onSubmit={() => void submitRound()}
          />
        ) : null}

        {selectedRace ? (
          <RaceBetSheet
            race={selectedRace.race}
            position={selectedRace.position}
            amount={amount}
            amountValid={raceAmountValid}
            busy={busy}
            locale={locale}
            onClose={() => !busy && setSelectedRace(null)}
            onAmountChange={setAmount}
            onSubmit={() => void submitRace()}
          />
        ) : null}
      </AnimatePresence>
    </div>
  )
}
