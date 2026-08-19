import { useMemo, useState } from "react"
import type { Dispatch, SetStateAction } from "react"
import { GlobalEventArt, ItemTypeArt } from "../components/EventArt"
import { BET_ITEM_TYPES, EVENT_BET_OUTCOMES } from "../contracts"
import { betRounds } from "../mockData"
import type {
  BetGlobalEventType,
  BetItemType,
  BetRound,
  BetSide,
  EventBetOutcome,
  EventBetRoundStatus,
  Route,
} from "../model"
import { globalEventLabels, itemTypeLabels } from "../presentation"
import { CurrencyValue, Field, PageHeader } from "../shared/Primitives"
import { Glyph } from "../shared/MnemosIcon"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type BetPlacement = {
  id: string
  account: string
  side: BetSide
  amount: number
  odds: number
  placedAt: string
  payout?: number
}

type AdminRound = BetRound & {
  result?: EventBetOutcome
  placements: BetPlacement[]
}

type AdminRace = {
  id: string
  itemName: string
  itemType: BetItemType
  entry: number
  closesAt: string
  participants: number
  totalPool: number
  status: "OPEN" | "CLOSED" | "SETTLED"
}

const outcomeLabels: Record<EventBetOutcome, string> = {
  YES: "Так",
  NO: "Ні",
  REFUND: "Повернення",
}

const statusLabels: Record<EventBetRoundStatus, string> = {
  OPEN: "Відкрито",
  LOCKED: "Закрито для ставок",
  AWAITING_RESULT: "Очікує результат",
  AWAITING_ADMIN_CONFIRMATION: "Потрібне підтвердження",
  SETTLED: "Розраховано",
}

const eventOptions: BetGlobalEventType[] = ["MOON", "ECLIPSE", "EXPLOSION"]

const seedPlacements: Record<string, BetPlacement[]> = {
  "round-moon": [
    { id: "p-1", account: "knalis", side: "YES", amount: 850, odds: 1.84, placedAt: "17 серп. · 22:41" },
    { id: "p-2", account: "north", side: "NO", amount: 1200, odds: 1.94, placedAt: "17 серп. · 22:54" },
    { id: "p-3", account: "vex", side: "YES", amount: 500, odds: 1.84, placedAt: "17 серп. · 23:06" },
    { id: "p-4", account: "mare", side: "NO", amount: 2100, odds: 1.94, placedAt: "17 серп. · 23:12" },
  ],
  "round-explosion": [
    { id: "p-5", account: "knalis", side: "YES", amount: 750, odds: 1.05, placedAt: "17 серп. · 23:30" },
    { id: "p-6", account: "north", side: "NO", amount: 1000, odds: 16.2, placedAt: "17 серп. · 23:41" },
    { id: "p-7", account: "vex", side: "YES", amount: 1800, odds: 1.05, placedAt: "17 серп. · 23:44" },
  ],
  "round-eclipse": [
    { id: "p-8", account: "north", side: "YES", amount: 900, odds: 1.91, placedAt: "18 серп. · 00:16" },
    { id: "p-9", account: "mare", side: "NO", amount: 900, odds: 1.91, placedAt: "18 серп. · 00:22" },
  ],
}

