import { AnimatePresence, motion } from "framer-motion"
import { useMemo, useState } from "react"
import { EventArt, GlobalEventArt, ItemTypeArt, OwnerCrown } from "../components/EventArt"
import { BET_SIDES, EVENT_KINDS } from "../contracts"
import { eventVisualTone, globalEventVisualTone } from "../eventVisualTone"
import { betRounds as seedRounds, feed, initialSubscriptions, itemRaces as seedRaces } from "../mockData"
import type {
  Account,
  BetRound,
  BetSide,
  EventKind,
  FeedEvent,
  ItemRace,
  Route,
} from "../model"
import {
  betStatusLabels,
  eventLabels,
  globalEventLabels,
  itemTypeLabels,
} from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { BottomSheet, CurrencyValue, Field } from "../shared/Primitives"
import { formatOccurredAt } from "../timePresentation"
import { EventDetail } from "./EventsHomeFeature"
import { SubscriptionsPage } from "./SubscriptionsFeature"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  activeAccount: Account
  navigate: (route: Route) => void
  back: () => void
}

type EventFilter = "ALL" | EventKind

type BetMode = "EVENTS" | "MY" | "RACES"

export function EventsArea(props: Props) {
  if (props.routeId === "bets") {
    return <BetsRethink activeAccount={props.activeAccount} />
  }

  if (props.routeId === "event-detail") {
    return (
      <EventDetail
        eventId={props.params?.eventId}
        activeAccount={props.activeAccount}
        back={props.back}
      />
    )
  }

  if (props.routeId === "subscriptions") {
    return <SubscriptionsPage back={props.back} />
  }

  return (
    <EventsRethink
      activeAccount={props.activeAccount}
      navigate={props.navigate}
    />
  )
}

function EventsRethink({ activeAccount, navigate }: { activeAccount: Account; navigate: (route: Route) => void }) {
  const [filter, setFilter] = useState<EventFilter>("ALL")
  const [query, setQuery] = useState("")

  const visible = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    return feed
      .filter((event) => filter === "ALL" || event.kind === filter)
      .filter((event) => {
        if (!normalized) return true

        return [event.title, event.detail, event.meta, event.rarity, eventLabels[event.kind]]
          .filter(Boolean)
          .join(" ")
          .toLowerCase()
          .includes(normalized)
      })
      .sort((left, right) => Date.parse(right.occurredAt) - Date.parse(left.occurredAt))
  }, [filter, query])

  const featured = visible[0]
  const rest = visible.slice(1)

  return (
    <div className="screen events-r2-screen root-screen">
      <header className="root-title-block events-r2-title">
        <span>
          <small>LIVE FEED</small>
          <h1>Події</h1>
          <p>Що відбувається на Cristalix просто зараз</p>
        </span>
        <button className="events-r2-subscriptions" onClick={() => navigate({ id: "subscriptions" })}>
          <Glyph name="bell" size={17} />
          <b>{initialSubscriptions.length}</b>
        </button>
      </header>

      <section className="events-r2-filter-rail" aria-label="Тип подій">
        <button className={filter === "ALL" ? "is-active" : ""} onClick={() => setFilter("ALL")}>
          <span className="events-r2-filter-dot" />
          Усі
        </button>
        {EVENT_KINDS.map((kind) => (
          <button key={kind} className={filter === kind ? "is-active" : ""} onClick={() => setFilter(kind)}>
            <Glyph name={eventGlyph(kind)} size={15} />
            {eventLabels[kind]}
          </button>
        ))}
      </section>

      <label className="events-r2-search">
        <Glyph name="search" size={18} />
        <input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Предмет, нік або подія" />
        {query ? <button onClick={() => setQuery("")} aria-label="Очистити"><Glyph name="close" size={14} /></button> : null}
      </label>

      {featured ? (
        <motion.button
          className={`events-r2-featured event-surface--${eventVisualTone(featured)}`}
          whileTap={{ scale: 0.992 }}
          onClick={() => navigate({ id: "event-detail", params: { eventId: featured.id } })}
        >
          <span className="events-r2-featured__signal"><i /><i /><i /></span>
          <span className="events-r2-featured__art"><EventArt event={featured} size={78} /></span>
          <span className="events-r2-featured__copy">
            <small>ОСТАННЄ · {formatOccurredAt(featured.occurredAt)}</small>
            <strong>{eventDisplayTitle(featured)}</strong>
            <em>{eventDisplayDescription(featured)}</em>
            <span>{eventLabels[featured.kind]}{belongsToAccount(featured, activeAccount) ? <OwnerCrown compact /> : null}</span>
          </span>
          <b className="events-r2-featured__open">›</b>
        </motion.button>
      ) : null}

      <header className="events-r2-section-head">
        <span><small>СТРІЧКА</small><strong>{visible.length} подій</strong></span>
        <i />
      </header>

      <section className="events-r2-stream">
        <AnimatePresence initial={false}>
          {rest.map((event, index) => (
            <motion.button
              layout
              key={event.id}
              className={`events-r2-row event-surface--${eventVisualTone(event)}`}
              initial={{ opacity: 0, y: 5 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.985 }}
              transition={{ delay: Math.min(index * 0.025, 0.1) }}
              onClick={() => navigate({ id: "event-detail", params: { eventId: event.id } })}
            >
              <span className="events-r2-row__art"><EventArt event={event} size={48} /></span>
              <span className="events-r2-row__copy">
                <small>{eventLabels[event.kind]}</small>
                <strong>{eventDisplayTitle(event)}</strong>
                <em>{eventDisplayDescription(event)}</em>
              </span>
              <span className="events-r2-row__time">{formatOccurredAt(event.occurredAt)}</span>
              <span className="events-r2-row__edge" />
            </motion.button>
          ))}
        </AnimatePresence>
      </section>

      {!visible.length ? (
        <section className="events-r2-empty">
          <Glyph name="events" size={24} />
          <strong>Подій не знайдено</strong>
          <small>Зміни тип або пошуковий запит</small>
        </section>
      ) : null}
    </div>
  )
}

