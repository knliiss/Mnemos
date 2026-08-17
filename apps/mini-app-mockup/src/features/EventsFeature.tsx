import { AnimatePresence } from "framer-motion"
import { useMemo, useState } from "react"
import { betRounds, feed, initialSubscriptions, itemRaces } from "../mockData"
import type { BetRound, BetSide, BoosterType, DungeonType, EventKind, GlobalEventType, Route, Subscription, SubscriptionType } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { BottomSheet, ChoiceGrid, CurrencyValue, Field, IconButton, PageHeader, Segmented, StatusDot } from "../shared/Primitives"

type Nav = (route: Route) => void
const eventLabels: Record<EventKind, string> = { ITEM_DROP: "Drop", BOOSTER: "Booster", GLOBAL: "Global", RAID: "Raid" }
const eventIcons: Record<EventKind, "item-drop" | "booster" | "global" | "raid"> = { ITEM_DROP: "item-drop", BOOSTER: "booster", GLOBAL: "global", RAID: "raid" }

export function EventsHome({ navigate }: { navigate: Nav }) {
  const [filter, setFilter] = useState<"ALL" | EventKind>("ALL")
  const visible = useMemo(() => filter === "ALL" ? feed : feed.filter((event) => event.kind === filter), [filter])
  const latest = visible[0]
  return <div className="screen">
    <PageHeader title="Події" eyebrow="LIVE" actions={<><IconButton icon="search" label="Пошук профілів" onClick={() => navigate({ id: "search" })} /><IconButton icon="bell" label="Підписки" onClick={() => navigate({ id: "subscriptions" })} /><IconButton icon="bet" label="Ставки" onClick={() => navigate({ id: "bets" })} /></>} />
    {latest ? <button className="event-lead" onClick={() => setFilter(latest.kind)}><span className="event-lead__glyph"><Glyph name={eventIcons[latest.kind]} size={31} /></span><span className="event-lead__main"><span className="event-lead__top"><StatusDot tone="live" />{eventLabels[latest.kind]}<time>{latest.relative}</time></span><strong>{latest.title}</strong><span>{latest.detail}{latest.rarity ? ` · ${latest.rarity}` : ""}</span></span></button> : null}
    <div className="event-filter">{(["ALL", "ITEM_DROP", "BOOSTER", "GLOBAL", "RAID"] as const).map((value) => <button key={value} className={filter === value ? "is-active" : ""} onClick={() => setFilter(value)}>{value === "ALL" ? "Усі" : eventLabels[value]}</button>)}</div>
    <div className="event-stream">{visible.map((event, index) => <article className="event-line" key={event.id}><span className="event-line__axis"><span className={index === 0 ? "is-live" : ""} /></span><span className="event-line__glyph"><Glyph name={eventIcons[event.kind]} size={18} /></span><span className="event-line__copy"><strong>{event.title}</strong><small>{event.detail}{event.meta ? ` · ${event.meta}` : ""}</small></span><time>{event.relative}</time></article>)}</div>
  </div>
}

const subscriptionLabels: Record<SubscriptionType, string> = { ITEM_DROP: "Drop", BOOSTER: "Booster", DUNGEON: "Dungeon", RAID: "Raid", GLOBAL: "Global" }
function subscriptionDescription(subscription: Subscription) {
  if (subscription.type === "ITEM_DROP") return [subscription.droppedFor && `player: ${subscription.droppedFor}`, subscription.itemName && `item: ${subscription.itemName}`].filter(Boolean).join(" · ")
  if (subscription.type === "BOOSTER") return subscription.boosterType
  if (subscription.type === "DUNGEON") return subscription.dungeonType
  if (subscription.type === "GLOBAL") return subscription.eventType
  return subscription.locations.length ? `locations: ${subscription.locations.join(", ")}` : "all locations"
}