const archivedRounds: AdminRound[] = [
  {
    id: "history-moon-1",
    eventType: "MOON",
    scheduledAt: "17 серп. · 00:00",
    closesAt: "16 серп. · 23:55",
    resolvesAt: "17 серп. · 00:05",
    yesOdds: 1.82,
    noOdds: 1.96,
    status: "SETTLED",
    result: "YES",
    placements: [
      { id: "hp-1", account: "north", side: "YES", amount: 1500, odds: 1.82, placedAt: "16 серп. · 22:18", payout: 2730 },
      { id: "hp-2", account: "knalis", side: "YES", amount: 600, odds: 1.82, placedAt: "16 серп. · 23:01", payout: 1092 },
      { id: "hp-3", account: "vex", side: "NO", amount: 1200, odds: 1.96, placedAt: "16 серп. · 23:17", payout: 0 },
      { id: "hp-4", account: "mare", side: "NO", amount: 400, odds: 1.96, placedAt: "16 серп. · 23:43", payout: 0 },
    ],
  },
  {
    id: "history-explosion-1",
    eventType: "EXPLOSION",
    scheduledAt: "17 серп. · 01:00",
    closesAt: "17 серп. · 00:55",
    resolvesAt: "17 серп. · 01:05",
    yesOdds: 1.06,
    noOdds: 14.8,
    status: "SETTLED",
    result: "NO",
    placements: [
      { id: "hp-5", account: "north", side: "YES", amount: 2100, odds: 1.06, placedAt: "17 серп. · 00:11", payout: 0 },
      { id: "hp-6", account: "vex", side: "YES", amount: 900, odds: 1.06, placedAt: "17 серп. · 00:22", payout: 0 },
      { id: "hp-7", account: "mare", side: "NO", amount: 250, odds: 14.8, placedAt: "17 серп. · 00:49", payout: 3700 },
    ],
  },
  {
    id: "history-eclipse-1",
    eventType: "ECLIPSE",
    scheduledAt: "16 серп. · 03:00",
    closesAt: "16 серп. · 02:55",
    resolvesAt: "16 серп. · 03:05",
    yesOdds: 1.9,
    noOdds: 1.92,
    status: "SETTLED",
    result: "REFUND",
    placements: [
      { id: "hp-8", account: "knalis", side: "YES", amount: 800, odds: 1.9, placedAt: "16 серп. · 01:42", payout: 800 },
      { id: "hp-9", account: "north", side: "NO", amount: 1300, odds: 1.92, placedAt: "16 серп. · 02:13", payout: 1300 },
    ],
  },
]

const seedRaces: AdminRace[] = [
  {
    id: "race-admin-1",
    itemName: "Eternal Blade",
    itemType: "SWORD",
    entry: 1200,
    closesAt: "18 серп. · 20:00",
    participants: 17,
    totalPool: 28_600,
    status: "OPEN",
  },
  {
    id: "race-admin-2",
    itemName: "Moon Necklace",
    itemType: "JEWELRY",
    entry: 1000,
    closesAt: "19 серп. · 21:00",
    participants: 11,
    totalPool: 14_000,
    status: "OPEN",
  },
]

function withPlacements(round: BetRound): AdminRound {
  return {
    ...round,
    placements: seedPlacements[round.id] ?? [],
  }
}

export function BetAdminOverhaul({ params, navigate, back }: Props) {
  const [rounds, setRounds] = useState<AdminRound[]>(() => [
    ...betRounds.map(withPlacements),
    ...archivedRounds,
  ])
  const [races, setRaces] = useState<AdminRace[]>(seedRaces)
  const section = params?.section ?? "home"

  if (section === "rounds") {
    return <CurrentRoundsPage rounds={rounds} navigate={navigate} back={back} />
  }

  if (section === "history") {
    return <HistoryPage rounds={rounds} navigate={navigate} back={back} />
  }

  if (section === "round") {
    return (
      <RoundDetailPage
        round={rounds.find((item) => item.id === params?.roundId) ?? rounds[0]}
        setRounds={setRounds}
        back={back}
      />
    )
  }

  if (section === "create-round") {
    return <CreateRoundPage rounds={rounds} setRounds={setRounds} back={back} />
  }

  if (section === "races") {
    return <RaceManagementPage races={races} setRaces={setRaces} back={back} />
  }

  return <BetAdminHome rounds={rounds} races={races} navigate={navigate} back={back} />
}

