import { useMemo, useState } from "react"
import { betRounds, profiles, reviewQueue } from "../mockData"
import type { BetItemType, Currency, EventBetOutcome, Personalization, Route, SanctionType, TransactionType, WalletOwnerType } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { ChoiceGrid, CurrencyValue, Field, IconButton, PageHeader, Row, Section, Segmented } from "../shared/Primitives"

type Nav = (route: Route) => void

type Props = { routeId: Route["id"]; params?: Record<string, string>; navigate: Nav; back: () => void }
type Sanction = { id: string; type: SanctionType; reason: string; durationHours?: number; active: boolean }

export function ModerationFeature(props: Props) {
  if (props.routeId === "moderation-user") return <ModerationUserPage {...props} />
  if (props.routeId === "personalization-review") return <PersonalizationReviewPage {...props} />
  if (props.routeId === "bet-admin") return <BetAdminPage {...props} />
  return <ModerationHome {...props} />
}

function ModerationHome({ navigate, back }: Props) {
  const [query, setQuery] = useState("")
  const results = useMemo(() => {
    const normalized = query.trim().toLowerCase()
    if (normalized.length < 2) return []
    return profiles.filter((profile) => profile.nickname.toLowerCase().includes(normalized)).slice(0, 6)
  }, [query])

  return <div className="screen moderation-screen">
    <PageHeader title="Модерація" eyebrow="STAFF" back={back} />
    <div className="moderation-search"><Glyph name="search" size={18} /><input value={query} minLength={2} maxLength={64} onChange={(event) => setQuery(event.target.value)} placeholder="user / account" /></div>
    {results.length ? <div className="moderation-results">{results.map((profile) => <button key={profile.accountId} onClick={() => navigate({ id: "moderation-user", params: { accountId: profile.accountId } })}><span className="user-monogram user-monogram--small">{profile.nickname[0].toUpperCase()}</span><span><strong>{profile.nickname}</strong><small>{profile.accountId.slice(0, 12)}</small></span><span>›</span></button>)}</div> : null}
    <Section title="Черги">
      <Row icon="creator" title="Personalization review" meta={`${reviewQueue.length} pending`} onClick={() => navigate({ id: "personalization-review" })} />
      <Row icon="bet" title="Bet control" meta="resolve rounds · create item race" onClick={() => navigate({ id: "bet-admin" })} />
    </Section>
  </div>
}

function ModerationUserPage({ params, back }: Props) {
  const profile = profiles.find((item) => item.accountId === params?.accountId) ?? profiles[0]
  const [sanctions, setSanctions] = useState<Sanction[]>([{ id: "s-1", type: "NOTIFICATION_SUSPENSION", reason: "spam", durationHours: 24, active: true }])
  const [sanctionType, setSanctionType] = useState<SanctionType>("BAN")
  const [reason, setReason] = useState("")
  const [internalNote, setInternalNote] = useState("")
  const [duration, setDuration] = useState("24")
  const [targetType, setTargetType] = useState<WalletOwnerType>("USER")
  const [currency, setCurrency] = useState<Currency>("COIN")
  const [transactionType, setTransactionType] = useState<TransactionType>("CREDIT")
  const [amount, setAmount] = useState("100")
  const [walletReason, setWalletReason] = useState("")

  const addSanction = () => {
    if (!reason.trim()) return
    setSanctions([{ id: `s-${Date.now()}`, type: sanctionType, reason: reason.trim(), durationHours: duration ? Number(duration) : undefined, active: true }, ...sanctions])
    setReason(""); setInternalNote("")
  }

  return <div className="screen"><PageHeader title={profile.nickname} eyebrow="MODERATION" back={back} />
    <section className="moderation-target"><span className="user-monogram">{profile.nickname[0].toUpperCase()}</span><div><strong>{profile.nickname}</strong><small>{profile.accountId}</small></div></section>

    <Section title="Sanctions"><div className="sanction-list">{sanctions.map((sanction) => <article key={sanction.id} className={!sanction.active ? "is-inactive" : ""}><span className={`sanction-mark sanction-mark--${sanction.type.toLowerCase()}`} /><span><strong>{sanction.type.replaceAll("_", " ")}</strong><small>{sanction.reason}{sanction.durationHours ? ` · ${sanction.durationHours}h` : ""}</small></span>{sanction.active ? <button onClick={() => setSanctions(sanctions.map((item) => item.id === sanction.id ? { ...item, active: false } : item))}>Revoke</button> : <small>revoked</small>}</article>)}</div></Section>
    <div className="moderation-form"><h2>Нова санкція</h2><Field label="Тип"><ChoiceGrid value={sanctionType} options={["BAN", "NOTIFICATION_SUSPENSION"] as const} onChange={setSanctionType} labels={{ BAN: "Ban", NOTIFICATION_SUSPENSION: "Notifications" }} /></Field><Field label="Причина"><input value={reason} onChange={(event) => setReason(event.target.value)} /></Field><Field label="Internal note"><input value={internalNote} onChange={(event) => setInternalNote(event.target.value)} /></Field><Field label="Duration, h" hint="Порожнє — без строку"><input type="number" min={1} value={duration} onChange={(event) => setDuration(event.target.value)} /></Field><button className="destructive-action" onClick={addSanction}>Застосувати</button></div>

    <div className="moderation-form"><h2>Wallet adjustment</h2><Field label="Target"><Segmented value={targetType} options={["USER", "ACCOUNT"] as const} onChange={setTargetType} /></Field><div className="two-fields"><Field label="Currency"><Segmented value={currency} options={["COIN", "PRIME"] as const} onChange={setCurrency} /></Field><Field label="Operation"><Segmented value={transactionType} options={["CREDIT", "DEBIT"] as const} onChange={setTransactionType} /></Field></div><Field label="Amount"><div className="amount-input"><span><CurrencyValue currency={currency} value={0} compact /></span><input type="number" min={1} value={amount} onChange={(event) => setAmount(event.target.value)} /></div></Field><Field label="Reason"><input value={walletReason} maxLength={256} onChange={(event) => setWalletReason(event.target.value)} /></Field><button className="primary-action" disabled={!walletReason.trim() || Number(amount) <= 0}>Apply</button></div>
  </div>
}

