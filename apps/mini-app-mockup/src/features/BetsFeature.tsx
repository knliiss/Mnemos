import { AnimatePresence } from "framer-motion"
import { useState } from "react"
import { BET_SIDES } from "../contracts"
import { accounts, betRounds, itemRaces as initialItemRaces } from "../mockData"
import type { BetRound, BetSide, ItemRace } from "../model"
import { betStatusLabels, globalEventLabels, itemTypeLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { BottomSheet, CurrencyValue, Field, Segmented } from "../shared/Primitives"

type BetMode = "ROUNDS" | "MY" | "RACES"
type OperationState = "IDLE" | "PENDING"

export function BetsPage() {
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
    <div className="screen bets-root-screen root-screen">
      <header className="root-title-block bets-title-block">
        <span>
          <h1>Ставки</h1>
          <p>Активний аккаунт</p>
        </span>
        <button className="root-wallet-chip">
          <CurrencyValue currency="COIN" value={accounts[0].coin} compact />
        </button>
      </header>

      <Segmented
        value={mode}
        options={["ROUNDS", "MY", "RACES"] as const}
        onChange={setMode}
        label={(value) => ({ ROUNDS: "Події", MY: "Мої", RACES: "Перегони" }[value])}
      />

      {mode === "ROUNDS" ? (
        <section className="bet-redesign-list">
          {rounds.map((round) => <RoundRow key={round.id} round={round} onClick={() => openRound(round)} />)}
        </section>
      ) : null}

      {mode === "RACES" ? (
        <section className="bet-redesign-list">
          {races.map((race) => <RaceRow key={race.id} race={race} onClick={() => openRace(race)} />)}
        </section>
      ) : null}

      {mode === "MY" ? (
        <section className="my-bets-redesign">
          {myRounds.length ? <div className="bet-redesign-list">{myRounds.map((round) => <RoundRow key={round.id} round={round} onClick={() => openRound(round)} />)}</div> : null}
          {myRaces.length ? <div className="bet-redesign-list">{myRaces.map((race) => <RaceRow key={race.id} race={race} onClick={() => openRace(race)} />)}</div> : null}
          {!myRounds.length && !myRaces.length ? <p className="inline-hint">Поки ставок немає</p> : null}
        </section>
      ) : null}

      <AnimatePresence>
        {selectedRound ? (
          <BottomSheet title={globalEventLabels[selectedRound.eventType]} onClose={() => !pending && setSelectedRound(null)}>
            <div className="bet-sheet bet-sheet--redesign">
              <div className="bet-sheet__timing">
                <span><small>Подія</small><strong>{selectedRound.scheduledAt}</strong></span>
                <span><small>Закриття</small><strong>{selectedRound.closesAt}</strong></span>
              </div>

              {selectedRound.myAmount ? (
                <div className="bet-existing-position">
                  <span><small>Твоя сторона</small><strong>{selectedRound.mySide === "YES" ? "Так" : "Ні"}</strong></span>
                  <CurrencyValue currency="COIN" value={selectedRound.myAmount} compact />
                </div>
              ) : (
                <Field label="Результат">
                  <div className="bet-side-choice">
                    {BET_SIDES.map((value) => (
                      <button key={value} className={side === value ? "is-active" : ""} onClick={() => setSide(value)}>
                        <span>{value === "YES" ? "Так" : "Ні"}</span>
                        <b>× {value === "YES" ? selectedRound.yesOdds : selectedRound.noOdds}</b>
                      </button>
                    ))}
                  </div>
                </Field>
              )}

              <Field
                label={selectedRound.myAmount ? "Збільшити на" : "Сума ставки"}
                hint={!roundAmountValid && amount.length
                  ? selectedRound.myAmount ? "Потрібне додатне ціле число" : "Від 100 до 5000 COIN"
                  : undefined}
              >
                <div className="amount-input amount-input--redesign">
                  <CurrencyValue currency="COIN" value={0} compact />
                  <input
                    type="number"
                    min={selectedRound.myAmount ? 1 : 100}
                    max={selectedRound.myAmount ? undefined : 5000}
                    step={1}
                    value={amount}
                    onChange={(event) => setAmount(event.target.value)}
                  />
                </div>
              </Field>

              {!selectedRound.myAmount ? (
                <div className="bet-odds-note">
                  <span>Коефіцієнт</span>
                  <b>× {side === "YES" ? selectedRound.yesOdds : selectedRound.noOdds}</b>
                </div>
              ) : null}

              <button className="primary-action" disabled={selectedRound.status !== "OPEN" || !roundAmountValid || pending} onClick={saveRound}>
                {pending ? <><Glyph name="clock" size={15} /> Обробка</> : selectedRound.myAmount ? "Збільшити ставку" : "Поставити"}
              </button>
            </div>
          </BottomSheet>
        ) : null}

        {selectedRace ? (
          <BottomSheet title={selectedRace.itemName} onClose={() => !pending && setSelectedRace(null)}>
            <div className="bet-sheet bet-sheet--redesign">
              <div className="race-sheet-summary">
                <span><small>Тип</small><strong>{itemTypeLabels[selectedRace.itemType]}</strong></span>
                <span><small>Учасники</small><strong>{selectedRace.participants}</strong></span>
                <span><small>Закриття</small><strong>{selectedRace.closesAt}</strong></span>
              </div>

              <div className="race-entry-line">
                <span>Вхід</span>
                <CurrencyValue currency="COIN" value={selectedRace.entryAmount} compact />
              </div>

              {selectedRace.joined ? (
                <>
                  <div className="bet-existing-position">
                    <span><small>Твоя участь</small><strong>Активна</strong></span>
                    <CurrencyValue currency="COIN" value={selectedRace.myAmount ?? selectedRace.entryAmount} compact />
                  </div>
                  <Field label="Збільшити на" hint={!raceIncreaseValid && amount.length ? "Потрібне додатне ціле число" : undefined}>
                    <div className="amount-input amount-input--redesign">
                      <CurrencyValue currency="COIN" value={0} compact />
                      <input type="number" min="1" step="1" value={amount} onChange={(event) => setAmount(event.target.value)} />
                    </div>
                  </Field>
                  <button className="primary-action" disabled={!raceIncreaseValid || pending} onClick={increaseRace}>{pending ? <><Glyph name="clock" size={15} /> Обробка</> : "Збільшити"}</button>
                </>
              ) : (
                <button className="primary-action" disabled={pending} onClick={() => joinRace(selectedRace)}>
                  {pending ? <><Glyph name="clock" size={15} /> Обробка</> : <>Приєднатись · <CurrencyValue currency="COIN" value={selectedRace.entryAmount} compact /></>}
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
    <button className={`bet-redesign-card ${round.status !== "OPEN" ? "is-locked" : ""}`} onClick={onClick}>
      <span className="bet-redesign-card__icon"><Glyph name="global" size={20} /></span>
      <span className="bet-redesign-card__copy">
        <small>{globalEventLabels[round.eventType]}</small>
        <strong>{betStatusLabels[round.status]}</strong>
        <time>до {round.closesAt}</time>
      </span>
      <span className="bet-redesign-card__odds"><span>Так <b>{round.yesOdds}</b></span><span>Ні <b>{round.noOdds}</b></span></span>
      {round.myAmount ? <span className="bet-redesign-card__mine"><CurrencyValue currency="COIN" value={round.myAmount} compact /><b>{round.mySide === "YES" ? "Так" : "Ні"}</b></span> : <span className="bet-redesign-card__open">Відкрити ›</span>}
    </button>
  )
}

function RaceRow({ race, onClick }: { race: ItemRace; onClick: () => void }) {
  return (
    <button className="bet-redesign-card race-redesign-card" onClick={onClick}>
      <span className="bet-redesign-card__icon bet-redesign-card__icon--amber"><Glyph name="item-drop" size={20} /></span>
      <span className="bet-redesign-card__copy">
        <small>{itemTypeLabels[race.itemType]}</small>
        <strong>{race.itemName}</strong>
        <time>{race.participants} учасників · до {race.closesAt}</time>
      </span>
      <span className="race-redesign-card__entry"><small>{race.joined ? "Твоя сума" : "Вхід"}</small><CurrencyValue currency="COIN" value={race.joined ? (race.myAmount ?? race.entryAmount) : race.entryAmount} compact /></span>
      <span className="bet-redesign-card__open">›</span>
    </button>
  )
}
