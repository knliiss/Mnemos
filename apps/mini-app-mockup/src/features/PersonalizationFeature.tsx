import { useMemo, useState } from "react"
import { creatorItems, personalizations } from "../mockData"
import type { Currency, EffectType, NotificationTemplateKey, Personalization, PersonalizationType, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { ChoiceGrid, CurrencyValue, Field, IconButton, PageHeader, Section, Segmented, Toggle } from "../shared/Primitives"

type Nav = (route: Route) => void

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: Nav
  back: () => void
}

const typeLabels: Record<PersonalizationType, string> = {
  NOTIFICATION_PACK: "Notification",
  NICK_STYLE: "Nick",
  PROFILE_THEME: "Theme",
  AVATAR_FRAME: "Frame",
  PROFILE_EFFECT: "Effect",
}
const typeTargets: Record<PersonalizationType, "USER" | "ACCOUNT"> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}
const templateKeys: NotificationTemplateKey[] = ["ITEM_DROP", "BOOSTER", "DUNGEON", "RAID", "GLOBAL"]
const placeholderHints: Record<NotificationTemplateKey, string> = {
  ITEM_DROP: "{player} {item} {rarity} {itemType}",
  BOOSTER: "{player} {booster}",
  DUNGEON: "{dungeon}",
  RAID: "{locations}",
  GLOBAL: "{event}",
}

export function PersonalizationFeature(props: Props) {
  const [catalog, setCatalog] = useState(personalizations)
  const [myCreatorItems, setMyCreatorItems] = useState(creatorItems)

  if (props.routeId === "personalization-owned") return <OwnedPage {...props} catalog={catalog} setCatalog={setCatalog} />
  if (props.routeId === "personalization-detail") return <DetailPage {...props} catalog={catalog} setCatalog={setCatalog} />
  if (props.routeId === "creator-studio") return <CreatorStudio {...props} items={myCreatorItems} />
  if (props.routeId === "personalization-editor") return <CreatorEditor {...props} items={myCreatorItems} setItems={setMyCreatorItems} />
  return <StorePage {...props} catalog={catalog} />
}

function StorePage({ navigate, back, catalog }: Props & { catalog: Personalization[] }) {
  const [type, setType] = useState<PersonalizationType>("NOTIFICATION_PACK")
  const [currency, setCurrency] = useState<"ALL" | Currency>("ALL")
  const visible = useMemo(() => catalog.filter((item) => item.type === type && (currency === "ALL" || item.currency === currency)), [catalog, type, currency])
  return <div className="screen personalization-screen">
    <PageHeader title="Store" back={back} actions={<IconButton icon="spark" label="Мої персоналізації" onClick={() => navigate({ id: "personalization-owned" })} />} />
    <div className="type-scroller">{(["NOTIFICATION_PACK", "NICK_STYLE", "PROFILE_THEME", "AVATAR_FRAME", "PROFILE_EFFECT"] as PersonalizationType[]).map((value) => <button key={value} className={type === value ? "is-active" : ""} onClick={() => setType(value)}><span className={`type-symbol type-symbol--${value.toLowerCase()}`} />{typeLabels[value]}</button>)}</div>
    <div className="store-toolbar"><span className="scope-chip">{typeTargets[type]}</span><Segmented value={currency} options={["ALL", "COIN", "PRIME"] as const} onChange={setCurrency} label={(value) => value === "ALL" ? "Усі" : value} /></div>
    <div className="catalog-grid">{visible.map((item) => <button className="catalog-item" key={item.id} onClick={() => navigate({ id: "personalization-detail", params: { personalizationId: item.id } })}><PersonalizationPreview item={item} /><span className="catalog-item__copy"><strong>{item.name}</strong><small>{item.creator}</small></span><span className="catalog-item__price">{item.owned ? <span className="owned-mark"><Glyph name="check" size={13} /></span> : <CurrencyValue currency={item.currency} value={item.price} compact />}</span></button>)}</div>
  </div>
}

