import { useState } from "react"
import {
  GlobalEventArt,
  ItemTypeArt,
} from "../components/EventArt"
import {
  BET_ITEM_TYPES,
  EVENT_BET_OUTCOMES,
} from "../contracts"
import { betRounds } from "../mockData"
import type {
  BetItemType,
  EventBetOutcome,
} from "../model"
import {
  globalEventLabels,
  itemTypeLabels,
} from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import {
  CurrencyValue,
  Field,
  PageHeader,
} from "../shared/Primitives"

type CreatedRace = {
  id: string
  itemName: string
  itemType: BetItemType
  hours: number
  entry: number
}

const outcomeLabels: Record<EventBetOutcome, string> = {
  YES: "Так",
  NO: "Ні",
  REFUND: "Повернення",
}

export function BetAdminOverhaul({ back }: { back: () => void }) {
  const [round, setRound] = useState(betRounds[0].id)
  const [outcome, setOutcome] = useState<EventBetOutcome>("YES")
  const [resolveReason, setResolveReason] = useState("")
  const [resolved, setResolved] = useState<Record<string, EventBetOutcome>>({})
  const [itemName, setItemName] = useState("")
  const [itemType, setItemType] = useState<BetItemType>("SWORD")
  const [hours, setHours] = useState("24")
  const [entry, setEntry] = useState("1000")
  const [created, setCreated] = useState<CreatedRace[]>([])

  const selectedRound = betRounds.find((item) => item.id === round)
    ?? betRounds[0]

  const numericHours = Number(hours)
  const numericEntry = Number(entry)

  const hoursValid = Number.isInteger(numericHours)
    && numericHours >= 1
    && numericHours <= 168

  const entryValid = Number.isInteger(numericEntry)
    && numericEntry >= 1000
    && numericEntry <= 5000

  const itemRaceValid = Boolean(itemName.trim())
    && hoursValid
    && entryValid

  const applyResult = () => {
    if (!resolveReason.trim()) return

    setResolved({
      ...resolved,
      [selectedRound.id]: outcome,
    })
    setResolveReason("")
  }

  const createRace = () => {
    if (!itemRaceValid) return

    setCreated([
      {
        id: crypto.randomUUID(),
        itemName: itemName.trim(),
        itemType,
        hours: numericHours,
        entry: numericEntry,
      },
      ...created,
    ])
    setItemName("")
  }

  return (
    <div className="screen bet-admin-overhaul">
      <PageHeader title="Ставки" back={back} />

      <section className="bet-admin-summary">
        <span className="bet-admin-summary__mark">
          <Glyph name="bet" size={21} />
        </span>
        <span>
          <small>ADMIN WORKSPACE</small>
          <strong>Результати та Item race</strong>
          <em>Раунд обирається карткою — видно іконку, статус, час і коефіцієнти.</em>
        </span>
      </section>

      <section className="bet-admin-block">
        <header className="bet-admin-block__head">
          <span>
            <small>EVENT BETS</small>
            <h2>Коригування результату</h2>
          </span>
          <b>{betRounds.length} раунди</b>
        </header>

        <div className="bet-admin-round-list">
          {betRounds.map((item) => {
            const finalOutcome = resolved[item.id]

            return (
              <button
                key={item.id}
                className={
                  round === item.id
                    ? "bet-admin-round is-selected"
                    : "bet-admin-round"
                }
                onClick={() => setRound(item.id)}
              >
                <GlobalEventArt eventType={item.eventType} size={42} />

                <span className="bet-admin-round__copy">
                  <span>
                    <strong>{globalEventLabels[item.eventType]}</strong>
                    <i className={`status-${item.status.toLowerCase()}`}>
                      {item.status}
                    </i>
                  </span>
                  <small>{item.scheduledAt}</small>
                  <em>закриття {item.closesAt}</em>
                </span>

                <span className="bet-admin-round__odds">
                  <small>Так <b>{item.yesOdds}</b></small>
                  <small>Ні <b>{item.noOdds}</b></small>
                </span>

                {finalOutcome ? (
                  <span className="bet-admin-round__resolved">
                    {outcomeLabels[finalOutcome]}
                  </span>
                ) : null}
              </button>
            )
          })}
        </div>

        <div className="bet-result-workbench">
          <header>
            <GlobalEventArt eventType={selectedRound.eventType} size={48} />
            <span>
              <small>Обраний раунд</small>
              <strong>{globalEventLabels[selectedRound.eventType]}</strong>
              <em>resolve {selectedRound.resolvesAt} · {selectedRound.status}</em>
            </span>
          </header>

          <div className="bet-outcome-picker">
            {EVENT_BET_OUTCOMES.map((value) => (
              <button
                key={value}
                className={outcome === value ? "is-active" : ""}
                onClick={() => setOutcome(value)}
              >
                <span>
                  <Glyph
                    name={value === "YES" ? "check" : value === "NO" ? "close" : "swap"}
                    size={16}
                  />
                </span>
                <strong>{outcomeLabels[value]}</strong>
                <small>
                  {value === "REFUND" ? "повернути всі ставки" : "зафіксувати результат"}
                </small>
              </button>
            ))}
          </div>

          <Field label="Причина зміни / підтвердження">
            <textarea
              value={resolveReason}
              maxLength={300}
              onChange={(event) => setResolveReason(event.target.value)}
              placeholder="Наприклад: підтверджено адміністрацією після відсутності collectors"
            />
          </Field>

          <button
            className="primary-action"
            disabled={!resolveReason.trim()}
            onClick={applyResult}
          >
            <Glyph name="check" size={16} />
            Застосувати до {globalEventLabels[selectedRound.eventType]}
          </button>
        </div>
      </section>

      <section className="bet-admin-block">
        <header className="bet-admin-block__head">
          <span>
            <small>ITEM RACE</small>
            <h2>Створити перегон</h2>
          </span>
          <b>{created.length} нових</b>
        </header>

        <div className="item-race-builder">
          <section className="item-race-builder__preview">
            <ItemTypeArt itemType={itemType} size={58} />
            <span>
              <small>LIVE PREVIEW</small>
              <strong>{itemName.trim() || "Назва предмета"}</strong>
              <em>{itemTypeLabels[itemType]}</em>
            </span>

            <div>
              <small>Вхід</small>
              <CurrencyValue currency="COIN" value={entryValid ? numericEntry : 0} compact />
            </div>
          </section>

          <Field label="Предмет">
            <input
              value={itemName}
              maxLength={24}
              onChange={(event) => setItemName(event.target.value)}
              placeholder="Eternal Blade"
            />
          </Field>

          <Field label="Тип">
            <div className="item-type-art-picker">
              {BET_ITEM_TYPES.map((value) => (
                <button
                  key={value}
                  className={itemType === value ? "is-active" : ""}
                  onClick={() => setItemType(value)}
                >
                  <ItemTypeArt itemType={value} size={34} />
                  <span>{itemTypeLabels[value]}</span>
                </button>
              ))}
            </div>
          </Field>

          <div className="bet-builder-presets">
            <Field label="Тривалість" hint={!hoursValid && hours.length ? "1–168 год" : undefined}>
              <div className="number-preset-field">
                <input
                  type="number"
                  min={1}
                  max={168}
                  step={1}
                  value={hours}
                  onChange={(event) => setHours(event.target.value)}
                />
                <span>год</span>
              </div>

              <div className="quick-preset-row">
                {[6, 12, 24, 48].map((value) => (
                  <button
                    key={value}
                    className={numericHours === value ? "is-active" : ""}
                    onClick={() => setHours(String(value))}
                  >
                    {value}h
                  </button>
                ))}
              </div>
            </Field>

            <Field label="Вхід" hint={!entryValid && entry.length ? "1000–5000" : undefined}>
              <div className="number-preset-field">
                <CurrencyMark currency="COIN" size={19} />
                <input
                  type="number"
                  min={1000}
                  max={5000}
                  step={100}
                  value={entry}
                  onChange={(event) => setEntry(event.target.value)}
                />
              </div>

              <div className="quick-preset-row">
                {[1000, 1500, 2500, 5000].map((value) => (
                  <button
                    key={value}
                    className={numericEntry === value ? "is-active" : ""}
                    onClick={() => setEntry(String(value))}
                  >
                    {value}
                  </button>
                ))}
              </div>
            </Field>
          </div>

          <button
            className="primary-action"
            disabled={!itemRaceValid}
            onClick={createRace}
          >
            <Glyph name="plus" size={16} />
            Створити Item race
          </button>

          {created.length ? (
            <div className="created-race-list">
              {created.map((item) => (
                <article key={item.id}>
                  <ItemTypeArt itemType={item.itemType} size={34} />
                  <span>
                    <strong>{item.itemName}</strong>
                    <small>{item.hours} год · {itemTypeLabels[item.itemType]}</small>
                  </span>
                  <CurrencyValue currency="COIN" value={item.entry} compact />
                </article>
              ))}
            </div>
          ) : null}
        </div>
      </section>
    </div>
  )
}
