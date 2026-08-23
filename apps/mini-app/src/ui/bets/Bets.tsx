import { AnimatePresence, motion } from "framer-motion"
import { useEffect, useMemo, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  getBetOperation,
  increaseEventBet,
  increaseItemRaceBet,
  joinItemRace,
  placeEventBet,
  type EventBetRoundResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { AppError } from "@/shared/errors/AppError"
import { handleError } from "@/shared/errors/errorHandler"
import { GlobalEventArt } from "../components/events/EventArt"
import { useMnemosData } from "../data/MnemosDataProvider"
import { globalEventVisualTone } from "../eventVisualTone"
import type { Account, BetSide, Route } from "../model"
import { CurrencyMark } from "../shared/MnemosIcon"
import { CurrencyValue } from "../shared/Primitives"
import { FeatureLoading } from "../system/FeatureLoading"
import { BetRaceCard, BetRoundCard, EmptyBets } from "./BetCards"
import { EventBetHistoryCard, ItemRaceHistoryCard } from "./BetHistoryCards"
import { RaceBetSheet, RoundBetSheet } from "./BetSheets"
import {
  betAmount,
  betLocale,
  betModes,
  formatBetMoment,
  formatOdds,
  globalEventLabel,
  type BetMode,
} from "./betPresentation"
import "./bets.css"
import "./bets-featured.css"
import { useBetsData } from "./data/useBetsData"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
}

type ScopedSelection = {
  accountId: string
  id: string
}

type PendingRoundOperation = {
  accountId: string
  operationId: string
  side: BetSide
  amount: number
  baselineAmount: number
}

const FIRST_EVENT_BET_MIN = 500
const FIRST_EVENT_BET_MAX = 5_000
const BET_OPERATION_POLL_ATTEMPTS = 30
const BET_OPERATION_POLL_DELAY_MS = 750

function resolveNextOpenRounds(rounds: EventBetRoundResponse[]): EventBetRoundResponse[] {
  const openRounds = rounds
    .filter((round) => round.status === "OPEN")
    .sort((left, right) => Date.parse(left.scheduledAt) - Date.parse(right.scheduledAt))
  const nextByType = new Map<EventBetRoundResponse["eventType"], EventBetRoundResponse>()

  for (const round of openRounds) {
    if (!nextByType.has(round.eventType)) {
      nextByType.set(round.eventType, round)
    }
  }

  return [...nextByType.values()].sort(
    (left, right) => Date.parse(left.scheduledAt) - Date.parse(right.scheduledAt),
  )
}

async function waitForBetOperation(operationId: string) {
  for (let attempt = 0; attempt < BET_OPERATION_POLL_ATTEMPTS; attempt += 1) {
    const operation = await getBetOperation(operationId)

    if (operation.status !== "PENDING") {
      return operation.status
    }

    await new Promise<void>((resolve) => {
      window.setTimeout(resolve, BET_OPERATION_POLL_DELAY_MS)
    })
  }

  return "PENDING" as const
}