function PersonalizationReviewPage({ back }: Props) {
  const [items, setItems] = useState(reviewQueue)
  const [selected, setSelected] = useState<Personalization | null>(null)
  const [currency, setCurrency] = useState<Currency>("PRIME")
  const [price, setPrice] = useState("25")
  const [reason, setReason] = useState("")
  const update = (id: string, patch: Partial<Personalization>) => setItems(items.map((item) => item.id === id ? { ...item, ...patch } : item))
  return <div className="screen"><PageHeader title="Review" eyebrow="PERSONALIZATION" back={back} />
    <div className="review-list">{items.map((item) => <button key={item.id} onClick={() => { setSelected(item); setCurrency(item.currency); setPrice(item.price.toString()) }}><span className="review-type">{item.type.replaceAll("_", " ")}</span><span><strong>{item.name}</strong><small>{item.creator}</small></span><span>{item.status}</span></button>)}</div>
    {selected ? <section className="review-panel"><div className="section__head"><h2>{selected.name}</h2><button onClick={() => setSelected(null)}><Glyph name="close" size={16} /></button></div><Field label="Approved price"><div className="price-editor"><Segmented value={currency} options={["COIN", "PRIME"] as const} onChange={setCurrency} /><input type="number" min={1} value={price} onChange={(event) => setPrice(event.target.value)} /></div></Field><button className="primary-action" onClick={() => { update(selected.id, { status: "APPROVED", currency, price: Math.max(1, Number(price)) }); setSelected(null) }}>Approve</button><Field label="Reject reason"><input value={reason} onChange={(event) => setReason(event.target.value)} /></Field><button className="destructive-action" disabled={!reason.trim()} onClick={() => { update(selected.id, { status: "REJECTED" }); setSelected(null); setReason("") }}>Reject</button>{selected.status === "PUBLISHED" ? <button className="secondary-action" onClick={() => update(selected.id, { status: "UNLISTED" })}>Unlist</button> : selected.status === "UNLISTED" ? <button className="secondary-action" onClick={() => update(selected.id, { status: "PUBLISHED" })}>Relist</button> : null}</section> : null}
  </div>
}

function BetAdminPage({ back }: Props) {
  const [selectedRound, setSelectedRound] = useState(betRounds[0].id)
  const [outcome, setOutcome] = useState<EventBetOutcome>("YES")
  const [resolveReason, setResolveReason] = useState("")
  const [itemName, setItemName] = useState("")
  const [itemType, setItemType] = useState<BetItemType>("SWORD")
  const [duration, setDuration] = useState("24")
  const [entry, setEntry] = useState("1000")
  const [created, setCreated] = useState<string[]>([])
  return <div className="screen"><PageHeader title="Bet control" back={back} />
    <Section title="Resolve round"><div className="form-stack"><Field label="Round"><ChoiceGrid value={selectedRound} options={betRounds.map((round) => round.id)} onChange={setSelectedRound} labels={Object.fromEntries(betRounds.map((round) => [round.id, `${round.eventType} · ${round.status}`]))} /></Field><Field label="Outcome"><ChoiceGrid value={outcome} options={["YES", "NO", "REFUND"] as const} onChange={setOutcome} /></Field><Field label="Reason"><input value={resolveReason} onChange={(event) => setResolveReason(event.target.value)} /></Field><button className="primary-action" disabled={!resolveReason.trim()}>Resolve</button></div></Section>
    <Section title="New item race"><div className="form-stack"><Field label="Item name"><input value={itemName} maxLength={24} onChange={(event) => setItemName(event.target.value)} /></Field><Field label="Item type"><ChoiceGrid value={itemType} options={["SWORD", "AURA", "PET", "BOOK", "JEWELRY"] as const} onChange={setItemType} /></Field><div className="two-fields"><Field label="Hours"><input type="number" min={1} max={168} value={duration} onChange={(event) => setDuration(event.target.value)} /></Field><Field label="Entry"><input type="number" min={1000} max={5000} value={entry} onChange={(event) => setEntry(event.target.value)} /></Field></div><button className="primary-action" disabled={!itemName.trim() || Number(duration) < 1 || Number(duration) > 168 || Number(entry) < 1000 || Number(entry) > 5000} onClick={() => { setCreated([`${itemName} · ${itemType}`, ...created]); setItemName("") }}>Create</button>{created.map((item) => <div className="inline-result" key={item}><Glyph name="check" size={14} />{item}</div>)}</div></Section>
  </div>
}
