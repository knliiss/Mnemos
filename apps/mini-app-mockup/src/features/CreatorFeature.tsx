import { useState } from "react"
import { creatorItems as seed } from "../mockData"
import type { Currency, EffectType, NotificationTemplateKey, Personalization, PersonalizationStatus, PersonalizationType, Route } from "../model"
import { Glyph } from "../shared/MnemosIcon"
import { ChoiceGrid, CurrencyValue, Field, IconButton, PageHeader, Segmented, Toggle } from "../shared/Primitives"

type Props = { routeId: Route["id"]; params?: Record<string, string>; navigate: (route: Route) => void; back: () => void }
type Template = { emoji: string; soloTitle: string; soloBody: string; compact: string; aggregateTitle: string }
const types = ["NICK_STYLE", "PROFILE_THEME", "AVATAR_FRAME", "PROFILE_EFFECT", "NOTIFICATION_PACK"] as const
const labels: Record<PersonalizationType, string> = { NICK_STYLE: "Nick", PROFILE_THEME: "Theme", AVATAR_FRAME: "Frame", PROFILE_EFFECT: "Effect", NOTIFICATION_PACK: "Notification" }
const target: Record<PersonalizationType, "USER" | "ACCOUNT"> = { NICK_STYLE: "ACCOUNT", PROFILE_THEME: "ACCOUNT", AVATAR_FRAME: "ACCOUNT", PROFILE_EFFECT: "ACCOUNT", NOTIFICATION_PACK: "USER" }
const templateKeys: NotificationTemplateKey[] = ["ITEM_DROP", "BOOSTER", "DUNGEON", "RAID", "GLOBAL"]
const placeholders: Record<NotificationTemplateKey, string> = { ITEM_DROP: "{player} {item} {rarity} {itemType}", BOOSTER: "{player} {booster}", DUNGEON: "{dungeon}", RAID: "{locations}", GLOBAL: "{event}" }
const blank = (): Template => ({ emoji: "", soloTitle: "", soloBody: "", compact: "", aggregateTitle: "" })

export function CreatorFeature(props: Props) {
  const [items, setItems] = useState<Personalization[]>(seed)
  return props.routeId === "personalization-editor" ? <Editor {...props} items={items} setItems={setItems} /> : <Studio {...props} items={items} />
}

function Studio({ navigate, back, items }: Props & { items: Personalization[] }) {
  return <div className="screen creator-screen"><PageHeader title="Creator" back={back} actions={<IconButton icon="plus" label="Створити" onClick={() => navigate({ id: "personalization-editor", params: { personalizationId: "new" } })} />} />
    <section className="creator-revenue"><div><CurrencyValue currency="PRIME" value={128} /><small>confirmed</small></div><span><b>18</b><small>pending</small></span><span><b>5</b><small>failed</small></span></section>
    <div className="creator-status-strip"><span>{items.filter(i => i.status === "DRAFT" || i.status === "REJECTED").length}<small>draft</small></span><span>{items.filter(i => i.status === "IN_REVIEW").length}<small>review</small></span><span>{items.filter(i => i.status === "PUBLISHED").length}<small>published</small></span></div>
    <div className="creator-list">{items.map(item => <button key={item.id} onClick={() => navigate({ id: "personalization-editor", params: { personalizationId: item.id } })}><Preview item={item} /><span><strong>{item.name}</strong><small>{labels[item.type]}</small></span><span className={`status-tag status-tag--${item.status.toLowerCase()}`}>{item.status}</span></button>)}</div>
    <section className="revenue-log"><div><i className="revenue-state revenue-state--confirmed" /><span><strong>confirmed</strong><small>сьогодні · 14:20</small></span><CurrencyValue currency="PRIME" value={12} compact /></div><div><i className="revenue-state revenue-state--pending" /><span><strong>pending</strong><small>сьогодні · 13:04</small></span><CurrencyValue currency="PRIME" value={8} compact /></div></section>
  </div>
}