function OwnedPage({ navigate, back, catalog, setCatalog }: Props & { catalog: Personalization[]; setCatalog: (items: Personalization[]) => void }) {
  const [type, setType] = useState<"ALL" | PersonalizationType>("ALL")
  const owned = catalog.filter((item) => item.owned && (type === "ALL" || item.type === type))
  const toggle = (id: string) => setCatalog(catalog.map((item) => item.id === id ? { ...item, active: !item.active } : item))
  return <div className="screen"><PageHeader title="Мої" back={back} /><div className="compact-filter">{(["ALL", "NOTIFICATION_PACK", "NICK_STYLE", "PROFILE_THEME", "AVATAR_FRAME", "PROFILE_EFFECT"] as const).map((value) => <button key={value} className={type === value ? "is-active" : ""} onClick={() => setType(value)}>{value === "ALL" ? "Усі" : typeLabels[value]}</button>)}</div><div className="owned-list">{owned.map((item) => <article key={item.id}><button className="owned-list__main" onClick={() => navigate({ id: "personalization-detail", params: { personalizationId: item.id } })}><PersonalizationPreview item={item} small /><span><strong>{item.name}</strong><small>{typeLabels[item.type]} · {item.target}</small></span></button><Toggle checked={item.active} onChange={() => toggle(item.id)} label={`Активувати ${item.name}`} /></article>)}</div></div>
}

function DetailPage({ params, back, catalog, setCatalog }: Props & { catalog: Personalization[]; setCatalog: (items: Personalization[]) => void }) {
  const item = catalog.find((candidate) => candidate.id === params?.personalizationId) ?? catalog[0]
  const update = (patch: Partial<Personalization>) => setCatalog(catalog.map((candidate) => candidate.id === item.id ? { ...candidate, ...patch } : candidate))
  return <div className="screen personalization-detail"><PageHeader title={item.name} eyebrow={item.target} back={back} /><PersonalizationPreview item={item} detail /><div className="detail-meta"><span><small>Type</small>{typeLabels[item.type]}</span><span><small>Creator</small>{item.creator}</span><span><small>Status</small>{item.status}</span></div><div className="detail-action">{!item.owned ? <button className="primary-action" onClick={() => update({ owned: true })}><span>Купити</span><CurrencyValue currency={item.currency} value={item.price} compact /></button> : <button className={item.active ? "secondary-action is-active" : "primary-action"} onClick={() => update({ active: !item.active })}>{item.active ? "Деактивувати" : "Активувати"}</button>}</div></div>
}

function CreatorStudio({ navigate, back, items }: Props & { items: Personalization[] }) {
  return <div className="screen creator-screen"><PageHeader title="Creator" back={back} actions={<IconButton icon="plus" label="Створити" onClick={() => navigate({ id: "personalization-editor", params: { personalizationId: "new" } })} />} /><div className="creator-status-strip"><span>{items.filter((item) => item.status === "DRAFT").length}<small>draft</small></span><span>{items.filter((item) => item.status === "IN_REVIEW").length}<small>review</small></span><span>{items.filter((item) => item.status === "PUBLISHED").length}<small>published</small></span></div><div className="creator-list">{items.map((item) => <button key={item.id} onClick={() => navigate({ id: "personalization-editor", params: { personalizationId: item.id } })}><PersonalizationPreview item={item} small /><span><strong>{item.name}</strong><small>{typeLabels[item.type]}</small></span><span className={`status-tag status-tag--${item.status.toLowerCase()}`}>{item.status}</span></button>)}</div></div>
}

type GradientStopDraft = { color: string; position: number }
type TemplateDraft = { emoji: string; soloTitle: string; soloBody: string; compact: string; aggregateTitle: string }
const emptyTemplate = (): TemplateDraft => ({ emoji: "", soloTitle: "", soloBody: "", compact: "", aggregateTitle: "" })