function BetsRethink({ activeAccount }: { activeAccount: Account }) {
  const [mode, setMode] = useState<BetMode>("EVENTS")
  const [rounds, setRounds] = useState(seedRounds)
  const [races, setRaces] = useState<ItemRace[]>(seedRaces)
  const [selectedRound, setSelectedRound] = useState<BetRound | null>(null)
  const [selectedRace, setSelectedRace] = useState<ItemRace | null>(null)
  const [side, setSide] = useState<BetSide>("YES")
  const [amount, setAmount] = useState("500")

  const featured = rounds.find((round) => round.status === "OPEN") ?? rounds[0]
  const eventRounds = rounds.filter((round) => round.id !== featured.id)
  const myRounds = rounds.filter((round) => round.myAmount)
  const myRaces = races.filter((race) => race.joined)
  const numericAmount = Number(amount)
  const amountValid = Number.isInteger(numericAmount) && numericAmount > 0

  const openRound = (round: BetRound) => {
    setSelectedRound(round)
    setSide(round.mySide ?? "YES")
    setAmount(round.myAmount ? "100" : "500")
  }

  const placeRound = () => {
    if (!selectedRound || !amountValid || selectedRound.status !== "OPEN") return

    const selectedId = selectedRound.id
    const currentAmount = selectedRound.myAmount ?? 0
    const selectedSide = selectedRound.mySide ?? side

    setRounds((current) => current.map((round) => round.id === selectedId
      ? { ...round, myAmount: currentAmount + numericAmount, mySide: selectedSide }
      : round))
    setSelectedRound(null)
  }

  const joinRace = () => {
    if (!selectedRace) return

    setRaces((current) => current.map((race) => race.id === selectedRace.id
      ? { ...race, joined: true, myAmount: race.entryAmount, participants: race.participants + 1 }
      : race))
    setSelectedRace(null)
  }

  return (
    <div className="screen bets-r2-screen root-screen">
      <header className="root-title-block bets-r2-title">
        <span>
          <small>MNEMOS BETS</small>
          <h1>Ставки</h1>
          <p>{activeAccount.nickname}</p>
        </span>
        <button className="bets-r2-wallet"><CurrencyValue currency="COIN" value={activeAccount.coin} compact /></button>
      </header>

      <nav className="bets-r2-mode">
        {(["EVENTS", "MY", "RACES"] as const).map((value) => (
          <button key={value} className={mode === value ? "is-active" : ""} onClick={() => setMode(value)}>
            <span>{value === "EVENTS" ? "Події" : value === "MY" ? "Мої" : "Перегони"}</span>
            {value === "MY" && myRounds.length + myRaces.length ? <b>{myRounds.length + myRaces.length}</b> : null}
          </button>
        ))}
      </nav>

      {mode === "EVENTS" ? (
        <>
          <motion.button
            className={`bets-r2-feature event-surface--${globalEventVisualTone(featured.eventType)}`}
            whileTap={{ scale: 0.992 }}
            onClick={() => openRound(featured)}
          >
            <span className="bets-r2-feature__art"><GlobalEventArt eventType={featured.eventType} size={82} /></span>
            <span className="bets-r2-feature__copy">
              <small>НАСТУПНИЙ РАУНД</small>
              <strong>{globalEventLabels[featured.eventType]}</strong>
              <em>Закриття {featured.closesAt}</em>
            </span>
            <span className="bets-r2-feature__odds">
              <span><small>ТАК</small><strong>{featured.yesOdds}</strong></span>
              <i />
              <span><small>НІ</small><strong>{featured.noOdds}</strong></span>
            </span>
            {featured.myAmount ? <span className="bets-r2-feature__mine"><CurrencyMark currency="COIN" size={13} />{featured.myAmount}</span> : null}
          </motion.button>

          <header className="bets-r2-section-head"><span><small>ЩЕ РАУНДИ</small><strong>{eventRounds.length}</strong></span></header>
          <section className="bets-r2-rounds">
            {eventRounds.map((round) => <BetRoundCard key={round.id} round={round} onClick={() => openRound(round)} />)}
          </section>
        </>
      ) : null}

      {mode === "MY" ? (
        <section className="bets-r2-rounds">
          {myRounds.map((round) => <BetRoundCard key={round.id} round={round} onClick={() => openRound(round)} />)}
          {myRaces.map((race) => <BetRaceCard key={race.id} race={race} onClick={() => setSelectedRace(race)} />)}
          {!myRounds.length && !myRaces.length ? <div className="bets-r2-empty"><Glyph name="bet" size={23} /><strong>Позицій ще немає</strong><small>Відкрий раунд і зроби першу ставку</small></div> : null}
        </section>
      ) : null}

      {mode === "RACES" ? (
        <section className="bets-r2-races">
          {races.map((race) => <BetRaceCard key={race.id} race={race} onClick={() => setSelectedRace(race)} />)}
        </section>
      ) : null}

      <AnimatePresence>
        {selectedRound ? (
          <BottomSheet title={globalEventLabels[selectedRound.eventType]} onClose={() => setSelectedRound(null)}>
            <div className="bets-r2-sheet">
              <div className="bets-r2-sheet__event">
                <GlobalEventArt eventType={selectedRound.eventType} size={56} />
                <span><small>{betStatusLabels[selectedRound.status]}</small><strong>{selectedRound.scheduledAt}</strong><em>до {selectedRound.closesAt}</em></span>
              </div>

              {!selectedRound.myAmount ? (
                <div className="bets-r2-side-choice">
                  {BET_SIDES.map((value) => (
                    <button key={value} className={side === value ? "is-active" : ""} onClick={() => setSide(value)}>
                      <span>{value === "YES" ? "Так" : "Ні"}</span>
                      <strong>× {value === "YES" ? selectedRound.yesOdds : selectedRound.noOdds}</strong>
                    </button>
                  ))}
                </div>
              ) : (
                <div className="bets-r2-current-position"><span><small>Твоя позиція</small><strong>{selectedRound.mySide === "YES" ? "Так" : "Ні"}</strong></span><CurrencyValue currency="COIN" value={selectedRound.myAmount} compact /></div>
              )}

              <Field label={selectedRound.myAmount ? "Збільшити на" : "Сума"}>
                <div className="bets-r2-amount"><CurrencyMark currency="COIN" size={16} /><input type="number" min={1} value={amount} onChange={(event) => setAmount(event.target.value)} /></div>
              </Field>

              <button className="bets-r2-submit" disabled={!amountValid || selectedRound.status !== "OPEN"} onClick={placeRound}>{selectedRound.myAmount ? "Збільшити" : "Поставити"}</button>
            </div>
          </BottomSheet>
        ) : null}

        {selectedRace ? (
          <BottomSheet title={selectedRace.itemName} onClose={() => setSelectedRace(null)}>
            <div className="bets-r2-sheet">
              <div className="bets-r2-sheet__event">
                <ItemTypeArt itemType={selectedRace.itemType} size={56} />
                <span><small>{itemTypeLabels[selectedRace.itemType]}</small><strong>{selectedRace.participants} учасників</strong><em>до {selectedRace.closesAt}</em></span>
              </div>
              <div className="bets-r2-current-position"><span><small>{selectedRace.joined ? "Твоя сума" : "Вхід"}</small><strong>{selectedRace.joined ? "Участь активна" : "Перший дроп виграє"}</strong></span><CurrencyValue currency="COIN" value={selectedRace.joined ? (selectedRace.myAmount ?? selectedRace.entryAmount) : selectedRace.entryAmount} compact /></div>
              {!selectedRace.joined ? <button className="bets-r2-submit" onClick={joinRace}>Приєднатись</button> : null}
            </div>
          </BottomSheet>
        ) : null}
      </AnimatePresence>
    </div>
  )
}