function Editor({ params, back, items, setItems }: Props & { items: Personalization[]; setItems: (items: Personalization[]) => void }) {
  const existing = items.find(i => i.id === params?.personalizationId)
  const [type, setType] = useState<PersonalizationType>(existing?.type ?? "NICK_STYLE")
  const [name, setName] = useState(existing?.name ?? "")
  const [currency, setCurrency] = useState<Currency>(existing?.currency ?? "PRIME")
  const [price, setPrice] = useState(String(existing?.price ?? 25))
  const [glow, setGlow] = useState(false); const [glowColor, setGlowColor] = useState("#8F9692"); const [glowStrength, setGlowStrength] = useState(24)
  const [stop1, setStop1] = useState("#ECEEED"); const [stop2, setStop2] = useState("#858A87"); const [pos1, setPos1] = useState(0); const [pos2, setPos2] = useState(100); const [angle, setAngle] = useState(90)
  const [background, setBackground] = useState("#0B0C0C"); const [surface, setSurface] = useState("#181919"); const [accent, setAccent] = useState("#A5ABA7"); const [text, setText] = useState("#F1F2F2"); const [border, setBorder] = useState("#303232")
  const [borderWidth, setBorderWidth] = useState(2); const [radius, setRadius] = useState(12)
  const [effect, setEffect] = useState<EffectType>("AURORA"); const [intensity, setIntensity] = useState(42); const [primary, setPrimary] = useState("#B3B8B5"); const [secondary, setSecondary] = useState("#646A66")
  const [mixed, setMixed] = useState(""); const [openTemplate, setOpenTemplate] = useState<NotificationTemplateKey>("ITEM_DROP"); const [templates, setTemplates] = useState<Record<NotificationTemplateKey, Template>>({ ITEM_DROP: blank(), BOOSTER: blank(), DUNGEON: blank(), RAID: blank(), GLOBAL: blank() })
  const value = Math.max(1, Number(price) || 1); const valid = name.trim().length >= 3 && name.trim().length <= 64
  const persist = (status?: PersonalizationStatus) => { if (!valid) return false; const next: Personalization = { id: existing?.id ?? `creator-${Date.now()}`, type, target: target[type], name: name.trim(), creator: "knalis", currency, price: value, status: status ?? existing?.status ?? "DRAFT", owned: true, active: false, preview: "editor" }; setItems(existing ? items.map(i => i.id === existing.id ? next : i) : [...items, next]); return true }
  const finish = (status?: PersonalizationStatus) => { if (persist(status)) back() }
  const updateTemplate = (patch: Partial<Template>) => setTemplates({ ...templates, [openTemplate]: { ...templates[openTemplate], ...patch } })
  const preview: Personalization = { id: "preview", type, target: target[type], name: name || "Untitled", creator: "knalis", currency, price: value, status: existing?.status ?? "DRAFT", owned: true, active: false, preview: "editor" }
  return <div className="screen editor-screen"><PageHeader title={existing ? "Редагування" : "Створення"} eyebrow={existing?.status ?? "DRAFT"} back={back} /><div className="editor-preview"><Preview item={preview} large /></div><div className="form-stack editor-form">
    {!existing ? <Field label="Тип"><ChoiceGrid value={type} options={types} onChange={setType} labels={labels} /></Field> : null}<Field label="Назва"><input value={name} minLength={3} maxLength={64} onChange={e => setName(e.target.value)} /></Field>
    {type === "NICK_STYLE" ? <><Field label="Gradient"><div className="gradient-pair"><Color value={stop1} set={setStop1} /><input type="number" min={0} max={100} value={pos1} onChange={e => setPos1(Number(e.target.value))} /><Color value={stop2} set={setStop2} /><input type="number" min={0} max={100} value={pos2} onChange={e => setPos2(Number(e.target.value))} /></div></Field><Range label="Angle" min={0} max={360} value={angle} set={setAngle} suffix="°" /></> : null}
    {type === "PROFILE_THEME" ? <div className="color-matrix"><ColorField label="Background" value={background} set={setBackground} /><ColorField label="Surface" value={surface} set={setSurface} /><ColorField label="Accent" value={accent} set={setAccent} /><ColorField label="Text" value={text} set={setText} /><ColorField label="Border" value={border} set={setBorder} /></div> : null}
    {type === "AVATAR_FRAME" ? <><ColorField label="Border" value={border} set={setBorder} /><Range label="Width" min={1} max={8} value={borderWidth} set={setBorderWidth} suffix="px" /><Range label="Radius" min={0} max={32} value={radius} set={setRadius} suffix="px" /></> : null}
    {type === "PROFILE_EFFECT" ? <><Field label="Effect"><ChoiceGrid value={effect} options={["AURORA", "PULSE", "SHIMMER", "PARTICLES"] as const} onChange={setEffect} /></Field><Range label="Intensity" min={1} max={100} value={intensity} set={setIntensity} /><ColorField label="Primary" value={primary} set={setPrimary} /><ColorField label="Secondary" value={secondary} set={setSecondary} /></> : null}
    {type === "NOTIFICATION_PACK" ? <><Field label="Mixed aggregate title"><input value={mixed} maxLength={512} onChange={e => setMixed(e.target.value)} /></Field><div className="template-editor"><div className="template-tabs">{templateKeys.map(key => <button key={key} className={openTemplate === key ? "is-active" : ""} onClick={() => setOpenTemplate(key)}>{key.replace("ITEM_", "")}</button>)}</div><small className="placeholder-hint">{placeholders[openTemplate]}</small><Field label="Emoji"><input maxLength={16} value={templates[openTemplate].emoji} onChange={e => updateTemplate({ emoji: e.target.value })} /></Field><Field label="Solo title"><input maxLength={512} value={templates[openTemplate].soloTitle} onChange={e => updateTemplate({ soloTitle: e.target.value })} /></Field><Field label="Solo body"><textarea maxLength={512} value={templates[openTemplate].soloBody} onChange={e => updateTemplate({ soloBody: e.target.value })} /></Field><Field label="Compact"><input maxLength={512} value={templates[openTemplate].compact} onChange={e => updateTemplate({ compact: e.target.value })} /></Field><Field label="Aggregate title"><input maxLength={512} value={templates[openTemplate].aggregateTitle} onChange={e => updateTemplate({ aggregateTitle: e.target.value })} /></Field></div></> : null}
    {type !== "PROFILE_EFFECT" && type !== "NOTIFICATION_PACK" ? <><div className="inline-toggle"><span><strong>Glow</strong></span><Toggle checked={glow} onChange={setGlow} label="Glow" /></div>{glow ? <><ColorField label="Glow color" value={glowColor} set={setGlowColor} /><Range label="Strength" min={1} max={100} value={glowStrength} set={setGlowStrength} /></> : null}</> : null}
    <Field label="Ціна"><div className="price-editor"><Segmented value={currency} options={["COIN", "PRIME"] as const} onChange={setCurrency} /><input type="number" min={1} value={price} onChange={e => setPrice(e.target.value)} /></div></Field>
  </div><div className="sticky-actions creator-actions"><button className="secondary-action" disabled={!valid} onClick={() => finish()}>Зберегти</button>{!existing || existing.status === "DRAFT" || existing.status === "REJECTED" ? <button className="primary-action" disabled={!valid} onClick={() => finish("IN_REVIEW")}>На review</button> : null}{existing?.status === "APPROVED" ? <button className="primary-action" onClick={() => finish("PUBLISHED")}>Publish</button> : null}</div></div>
}