function CreatorEditor({ params, back, items, setItems }: Props & { items: Personalization[]; setItems: (items: Personalization[]) => void }) {
  const existing = items.find((item) => item.id === params?.personalizationId)
  const [type, setType] = useState<PersonalizationType>(existing?.type ?? "NICK_STYLE")
  const [name, setName] = useState(existing?.name ?? "")
  const [currency, setCurrency] = useState<Currency>(existing?.currency ?? "PRIME")
  const [amount, setAmount] = useState(existing?.price.toString() ?? "25")
  const [glow, setGlow] = useState(false)
  const [glowColor, setGlowColor] = useState("#8F9692")
  const [glowStrength, setGlowStrength] = useState(24)
  const [stops, setStops] = useState<GradientStopDraft[]>([{ color: "#ECEEED", position: 0 }, { color: "#858A87", position: 100 }])
  const [angle, setAngle] = useState(90)
  const [background, setBackground] = useState("#0B0C0C")
  const [surface, setSurface] = useState("#181919")
  const [accent, setAccent] = useState("#A5ABA7")
  const [text, setText] = useState("#F1F2F2")
  const [border, setBorder] = useState("#303232")
  const [borderWidth, setBorderWidth] = useState(2)
  const [radius, setRadius] = useState(12)
  const [effect, setEffect] = useState<EffectType>("AURORA")
  const [intensity, setIntensity] = useState(42)
  const [primary, setPrimary] = useState("#B3B8B5")
  const [secondary, setSecondary] = useState("#646A66")
  const [mixedTitle, setMixedTitle] = useState("Події за останній момент")
  const [templates, setTemplates] = useState<Record<NotificationTemplateKey, TemplateDraft>>({ ITEM_DROP: emptyTemplate(), BOOSTER: emptyTemplate(), DUNGEON: emptyTemplate(), RAID: emptyTemplate(), GLOBAL: emptyTemplate() })
  const [openTemplate, setOpenTemplate] = useState<NotificationTemplateKey>("ITEM_DROP")

  const price = Math.max(1, Number(amount) || 1)
  const preview: Personalization = { id: existing?.id ?? "preview", type, target: typeTargets[type], name: name || "Untitled", creator: "knalis", currency, price, status: existing?.status ?? "DRAFT", owned: true, active: false, preview: "editor" }
  const save = () => {
    if (name.trim().length < 3) return
    if (existing) setItems(items.map((item) => item.id === existing.id ? { ...item, type, target: typeTargets[type], name: name.trim(), currency, price } : item))
    else setItems([...items, { ...preview, id: `creator-${Date.now()}`, name: name.trim() }])
    back()
  }
  const changeTemplate = (key: NotificationTemplateKey, patch: Partial<TemplateDraft>) => setTemplates({ ...templates, [key]: { ...templates[key], ...patch } })

  return <div className="screen editor-screen"><PageHeader title={existing ? "Редагування" : "Створення"} eyebrow={existing?.status ?? "DRAFT"} back={back} /><div className="editor-preview"><PersonalizationPreview item={preview} detail /></div><div className="form-stack editor-form">
    {!existing ? <Field label="Тип"><ChoiceGrid value={type} options={["NICK_STYLE", "PROFILE_THEME", "AVATAR_FRAME", "PROFILE_EFFECT", "NOTIFICATION_PACK"] as const} onChange={setType} labels={typeLabels} /></Field> : null}
    <Field label="Назва"><input value={name} minLength={3} maxLength={64} onChange={(event) => setName(event.target.value)} placeholder="3–64" /></Field>
    {type === "NICK_STYLE" ? <><Field label="Gradient stops" hint="1–4, позиції 0–100"><div className="gradient-editor">{stops.map((stop, index) => <div key={`${index}-${stop.position}`}><input type="color" value={stop.color} onChange={(event) => setStops(stops.map((item, itemIndex) => itemIndex === index ? { ...item, color: event.target.value.toUpperCase() } : item))} /><input type="number" min={0} max={100} value={stop.position} onChange={(event) => setStops(stops.map((item, itemIndex) => itemIndex === index ? { ...item, position: Number(event.target.value) } : item))} />{stops.length > 1 ? <button onClick={() => setStops(stops.filter((_, itemIndex) => itemIndex !== index))}><Glyph name="close" size={14} /></button> : null}</div>)}{stops.length < 4 ? <button type="button" className="add-inline" onClick={() => setStops([...stops, { color: "#9A9F9C", position: Math.min(100, stops.length * 33) }])}><Glyph name="plus" size={15} />stop</button> : null}</div></Field><Field label="Angle"><input type="range" min={0} max={360} value={angle} onChange={(event) => setAngle(Number(event.target.value))} /><output>{angle}°</output></Field></> : null}
    {type === "PROFILE_THEME" ? <ColorMatrix values={{ background, surface, accent, text, border }} setters={{ background: setBackground, surface: setSurface, accent: setAccent, text: setText, border: setBorder }} /> : null}
    {type === "AVATAR_FRAME" ? <><ColorInput label="Border" value={border} setValue={setBorder} /><Field label="Width"><input type="range" min={1} max={8} value={borderWidth} onChange={(event) => setBorderWidth(Number(event.target.value))} /><output>{borderWidth}px</output></Field><Field label="Radius"><input type="range" min={0} max={32} value={radius} onChange={(event) => setRadius(Number(event.target.value))} /><output>{radius}px</output></Field></> : null}
    {type === "PROFILE_EFFECT" ? <><Field label="Effect"><ChoiceGrid value={effect} options={["AURORA", "PULSE", "SHIMMER", "PARTICLES"] as const} onChange={setEffect} /></Field><Field label="Intensity"><input type="range" min={1} max={100} value={intensity} onChange={(event) => setIntensity(Number(event.target.value))} /><output>{intensity}</output></Field><ColorInput label="Primary" value={primary} setValue={setPrimary} /><ColorInput label="Secondary" value={secondary} setValue={setSecondary} /></> : null}
    {type === "NOTIFICATION_PACK" ? <><Field label="Mixed aggregate title"><input value={mixedTitle} maxLength={512} onChange={(event) => setMixedTitle(event.target.value)} /></Field><div className="template-editor"><div className="template-tabs">{templateKeys.map((key) => <button key={key} className={openTemplate === key ? "is-active" : ""} onClick={() => setOpenTemplate(key)}>{key.replace("ITEM_", "")}</button>)}</div><small className="placeholder-hint">{placeholderHints[openTemplate]}</small><Field label="Emoji"><input value={templates[openTemplate].emoji} maxLength={16} onChange={(event) => changeTemplate(openTemplate, { emoji: event.target.value })} /></Field><Field label="Solo title"><input value={templates[openTemplate].soloTitle} maxLength={512} onChange={(event) => changeTemplate(openTemplate, { soloTitle: event.target.value })} /></Field><Field label="Solo body"><textarea value={templates[openTemplate].soloBody} maxLength={512} onChange={(event) => changeTemplate(openTemplate, { soloBody: event.target.value })} /></Field><Field label="Compact"><input value={templates[openTemplate].compact} maxLength={512} onChange={(event) => changeTemplate(openTemplate, { compact: event.target.value })} /></Field><Field label="Aggregate title"><input value={templates[openTemplate].aggregateTitle} maxLength={512} onChange={(event) => changeTemplate(openTemplate, { aggregateTitle: event.target.value })} /></Field></div></> : null}
    {type !== "PROFILE_EFFECT" && type !== "NOTIFICATION_PACK" ? <div className="inline-toggle"><span><strong>Glow</strong><small>optional</small></span><Toggle checked={glow} onChange={setGlow} label="Glow" /></div> : null}
    {glow ? <div className="nested-fields"><ColorInput label="Glow color" value={glowColor} setValue={setGlowColor} /><Field label="Strength"><input type="range" min={1} max={100} value={glowStrength} onChange={(event) => setGlowStrength(Number(event.target.value))} /><output>{glowStrength}</output></Field></div> : null}
    <Field label="Ціна"><div className="price-editor"><Segmented value={currency} options={["COIN", "PRIME"] as const} onChange={setCurrency} /><input type="number" min={1} value={amount} onChange={(event) => setAmount(event.target.value)} /></div></Field>
  </div><div className="sticky-actions"><button className="primary-action" onClick={save}>Зберегти</button>{existing?.status === "DRAFT" || existing?.status === "REJECTED" ? <button className="secondary-action">На review</button> : null}{existing?.status === "APPROVED" ? <button className="secondary-action">Publish</button> : null}</div></div>
}

