import { AnimatePresence, motion } from "framer-motion"
import { useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  increaseEventBet,
  increaseItemRaceBet,
  joinItemRace,
  placeEventBet,
  type EventBetRoundResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { handleError } from "@/shared/errors/errorHandler"
import { GlobalEventArt } from "../components/events/EventArt"
import { useMnemosData } from "../data/MnemosDataProvider"
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
import "./bets.css"
import { useBetsData } from "./data/useBetsData"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
}

type ScopedSelection = {
  accountId: string
  id: string
}

export function Bets({ activeAccount, navigate }: Props) {
  const { t, i18n } = useTranslation()
  const data = useMnemosData()
  const canViewBets = data.hasPermission("VIEW_BETS")
  const { rounds, races, positions, loading, error, refresh } = useBetsData(
    activeAccount.id,
    data.refreshAccount,
    canViewBets,
  )
  const [mode, setMode] = useState<BetMode>("EVENTS")
  const [selectedRound, setSelectedRound] = useState<ScopedSelection | null>(null)
  const [selectedRace, setSelectedRace] = useState<ScopedSelection | null>(null)
  const [side, setSide] = useState<BetSide>("YES")
  const [amount, setAmount] = useState("500")
  const [busy, setBusy] = useState(false)
  const submitLocked = useRef(false)
  const locale = betLocale(i18n.resolvedLanguage)
  const canViewWallet = data.hasPermission("VIEW_WALLET")

  const positionsByRace = useMemo(
    () => new Map(positions.map((position) => [position.raceId, position])),
    [positions],
  )
  const selectedRoundValue =
    selectedRound?.accountId === activeAccount.id
      ? (rounds.find((round) => round.id === selectedRound.id) ?? null)
      : null
  const selectedRaceValue =
    selectedRace?.accountId === activeAccount.id
      ? (races.find((race) => race.id === selectedRace.id) ?? null)
      : null
  const selectedRacePosition = selectedRaceValue
    ? (positionsByRace.get(selectedRaceValue.id) ?? null)
    : null
  const featured = rounds.find((round) => round.status === "OPEN") ?? rounds[0] ?? null
  const eventRounds = featured ? rounds.filter((round) => round.id !== featured.id) : rounds
  const myRounds = rounds.filter((round) => round.myBet)
  const myRaces = races.filter((race) => positionsByRace.has(race.id))
  const numericAmount = Number(amount)
  const selectedHasBet = Boolean(selectedRoundValue?.myBet)
  const withinVisibleBalance = !canViewWallet || numericAmount <= activeAccount.coin
  const roundAmountValid =
    Number.isInteger(numericAmount) &&
    numericAmount >= (selectedHasBet ? 1 : 100) &&
    withinVisibleBalance &&
    (selectedHasBet || numericAmount <= 5_000)
  const raceAmountValid =
    Number.isInteger(numericAmount) && numericAmount > 0 && withinVisibleBalance
  const raceJoinValid = Boolean(
    selectedRaceValue && (!canViewWallet || selectedRaceValue.entryAmount <= activeAccount.coin),
  )

  const openRound = (round: EventBetRoundResponse) => {
    setSelectedRound({
      accountId: activeAccount.id,
      id: round.id,
    })
    setSide(round.myBet?.side ?? "YES")
    setAmount(round.myBet ? "100" : "500")
  }

  const openRace = (race: ItemRaceResponse) => {
    const position = positionsByRace.get(race.id) ?? null

    setSelectedRace({
      accountId: activeAccount.id,
      id: race.id,
    })
    setAmount(position ? "100" : String(race.entryAmount))
  }

  const submitRound = async () => {
    if (
      !selectedRoundValue ||
      !roundAmountValid ||
      selectedRoundValue.status !== "OPEN" ||
      busy ||
      submitLocked.current
    ) {
      return
    }

    submitLocked.current = true
    setBusy(true)

    try {
      if (selectedRoundValue.myBet) {
        await increaseEventBet(selectedRoundValue.id, numericAmount)
      } else {
        await placeEventBet(selectedRoundValue.id, side, numericAmount)
      }

      setSelectedRound(null)
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      submitLocked.current = false
      setBusy(false)
    }
  }

  const submitRace = async () => {
    if (
      !selectedRaceValue ||
      busy ||
      submitLocked.current ||
      selectedRaceValue.status !== "REGISTRATION"
    ) {
      return
    }

    if (selectedRacePosition ? !raceAmountValid : !raceJoinValid) {
      return
    }

    submitLocked.current = true
    setBusy(true)

    try {
      if (selectedRacePosition) {
        await increaseItemRaceBet(selectedRaceValue.id, numericAmount)
      } else {
        await joinItemRace(selectedRaceValue.id)
      }

      setSelectedRace(null)
      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      submitLocked.current = false
      setBusy(false)
    }
  }

  return (
    <div className="screen bets-screen root-screen">
      <header className="root-title-block bets-title">
        <span>
          <small>{t("bets.eyebrow")}</small>
          <h1>{t("bets.title")}</h1>
          <p>{activeAccount.nickname}</p>
        </span>

        <button
          type="button"
          className="bets-wallet"
          onClick={() => navigate({ id: "account-wallet" })}
        >
          <span className="bets-wallet__label">{t("bets.balance")}</span>
          {canViewWallet ? (
            <CurrencyValue currency="COIN" value={activeAccount.coin} compact />
          ) : (
            <strong title={t("bets.balanceUnavailable")}>—</strong>
          )}
        </button>
      </header>

      <nav className="bets-mode">
        {betModes.map((value) => (
          <button
            type="button"
            key={value}
            className={mode === value ? "is-active" : ""}
            onClick={() => setMode(value)}
          >
            <span>{t(`bets.modes.${value}`)}</span>
            {value === "MY" && myRounds.length + myRaces.length ? (
              <b>{myRounds.length + myRaces.length}</b>
            ) : null}
          </button>
        ))}
      </nav>

      {loading ? <FeatureLoading label={t("bets.loading")} /> : null}

      {!loading && !canViewBets ? (
        <div className="bets-empty">
          <CurrencyMark currency="COIN" size={20} />
          <strong>{t("errorNotifications.tones.access.title")}</strong>
          <small>{t("errorNotifications.tones.access.message")}</small>
        </div>
      ) : null}

      {!loading && canViewBets && error ? (
        <div className="bets-empty">
          <CurrencyMark currency="COIN" size={20} />
          <strong>{t("bets.error.title")}</strong>
          <small>{t("bets.error.copy")}</small>
          <button type="button" onClick={() => void refresh()}>
            {t("bets.error.retry")}
          </button>
        </div>
      ) : null}

      {!loading && canViewBets && !error && mode === "EVENTS" ? (
        <>
          {featured ? (
            <motion.button
              type="button"
              className={`bets-feature event-surface--${globalEventVisualTone(featured.eventType)}`}
              whileTap={{ scale: 0.992 }}
              onClick={() => openRound(featured)}
            >
              <span className="bets-feature__art">
                <GlobalEventArt eventType={featured.eventType} size={82} />
              </span>

              <span className="bets-feature__copy">
                <small>
                  {featured.status === "OPEN"
                    ? t("bets.nextRound")
                    : roundStatusLabel(t, featured.status)}
                </small>
                <strong>{globalEventLabel(t, featured.eventType)}</strong>
                <em>
                  {t("bets.closes", {
                    time: formatBetMoment(featured.closesAt, locale),
                  })}
                </em>
              </span>

              <span className="bets-feature__odds">
                <span>
                  <small>{t("bets.yesUpper")}</small>
                  <strong>{formatOdds(featured.yesOdds, locale)}</strong>
                </span>
                <i />
                <span>
                  <small>{t("bets.noUpper")}</small>
                  <strong>{formatOdds(featured.noOdds, locale)}</strong>
                </span>
              </span>

              {featured.myBet ? (
                <span className="bets-feature__mine">
                  <CurrencyMark currency="COIN" size={13} />
                  {betAmount(featured.myBet)}
                </span>
              ) : null}
            </motion.button>
          ) : (
            <EmptyBets title={t("bets.empty.roundsTitle")} note={t("bets.empty.roundsCopy")} />
          )}

          {eventRounds.length ? (
            <>
              <header className="bets-section-head">
                <span>
                  <small>{t("bets.moreRounds")}</small>
                  <strong>{eventRounds.length}</strong>
                </span>
              </header>

              <section className="bets-rounds">
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

      {!loading && canViewBets && !error && mode === "MY" ? (
        <section className="bets-rounds">
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
            <EmptyBets title={t("bets.empty.myTitle")} note={t("bets.empty.myCopy")} />
          ) : null}
        </section>
      ) : null}

      {!loading && canViewBets && !error && mode === "RACES" ? (
        <section className="bets-races">
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
            <EmptyBets title={t("bets.empty.racesTitle")} note={t("bets.empty.racesCopy")} />
          ) : null}
        </section>
      ) : null}

      <AnimatePresence>
        {selectedRoundValue ? (
          <RoundBetSheet
            round={selectedRoundValue}
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

        {selectedRaceValue ? (
          <RaceBetSheet
            race={selectedRaceValue}
            position={selectedRacePosition}
            amount={amount}
            amountValid={selectedRacePosition ? raceAmountValid : raceJoinValid}
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