export function Bets({ activeAccount, navigate }: Props) {
  const { t, i18n } = useTranslation()
  const data = useMnemosData()
  const canPlaceBets = data.hasPermission("PLACE_BETS")
  const {
    rounds,
    races,
    positions,
    eventHistory,
    raceHistory,
    loading,
    error,
    refresh,
  } = useBetsData(activeAccount.id, data.refreshAccount)
  const [mode, setMode] = useState<BetMode>("EVENTS")
  const [selectedRound, setSelectedRound] = useState<ScopedSelection | null>(null)
  const [selectedRace, setSelectedRace] = useState<ScopedSelection | null>(null)
  const [side, setSide] = useState<BetSide>("YES")
  const [amount, setAmount] = useState("500")
  const [busy, setBusy] = useState(false)
  const [pendingRoundOperations, setPendingRoundOperations] = useState<
    Record<string, PendingRoundOperation>
  >({})
  const submitLocked = useRef(false)
  const mountedRef = useRef(true)
  const activeAccountIdRef = useRef(activeAccount.id)
  const locale = betLocale(i18n.resolvedLanguage)

  useEffect(() => {
    mountedRef.current = true

    return () => {
      mountedRef.current = false
    }
  }, [])

  useEffect(() => {
    activeAccountIdRef.current = activeAccount.id
  }, [activeAccount.id])

  const roundsWithPending = useMemo(
    () =>
      rounds.map((round) => {
        const pending = pendingRoundOperations[round.id]

        if (!pending || pending.accountId !== activeAccount.id) {
          return round
        }

        const currentAmount = round.myBet ? betAmount(round.myBet) : 0
        const targetAmount = pending.baselineAmount + pending.amount
        const missingAmount = Math.max(targetAmount - currentAmount, 0)

        if (missingAmount === 0) {
          return round
        }

        const myBet = round.myBet
          ? {
              ...round.myBet,
              pendingAmount: round.myBet.pendingAmount + missingAmount,
            }
          : {
              id: `pending:${pending.operationId}`,
              roundId: round.id,
              side: pending.side,
              confirmedAmount: 0,
              pendingAmount: missingAmount,
              status: "ACTIVE" as const,
            }

        return {
          ...round,
          myBet,
        }
      }),
    [activeAccount.id, pendingRoundOperations, rounds],
  )

  const positionsByRace = useMemo(
    () => new Map(positions.map((position) => [position.raceId, position])),
    [positions],
  )
  const nextOpenRounds = useMemo(
    () => resolveNextOpenRounds(roundsWithPending),
    [roundsWithPending],
  )
  const selectedRoundValue =
    selectedRound?.accountId === activeAccount.id
      ? (roundsWithPending.find((round) => round.id === selectedRound.id) ?? null)
      : null
  const selectedRaceValue =
    selectedRace?.accountId === activeAccount.id
      ? (races.find((race) => race.id === selectedRace.id) ?? null)
      : null
  const selectedRacePosition = selectedRaceValue
    ? (positionsByRace.get(selectedRaceValue.id) ?? null)
    : null
  const featured = nextOpenRounds[0] ?? null
  const eventRounds = nextOpenRounds.slice(1)
  const myRounds = roundsWithPending.filter((round) => round.myBet)
  const myRaces = races.filter((race) => positionsByRace.has(race.id))
  const currentRoundIds = new Set(myRounds.map((round) => round.id))
  const currentRaceIds = new Set(myRaces.map((race) => race.id))
  const historicalEventBets = eventHistory.filter(({ round }) => !currentRoundIds.has(round.id))
  const historicalRacePositions = raceHistory.filter(({ race }) => !currentRaceIds.has(race.id))
  const currentPositionCount = myRounds.length + myRaces.length
  const historicalPositionCount = historicalEventBets.length + historicalRacePositions.length
  const myPositionCount = currentPositionCount + historicalPositionCount
  const numericAmount = Number(amount)
  const selectedHasBet = Boolean(selectedRoundValue?.myBet)
  const amountIsInteger = Number.isInteger(numericAmount)
  const withinBalance = numericAmount <= activeAccount.coin
  const roundAmountValid =
    canPlaceBets &&
    amountIsInteger &&
    numericAmount >= (selectedHasBet ? 1 : FIRST_EVENT_BET_MIN) &&
    withinBalance &&
    (selectedHasBet || numericAmount <= FIRST_EVENT_BET_MAX)
  const roundAmountHint = (() => {
    if (!amountIsInteger) {
      return t("bets.sheet.amountInteger")
    }

    if (!selectedHasBet && numericAmount < FIRST_EVENT_BET_MIN) {
      return t("bets.sheet.amountMinimum")
    }

    if (!selectedHasBet && numericAmount > FIRST_EVENT_BET_MAX) {
      return t("bets.sheet.amountMaximum")
    }

    if (numericAmount < 1) {
      return t("bets.sheet.amountInteger")
    }

    if (!withinBalance) {
      return t("bets.sheet.insufficientBalance")
    }

    return null
  })()
  const raceAmountValid =
    canPlaceBets && Number.isInteger(numericAmount) && numericAmount > 0 && withinBalance
  const raceJoinValid = Boolean(
    canPlaceBets && selectedRaceValue && selectedRaceValue.entryAmount <= activeAccount.coin,
  )

  const removePendingRoundOperation = (roundId: string, operationId: string) => {
    setPendingRoundOperations((current) => {
      if (current[roundId]?.operationId !== operationId) {
        return current
      }

      const next = { ...current }
      delete next[roundId]

      return next
    })
  }

  const reconcileRoundOperation = async (
    accountId: string,
    roundId: string,
    operation: PendingRoundOperation,
  ) => {
    try {
      const status = await waitForBetOperation(operation.operationId)

      if (!mountedRef.current) {
        return
      }

      if (activeAccountIdRef.current !== accountId) {
        if (status !== "PENDING") {
          removePendingRoundOperation(roundId, operation.operationId)
        }

        return
      }

      await Promise.all([refresh(), data.refreshAccount()])

      if (!mountedRef.current || activeAccountIdRef.current !== accountId) {
        return
      }

      if (status === "FAILED") {
        handleError(new AppError("validation", t("bets.sheet.operationFailed")))
      }

      if (status !== "PENDING") {
        removePendingRoundOperation(roundId, operation.operationId)
      }
    } catch (operationError) {
      if (mountedRef.current && activeAccountIdRef.current === accountId) {
        handleError(operationError)
      }
    }
  }

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

    const accountId = activeAccount.id
    const roundId = selectedRoundValue.id
    const selectedBet = selectedRoundValue.myBet
    const operationSide = selectedBet?.side ?? side
    const baselineAmount = selectedBet ? betAmount(selectedBet) : 0

    submitLocked.current = true
    setBusy(true)

    try {
      const accepted = selectedBet
        ? await increaseEventBet(roundId, numericAmount)
        : await placeEventBet(roundId, operationSide, numericAmount)
      const pendingOperation: PendingRoundOperation = {
        accountId,
        operationId: accepted.operationId,
        side: operationSide,
        amount: numericAmount,
        baselineAmount,
      }

      if (!mountedRef.current || activeAccountIdRef.current !== accountId) {
        return
      }

      setPendingRoundOperations((current) => ({
        ...current,
        [roundId]: pendingOperation,
      }))
      setSelectedRound(null)
      setMode("MY")

      void reconcileRoundOperation(accountId, roundId, pendingOperation)
    } catch (submitError) {
      handleError(submitError)
    } finally {
      submitLocked.current = false
      if (mountedRef.current) {
        setBusy(false)
      }
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
    } catch (submitError) {
      handleError(submitError)
    } finally {
      submitLocked.current = false
      if (mountedRef.current) {
        setBusy(false)
      }
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
          <CurrencyValue currency="COIN" value={activeAccount.coin} compact />
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
            {value === "MY" && myPositionCount ? <b>{myPositionCount}</b> : null}
          </button>
        ))}
      </nav>

      {loading ? <FeatureLoading label={t("bets.loading")} /> : null}

      {!loading && error ? (
        <div className="bets-empty">
          <CurrencyMark currency="COIN" size={20} />
          <strong>{t("bets.error.title")}</strong>
          <small>{t("bets.error.copy")}</small>
          <button type="button" onClick={() => void refresh()}>
            {t("bets.error.retry")}
          </button>
        </div>
      ) : null}

      {!loading && !error && mode === "EVENTS" ? (
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
                <small>{t("bets.nextRound")}</small>
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

      {!loading && !error && mode === "MY" ? (
        <div className="bets-my">
          {currentPositionCount ? (
            <>
              <header className="bets-section-head">
                <span>
                  <small>{t("bets.history.active")}</small>
                  <strong>{currentPositionCount}</strong>
                </span>
              </header>

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
              </section>
            </>
          ) : null}

          {historicalPositionCount ? (
            <>
              <header className="bets-section-head bets-section-head--history">
                <span>
                  <small>{t("bets.history.previous")}</small>
                  <strong>{historicalPositionCount}</strong>
                </span>
              </header>

              <section className="bets-rounds">
                {historicalEventBets.map(({ round, bet }) => (
                  <EventBetHistoryCard
                    key={bet.id}
                    round={round}
                    bet={bet}
                    locale={locale}
                    t={t}
                  />
                ))}

                {historicalRacePositions.map(({ race, position }) => (
                  <ItemRaceHistoryCard
                    key={position.id}
                    race={race}
                    position={position}
                    locale={locale}
                    t={t}
                  />
                ))}
              </section>
            </>
          ) : null}

          {!myPositionCount ? (
            <EmptyBets title={t("bets.empty.myTitle")} note={t("bets.empty.myCopy")} />
          ) : null}
        </div>
      ) : null}

      {!loading && !error && mode === "RACES" ? (
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
            amountHint={roundAmountHint}
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