export function SubscriptionsPage({ back }: { back: () => void }) {
  const [subscriptions, setSubscriptions] = useState(initialSubscriptions)
  const [creating, setCreating] = useState(false)
  const [type, setType] = useState<SubscriptionType>("ITEM_DROP")
  const [droppedFor, setDroppedFor] = useState("")
  const [itemName, setItemName] = useState("")
  const [boosterType, setBoosterType] = useState<BoosterType>("LUCK")
  const [dungeonType, setDungeonType] = useState<DungeonType>("EASY")
  const [globalType, setGlobalType] = useState<GlobalEventType>("ECLIPSE")
  const [raidLocations, setRaidLocations] = useState<number[]>([])
  const [raidInput, setRaidInput] = useState("")
  const addRaidLocation = () => { const value = Number(raidInput); if (Number.isInteger(value) && value > 0 && !raidLocations.includes(value)) setRaidLocations([...raidLocations, value]); setRaidInput("") }
  const create = () => {
    const id = `sub-${Date.now()}`
    let next: Subscription
    if (type === "ITEM_DROP") { if (!droppedFor.trim() && !itemName.trim()) return; next = { id, type, droppedFor: droppedFor.trim() || undefined, itemName: itemName.trim() || undefined } }
    else if (type === "BOOSTER") next = { id, type, boosterType }
    else if (type === "DUNGEON") next = { id, type, dungeonType }
    else if (type === "GLOBAL") next = { id, type, eventType: globalType }
    else next = { id, type, locations: raidLocations }
    setSubscriptions([next, ...subscriptions]); setCreating(false)
  }
  return <div className="screen">
    <PageHeader title="Підписки" back={back} actions={<IconButton icon="plus" label="Додати" onClick={() => setCreating(true)} />} />
    <div className="plain-list">{subscriptions.map((subscription) => <div className="subscription-row" key={subscription.id}><span className="subscription-row__type">{subscriptionLabels[subscription.type]}</span><span className="subscription-row__copy">{subscriptionDescription(subscription)}</span><button className="micro-action micro-action--danger" onClick={() => setSubscriptions(subscriptions.filter((item) => item.id !== subscription.id))}><Glyph name="trash" size={16} /></button></div>)}</div>
    <AnimatePresence>{creating ? <BottomSheet title="Нова підписка" onClose={() => setCreating(false)}><div className="form-stack">
      <Field label="Тип"><ChoiceGrid value={type} options={["ITEM_DROP", "BOOSTER", "DUNGEON", "RAID", "GLOBAL"] as const} onChange={setType} labels={subscriptionLabels} /></Field>
      {type === "ITEM_DROP" ? <><Field label="Гравець" hint="Можна залишити порожнім, якщо вказано item"><input value={droppedFor} onChange={(event) => setDroppedFor(event.target.value)} placeholder="nickname" maxLength={20} /></Field><Field label="Item"><input value={itemName} onChange={(event) => setItemName(event.target.value)} placeholder="item name" maxLength={24} /></Field></> : null}
      {type === "BOOSTER" ? <Field label="Booster"><ChoiceGrid value={boosterType} options={["LUCK", "MONEY", "DAMAGE", "POWER"] as const} onChange={setBoosterType} /></Field> : null}
      {type === "DUNGEON" ? <Field label="Dungeon"><ChoiceGrid value={dungeonType} options={["EASY", "MEDIUM", "HARD", "EXPERT"] as const} onChange={setDungeonType} /></Field> : null}
      {type === "GLOBAL" ? <Field label="Global"><ChoiceGrid value={globalType} options={["DARKNESS", "MOON", "ECLIPSE", "EXPLOSION"] as const} onChange={setGlobalType} /></Field> : null}
      {type === "RAID" ? <Field label="Локації"><div className="inline-input"><input type="number" min="1" value={raidInput} onChange={(event) => setRaidInput(event.target.value)} placeholder="№" /><button type="button" onClick={addRaidLocation}><Glyph name="plus" size={17} /></button></div><div className="token-list">{raidLocations.map((location) => <button key={location} onClick={() => setRaidLocations(raidLocations.filter((value) => value !== location))}>{location}<Glyph name="close" size={12} /></button>)}</div></Field> : null}
      <button className="primary-action" onClick={create}>Створити</button>
    </div></BottomSheet> : null}</AnimatePresence>
  </div>
}

export function BetsPage({ back }: { back: () => void }) {
  const [mode, setMode] = useState<"EVENT" | "RACE">("EVENT")
  const [selectedRound, setSelectedRound] = useState<BetRound | null>(null)
  const [side, setSide] = useState<BetSide>("YES")
  const [amount, setAmount] = useState("500")
  return <div className="screen">
    <PageHeader title="Ставки" back={back} /><Segmented value={mode} options={["EVENT", "RACE"] as const} onChange={setMode} label={(value) => value === "EVENT" ? "Події" : "Item race"} />
    {mode === "EVENT" ? <div className="bet-list">{betRounds.map((round) => <button className="bet-row" key={round.id} onClick={() => { setSelectedRound(round); setSide(round.mySide ?? "YES"); setAmount(round.myAmount?.toString() ?? "500") }}><span className="bet-row__event"><Glyph name="global" size={20} /><strong>{round.eventType}</strong><small>{round.status}</small></span><span className="bet-odds"><span>YES <b>{round.yesOdds}</b></span><span>NO <b>{round.noOdds}</b></span></span><time>{round.closesAt}</time></button>)}</div> : <div className="bet-list">{itemRaces.map((race) => <article className="race-row" key={race.id}><div><strong>{race.itemName}</strong><small>{race.itemType} · {race.participants} participants</small></div><div className="race-row__entry"><CurrencyValue currency="COIN" value={race.entryAmount} compact />{race.joined ? <span className="state-label">joined</span> : <button>Join</button>}</div></article>)}</div>}
    <AnimatePresence>{selectedRound ? <BottomSheet title={selectedRound.eventType} onClose={() => setSelectedRound(null)}><div className="bet-sheet"><div className="timing-grid"><span><small>Scheduled</small>{selectedRound.scheduledAt}</span><span><small>Closes</small>{selectedRound.closesAt}</span><span><small>Resolve</small>{selectedRound.resolvesAt}</span></div><Field label="Сторона"><Segmented value={side} options={["YES", "NO"] as const} onChange={setSide} /></Field><Field label="Сума"><div className="amount-input"><span><CurrencyValue currency="COIN" value={0} compact /></span><input type="number" min="1" value={amount} onChange={(event) => setAmount(event.target.value)} /></div></Field><div className="odds-summary"><span>{side}</span><b>× {side === "YES" ? selectedRound.yesOdds : selectedRound.noOdds}</b></div><button className="primary-action" disabled={selectedRound.status !== "OPEN"}>{selectedRound.myAmount ? "Збільшити ставку" : "Поставити"}</button></div></BottomSheet> : null}</AnimatePresence>
  </div>
}
