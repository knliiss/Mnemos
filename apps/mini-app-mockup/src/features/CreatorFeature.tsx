import { useMemo, useState } from "react"
import { CURRENCIES, EFFECT_TYPES, NOTIFICATION_TEMPLATE_KEYS, PERSONALIZATION_TYPES } from "../contracts"
import { creatorItems as seed } from "../mockData"
import type { Currency, EffectType, NotificationTemplateKey, Personalization, PersonalizationStatus, PersonalizationType, Route } from "../model"
import { personalizationLabels, personalizationStatusLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { ChoiceGrid, CurrencyValue, Field, IconButton, PageHeader, Segmented, Toggle } from "../shared/Primitives"

type Props = { routeId: Route["id"]; params?: Record<string, string>; navigate: (route: Route) => void; back: () => void }
type Template = { emoji: string; soloTitle: string; soloBody: string; compact: string; aggregateTitle: string }
type GradientStopDraft = { color: string; position: number }

const targets: Record<PersonalizationType, "USER" | "ACCOUNT"> = { NOTIFICATION_PACK: "USER", NICK_STYLE: "ACCOUNT", PROFILE_THEME: "ACCOUNT", AVATAR_FRAME: "ACCOUNT", PROFILE_EFFECT: "ACCOUNT" }
const placeholders: Record<NotificationTemplateKey, string> = { ITEM_DROP: "{player} {item} {rarity} {itemType}", BOOSTER: "{player} {booster}", DUNGEON: "{dungeon}", RAID: "{locations}", GLOBAL: "{event}" }
const blankTemplate = (): Template => ({ emoji: "", soloTitle: "", soloBody: "", compact: "", aggregateTitle: "" })

export function CreatorFeature(props: Props) {
  const [items, setItems] = useState<Personalization[]>(seed)

  if (props.routeId === "creator-revenue") return <RevenuePage back={props.back} />
  if (props.routeId === "personalization-editor") return <Editor {...props} items={items} setItems={setItems} />

  return <Studio {...props} items={items} />
}

function Studio({ navigate, back, items }: Props & { items: Personalization[] }) {
  const counts = useMemo(() => ({
    draft: items.filter(item => item.status === "DRAFT" || item.status === "REJECTED").length,
    review: items.filter(item => item.status === "IN_REVIEW").length,
    published: items.filter(item => item.status === "PUBLISHED").length,
  }), [items])

  return <div className="screen creator-screen"><PageHeader title="Creator" back={back} actions={<IconButton icon="plus" label="Створити" onClick={() => navigate({ id: "personalization-editor", params: { personalizationId: "new" } })} />} /><button className="creator-revenue creator-revenue--button" onClick={() => navigate({ id: "creator-revenue" })}><div><CurrencyValue currency="PRIME" value={128} /><small>підтверджено</small></div><span><b>18</b><small>очікує</small></span><span className="creator-revenue__arrow">›</span></button><div className="creator-status-strip"><span>{counts.draft}<small>чернетки</small></span><span>{counts.review}<small>перевірка</small></span><span>{counts.published}<small>опубліковано</small></span></div><div className="creator-list">{items.map(item => <button key={item.id} onClick={() => navigate({ id: "personalization-editor", params: { personalizationId: item.id } })}><Preview item={item} /><span><strong>{item.name}</strong><small>{personalizationLabels[item.type]}</small></span><span className={`status-tag status-tag--${item.status.toLowerCase()}`}>{personalizationStatusLabels[item.status]}</span></button>)}</div></div>
}

function RevenuePage({ back }: { back: () => void }) {
  const operations = [
    { id: "rev-1", status: "CONFIRMED", amount: 12, time: "сьогодні · 14:20" },
    { id: "rev-2", status: "PENDING", amount: 8, time: "сьогодні · 13:04" },
    { id: "rev-3", status: "FAILED", amount: 5, time: "вчора · 22:41" },
  ] as const

  return <div className="screen creator-revenue-screen"><PageHeader title="Дохід" back={back} /><section className="revenue-summary"><div><CurrencyValue currency="PRIME" value={128} /><small>підтверджено</small></div><div><CurrencyValue currency="PRIME" value={18} compact /><small>очікує</small></div><div><CurrencyValue currency="PRIME" value={5} compact /><small>помилка</small></div></section><div className="revenue-log">{operations.map(operation => <div key={operation.id}><i className={`revenue-state revenue-state--${operation.status.toLowerCase()}`} /><span><strong>{operation.status === "CONFIRMED" ? "Зараховано" : operation.status === "PENDING" ? "Очікує" : "Не зараховано"}</strong><small>{operation.time}</small></span><CurrencyValue currency="PRIME" value={operation.amount} compact /></div>)}</div></div>
}

function Editor({ params, back, items, setItems }: Props & { items: Personalization[]; setItems: (items: Personalization[]) => void }) {
  const existing = items.find(item => item.id === params?.personalizationId)
  const [type, setType] = useState<PersonalizationType>(existing?.type ?? "NICK_STYLE")
  const [name, setName] = useState(existing?.name ?? "")
  const [currency, setCurrency] = useState<Currency>(existing?.currency ?? "PRIME")
  const [price, setPrice] = useState(String(existing?.price ?? 25))
  const [glow, setGlow] = useState(false)
  const [glowColor, setGlowColor] = useState("#92959D")
  const [glowStrength, setGlowStrength] = useState(24)
  const [stops, setStops] = useState<GradientStopDraft[]>([{ color: "#ECEDEF", position: 0 }, { color: "#858891", position: 100 }])
  const [angle, setAngle] = useState(90)
  const [background, setBackground] = useState("#0A0B0D")
  const [surface, setSurface] = useState("#18191D")
  const [accent, setAccent] = useState("#B7BBC6")
  const [text, setText] = useState("#F2F3F5")
  const [border, setBorder] = useState("#32343A")
  const [borderWidth, setBorderWidth] = useState(2)
  const [radius, setRadius] = useState(12)
  const [effect, setEffect] = useState<EffectType>("AURORA")
  const [intensity, setIntensity] = useState(42)
  const [primary, setPrimary] = useState("#B8BCC6")
  const [secondary, setSecondary] = useState("#696D77")
  const [mixed, setMixed] = useState("")
  const [openTemplate, setOpenTemplate] = useState<NotificationTemplateKey>("ITEM_DROP")
  const [templates, setTemplates] = useState<Record<NotificationTemplateKey, Template>>({ ITEM_DROP: blankTemplate(), BOOSTER: blankTemplate(), DUNGEON: blankTemplate(), RAID: blankTemplate(), GLOBAL: blankTemplate() })

  const numericPrice = Number(price)
  const priceValid = Number.isInteger(numericPrice) && numericPrice > 0
  const previewPrice = priceValid ? numericPrice : 0
  const gradientValid = stops.length >= 1 && stops.length <= 4 && stops.every(stop => stop.position >= 0 && stop.position <= 100) && new Set(stops.map(stop => stop.position)).size === stops.length
  const notificationValid = mixed.trim().length > 0 && NOTIFICATION_TEMPLATE_KEYS.every(key => { const template = templates[key]; return template.soloTitle.trim() && template.soloBody.trim() && template.compact.trim() && template.aggregateTitle.trim() })
  const valid = name.trim().length >= 3 && name.trim().length <= 64 && priceValid && (type === "NICK_STYLE" ? gradientValid : type === "NOTIFICATION_PACK" ? Boolean(notificationValid) : true)

  const persist = (status?: PersonalizationStatus) => {
    if (!valid) return false

    const next: Personalization = {
      id: existing?.id ?? `creator-${Date.now()}`,
      type,
      target: targets[type],
      name: name.trim(),
      creator: "knalis",
      currency,
      price: numericPrice,
      status: status ?? existing?.status ?? "DRAFT",
      owned: true,
      active: false,
      preview: "editor",
    }

    setItems(existing ? items.map(item => item.id === existing.id ? next : item) : [...items, next])
    return true
  }

  const finish = (status?: PersonalizationStatus) => {
    if (persist(status)) back()
  }

  const updateTemplate = (patch: Partial<Template>) => setTemplates({ ...templates, [openTemplate]: { ...templates[openTemplate], ...patch } })
  const preview: Personalization = { id: "preview", type, target: targets[type], name: name || "Untitled", creator: "knalis", currency, price: previewPrice, status: existing?.status ?? "DRAFT", owned: true, active: false, preview: "editor" }

  return <div className="screen editor-screen"><PageHeader title={existing ? "Редагування" : "Створення"} eyebrow={existing ? personalizationStatusLabels[existing.status] : undefined} back={back} /><div className="editor-preview"><Preview item={preview} large /></div><div className="form-stack editor-form">
    {!existing ? <Field label="Тип"><ChoiceGrid value={type} options={PERSONALIZATION_TYPES} onChange={setType} labels={personalizationLabels} /></Field> : null}
    <Field label="Назва"><input value={name} minLength={3} maxLength={64} onChange={event => setName(event.target.value)} /></Field>
    {type === "NICK_STYLE" ? <><Field label="Градієнт" hint="1–4 точки, позиції не повторюються"><div className="gradient-editor">{stops.map((stop, index) => <div key={`${index}-${stop.position}`}><Color value={stop.color} set={value => setStops(stops.map((item, itemIndex) => itemIndex === index ? { ...item, color: value } : item))} /><input type="number" min={0} max={100} value={stop.position} onChange={event => setStops(stops.map((item, itemIndex) => itemIndex === index ? { ...item, position: Number(event.target.value) } : item))} />{stops.length > 1 ? <button type="button" aria-label="Видалити точку" onClick={() => setStops(stops.filter((_, itemIndex) => itemIndex !== index))}><Glyph name="close" size={14} /></button> : <span />}</div>)}{stops.length < 4 ? <button type="button" className="add-inline" onClick={() => setStops([...stops, { color: "#A0A3AA", position: Math.min(100, stops.length * 30 + 20) }])}><Glyph name="plus" size={15} />Точка</button> : null}</div></Field><Range label="Кут" min={0} max={360} value={angle} set={setAngle} suffix="°" /></> : null}
    {type === "PROFILE_THEME" ? <div className="color-matrix"><ColorField label="Фон" value={background} set={setBackground} /><ColorField label="Поверхня" value={surface} set={setSurface} /><ColorField label="Акцент" value={accent} set={setAccent} /><ColorField label="Текст" value={text} set={setText} /><ColorField label="Контур" value={border} set={setBorder} /></div> : null}
    {type === "AVATAR_FRAME" ? <><ColorField label="Контур" value={border} set={setBorder} /><Range label="Товщина" min={1} max={8} value={borderWidth} set={setBorderWidth} suffix="px" /><Range label="Радіус" min={0} max={32} value={radius} set={setRadius} suffix="px" /></> : null}
    {type === "PROFILE_EFFECT" ? <><Field label="Ефект"><ChoiceGrid value={effect} options={EFFECT_TYPES} onChange={setEffect} /></Field><Range label="Інтенсивність" min={1} max={100} value={intensity} set={setIntensity} /><ColorField label="Основний" value={primary} set={setPrimary} /><ColorField label="Другий" value={secondary} set={setSecondary} /></> : null}
    {type === "NOTIFICATION_PACK" ? <><Field label="Заголовок групи"><input value={mixed} maxLength={512} onChange={event => setMixed(event.target.value)} /></Field><div className="template-editor"><div className="template-tabs">{NOTIFICATION_TEMPLATE_KEYS.map(key => <button key={key} type="button" className={openTemplate === key ? "is-active" : ""} onClick={() => setOpenTemplate(key)}>{key.replace("ITEM_", "")}</button>)}</div><small className="placeholder-hint">{placeholders[openTemplate]}</small><Field label="Emoji"><input maxLength={16} value={templates[openTemplate].emoji} onChange={event => updateTemplate({ emoji: event.target.value })} /></Field><Field label="Заголовок"><input maxLength={512} value={templates[openTemplate].soloTitle} onChange={event => updateTemplate({ soloTitle: event.target.value })} /></Field><Field label="Текст"><textarea maxLength={512} value={templates[openTemplate].soloBody} onChange={event => updateTemplate({ soloBody: event.target.value })} /></Field><Field label="Компактно"><input maxLength={512} value={templates[openTemplate].compact} onChange={event => updateTemplate({ compact: event.target.value })} /></Field><Field label="Група"><input maxLength={512} value={templates[openTemplate].aggregateTitle} onChange={event => updateTemplate({ aggregateTitle: event.target.value })} /></Field></div></> : null}
    {type !== "PROFILE_EFFECT" && type !== "NOTIFICATION_PACK" ? <><div className="inline-toggle"><strong>Glow</strong><Toggle checked={glow} onChange={setGlow} label="Glow" /></div>{glow ? <><ColorField label="Glow" value={glowColor} set={setGlowColor} /><Range label="Сила" min={1} max={100} value={glowStrength} set={setGlowStrength} /></> : null}</> : null}
    <Field label="Ціна" hint={!priceValid && price.length ? "Додатне ціле число" : undefined}><div className="price-editor"><Segmented value={currency} options={CURRENCIES} onChange={setCurrency} /><input type="number" min={1} step={1} value={price} onChange={event => setPrice(event.target.value)} /></div></Field>
  </div><div className="sticky-actions creator-actions"><button className="secondary-action" disabled={!valid} onClick={() => finish()}>Зберегти</button>{!existing || existing.status === "DRAFT" || existing.status === "REJECTED" ? <button className="primary-action" disabled={!valid} onClick={() => finish("IN_REVIEW")}>На перевірку</button> : null}{existing?.status === "APPROVED" ? <button className="primary-action" onClick={() => finish("PUBLISHED")}>Опублікувати</button> : null}</div></div>
}

function Color({ value, set }: { value: string; set: (value: string) => void }) {
  return <input className="bare-color" type="color" value={value} onChange={event => set(event.target.value.toUpperCase())} />
}

function ColorField({ label, value, set }: { label: string; value: string; set: (value: string) => void }) {
  return <Field label={label}><div className="color-field"><Color value={value} set={set} /><code>{value}</code></div></Field>
}

function Range({ label, min, max, value, set, suffix = "" }: { label: string; min: number; max: number; value: number; set: (value: number) => void; suffix?: string }) {
  return <Field label={label}><input type="range" min={min} max={max} value={value} onChange={event => set(Number(event.target.value))} /><output>{value}{suffix}</output></Field>
}

function Preview({ item, large = false }: { item: Personalization; large?: boolean }) {
  return <div className={`personalization-preview personalization-preview--creator ${large ? "is-detail" : "is-small"}`}><span className="preview-orbit" /><span className="preview-core">{item.type === "NOTIFICATION_PACK" ? <Glyph name="bell" size={large ? 25 : 16} /> : item.type === "NICK_STYLE" ? <b>Aa</b> : item.type === "PROFILE_THEME" ? <span className="theme-window" /> : item.type === "AVATAR_FRAME" ? <span className="frame-sample" /> : <Glyph name="spark" size={large ? 25 : 16} />}</span></div>
}