function BetAdminHome({ rounds, races, navigate, back }: {
  rounds: AdminRound[]
  races: AdminRace[]
  navigate: (route: Route) => void
  back: () => void
}) {
  const active = rounds.filter((round) => round.status !== "SETTLED")
  const history = rounds.filter((round) => round.status === "SETTLED")
  const totalVolume = rounds.flatMap((round) => round.placements).reduce((sum, placement) => sum + placement.amount, 0)
  const bettorCount = new Set(rounds.flatMap((round) => round.placements.map((placement) => placement.account))).size

  return (
    <div className="screen bet-admin-v2 bet-admin-dashboard">
      <PageHeader title="Ставки" back={back} />

      <section className="bet-admin-v2-hero">
        <span className="bet-admin-v2-hero__icon"><Glyph name="bet" size={22} /></span>
        <span>
          <small>ADMIN WORKSPACE</small>
          <strong>Керування ставками</strong>
          <em>Раунди, результати, історія та Item race розділені на окремі екрани.</em>
        </span>
      </section>

      <section className="bet-admin-kpis">
        <article><small>Активні</small><strong>{active.length}</strong><em>раунди</em></article>
        <article><small>Історія</small><strong>{history.length}</strong><em>завершено</em></article>
        <article><small>Гравці</small><strong>{bettorCount}</strong><em>унікальних</em></article>
        <article><small>Оборот</small><CurrencyValue currency="COIN" value={totalVolume} compact /></article>
      </section>

      <nav className="bet-admin-nav-grid">
        <button onClick={() => navigate({ id: "bet-admin", params: { section: "rounds" } })}>
          <span className="is-lime"><Glyph name="events" size={20} /></span>
          <strong>Поточні раунди</strong>
          <small>Перегляд стану та перехід до коригування</small>
          <b>{active.length} ›</b>
        </button>
        <button onClick={() => navigate({ id: "bet-admin", params: { section: "history" } })}>
          <span className="is-plum"><Glyph name="clock" size={20} /></span>
          <strong>Історія ставок</strong>
          <small>Результати, банки, учасники та виплати</small>
          <b>{history.length} ›</b>
        </button>
        <button onClick={() => navigate({ id: "bet-admin", params: { section: "create-round" } })}>
          <span className="is-amber"><Glyph name="plus" size={20} /></span>
          <strong>Створити раунд</strong>
          <small>Подія, час закриття та стартові коефіцієнти</small>
          <b>›</b>
        </button>
        <button onClick={() => navigate({ id: "bet-admin", params: { section: "races" } })}>
          <span className="is-blue"><Glyph name="item-drop" size={20} /></span>
          <strong>Item race</strong>
          <small>Створення та контроль активних перегонів</small>
          <b>{races.filter((race) => race.status === "OPEN").length} ›</b>
        </button>
      </nav>

      <section className="bet-admin-recent">
        <header><span><small>ОСТАННЯ АКТИВНІСТЬ</small><strong>Раунди</strong></span></header>
        {rounds.slice(0, 3).map((round) => (
          <RoundSummaryRow
            key={round.id}
            round={round}
            onClick={() => navigate({ id: "bet-admin", params: { section: "round", roundId: round.id } })}
          />
        ))}
      </section>
    </div>
  )
}

function CurrentRoundsPage({ rounds, navigate, back }: {
  rounds: AdminRound[]
  navigate: (route: Route) => void
  back: () => void
}) {
  const active = rounds.filter((round) => round.status !== "SETTLED")

  return (
    <div className="screen bet-admin-v2">
      <PageHeader
        title="Поточні раунди"
        back={back}
        actions={(
          <button
            className="bet-admin-header-action"
            onClick={() => navigate({ id: "bet-admin", params: { section: "create-round" } })}
          >
            <Glyph name="plus" size={15} />
            Новий
          </button>
        )}
      />

      <section className="bet-admin-round-stack">
        {active.map((round) => (
          <RoundSummaryRow
            key={round.id}
            round={round}
            onClick={() => navigate({ id: "bet-admin", params: { section: "round", roundId: round.id } })}
          />
        ))}
      </section>
    </div>
  )
}

function HistoryPage({ rounds, navigate, back }: {
  rounds: AdminRound[]
  navigate: (route: Route) => void
  back: () => void
}) {
  const [query, setQuery] = useState("")
  const [eventType, setEventType] = useState<"ALL" | BetGlobalEventType>("ALL")
  const history = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    return rounds.filter((round) => {
      if (round.status !== "SETTLED") return false
      if (eventType !== "ALL" && round.eventType !== eventType) return false
      if (!normalized) return true

      return globalEventLabels[round.eventType].toLowerCase().includes(normalized)
        || round.scheduledAt.toLowerCase().includes(normalized)
        || round.placements.some((placement) => placement.account.toLowerCase().includes(normalized))
    })
  }, [rounds, query, eventType])

  return (
    <div className="screen bet-admin-v2">
      <PageHeader title="Історія ставок" back={back} />

      <div className="bet-history-toolbar">
        <label>
          <Glyph name="search" size={16} />
          <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Подія, дата або гравець" />
        </label>
        <div>
          {(["ALL", ...eventOptions] as const).map((value) => (
            <button key={value} className={eventType === value ? "is-active" : ""} onClick={() => setEventType(value)}>
              {value === "ALL" ? "Усі" : globalEventLabels[value]}
            </button>
          ))}
        </div>
      </div>

      <section className="bet-history-list">
        {history.map((round) => (
          <RoundSummaryRow
            key={round.id}
            round={round}
            onClick={() => navigate({ id: "bet-admin", params: { section: "round", roundId: round.id } })}
          />
        ))}
      </section>
    </div>
  )
}

