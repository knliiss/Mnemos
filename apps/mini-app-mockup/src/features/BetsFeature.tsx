import { AnimatePresence } from "framer-motion"
import { useState } from "react"
import { BET_SIDES } from "../contracts"
import { betRounds, itemRaces as initialItemRaces } from "../mockData"
import type { BetRound, BetSide, ItemRace } from "../model"
import { betStatusLabels, itemTypeLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { BottomSheet, CurrencyValue, Field, PageHeader, Segmented } from "../shared/Primitives"

type BetMode = "ROUNDS" | "MY" | "RACES"
type OperationState = "IDLE" | "PENDING"

export function BetsPage({ back }: { back: () => void }) {
  const [mode, setMode] = useState<BetMode>("ROUNDS")
  const [rounds, setRounds] = useState(betRounds)
  const [races, setRaces] = useState<ItemRace[]>(initialItemRaces)
  const [selectedRound, setSelectedRound] = useState<BetRound | null>(null)
  const [selectedRace, setSelectedRace] = useState<ItemRace | null>(null)
  const [side, setSide] = useState<BetSide>("YES")
  const [amount, setAmount] = useState("500")
  const [operationState, setOperationState] = useState<OperationState>("IDLE")

  const myRounds = rounds.filter((round) => round.myAmount)
  const myRaces = races.filter((race) => race.joined)
  const numericAmount = Number(amount)
  const integerAmount = Number.isInteger(numericAmount)
  const increasingRound = Boolean(selectedRound?.myAmount)
  const roundAmountValid = integerAmount && (increasingRound
    ? numericAmount > 0
    : numericAmount >= 100 && numericAmount <= 5000)
  const raceIncreaseValid = integerAmount && numericAmount > 0
  const pending = operationState === "PENDING"

  const openRound = (round: BetRound) => {
    setSelectedRound(round)
    setSide(round.mySide ?? "YES")
    setAmount(round.myAmount ? "100" : "500")
    setOperationState("IDLE")
  }

  const saveRound = () => {
    if (!selectedRound || !roundAmountValid || selectedRound.status !== "OPEN" || pending) return

    const roundId = selectedRound.id
    const nextSide = side
    const delta = numericAmount
    const wasIncrease = Boolean(selectedRound.myAmount)

    setOperationState("PENDING")
    window.setTimeout(() => {
      setRounds((current) => current.map((round) => {
        if (round.id !== roundId) return round

        return {
          ...round,
          mySide: wasIncrease ? round.mySide : nextSide,
          myAmount: (round.myAmount ?? 0) + delta,
        }
      }))
      setOperationState("IDLE")
      setSelectedRound(null)
    }, 900)
  }

  const openRace = (race: ItemRace) => {
    setSelectedRace(race)
    setAmount("500")
    setOperationState("IDLE")
  }

  const joinRace = (race: ItemRace) => {
    if (pending) return

    const raceId = race.id
    setOperationState("PENDING")
    window.setTimeout(() => {
      setRaces((current) => current.map((item) => item.id === raceId
        ? {
            ...item,
            joined: true,
            myAmount: item.entryAmount,
            participants: item.participants + 1,
          }
        : item))
      setOperationState("IDLE")
      setSelectedRace(null)
    }, 900)
  }

  const increaseRace = () => {
    if (!selectedRace || !raceIncreaseValid || pending) return

    const raceId = selectedRace.id
    const delta = numericAmount
    setOperationState("PENDING")
    window.setTimeout(() => {
      setRaces((current) => current.map((item) => item.id === raceId
        ? { ...item, myAmount: (item.myAmount ?? item.entryAmount) + delta }
        : item))
      setOperationState("IDLE")
      setSelectedRace(null)
    }, 900)
  }

  return (
    <div className="screen bets-screen">
      <PageHeader title="Ставки" back={back} />

      <Segmented
        value={mode}
        options={["ROUNDS", "MY", "RACES"] as const}
        onChange={setMode}
        label={(value) => ({ ROUNDS: "Події", MY: "Мої", RACES: "Дропи" }[value])}
      />

      {mode === "ROUNDS" ? (
        <div className="bet-list">
          {rounds.map((round) => <RoundRow key={round.id} round={round} onClick={() => openRound(round)} />)}
        </div>
      ) : null}

      {mode === "RACES" ? (
        <div className="bet-list">
          {races.map((race) => <RaceRow key={race.id} race={race} onClick={() => openRace(race)} />)}
        </div>
      ) : null}

      {mode === "MY" ? (
        <div className="my-bets">
          <div className="bet-list">
            {myRounds.map((round) => <RoundRow key={round.id} round={round} onClick={() => openRound(round)} />)}
          </div>
          <div className="bet-list">
            {myRaces.map((race) => <RaceRow key={race.id} race={race} onClick={() => openRace(race)} />)}
          </div>
          {!myRounds.length && !myRaces.length ? <p className="inline-hint">Порожньо</p> : null}
        </div>
      ) : null}

      <AnimatePresence>
        {selectedRound ? (
          <BottomSheet title={selectedRound.eventType} onClose={() => !pending && setSelectedRound(null)}>
            <div className="bet-sheet">
              <div className="timing-grid">
                <span><small>Подія</small>{selectedRound.scheduledAt}</span>
                <span><small>Закриття</small>{selectedRound.closesAt}</span>
                <span><small>Результат</small>{selectedRound.resolvesAt}</span>
              </div>

              {selectedRound.myAmount ? (
                <div className="bet-side-lock">
                  <small>Варіант</small>
                  <strong>{selectedRound.mySide === "YES" ? "Так" : "Ні"}</strong>
                </div>
              ) : (
                <Field label="Варіант">
                  <Segmented value={side} options={BET_SIDES} onChange={setSide} label={value => value === "YES" ? "Так" : "Ні"} />
                </Field>
              )}

              <Field
                label={selectedRound.myAmount ? "Додати" : "Сума"}
                hint={!roundAmountValid && amount.length
                  ? selectedRound.myAmount ? "Додатне ціле" : "100–5000"
                  : undefined}
              >
                <div className="amount-input">
                  <span><CurrencyValue currency="COIN" value={0} compact /></span>
                  <input type="number" min={selectedRound.myAmount ? 1 : 100} max={selectedRound.myAmount ? undefined : 5000} step={1} value={amount} onChange={(event) => setAmount(event.target.value)} />
                </div>
              </Field>

              <div className="odds-summary">
                <span>{(selectedRound.mySide ?? side) === "YES" ? "Так" : "Ні"}</span>
                <b>× {(selectedRound.mySide ?? side) === "YES" ? selectedRound.yesOdds : selectedRound.noOdds}</b>
              </div>

              <button
                className="primary-action"
                disabled={selectedRound.status !== "OPEN" || !roundAmountValid || pending}
                onClick={saveRound}
              >
                {pending ? <><Glyph name="clock" size={15} /> Обробка</> : selectedRound.myAmount ? "Збільшити" : "Поставити"}
              </button>
            </div>
          </BottomSheet>
        ) : null}

        {selectedRace ? (
          <BottomSheet title={selectedRace.itemName} onClose={() => !pending && setSelectedRace(null)}>
            <div className="bet-sheet">
              <div className="race-summary">
                <span><small>Вхід</small><CurrencyValue currency="COIN" value={selectedRace.entryAmount} compact /></span>
                <span><small>Учасники</small>{selectedRace.participants}</span>
                <span><small>До</small>{selectedRace.closesAt}</span>
              </div>

              {selectedRace.joined ? (
                <>
                  <Field label="Додати" hint={!raceIncreaseValid && amount.length ? "Додатне ціле" : undefined}>
                    <div className="amount-input">
                      <span><CurrencyValue currency="COIN" value={0} compact /></span>
                      <input type="number" min="1" step="1" value={amount} onChange={(event) => setAmount(event.target.value)} />
                    </div>
                  </Field>
                  <button className="primary-action" disabled={!raceIncreaseValid || pending} onClick={increaseRace}>{pending ? <><Glyph name="clock" size={15} /> Обробка</> : "Збільшити"}</button>
                </>
              ) : (
                <button className="primary-action" disabled={pending} onClick={() => joinRace(selectedRace)}>
                  {pending ? <><Glyph name="clock" size={15} /> Обробка</> : <>Увійти · <CurrencyValue currency="COIN" value={selectedRace.entryAmount} compact /></>}
                </button>
              )}
            </div>
          </BottomSheet>
        ) : null}
      </AnimatePresence>
    </div>
  )
}

function RoundRow({ round, onClick }: { round: BetRound; onClick: () => void }) {
  return (
    <button className="bet-row" onClick={onClick}>
      <span className="bet-row__event">
        <Glyph name="global" size={20} />
        <strong>{round.eventType}</strong>
        <small>{betStatusLabels[round.status]}</small>
      </span>
      <span className="bet-odds">
        <span>Так <b>{round.yesOdds}</b></span>
        <span>Ні <b>{round.noOdds}</b></span>
      </span>
      <time>{round.closesAt}</time>
      {round.myAmount ? (
        <span className="my-position">
          <CurrencyValue currency="COIN" value={round.myAmount} compact /> · {round.mySide === "YES" ? "Так" : "Ні"}
        </span>
      ) : null}
    </button>
  )
}

function RaceRow({ race, onClick }: { race: ItemRace; onClick: () => void }) {
  return (
    <button className="race-row race-row--button" onClick={onClick}>
      <div>
        <strong>{race.itemName}</strong>
        <small>{itemTypeLabels[race.itemType]} · {race.participants}</small>
      </div>
      <div className="race-row__entry">
        <CurrencyValue currency="COIN" value={race.joined ? (race.myAmount ?? race.entryAmount) : race.entryAmount} compact />
        {race.joined ? <span className="state-label">Моя</span> : null}
      </div>
    </button>
  )
}