function BetRoundCard({ round, onClick }: { round: BetRound; onClick: () => void }) {
  return (
    <motion.button className={`bets-r2-card event-surface--${globalEventVisualTone(round.eventType)} ${round.status !== "OPEN" ? "is-locked" : ""}`} whileTap={{ scale: 0.992 }} onClick={onClick}>
      <span className="bets-r2-card__art"><GlobalEventArt eventType={round.eventType} size={52} /></span>
      <span className="bets-r2-card__copy"><small>{globalEventLabels[round.eventType]}</small><strong>{betStatusLabels[round.status]}</strong><em>{round.closesAt}</em></span>
      <span className="bets-r2-card__odds"><b>{round.yesOdds}</b><i /><b>{round.noOdds}</b></span>
      {round.myAmount ? <span className="bets-r2-card__mine"><CurrencyMark currency="COIN" size={12} />{round.myAmount}</span> : null}
    </motion.button>
  )
}

function BetRaceCard({ race, onClick }: { race: ItemRace; onClick: () => void }) {
  return (
    <motion.button className="bets-r2-race" whileTap={{ scale: 0.992 }} onClick={onClick}>
      <span className="bets-r2-race__art"><ItemTypeArt itemType={race.itemType} size={54} /></span>
      <span className="bets-r2-race__copy"><small>{itemTypeLabels[race.itemType]}</small><strong>{race.itemName}</strong><em>{race.participants} учасників · до {race.closesAt}</em></span>
      <span className="bets-r2-race__entry"><small>{race.joined ? "ТВОЯ" : "ВХІД"}</small><CurrencyValue currency="COIN" value={race.joined ? (race.myAmount ?? race.entryAmount) : race.entryAmount} compact /></span>
    </motion.button>
  )
}

function belongsToAccount(event: FeedEvent, account: Account) {
  return (event.kind === "ITEM_DROP" || event.kind === "BOOSTER")
    && event.detail.toLowerCase() === account.nickname.toLowerCase()
}

function eventDisplayTitle(event: FeedEvent) {
  if (event.kind === "GLOBAL") return globalEventLabels[event.title as keyof typeof globalEventLabels] ?? event.title
  if (event.kind === "RAID") return "Рейд"
  return event.title
}

function eventDisplayDescription(event: FeedEvent) {
  if (event.kind === "ITEM_DROP") return `${event.detail}${event.rarity ? ` · ${event.rarity === "SECRET" ? "Секретний" : event.rarity === "MYTHICAL" ? "Міфічний" : event.rarity}` : ""}`
  if (event.kind === "GLOBAL") return "Глобальна подія Cristalix"
  if (event.kind === "RAID") return `Локації ${event.detail}`
  return event.detail
}

function eventGlyph(kind: EventKind): "item-drop" | "booster" | "global" | "raid" {
  if (kind === "ITEM_DROP") return "item-drop"
  if (kind === "BOOSTER") return "booster"
  if (kind === "GLOBAL") return "global"
  return "raid"
}