function RoundDetailPage({ round, setRounds, back }: {
  round: AdminRound
  setRounds: Dispatch<SetStateAction<AdminRound[]>>
  back: () => void
}) {
  const [outcome, setOutcome] = useState<EventBetOutcome>(round.result ?? "YES")
  const [reason, setReason] = useState("")
  const totalPool = round.placements.reduce((sum, placement) => sum + placement.amount, 0)
  const yesPool = round.placements.filter((placement) => placement.side === "YES").reduce((sum, placement) => sum + placement.amount, 0)
  const noPool = totalPool - yesPool

  const apply = () => {
    if (!reason.trim()) return

    setRounds((current) => current.map((item) => item.id === round.id
      ? { ...item, result: outcome, status: "SETTLED" }
      : item))
    setReason("")
  }

  return (
    <div className="screen bet-admin-v2 bet-round-detail">
      <PageHeader title={globalEventLabels[round.eventType]} back={back} />

      <section className="bet-round-hero">
        <GlobalEventArt eventType={round.eventType} size={62} />
        <span>
          <small>{statusLabels[round.status]}</small>
          <strong>{round.scheduledAt}</strong>
          <em>Закриття {round.closesAt} · resolve {round.resolvesAt}</em>
        </span>
        {round.result ? <b className={`result-${round.result.toLowerCase()}`}>{outcomeLabels[round.result]}</b> : null}
      </section>

      <section className="bet-round-metrics">
        <article><small>Банк</small><CurrencyValue currency="COIN" value={totalPool} compact /></article>
        <article><small>Так</small><strong>{round.placements.filter((placement) => placement.side === "YES").length}</strong><CurrencyValue currency="COIN" value={yesPool} compact /></article>
        <article><small>Ні</small><strong>{round.placements.filter((placement) => placement.side === "NO").length}</strong><CurrencyValue currency="COIN" value={noPool} compact /></article>
        <article><small>Ставок</small><strong>{round.placements.length}</strong></article>
      </section>

      <section className="bet-bettors-panel">
        <header><span><small>УЧАСНИКИ</small><strong>Хто ставив</strong></span><b>{round.placements.length}</b></header>
        <div className="bet-bettor-list">
          {round.placements.map((placement) => (
            <article key={placement.id}>
              <span className="bet-bettor-avatar">{placement.account.slice(0, 1).toUpperCase()}</span>
              <span><strong>{placement.account}</strong><small>{placement.placedAt}</small></span>
              <span className={`bet-side-pill is-${placement.side.toLowerCase()}`}>{placement.side === "YES" ? "Так" : "Ні"}</span>
              <span className="bet-bettor-money">
                <CurrencyValue currency="COIN" value={placement.amount} compact />
                <small>× {placement.odds}</small>
              </span>
              {placement.payout !== undefined ? (
                <span className={placement.payout > 0 ? "bet-payout is-win" : "bet-payout"}>
                  {placement.payout > 0 ? `+${placement.payout.toLocaleString("uk-UA")}` : "0"}
                </span>
              ) : null}
            </article>
          ))}
        </div>
      </section>

      <section className="bet-correction-panel">
        <header><span><small>РЕЗУЛЬТАТ</small><strong>{round.result ? "Коригування" : "Підтвердження"}</strong></span></header>
        <div className="bet-outcome-picker-v2">
          {EVENT_BET_OUTCOMES.map((value) => (
            <button key={value} className={outcome === value ? "is-active" : ""} onClick={() => setOutcome(value)}>
              <Glyph name={value === "YES" ? "check" : value === "NO" ? "close" : "swap"} size={16} />
              <span><strong>{outcomeLabels[value]}</strong><small>{value === "REFUND" ? "повернути всі ставки" : "зафіксувати результат"}</small></span>
            </button>
          ))}
        </div>
        <Field label="Причина зміни / підтвердження">
          <textarea value={reason} maxLength={300} onChange={(event) => setReason(event.target.value)} placeholder="Причина потрібна для аудиту" />
        </Field>
        <button className="primary-action" disabled={!reason.trim()} onClick={apply}>
          <Glyph name="check" size={16} />
          {round.result ? "Зберегти коригування" : "Зафіксувати результат"}
        </button>
      </section>
    </div>
  )
}

