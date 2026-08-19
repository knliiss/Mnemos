import { AnimatePresence, motion } from "framer-motion"
import { useCallback, useEffect, useMemo, useState } from "react"
import {
  increaseEventBet,
  increaseItemRaceBet,
  joinItemRace,
  listEventBetRounds,
  listItemRaces,
  listMyItemRacePositions,
  placeEventBet,
  type EventBetRoundResponse,
  type ItemRaceParticipantResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { handleError } from "@/shared/errors/errorHandler"
import { realtimeEvents } from "@/shared/realtime/realtimeEventBus"
import { GlobalEventArt, ItemTypeArt } from "../components/EventArt"
import { BET_SIDES } from "../contracts"
import { useProductData } from "../data/ProductDataProvider"
import { globalEventVisualTone } from "../eventVisualTone"
import type { Account, BetSide, Route } from "../model"
import { globalEventLabels, itemTypeLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { BottomSheet, CurrencyValue, Field } from "../shared/Primitives"

type Props = {
  activeAccount: Account
  navigate: (route: Route) => void
}

type BetMode = "EVENTS" | "MY" | "RACES"

type SelectedRace = {
  race: ItemRaceResponse
  position: ItemRaceParticipantResponse | null
}

export function BetsProduction({ activeAccount, navigate }: Props) {
  const product = useProductData()
  const [mode, setMode] = useState<BetMode>("EVENTS")
  const [rounds, setRounds] = useState<EventBetRoundResponse[]>([])
  const [races, setRaces] = useState<ItemRaceResponse[]>([])
  const [positions, setPositions] = useState<ItemRaceParticipantResponse[]>([])
  const [selectedRound, setSelectedRound] = useState<EventBetRoundResponse | null>(null)
  const [selectedRace, setSelectedRace] = useState<SelectedRace | null>(null)
  const [side, setSide] = useState<BetSide>("YES")
  const [amount, setAmount] = useState("500")
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)

  const refresh = useCallback(async () => {
    try {
      const [roundResponses, raceResponses, positionResponses] = await Promise.all([
        listEventBetRounds(),
        listItemRaces(),
        listMyItemRacePositions(),
      ])

      setRounds(roundResponses)
      setRaces(raceResponses)
      setPositions(positionResponses)
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void refresh()
  }, [refresh])

  useEffect(() => realtimeEvents.subscribe((event) => {
    if (event.type !== "bet.operation.completed") return

    void refresh()
    void product.refreshAccount()
  }), [product.refreshAccount, refresh])

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
  const amountValid = Number.isInteger(numericAmount)
    && numericAmount >= (selectedHasBet ? 1 : 100)
    && (selectedHasBet || numericAmount <= 5_000)

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
    if (!selectedRound || !amountValid || selectedRound.status !== "OPEN" || busy) return

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
    if (!selectedRace || busy) return

    const { race, position } = selectedRace
    const canWrite = race.status === "REGISTRATION"
    if (!canWrite) return

    if (position && (!Number.isInteger(numericAmount) || numericAmount <= 0)) return

    setBusy(true)

    try {
      if (position) {
        await increaseItemRaceBet(race.id, numericAmount)
      } else {
        await joinItemRace(race.id)
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
          <h1>Ставки</h1>
          <p>{activeAccount.nickname}</p>
        </span>
        <button className="bets-r2-wallet" onClick={() => navigate({ id: "account-wallet" })}>
          <CurrencyValue currency="COIN" value={activeAccount.coin} compact />
        </button>
      </header>

      <nav className="bets-r2-mode">
        {(["EVENTS", "MY", "RACES"] as const).map((value) => (
          <button key={value} className={mode === value ? "is-active" : ""} onClick={() => setMode(value)}>
            <span>{value === "EVENTS" ? "Події" : value === "MY" ? "Мої" : "Перегони"}</span>
            {value === "MY" && myRounds.length + myRaces.length ? <b>{myRounds.length + myRaces.length}</b> : null}
          </button>
        ))}
      </nav>

      {loading ? <div className="events-r2-loading" aria-label="Завантаження ставок"><i /><i /><i /></div> : null}

      {!loading && mode === "EVENTS" ? (
        <>
          {featured ? (
            <motion.button
              className={`bets-r2-feature event-surface--${globalEventVisualTone(featured.eventType)}`}
              whileTap={{ scale: 0.992 }}
              onClick={() => openRound(featured)}
            >
              <span className="bets-r2-feature__art"><GlobalEventArt eventType={featured.eventType} size={82} /></span>
              <span className="bets-r2-feature__copy">
                <small>{featured.status === "OPEN" ? "НАСТУПНИЙ РАУНД" : roundStatusLabel(featured.status)}</small>
                <strong>{globalEventLabels[featured.eventType]}</strong>
                <em>Закриття {formatMoment(featured.closesAt)}</em>
              </span>
              <span className="bets-r2-feature__odds">
                <span><small>ТАК</small><strong>{formatOdds(featured.yesOdds)}</strong></span>
                <i />
                <span><small>НІ</small><strong>{formatOdds(featured.noOdds)}</strong></span>
              </span>
              {featured.myBet ? (
                <span className="bets-r2-feature__mine">
                  <CurrencyMark currency="COIN" size={13} />{betAmount(featured.myBet)}
                </span>
              ) : null}
            </motion.button>
          ) : (
            <EmptyBets title="Раундів зараз немає" note="Нові раунди зʼявляться за розкладом." />
          )}

          {eventRounds.length ? (
            <>
              <header className="bets-r2-section-head"><span><small>ЩЕ РАУНДИ</small><strong>{eventRounds.length}</strong></span></header>
              <section className="bets-r2-rounds">
                {eventRounds.map((round) => <BetRoundCard key={round.id} round={round} onClick={() => openRound(round)} />)}
              </section>
            </>
          ) : null}
        </>
      ) : null}

      {!loading && mode === "MY" ? (
        <section className="bets-r2-rounds">
          {myRounds.map((round) => <BetRoundCard key={round.id} round={round} onClick={() => openRound(round)} />)}
          {myRaces.map((race) => (
            <BetRaceCard
              key={race.id}
              race={race}
              position={positionsByRace.get(race.id) ?? null}
              onClick={() => openRace(race)}
            />
          ))}
          {!myRounds.length && !myRaces.length ? (
            <EmptyBets title="Позицій ще немає" note="Відкрий раунд і зроби першу ставку." />
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
              onClick={() => openRace(race)}
            />
          ))}
          {!races.length ? <EmptyBets title="Перегонів зараз немає" note="Активні перегони зʼявляться тут." /> : null}
        </section>
      ) : null}

      <AnimatePresence>
        {selectedRound ? (
          <BottomSheet title={globalEventLabels[selectedRound.eventType]} onClose={() => !busy && setSelectedRound(null)}>
            <div className="bets-r2-sheet">
              <div className="bets-r2-sheet__event">
                <GlobalEventArt eventType={selectedRound.eventType} size={56} />
                <span>
                  <small>{roundStatusLabel(selectedRound.status)}</small>
                  <strong>{formatMoment(selectedRound.scheduledAt)}</strong>
                  <em>до {formatMoment(selectedRound.closesAt)}</em>
                </span>
              </div>

              {!selectedRound.myBet ? (
                <div className="bets-r2-side-choice">
                  {BET_SIDES.map((value) => (
                    <button key={value} disabled={busy} className={side === value ? "is-active" : ""} onClick={() => setSide(value)}>
                      <span>{value === "YES" ? "Так" : "Ні"}</span>
                      <strong>× {formatOdds(value === "YES" ? selectedRound.yesOdds : selectedRound.noOdds)}</strong>
                    </button>
                  ))}
                </div>
              ) : (
                <div className="bets-r2-current-position">
                  <span>
                    <small>Твоя позиція</small>
                    <strong>{selectedRound.myBet.side === "YES" ? "Так" : "Ні"}</strong>
                  </span>
                  <CurrencyValue currency="COIN" value={betAmount(selectedRound.myBet)} compact />
                </div>
              )}

              <Field label={selectedRound.myBet ? "Збільшити на" : "Сума"}>
                <div className="bets-r2-amount">
                  <CurrencyMark currency="COIN" size={16} />
                  <input
                    type="number"
                    min={selectedRound.myBet ? 1 : 100}
                    max={selectedRound.myBet ? undefined : 5000}
                    value={amount}
                    disabled={busy}
                    onChange={(event) => setAmount(event.target.value)}
                  />
                </div>
              </Field>

              <button
                className="bets-r2-submit"
                disabled={!amountValid || selectedRound.status !== "OPEN" || busy}
                onClick={() => void submitRound()}
              >
                {busy ? "Надсилаємо…" : selectedRound.myBet ? "Збільшити" : "Поставити"}
              </button>
            </div>
          </BottomSheet>
        ) : null}

        {selectedRace ? (
          <BottomSheet title={selectedRace.race.itemName} onClose={() => !busy && setSelectedRace(null)}>
            <div className="bets-r2-sheet">
              <div className="bets-r2-sheet__event">
                <ItemTypeArt itemType={selectedRace.race.itemType} size={56} />
                <span>
                  <small>{itemTypeLabels[selectedRace.race.itemType]}</small>
                  <strong>{raceStatusLabel(selectedRace.race.status)}</strong>
                  <em>реєстрація до {formatMoment(selectedRace.race.registrationClosesAt)}</em>
                </span>
              </div>

              <div className="bets-r2-current-position">
                <span>
                  <small>{selectedRace.position ? "Твоя сума" : "Вхід"}</small>
                  <strong>{selectedRace.position ? "Участь активна" : `Комісія ${selectedRace.race.feePercent}%`}</strong>
                </span>
                <CurrencyValue
                  currency="COIN"
                  value={selectedRace.position ? racePositionAmount(selectedRace.position) : selectedRace.race.entryAmount}
                  compact
                />
              </div>

              {selectedRace.position ? (
                <Field label="Збільшити на">
                  <div className="bets-r2-amount">
                    <CurrencyMark currency="COIN" size={16} />
                    <input
                      type="number"
                      min={1}
                      value={amount}
                      disabled={busy}
                      onChange={(event) => setAmount(event.target.value)}
                    />
                  </div>
                </Field>
              ) : null}

              <button
                className="bets-r2-submit"
                disabled={
                  busy
                  || selectedRace.race.status !== "REGISTRATION"
                  || (Boolean(selectedRace.position) && (!Number.isInteger(numericAmount) || numericAmount <= 0))
                }
                onClick={() => void submitRace()}
              >
                {busy ? "Надсилаємо…" : selectedRace.position ? "Збільшити" : "Приєднатись"}
              </button>
            </div>
          </BottomSheet>
        ) : null}
      </AnimatePresence>
    </div>
  )
}

function BetRoundCard({ round, onClick }: { round: EventBetRoundResponse; onClick: () => void }) {
  return (
    <motion.button
      className={`bets-r2-card event-surface--${globalEventVisualTone(round.eventType)} ${round.status !== "OPEN" ? "is-locked" : ""}`}
      whileTap={{ scale: 0.992 }}
      onClick={onClick}
    >
      <span className="bets-r2-card__art"><GlobalEventArt eventType={round.eventType} size={52} /></span>
      <span className="bets-r2-card__copy">
        <small>{globalEventLabels[round.eventType]}</small>
        <strong>{roundStatusLabel(round.status)}</strong>
        <em>{formatMoment(round.closesAt)}</em>
      </span>
      <span className="bets-r2-card__odds"><b>{formatOdds(round.yesOdds)}</b><i /><b>{formatOdds(round.noOdds)}</b></span>
      {round.myBet ? (
        <span className="bets-r2-card__mine"><CurrencyMark currency="COIN" size={12} />{betAmount(round.myBet)}</span>
      ) : null}
    </motion.button>
  )
}

function BetRaceCard({
  race,
  position,
  onClick,
}: {
  race: ItemRaceResponse
  position: ItemRaceParticipantResponse | null
  onClick: () => void
}) {
  return (
    <motion.button className="bets-r2-race" whileTap={{ scale: 0.992 }} onClick={onClick}>
      <span className="bets-r2-race__art"><ItemTypeArt itemType={race.itemType} size={54} /></span>
      <span className="bets-r2-race__copy">
        <small>{itemTypeLabels[race.itemType]}</small>
        <strong>{race.itemName}</strong>
        <em>{raceStatusLabel(race.status)} · до {formatMoment(race.registrationClosesAt)}</em>
      </span>
      <span className="bets-r2-race__entry">
        <small>{position ? "ТВОЯ" : "ВХІД"}</small>
        <CurrencyValue currency="COIN" value={position ? racePositionAmount(position) : race.entryAmount} compact />
      </span>
    </motion.button>
  )
}

function EmptyBets({ title, note }: { title: string; note: string }) {
  return (
    <div className="bets-r2-empty">
      <Glyph name="bet" size={23} />
      <strong>{title}</strong>
      <small>{note}</small>
    </div>
  )
}

function betAmount(bet: NonNullable<EventBetRoundResponse["myBet"]>) {
  return bet.confirmedAmount + bet.pendingAmount
}

function racePositionAmount(position: ItemRaceParticipantResponse) {
  return position.confirmedAmount + position.pendingAmount
}

function formatOdds(value: number) {
  return Number(value).toLocaleString("uk-UA", { maximumFractionDigits: 2 })
}

function formatMoment(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value

  return new Intl.DateTimeFormat("uk-UA", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date)
}

function roundStatusLabel(status: EventBetRoundResponse["status"]) {
  if (status === "OPEN") return "Відкрито"
  if (status === "LOCKED") return "Закрито"
  if (status === "AWAITING_RESULT") return "Очікуємо результат"
  if (status === "AWAITING_ADMIN_CONFIRMATION") return "Очікуємо підтвердження"
  return "Завершено"
}

function raceStatusLabel(status: ItemRaceResponse["status"]) {
  if (status === "REGISTRATION") return "Реєстрація"
  if (status === "ACTIVE") return "Триває"
  if (status === "SETTLING") return "Підрахунок"
  if (status === "SETTLED") return "Завершено"
  if (status === "REFUNDING") return "Повернення"
  if (status === "REFUNDED") return "Повернено"
  return "Скасовано"
}
