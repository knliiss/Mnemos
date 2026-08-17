import { AnimatePresence } from "framer-motion"
import { useState } from "react"
import { BET_SIDES } from "../contracts"
import { betRounds, itemRaces as initialItemRaces } from "../mockData"
import type { BetRound, BetSide, ItemRace } from "../model"
import { betStatusLabels, itemTypeLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { BottomSheet, CurrencyValue, Field, PageHeader, Segmented } from "../shared/Primitives"

type BetMode = "ROUNDS" | "MY" | "RACES"

export function BetsPage({ back }: { back: () => void }) {
  const [mode, setMode] = useState<BetMode>("ROUNDS")
  const [rounds, setRounds] = useState(betRounds)
  const [races, setRaces] = useState<ItemRace[]>(initialItemRaces)
  const [selectedRound, setSelectedRound] = useState<BetRound | null>(null)
  const [selectedRace, setSelectedRace] = useState<ItemRace | null>(null)
  const [side, setSide] = useState<BetSide>("YES")
  const [amount, setAmount] = useState("500")

  const myRounds = rounds.filter((round) => round.myAmount)
  const myRaces = races.filter((race) => race.joined)

  const openRound = (round: BetRound) => {
    setSelectedRound(round)
    setSide(round.mySide ?? "YES")
    setAmount(round.myAmount ? "100" : "500")
  }

  const saveRound = () => {
    if (!selectedRound || Number(amount) <= 0 || selectedRound.status !== "OPEN") return

    const nextAmount = selectedRound.myAmount
      ? selectedRound.myAmount + Number(amount)
      : Number(amount)

    setRounds(rounds.map((round) => round.id === selectedRound.id
      ? { ...round, mySide: side, myAmount: nextAmount }
      : round))
    setSelectedRound(null)
  }

  const openRace = (race: ItemRace) => {
    setSelectedRace(race)
    setAmount("500")
  }

  const joinRace = (race: ItemRace) => {
    const updated = {
      ...race,
      joined: true,
      myAmount: race.entryAmount,
      participants: race.participants + 1,
    }

    setRaces(races.map((item) => item.id === race.id ? updated : item))
    setSelectedRace(null)
  }

  const increaseRace = () => {
    if (!selectedRace || Number(amount) <= 0) return

    const updated = {
      ...selectedRace,
      myAmount: (selectedRace.myAmount ?? selectedRace.entryAmount) + Number(amount),
    }

    setRaces(races.map((item) => item.id === selectedRace.id ? updated : item))
    setSelectedRace(null)
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
          <BottomSheet title={selectedRound.eventType} onClose={() => setSelectedRound(null)}>
            <div className="bet-sheet">
              <div className="timing-grid">
                <span><small>Подія</small>{selectedRound.scheduledAt}</span>
                <span><small>Закриття</small>{selectedRound.closesAt}</span>
                <span><small>Результат</small>{selectedRound.resolvesAt}</span>
              </div>

              <Field label="Варіант">
                <Segmented value={side} options={BET_SIDES} onChange={setSide} />
              </Field>

              <Field label={selectedRound.myAmount ? "Додати" : "Сума"}>
                <div className="amount-input">
                  <span><CurrencyValue currency="COIN" value={0} compact /></span>
                  <input type="number" min="1" value={amount} onChange={(event) => setAmount(event.target.value)} />
                </div>
              </Field>

              <div className="odds-summary">
                <span>{side}</span>
                <b>× {side === "YES" ? selectedRound.yesOdds : selectedRound.noOdds}</b>
              </div>

              <button
                className="primary-action"
                disabled={selectedRound.status !== "OPEN" || Number(amount) <= 0}
                onClick={saveRound}
              >
                {selectedRound.myAmount ? "Збільшити" : "Поставити"}
              </button>
            </div>
          </BottomSheet>
        ) : null}

        {selectedRace ? (
          <BottomSheet title={selectedRace.itemName} onClose={() => setSelectedRace(null)}>
            <div className="bet-sheet">
              <div className="race-summary">
                <span><small>Вхід</small><CurrencyValue currency="COIN" value={selectedRace.entryAmount} compact /></span>
                <span><small>Учасники</small>{selectedRace.participants}</span>
                <span><small>До</small>{selectedRace.closesAt}</span>
              </div>

              {selectedRace.joined ? (
                <>
                  <Field label="Додати">
                    <div className="amount-input">
                      <span><CurrencyValue currency="COIN" value={0} compact /></span>
                      <input type="number" min="1" value={amount} onChange={(event) => setAmount(event.target.value)} />
                    </div>
                  </Field>
                  <button className="primary-action" onClick={increaseRace}>Збільшити</button>
                </>
              ) : (
                <button className="primary-action" onClick={() => joinRace(selectedRace)}>
                  Увійти · {selectedRace.entryAmount.toLocaleString("uk-UA")}
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
        <span>YES <b>{round.yesOdds}</b></span>
        <span>NO <b>{round.noOdds}</b></span>
      </span>
      <time>{round.closesAt}</time>
      {round.myAmount ? (
        <span className="my-position">
          <CurrencyValue currency="COIN" value={round.myAmount} compact /> · {round.mySide}
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