function CreateRoundPage({ rounds, setRounds, back }: {
  rounds: AdminRound[]
  setRounds: Dispatch<SetStateAction<AdminRound[]>>
  back: () => void
}) {
  const [eventType, setEventType] = useState<BetGlobalEventType>("MOON")
  const [scheduledAt, setScheduledAt] = useState("2026-08-19T00:00")
  const [yesOdds, setYesOdds] = useState("1.90")
  const [noOdds, setNoOdds] = useState("1.90")
  const [created, setCreated] = useState(false)
  const numericYes = Number(yesOdds)
  const numericNo = Number(noOdds)
  const valid = Boolean(scheduledAt) && numericYes > 1 && numericNo > 1

  const create = () => {
    if (!valid) return

    const displayTime = scheduledAt.replace("T", " · ")
    const createdRound: AdminRound = {
      id: crypto.randomUUID(),
      eventType,
      scheduledAt: displayTime,
      closesAt: `${displayTime} − 5 хв`,
      resolvesAt: `${displayTime} + 5 хв`,
      yesOdds: numericYes,
      noOdds: numericNo,
      status: "OPEN",
      placements: [],
    }

    setRounds([createdRound, ...rounds])
    setCreated(true)
  }

  return (
    <div className="screen bet-admin-v2 bet-create-round">
      <PageHeader title="Новий раунд" back={back} />

      <section className="bet-create-preview">
        <GlobalEventArt eventType={eventType} size={64} />
        <span><small>НОВИЙ EVENT BET</small><strong>{globalEventLabels[eventType]}</strong><em>{scheduledAt ? scheduledAt.replace("T", " · ") : "Час не задано"}</em></span>
      </section>

      <section className="bet-create-form">
        <Field label="Подія">
          <div className="bet-event-art-picker">
            {eventOptions.map((value) => (
              <button key={value} className={eventType === value ? "is-active" : ""} onClick={() => setEventType(value)}>
                <GlobalEventArt eventType={value} size={34} />
                <span>{globalEventLabels[value]}</span>
              </button>
            ))}
          </div>
        </Field>

        <Field label="Час події">
          <input type="datetime-local" value={scheduledAt} onChange={(event) => setScheduledAt(event.target.value)} />
        </Field>

        <div className="bet-two-fields">
          <Field label="Коефіцієнт Так"><input type="number" min="1.01" step="0.01" value={yesOdds} onChange={(event) => setYesOdds(event.target.value)} /></Field>
          <Field label="Коефіцієнт Ні"><input type="number" min="1.01" step="0.01" value={noOdds} onChange={(event) => setNoOdds(event.target.value)} /></Field>
        </div>

        <div className="bet-create-policy">
          <Glyph name="clock" size={16} />
          <span><strong>Політика часу</strong><small>Ставки закриваються за 5 хвилин, підтвердження результату — через 5 хвилин після події.</small></span>
        </div>

        <button className="primary-action" disabled={!valid} onClick={create}><Glyph name="plus" size={16} />Створити раунд</button>
        {created ? <div className="bet-create-success"><Glyph name="check" size={15} />Раунд додано до поточних</div> : null}
      </section>
    </div>
  )
}

