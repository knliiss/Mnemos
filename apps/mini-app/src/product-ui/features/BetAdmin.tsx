import { useCallback, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  listEventBetRounds,
  listItemRaces,
  type EventBetOutcome,
  type EventBetRoundResponse,
  type ItemRaceResponse,
} from "@/shared/api/betApi"
import { cancelItemRace, createItemRace, resolveEventBetRound } from "@/shared/api/betAdminApi"
import { handleError } from "@/shared/errors/errorHandler"
import { GlobalEventArt, ItemTypeArt } from "../components/EventArt"
import { BET_ITEM_TYPES, EVENT_BET_OUTCOMES } from "../contracts"
import type { BetGlobalEventType, BetItemType, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, Field, PageHeader } from "../shared/Primitives"

type Props = {
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type Section = "home" | "rounds" | "history" | "round" | "races"

export function BetAdmin({ params, navigate, back }: Props) {
  const { t } = useTranslation()
  const [rounds, setRounds] = useState<EventBetRoundResponse[]>([])
  const [races, setRaces] = useState<ItemRaceResponse[]>([])
  const [loading, setLoading] = useState(true)
  const section = normalizeSection(params?.section)

  const refresh = useCallback(async () => {
    try {
      const [nextRounds, nextRaces] = await Promise.all([listEventBetRounds(), listItemRaces()])
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
    return <RoundDetail round={round} loading={loading} back={back} onResolved={refresh} />
  }

  if (section === "rounds") {
    return (
      <RoundList
        title={t("betAdmin.currentRounds")}
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
        title={t("betAdmin.historyTitle")}
        rounds={rounds.filter((round) => round.status === "SETTLED")}
        loading={loading}
        navigate={navigate}
        back={back}
      />
    )
  }

  if (section === "races") {
    return <RaceManagement races={races} loading={loading} back={back} onChanged={refresh} />
  }

  const active = rounds.filter((round) => round.status !== "SETTLED")
  const history = rounds.filter((round) => round.status === "SETTLED")
  const activeRaces = races.filter(
    (race) => !["SETTLED", "REFUNDED", "CANCELLED"].includes(race.status),
  )
  const participants = new Set(
    races.flatMap((race) => race.participants.map((participant) => participant.accountId)),
  ).size

  return (
    <div className="screen bet-admin-v2 bet-admin-dashboard">
      <PageHeader title={t("betAdmin.title")} back={back} />

      <section className="bet-admin-v2-hero">
        <span className="bet-admin-v2-hero__icon">
          <Glyph name="bet" size={22} />
        </span>
        <span>
          <small>ADMIN WORKSPACE</small>
          <strong>{t("betAdmin.workspaceTitle")}</strong>
          <em>{t("betAdmin.workspaceDescription")}</em>
        </span>
      </section>

      {loading ? (
        <LoadingState />
      ) : (
        <>
          <section className="bet-admin-kpis">
            <article>
              <small>{t("betAdmin.active")}</small>
              <strong>{active.length}</strong>
              <em>{t("betAdmin.rounds")}</em>
            </article>
            <article>
              <small>{t("betAdmin.history")}</small>
              <strong>{history.length}</strong>
              <em>{t("betAdmin.completed")}</em>
            </article>
            <article>
              <small>{t("betAdmin.races")}</small>
              <strong>{activeRaces.length}</strong>
              <em>{t("betAdmin.activeLabel")}</em>
            </article>
            <article>
              <small>{t("betAdmin.players")}</small>
              <strong>{participants}</strong>
              <em>{t("betAdmin.inRace")}</em>
            </article>
          </section>

          <nav className="bet-admin-nav-grid">
            <button onClick={() => navigate({ id: "bet-admin", params: { section: "rounds" } })}>
              <span className="is-lime">
                <Glyph name="events" size={20} />
              </span>
              <strong>{t("betAdmin.currentRounds")}</strong>
              <small>{t("betAdmin.currentRoundsDescription")}</small>
              <b>{active.length} ›</b>
            </button>
            <button onClick={() => navigate({ id: "bet-admin", params: { section: "history" } })}>
              <span className="is-plum">
                <Glyph name="clock" size={20} />
              </span>
              <strong>{t("betAdmin.historyTitle")}</strong>
              <small>{t("betAdmin.historyDescription")}</small>
              <b>{history.length} ›</b>
            </button>
            <button onClick={() => navigate({ id: "bet-admin", params: { section: "races" } })}>
              <span className="is-blue">
                <Glyph name="item-drop" size={20} />
              </span>
              <strong>{t("betAdmin.itemRace")}</strong>
              <small>{t("betAdmin.itemRaceDescription")}</small>
              <b>{activeRaces.length} ›</b>
            </button>
          </nav>

          <section className="bet-admin-recent">
            <header>
              <span>
                <small>{t("betAdmin.needsAttention")}</small>
                <strong>{t("betAdmin.rounds")}</strong>
              </span>
            </header>
            {active.slice(0, 4).map((round) => (
              <RoundRow
                key={round.id}
                round={round}
                onClick={() =>
                  navigate({ id: "bet-admin", params: { section: "round", roundId: round.id } })
                }
              />
            ))}
            {!active.length ? (
              <p className="inline-hint inline-hint--left">{t("betAdmin.noActiveRounds")}</p>
            ) : null}
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
  const { t } = useTranslation()
  const eventLabels = useEventLabels()
  const [query, setQuery] = useState("")
  const visible = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    if (!normalized) return rounds

    return rounds.filter(
      (round) =>
        eventLabels[round.eventType].toLowerCase().includes(normalized) ||
        round.status.toLowerCase().includes(normalized),
    )
  }, [eventLabels, query, rounds])

  return (
    <div className="screen bet-admin-v2">
      <PageHeader title={title} back={back} />
      <label className="moderation-polished-search">
        <Glyph name="search" size={17} />
        <input
          value={query}
          onChange={(event) => setQuery(event.target.value)}
          placeholder={t("betAdmin.searchPlaceholder")}
        />
      </label>

      {loading ? (
        <LoadingState />
      ) : (
        <section className="bet-admin-round-stack">
          {visible.map((round) => (
            <RoundRow
              key={round.id}
              round={round}
              onClick={() =>
                navigate({ id: "bet-admin", params: { section: "round", roundId: round.id } })
              }
            />
          ))}
          {!visible.length ? <p className="inline-hint">{t("betAdmin.noRounds")}</p> : null}
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
  const { t, i18n } = useTranslation()
  const eventLabels = useEventLabels()
  const outcomeLabels = useOutcomeLabels()
  const [outcome, setOutcome] = useState<EventBetOutcome>("YES")
  const [reason, setReason] = useState("")
  const [busy, setBusy] = useState(false)

  if (loading) {
    return (
      <div className="screen bet-admin-v2">
        <PageHeader title={t("betAdmin.round")} back={back} />
        <LoadingState />
      </div>
    )
  }

  if (!round) {
    return (
      <div className="screen bet-admin-v2">
        <PageHeader title={t("betAdmin.round")} back={back} />
        <p className="inline-hint">{t("betAdmin.roundNotFound")}</p>
      </div>
    )
  }

  const canResolve =
    round.status === "AWAITING_RESULT" || round.status === "AWAITING_ADMIN_CONFIRMATION"

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
      <PageHeader title={eventLabels[round.eventType]} back={back} />

      <section className="bet-admin-round-detail-hero">
        <GlobalEventArt eventType={round.eventType} size={72} />
        <span>
          <small>{round.status}</small>
          <strong>{formatDate(round.scheduledAt, i18n.language)}</strong>
          <em>{t("betAdmin.closing", { date: formatDate(round.closesAt, i18n.language) })}</em>
        </span>
      </section>

      <section className="bet-admin-kpis">
        <article>
          <small>{t("betAdmin.yes")}</small>
          <strong>× {round.yesOdds.toFixed(2)}</strong>
        </article>
        <article>
          <small>{t("betAdmin.no")}</small>
          <strong>× {round.noOdds.toFixed(2)}</strong>
        </article>
        <article>
          <small>{t("betAdmin.result")}</small>
          <strong>{round.outcome ? outcomeLabels[round.outcome] : "—"}</strong>
        </article>
      </section>

      {canResolve ? (
        <section className="creator-r2-form-block">
          <header>
            <small>MANUAL RESOLUTION</small>
            <strong>{t("betAdmin.manualResolution")}</strong>
          </header>
          <div className="bets-r2-side-choice">
            {EVENT_BET_OUTCOMES.map((value) => (
              <button
                key={value}
                className={outcome === value ? "is-active" : ""}
                onClick={() => setOutcome(value)}
              >
                <strong>{outcomeLabels[value]}</strong>
              </button>
            ))}
          </div>
          <Field label={t("betAdmin.reason")}>
            <textarea
              value={reason}
              maxLength={500}
              onChange={(event) => setReason(event.target.value)}
              placeholder={t("betAdmin.reasonPlaceholder")}
            />
          </Field>
          <button
            className="primary-action"
            disabled={!reason.trim() || busy}
            onClick={() => void resolve()}
          >
            {busy ? t("betAdmin.saving") : t("betAdmin.confirmResult")}
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
  const { t } = useTranslation()
  const itemLabels = useItemLabels()
  const [formOpen, setFormOpen] = useState(false)
  const [itemName, setItemName] = useState("")
  const [itemType, setItemType] = useState<BetItemType>("SWORD")
  const [durationHours, setDurationHours] = useState("24")
  const [entryAmount, setEntryAmount] = useState("1000")
  const [busy, setBusy] = useState(false)

  const duration = Number(durationHours)
  const entry = Number(entryAmount)
  const valid =
    itemName.trim().length > 0 &&
    itemName.trim().length <= 24 &&
    Number.isInteger(duration) &&
    duration >= 1 &&
    duration <= 168 &&
    Number.isInteger(entry) &&
    entry >= 1000 &&
    entry <= 5000

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
        title={t("betAdmin.itemRace")}
        back={back}
        actions={
          <button
            className="bet-admin-header-action"
            onClick={() => setFormOpen((value) => !value)}
          >
            <Glyph name={formOpen ? "close" : "plus"} size={15} />
            {formOpen ? t("betAdmin.close") : t("betAdmin.new")}
          </button>
        }
      />

      {formOpen ? (
        <section className="creator-r2-form-block">
          <header>
            <small>NEW RACE</small>
            <strong>{t("betAdmin.newRace")}</strong>
          </header>
          <Field label={t("betAdmin.item")}>
            <input
              value={itemName}
              maxLength={24}
              onChange={(event) => setItemName(event.target.value)}
            />
          </Field>
          <Field label={t("betAdmin.type")}>
            <div className="bets-r2-side-choice">
              {BET_ITEM_TYPES.map((value) => (
                <button
                  key={value}
                  className={itemType === value ? "is-active" : ""}
                  onClick={() => setItemType(value)}
                >
                  {itemLabels[value]}
                </button>
              ))}
            </div>
          </Field>
          <Field label={t("betAdmin.durationHours")}>
            <input
              type="number"
              min={1}
              max={168}
              value={durationHours}
              onChange={(event) => setDurationHours(event.target.value)}
            />
          </Field>
          <Field label={t("betAdmin.entry")}>
            <input
              type="number"
              min={1000}
              max={5000}
              value={entryAmount}
              onChange={(event) => setEntryAmount(event.target.value)}
            />
          </Field>
          <button
            className="primary-action"
            disabled={!valid || busy}
            onClick={() => void create()}
          >
            {busy ? t("betAdmin.creating") : t("betAdmin.create")}
          </button>
        </section>
      ) : null}

      {loading ? (
        <LoadingState />
      ) : (
        <section className="bet-admin-race-list">
          {races.map((race) => {
            const pool = race.participants.reduce(
              (sum, participant) => sum + participant.confirmedAmount + participant.pendingAmount,
              0,
            )
            const cancellable = race.status === "REGISTRATION" || race.status === "ACTIVE"

            return (
              <article key={race.id} className="bet-admin-race-card">
                <span className="bet-admin-race-card__art">
                  <ItemTypeArt itemType={race.itemType} size={52} />
                </span>
                <span className="bet-admin-race-card__copy">
                  <small>{itemLabels[race.itemType]}</small>
                  <strong>{race.itemName}</strong>
                  <em>
                    {race.status} ·{" "}
                    {t("betAdmin.participantsShort", { count: race.participants.length })}
                  </em>
                </span>
                <span className="bet-admin-race-card__money">
                  <CurrencyValue currency="COIN" value={pool} compact />
                  <small>{t("betAdmin.bank")}</small>
                </span>
                {cancellable ? (
                  <button disabled={busy} onClick={() => void cancel(race.id)}>
                    {t("betAdmin.cancel")}
                  </button>
                ) : null}
              </article>
            )
          })}
          {!races.length ? <p className="inline-hint">{t("betAdmin.noRaces")}</p> : null}
        </section>
      )}
    </div>
  )
}

function RoundRow({ round, onClick }: { round: EventBetRoundResponse; onClick: () => void }) {
  const { t, i18n } = useTranslation()
  const eventLabels = useEventLabels()

  return (
    <button className="bet-admin-round-row" onClick={onClick}>
      <span>
        <GlobalEventArt eventType={round.eventType} size={48} />
      </span>
      <span>
        <small>{eventLabels[round.eventType]}</small>
        <strong>{round.status}</strong>
        <em>{formatDate(round.scheduledAt, i18n.language)}</em>
      </span>
      <span>
        <small>{t("betAdmin.yesNo")}</small>
        <strong>
          {round.yesOdds.toFixed(2)} / {round.noOdds.toFixed(2)}
        </strong>
      </span>
      <b>›</b>
    </button>
  )
}

function LoadingState() {
  const { t } = useTranslation()

  return (
    <div className="events-r2-loading" aria-label={t("betAdmin.loading")}>
      <i />
      <i />
      <i />
    </div>
  )
}

function useEventLabels(): Record<BetGlobalEventType, string> {
  const { t } = useTranslation()

  return {
    MOON: t("betAdmin.eventMoon"),
    ECLIPSE: t("betAdmin.eventEclipse"),
    EXPLOSION: t("betAdmin.eventExplosion"),
  }
}

function useItemLabels(): Record<BetItemType, string> {
  const { t } = useTranslation()

  return {
    SWORD: t("betAdmin.itemSword"),
    AURA: t("betAdmin.itemAura"),
    PET: t("betAdmin.itemPet"),
    BOOK: t("betAdmin.itemBook"),
    JEWELRY: t("betAdmin.itemJewelry"),
  }
}

function useOutcomeLabels(): Record<EventBetOutcome, string> {
  const { t } = useTranslation()

  return {
    YES: t("betAdmin.yes"),
    NO: t("betAdmin.no"),
    REFUND: t("betAdmin.refund"),
  }
}

function formatDate(value: string, language: string) {
  const locale = language.startsWith("ru") ? "ru-RU" : language.startsWith("en") ? "en-US" : "uk-UA"

  return new Intl.DateTimeFormat(locale, {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value))
}

function normalizeSection(value?: string): Section {
  if (value === "rounds" || value === "history" || value === "round" || value === "races")
    return value
  return "home"
}
