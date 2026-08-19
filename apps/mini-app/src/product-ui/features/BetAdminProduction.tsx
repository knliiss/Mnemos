import { useCallback, useEffect, useMemo, useState } from "react"
import {
  listEventBetRounds,
  listItemRaces,
  type EventBetOutcome,
  type EventBetRoundResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import {
  cancelItemRace,
  createItemRace,
  resolveEventBetRound,
} from "@/shared/api/betAdminApi"
import { handleError } from "@/shared/errors/errorHandler"
import { GlobalEventArt, ItemTypeArt } from "../components/EventArt"
import { BET_ITEM_TYPES, EVENT_BET_OUTCOMES } from "../contracts"
import type {
  BetItemType,
  Route,
} from "../model"
import { globalEventLabels, itemTypeLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, Field, PageHeader } from "../shared/Primitives"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type Section = "home" | "rounds" | "history" | "round" | "races"

const outcomeLabels: Record<EventBetOutcome, string> = {
  YES: "Так",
  NO: "Ні",
  REFUND: "Повернення",
}

export function BetAdminProduction({ params, navigate, back }: Props) {
  const [rounds, setRounds] = useState<EventBetRoundResponse[]>([])
  const [races, setRaces] = useState<ItemRaceResponse[]>([])
  const [loading, setLoading] = useState(true)
  const section = normalizeSection(params?.section)

  const refresh = useCallback(async () => {
    try {
      const [nextRounds, nextRaces] = await Promise.all([
        listEventBetRounds(),
        listItemRaces(),
      ])
      setRounds(nextRounds)
      setRaces(nextRaces)
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refresh])

  if (section === "round") {
    const round = rounds.find((item) => item.id === params?.roundId) ?? null
    return (
      <RoundDetail
        round={round}
        loading={loading}
        back={back}
        onResolved={refresh}
      />
    )
  }

  if (section === "rounds") {
    return (
      <RoundList
        title="Поточні раунди"
        rounds={rounds.filter((round) => round.status !== "SETTLED")}
        loading={loading}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (section === "history") {
    return (
      <RoundList
        title="Історія ставок"
        rounds={rounds.filter((round) => round.status === "SETTLED")}
        loading={loading}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (section === "races") {
    return (
      <RaceManagement
        races={races}
        loading={loading}
        back={back}
        onChanged={refresh}
      />
    )
  }

  const active = rounds.filter((round) => round.status !== "SETTLED")
  const history = rounds.filter((round) => round.status === "SETTLED")
  const activeRaces = races.filter((race) => !["SETTLED", "REFUNDED", "CANCELLED"].includes(race.status))
  const participants = new Set(races.flatMap((race) => race.participants.map((participant) => participant.accountId))).size

  return (
    <div className="screen bet-admin-v2 bet-admin-dashboard">
      <PageHeader title="Ставки" back={back} />

      <section className="bet-admin-v2-hero">
        <span className="bet-admin-v2-hero__icon"><Glyph name="bet" size={22} /></span>
        <span>
          <small>ADMIN WORKSPACE</small>
          <strong>Керування ставками</strong>
          <em>Стан раундів і перегонів завантажується напряму з bet-service.</em>
        </span>
      </section>

      {loading ? <LoadingState /> : (
        <>
          <section className="bet-admin-kpis">
            <article><small>Активні</small><strong>{active.length}</strong><em>раунди</em></article>
            <article><small>Історія</small><strong>{history.length}</strong><em>завершено</em></article>
            <article><small>Перегони</small><strong>{activeRaces.length}</strong><em>активні</em></article>
            <article><small>Гравці</small><strong>{participants}</strong><em>у race</em></article>
          </section>

          <nav className="bet-admin-nav-grid">
            <button onClick={() => navigate({ id: "bet-admin", params: { section: "rounds" } })}>
              <span className="is-lime"><Glyph name="events" size={20} /></span>
              <strong>Поточні раунди</strong>
              <small>Статус і ручне підтвердження результату</small>
              <b>{active.length} ›</b>
            </button>
            <button onClick={() => navigate({ id: "bet-admin", params: { section: "history" } })}>
              <span className="is-plum"><Glyph name="clock" size={20} /></span>
              <strong>Історія ставок</strong>
              <small>Завершені раунди та їх результат</small>
              <b>{history.length} ›</b>
            </button>
            <button onClick={() => navigate({ id: "bet-admin", params: { section: "races" } })}>
              <span className="is-blue"><Glyph name="item-drop" size={20} /></span>
              <strong>Item race</strong>
              <small>Створення, учасники та скасування</small>
              <b>{activeRaces.length} ›</b>
            </button>
          </nav>

          <section className="bet-admin-recent">
            <header><span><small>ПОТРЕБУЮТЬ УВАГИ</small><strong>Раунди</strong></span></header>
            {active.slice(0, 4).map((round) => (
              <RoundRow
                key={round.id}
                round={round}
                onClick={() => navigate({ id: "bet-admin", params: { section: "round", roundId: round.id } })}
              />
            ))}
            {!active.length ? <p className="inline-hint inline-hint--left">Активних раундів немає</p> : null}
          </section>
        </>
      )}
    </div>
  )
}

function RoundList({
  title,
  rounds,
  loading,
  navigate,
  back,
}: {
  title: string
  rounds: EventBetRoundResponse[]
  loading: boolean
  navigate: (route: Route) => void
  back: () => void
}) {
  const [query, setQuery] = useState("")
  const visible = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    if (!normalized) return rounds

    return rounds.filter((round) =>
      globalEventLabels[round.eventType].toLowerCase().includes(normalized)
      || round.status.toLowerCase().includes(normalized),
    )
  }, [query, rounds])

  return (
    <div className="screen bet-admin-v2">
      <PageHeader title={title} back={back} />
      <label className="moderation-polished-search">
        <Glyph name="search" size={17} />
        <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Подія або статус" />
      </label>

      {loading ? <LoadingState /> : (
        <section className="bet-admin-round-stack">
          {visible.map((round) => (
            <RoundRow
              key={round.id}
              round={round}
              onClick={() => navigate({ id: "bet-admin", params: { section: "round", roundId: round.id } })}
            />
          ))}
          {!visible.length ? <p className="inline-hint">Раундів немає</p> : null}
        </section>
      )}
    </div>
  )
}

function RoundDetail({
  round,
  loading,
  back,
  onResolved,
}: {
  round: EventBetRoundResponse | null
  loading: boolean
  back: () => void
  onResolved: () => Promise<void>
}) {
  const [outcome, setOutcome] = useState<EventBetOutcome>("YES")
  const [reason, setReason] = useState("")
  const [busy, setBusy] = useState(false)

  if (loading) {
    return <div className="screen bet-admin-v2"><PageHeader title="Раунд" back={back} /><LoadingState /></div>
  }

  if (!round) {
    return <div className="screen bet-admin-v2"><PageHeader title="Раунд" back={back} /><p className="inline-hint">Раунд не знайдено</p></div>
  }

  const canResolve = round.status === "AWAITING_RESULT" || round.status === "AWAITING_ADMIN_CONFIRMATION"

  const resolve = async () => {
    if (!canResolve || !reason.trim() || busy) return
    setBusy(true)

    try {
      await resolveEventBetRound(round.id, outcome, reason.trim())
      await onResolved()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="screen bet-admin-v2">
      <PageHeader title={globalEventLabels[round.eventType]} back={back} />

      <section className="bet-admin-round-detail-hero">
        <GlobalEventArt eventType={round.eventType} size={72} />
        <span>
          <small>{round.status}</small>
          <strong>{formatDate(round.scheduledAt)}</strong>
          <em>закриття {formatDate(round.closesAt)}</em>
        </span>
      </section>

      <section className="bet-admin-kpis">
        <article><small>Так</small><strong>× {round.yesOdds.toFixed(2)}</strong></article>
        <article><small>Ні</small><strong>× {round.noOdds.toFixed(2)}</strong></article>
        <article><small>Результат</small><strong>{round.outcome ? outcomeLabels[round.outcome] : "—"}</strong></article>
      </section>

      {canResolve ? (
        <section className="creator-r2-form-block">
          <header><small>MANUAL RESOLUTION</small><strong>Підтвердження результату</strong></header>
          <div className="bets-r2-side-choice">
            {EVENT_BET_OUTCOMES.map((value) => (
              <button key={value} className={outcome === value ? "is-active" : ""} onClick={() => setOutcome(value)}>
                <strong>{outcomeLabels[value]}</strong>
              </button>
            ))}
          </div>
          <Field label="Причина">
            <textarea value={reason} maxLength={500} onChange={(event) => setReason(event.target.value)} placeholder="Чому результат підтверджується вручну" />
          </Field>
          <button className="primary-action" disabled={!reason.trim() || busy} onClick={() => void resolve()}>
            {busy ? "Зберігаємо…" : "Підтвердити результат"}
          </button>
        </section>
      ) : null}
    </div>
  )
}

function RaceManagement({
  races,
  loading,
  back,
  onChanged,
}: {
  races: ItemRaceResponse[]
  loading: boolean
  back: () => void
  onChanged: () => Promise<void>
}) {
  const [formOpen, setFormOpen] = useState(false)
  const [itemName, setItemName] = useState("")
  const [itemType, setItemType] = useState<BetItemType>("SWORD")
  const [durationHours, setDurationHours] = useState("24")
  const [entryAmount, setEntryAmount] = useState("1000")
  const [busy, setBusy] = useState(false)

  const duration = Number(durationHours)
  const entry = Number(entryAmount)
  const valid = itemName.trim().length > 0
    && itemName.trim().length <= 24
    && Number.isInteger(duration)
    && duration >= 1
    && duration <= 168
    && Number.isInteger(entry)
    && entry >= 1000
    && entry <= 5000

  const create = async () => {
    if (!valid || busy) return
    setBusy(true)

    try {
      await createItemRace({
        itemName: itemName.trim(),
        itemType,
        durationHours: duration,
        entryAmount: entry,
      })
      setItemName("")
      setDurationHours("24")
      setEntryAmount("1000")
      setFormOpen(false)
      await onChanged()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const cancel = async (raceId: string) => {
    if (busy) return
    setBusy(true)

    try {
      await cancelItemRace(raceId)
      await onChanged()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="screen bet-admin-v2">
      <PageHeader
        title="Item race"
        back={back}
        actions={(
          <button className="bet-admin-header-action" onClick={() => setFormOpen((value) => !value)}>
            <Glyph name={formOpen ? "close" : "plus"} size={15} />
            {formOpen ? "Закрити" : "Новий"}
          </button>
        )}
      />

      {formOpen ? (
        <section className="creator-r2-form-block">
          <header><small>NEW RACE</small><strong>Новий Item race</strong></header>
          <Field label="Предмет"><input value={itemName} maxLength={24} onChange={(event) => setItemName(event.target.value)} /></Field>
          <Field label="Тип">
            <div className="bets-r2-side-choice">
              {BET_ITEM_TYPES.map((value) => (
                <button key={value} className={itemType === value ? "is-active" : ""} onClick={() => setItemType(value)}>
                  {itemTypeLabels[value]}
                </button>
              ))}
            </div>
          </Field>
          <Field label="Тривалість, год"><input type="number" min={1} max={168} value={durationHours} onChange={(event) => setDurationHours(event.target.value)} /></Field>
          <Field label="Вхід"><input type="number" min={1000} max={5000} value={entryAmount} onChange={(event) => setEntryAmount(event.target.value)} /></Field>
          <button className="primary-action" disabled={!valid || busy} onClick={() => void create()}>{busy ? "Створюємо…" : "Створити"}</button>
        </section>
      ) : null}

      {loading ? <LoadingState /> : (
        <section className="bet-admin-race-list">
          {races.map((race) => {
            const pool = race.participants.reduce((sum, participant) => sum + participant.confirmedAmount + participant.pendingAmount, 0)
            const cancellable = race.status === "REGISTRATION" || race.status === "ACTIVE"

            return (
              <article key={race.id} className="bet-admin-race-card">
                <span className="bet-admin-race-card__art"><ItemTypeArt itemType={race.itemType} size={52} /></span>
                <span className="bet-admin-race-card__copy">
                  <small>{itemTypeLabels[race.itemType]}</small>
                  <strong>{race.itemName}</strong>
                  <em>{race.status} · {race.participants.length} учасн.</em>
                </span>
                <span className="bet-admin-race-card__money">
                  <CurrencyValue currency="COIN" value={pool} compact />
                  <small>банк</small>
                </span>
                {cancellable ? <button disabled={busy} onClick={() => void cancel(race.id)}>Скасувати</button> : null}
              </article>
            )
          })}
          {!races.length ? <p className="inline-hint">Перегонів немає</p> : null}
        </section>
      )}
    </div>
  )
}

function RoundRow({ round, onClick }: { round: EventBetRoundResponse; onClick: () => void }) {
  return (
    <button className="bet-admin-round-row" onClick={onClick}>
      <span><GlobalEventArt eventType={round.eventType} size={48} /></span>
      <span>
        <small>{globalEventLabels[round.eventType]}</small>
        <strong>{round.status}</strong>
        <em>{formatDate(round.scheduledAt)}</em>
      </span>
      <span>
        <small>ТАК / НІ</small>
        <strong>{round.yesOdds.toFixed(2)} / {round.noOdds.toFixed(2)}</strong>
      </span>
      <b>›</b>
    </button>
  )
}

function LoadingState() {
  return <div className="events-r2-loading" aria-label="Завантаження"><i /><i /><i /></div>
}

function formatDate(value: string) {
  return new Intl.DateTimeFormat("uk-UA", {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value))
}

function normalizeSection(value?: string): Section {
  if (value === "rounds" || value === "history" || value === "round" || value === "races") return value
  return "home"
}