function RaceManagementPage({ races, setRaces, back }: {
  races: AdminRace[]
  setRaces: (races: AdminRace[]) => void
  back: () => void
}) {
  const [itemName, setItemName] = useState("")
  const [itemType, setItemType] = useState<BetItemType>("SWORD")
  const [entry, setEntry] = useState("1000")
  const [hours, setHours] = useState("24")
  const numericEntry = Number(entry)
  const numericHours = Number(hours)
  const valid = Boolean(itemName.trim())
    && Number.isInteger(numericEntry)
    && numericEntry >= 1000
    && numericEntry <= 5000
    && Number.isInteger(numericHours)
    && numericHours >= 1
    && numericHours <= 168

  const create = () => {
    if (!valid) return

    setRaces([
      {
        id: crypto.randomUUID(),
        itemName: itemName.trim(),
        itemType,
        entry: numericEntry,
        closesAt: `через ${numericHours} год`,
        participants: 0,
        totalPool: 0,
        status: "OPEN",
      },
      ...races,
    ])
    setItemName("")
  }

  return (
    <div className="screen bet-admin-v2 bet-races-v2">
      <PageHeader title="Item race" back={back} />

      <section className="bet-race-builder-v2">
        <header><span><small>СТВОРЕННЯ</small><strong>Новий перегон</strong></span></header>
        <div className="bet-race-preview-v2">
          <ItemTypeArt itemType={itemType} size={58} />
          <span><small>LIVE PREVIEW</small><strong>{itemName.trim() || "Назва предмета"}</strong><em>{itemTypeLabels[itemType]}</em></span>
          <span><small>Вхід</small><CurrencyValue currency="COIN" value={Number.isFinite(numericEntry) ? numericEntry : 0} compact /></span>
        </div>
        <Field label="Предмет"><input value={itemName} maxLength={24} onChange={(event) => setItemName(event.target.value)} placeholder="Eternal Blade" /></Field>
        <Field label="Тип">
          <div className="item-type-art-picker-v2">
            {BET_ITEM_TYPES.map((value) => (
              <button key={value} className={itemType === value ? "is-active" : ""} onClick={() => setItemType(value)}>
                <ItemTypeArt itemType={value} size={32} />
                <span>{itemTypeLabels[value]}</span>
              </button>
            ))}
          </div>
        </Field>
        <div className="bet-two-fields">
          <Field label="Тривалість, год"><input type="number" min="1" max="168" step="1" value={hours} onChange={(event) => setHours(event.target.value)} /></Field>
          <Field label="Вхід"><input type="number" min="1000" max="5000" step="100" value={entry} onChange={(event) => setEntry(event.target.value)} /></Field>
        </div>
        <button className="primary-action" disabled={!valid} onClick={create}><Glyph name="plus" size={16} />Створити перегон</button>
      </section>

      <section className="bet-race-list-v2">
        <header><span><small>АКТИВНІ</small><strong>Перегони</strong></span><b>{races.length}</b></header>
        {races.map((race) => (
          <article key={race.id}>
            <ItemTypeArt itemType={race.itemType} size={42} />
            <span><strong>{race.itemName}</strong><small>{itemTypeLabels[race.itemType]} · {race.participants} учасників</small><em>{race.closesAt}</em></span>
            <span><CurrencyValue currency="COIN" value={race.totalPool} compact /><small>банк</small></span>
            <b className={`status-${race.status.toLowerCase()}`}>{race.status}</b>
          </article>
        ))}
      </section>
    </div>
  )
}

function RoundSummaryRow({ round, onClick }: { round: AdminRound; onClick: () => void }) {
  const totalPool = round.placements.reduce((sum, placement) => sum + placement.amount, 0)

  return (
    <button className="bet-admin-round-v2" onClick={onClick}>
      <GlobalEventArt eventType={round.eventType} size={44} />
      <span className="bet-admin-round-v2__copy">
        <span><strong>{globalEventLabels[round.eventType]}</strong><i className={`status-${round.status.toLowerCase()}`}>{statusLabels[round.status]}</i></span>
        <small>{round.scheduledAt}</small>
        <em>{round.placements.length} ставок · банк {totalPool.toLocaleString("uk-UA")}</em>
      </span>
      <span className="bet-admin-round-v2__odds"><small>Так <b>{round.yesOdds}</b></small><small>Ні <b>{round.noOdds}</b></small></span>
      {round.result ? <span className={`bet-admin-round-v2__result result-${round.result.toLowerCase()}`}>{outcomeLabels[round.result]}</span> : <b className="bet-admin-round-v2__chevron">›</b>}
    </button>
  )
}