function Color({ value, set }: { value: string; set: (v: string) => void }) { return <input className="bare-color" type="color" value={value} onChange={e => set(e.target.value.toUpperCase())} /> }
function ColorField({ label, value, set }: { label: string; value: string; set: (v: string) => void }) { return <Field label={label}><div className="color-field"><Color value={value} set={set} /><code>{value}</code></div></Field> }
function Range({ label, min, max, value, set, suffix = "" }: { label: string; min: number; max: number; value: number; set: (v: number) => void; suffix?: string }) { return <Field label={label}><input type="range" min={min} max={max} value={value} onChange={e => set(Number(e.target.value))} /><output>{value}{suffix}</output></Field> }
function Preview({ item, large = false }: { item: Personalization; large?: boolean }) { return <div className={`personalization-preview personalization-preview--creator ${large ? "is-detail" : "is-small"}`}><span className="preview-orbit" /><span className="preview-core">{item.type === "NOTIFICATION_PACK" ? <Glyph name="bell" size={large ? 25 : 16} /> : item.type === "NICK_STYLE" ? <b>Aa</b> : item.type === "PROFILE_THEME" ? <span className="theme-window" /> : item.type === "AVATAR_FRAME" ? <span className="frame-sample" /> : <Glyph name="spark" size={large ? 25 : 16} />}</span></div> }
