import { useMemo, useState } from "react"
import { CURRENCIES, PERSONALIZATION_TYPES } from "../contracts"
import { personalizations as seed } from "../mockData"
import type { Currency, Personalization, PersonalizationType, Route } from "../model"
import { personalizationLabels, personalizationStatusLabels } from "../presentation"
import { Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, IconButton, PageHeader, Segmented, Toggle } from "../shared/Primitives"

type Props = { routeId: Route["id"]; params?: Record<string, string>; navigate: (route: Route) => void; back: () => void }
const targets: Record<PersonalizationType, "USER" | "ACCOUNT"> = { NOTIFICATION_PACK: "USER", NICK_STYLE: "ACCOUNT", PROFILE_THEME: "ACCOUNT", AVATAR_FRAME: "ACCOUNT", PROFILE_EFFECT: "ACCOUNT" }

export function PersonalizationFeature(props: Props) {
  const [items, setItems] = useState<Personalization[]>(seed)
  if (props.routeId === "personalization-owned") return <Owned {...props} items={items} setItems={setItems} />
  if (props.routeId === "personalization-detail") return <Detail {...props} items={items} setItems={setItems} />
  return <Store {...props} items={items} />
}

function Store({ navigate, back, items }: Props & { items: Personalization[] }) {
  const [type, setType] = useState<PersonalizationType>("NOTIFICATION_PACK")
  const [currency, setCurrency] = useState<"ALL" | Currency>("ALL")
  const visible = useMemo(() => items.filter(item => item.status === "PUBLISHED" && item.type === type && (currency === "ALL" || item.currency === currency)), [items, type, currency])
  return <div className="screen personalization-screen"><PageHeader title="Магазин" back={back} actions={<IconButton icon="spark" label="Мої" onClick={() => navigate({ id: "personalization-owned" })} />} /><div className="type-scroller">{PERSONALIZATION_TYPES.map(value => <button key={value} className={type === value ? "is-active" : ""} onClick={() => setType(value)}><span className={`type-symbol type-symbol--${value.toLowerCase()}`} />{personalizationLabels[value]}</button>)}</div><div className="store-toolbar"><ScopeMark target={targets[type]} /><Segmented value={currency} options={["ALL", ...CURRENCIES] as const} onChange={setCurrency} label={value => value === "ALL" ? "Усі" : value} /></div><div className="catalog-grid">{visible.map(item => <button className="catalog-item" key={item.id} onClick={() => navigate({ id: "personalization-detail", params: { personalizationId: item.id } })}><Preview item={item} /><span className="catalog-item__copy"><strong>{item.name}</strong><small>{item.creator}</small></span><span className="catalog-item__price">{item.owned ? <span className="owned-mark"><Glyph name="check" size={13} /></span> : <CurrencyValue currency={item.currency} value={item.price} compact />}</span></button>)}</div></div>
}

function Owned({ navigate, back, items, setItems }: Props & { items: Personalization[]; setItems: (items: Personalization[]) => void }) {
  const [type, setType] = useState<"ALL" | PersonalizationType>("ALL")
  const visible = items.filter(item => item.owned && (type === "ALL" || item.type === type))
  const toggle = (id: string, active: boolean) => setItems(items.map(item => item.id === id ? { ...item, active } : item))
  return <div className="screen"><PageHeader title="Мої" back={back} /><div className="compact-filter">{(["ALL", ...PERSONALIZATION_TYPES] as const).map(value => <button key={value} className={type === value ? "is-active" : ""} onClick={() => setType(value)}>{value === "ALL" ? "Усі" : personalizationLabels[value]}</button>)}</div><div className="owned-list">{visible.map(item => <article key={item.id}><button className="owned-list__main" onClick={() => navigate({ id: "personalization-detail", params: { personalizationId: item.id } })}><Preview item={item} small /><span><strong>{item.name}</strong><small>{personalizationLabels[item.type]}</small></span></button><ScopeMark target={item.target} compact /><Toggle checked={item.active} onChange={active => toggle(item.id, active)} label={item.name} /></article>)}</div></div>
}

function Detail({ params, back, items, setItems }: Props & { items: Personalization[]; setItems: (items: Personalization[]) => void }) {
  const item = items.find(value => value.id === params?.personalizationId) ?? items[0]
  const patch = (values: Partial<Personalization>) => setItems(items.map(value => value.id === item.id ? { ...value, ...values } : value))
  return <div className="screen personalization-detail"><PageHeader title={item.name} back={back} actions={<ScopeMark target={item.target} compact />} /><Preview item={item} detail /><div className="detail-meta"><span><small>Тип</small>{personalizationLabels[item.type]}</span><span><small>Автор</small>{item.creator}</span><span><small>Стан</small>{personalizationStatusLabels[item.status]}</span></div><div className="detail-action">{!item.owned ? <button className="primary-action" onClick={() => patch({ owned: true })}><span>Купити</span><CurrencyValue currency={item.currency} value={item.price} compact /></button> : <button className={item.active ? "secondary-action is-active" : "primary-action"} onClick={() => patch({ active: !item.active })}>{item.active ? "Вимкнути" : "Активувати"}</button>}</div></div>
}

function ScopeMark({ target, compact = false }: { target: "USER" | "ACCOUNT"; compact?: boolean }) { return <span className={`scope-symbol ${compact ? "scope-symbol--compact" : ""}`} title={target === "USER" ? "Для користувача" : "Для аккаунта"}><Glyph name={target === "USER" ? "person" : "account"} size={compact ? 14 : 15} /><span>{target === "USER" ? "User" : "Account"}</span></span> }
function Preview({ item, small, detail }: { item: Personalization; small?: boolean; detail?: boolean }) { return <div className={`personalization-preview personalization-preview--${item.type.toLowerCase()} ${small ? "is-small" : ""} ${detail ? "is-detail" : ""}`}><span className="preview-orbit" /><span className="preview-core">{item.type === "NOTIFICATION_PACK" ? <Glyph name="bell" size={small ? 17 : 26} /> : item.type === "NICK_STYLE" ? <b>Aa</b> : item.type === "PROFILE_THEME" ? <span className="theme-window" /> : item.type === "AVATAR_FRAME" ? <span className="frame-sample" /> : <Glyph name="spark" size={small ? 17 : 27} />}</span></div> }