function ColorInput({ label, value, setValue }: { label: string; value: string; setValue: (value: string) => void }) { return <Field label={label}><div className="color-field"><input type="color" value={value} onChange={(event) => setValue(event.target.value.toUpperCase())} /><code>{value}</code></div></Field> }
function ColorMatrix({ values, setters }: { values: Record<string, string>; setters: Record<string, (value: string) => void> }) { return <div className="color-matrix">{Object.entries(values).map(([key, value]) => <ColorInput key={key} label={key} value={value} setValue={setters[key]} />)}</div> }
function PersonalizationPreview({ item, small, detail }: { item: Personalization; small?: boolean; detail?: boolean }) { return <div className={`personalization-preview personalization-preview--${item.preview} personalization-preview--${item.type.toLowerCase()} ${small ? "is-small" : ""} ${detail ? "is-detail" : ""}`}><span className="preview-orbit" /><span className="preview-core">{item.type === "NOTIFICATION_PACK" ? <Glyph name="bell" size={small ? 17 : 26} /> : item.type === "NICK_STYLE" ? <b>Aa</b> : item.type === "PROFILE_THEME" ? <span className="theme-window" /> : item.type === "AVATAR_FRAME" ? <span className="frame-sample" /> : <Glyph name="spark" size={small ? 17 : 27} />}</span></div> }
