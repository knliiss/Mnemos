import { useMemo, useState } from "react"
import { CURRENCIES, PERSONALIZATION_TYPES } from "../contracts"
import { currentUser, personalizations as seed } from "../mockData"
import type { Currency, Personalization, PersonalizationTarget, PersonalizationType, Route } from "../model"
import { personalizationLabels, personalizationStatusLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader, Segmented, Toggle } from "../shared/Primitives"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}
type PurchaseState = "READY" | "PENDING" | "CONFIRMED"
type StoreType = "ALL" | PersonalizationType

const targets: Record<PersonalizationType, PersonalizationTarget> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

export function PersonalizationFeature(props: Props) {
  const [items, setItems] = useState<Personalization[]>(seed)
  const [storeType, setStoreType] = useState<StoreType>("ALL")
  const [storeCurrency, setStoreCurrency] = useState<"ALL" | Currency>("ALL")

  if (props.routeId === "personalization-owned") return <Owned {...props} items={items} setItems={setItems} />
  if (props.routeId === "personalization-detail") return <Detail {...props} items={items} setItems={setItems} />

  return (
    <Store
      {...props}
      items={items}
      type={storeType}
      setType={setStoreType}
      currency={storeCurrency}
      setCurrency={setStoreCurrency}
    />
  )
}

function Store({ navigate, items, type, setType, currency, setCurrency }: Props & {
  items: Personalization[]
  type: StoreType
  setType: (type: StoreType) => void
  currency: "ALL" | Currency
  setCurrency: (currency: "ALL" | Currency) => void
}) {
  const visible = useMemo(
    () => items.filter((item) => item.status === "PUBLISHED" && (type === "ALL" || item.type === type) && (currency === "ALL" || item.currency === currency)),
    [items, type, currency],
  )

  return (
    <div className="screen store-root-screen root-screen">
      <header className="root-title-block store-title-block">
        <span><h1>Магазин</h1><p>Персоналізації Mnemos</p></span>
        <button className="store-owned-button" onClick={() => navigate({ id: "personalization-owned" })}>
          <Glyph name="spark" size={17} />
          Мої
        </button>
      </header>

      <button className="store-prime-card" onClick={() => navigate({ id: "user-wallet" })}>
        <span className="store-prime-card__mark"><CurrencyMark currency="PRIME" size={24} /></span>
        <span><small>PRIME · User</small><strong>{currentUser.prime.toLocaleString("uk-UA")}</strong></span>
        <b>Історія ›</b>
      </button>

      <div className="store-type-rail">
        <button className={type === "ALL" ? "is-active" : ""} onClick={() => setType("ALL")}>
          <Glyph name="store" size={16} />
          Усі
        </button>
        {PERSONALIZATION_TYPES.map((value) => (
          <button key={value} className={type === value ? "is-active" : ""} onClick={() => setType(value)}>
            <TypeGlyph type={value} />
            {personalizationLabels[value]}
          </button>
        ))}
      </div>

      <div className="store-toolbar store-toolbar--redesign">
        <span>{type === "ALL" ? "Усі персоналізації" : personalizationLabels[type]}</span>
        <Segmented value={currency} options={["ALL", ...CURRENCIES] as const} onChange={setCurrency} label={(value) => value === "ALL" ? "Усі" : value} />
      </div>

      <div className="catalog-grid catalog-grid--redesign">
        {visible.map((item) => (
          <button className={`catalog-card catalog-card--${item.type.toLowerCase()}`} key={item.id} onClick={() => navigate({ id: "personalization-detail", params: { personalizationId: item.id } })}>
            <Preview item={item} />
            <span className="catalog-card__scope"><ScopeMark target={item.target} compact /></span>
            <span className="catalog-card__copy"><strong>{item.name}</strong><small>{personalizationLabels[item.type]} · {item.creator}</small></span>
            <span className="catalog-card__footer">
              {item.owned ? <span className="catalog-state"><Glyph name="check" size={13} />{item.active ? "Активна" : "Придбано"}</span> : <CurrencyValue currency={item.currency} value={item.price} compact />}
              <b>›</b>
            </span>
          </button>
        ))}
      </div>

      {!visible.length ? <p className="inline-hint">У цій категорії поки порожньо</p> : null}
    </div>
  )
}

function Owned({ navigate, back, params, items, setItems }: Props & { items: Personalization[]; setItems: (items: Personalization[]) => void }) {
  const target = params?.target === "ACCOUNT" || params?.target === "USER" ? params.target : null
  const compatibleTypes = target
    ? PERSONALIZATION_TYPES.filter((value) => targets[value] === target)
    : PERSONALIZATION_TYPES
  const [type, setType] = useState<StoreType>("ALL")
  const visible = items.filter((item) => item.owned && (!target || item.target === target) && (type === "ALL" || item.type === type))
  const toggle = (id: string, active: boolean) => setItems(items.map((item) => item.id === id ? { ...item, active } : item))

  return (
    <div className="screen owned-personalization-screen">
      <PageHeader title={target === "ACCOUNT" ? "Персоналізації аккаунта" : target === "USER" ? "Персоналізації User" : "Мої персоналізації"} back={back} />
      <div className="compact-filter compact-filter--redesign">
        {(["ALL", ...compatibleTypes] as const).map((value) => (
          <button key={value} className={type === value ? "is-active" : ""} onClick={() => setType(value)}>{value === "ALL" ? "Усі" : personalizationLabels[value]}</button>
        ))}
      </div>
      <div className="owned-list owned-list--redesign">
        {visible.map((item) => (
          <article key={item.id}>
            <button className="owned-list__main" onClick={() => navigate({ id: "personalization-detail", params: { personalizationId: item.id } })}>
              <Preview item={item} small />
              <span><strong>{item.name}</strong><small>{personalizationLabels[item.type]}</small></span>
            </button>
            <ScopeMark target={item.target} compact />
            <Toggle checked={item.active} onChange={(active) => toggle(item.id, active)} label={item.name} />
          </article>
        ))}
      </div>
    </div>
  )
}

function Detail({ params, back, items, setItems }: Props & { items: Personalization[]; setItems: (items: Personalization[]) => void }) {
  const item = items.find((value) => value.id === params?.personalizationId) ?? items[0]
  const [purchaseState, setPurchaseState] = useState<PurchaseState>(item.owned ? "CONFIRMED" : "READY")
  const patch = (values: Partial<Personalization>) => setItems(items.map((value) => value.id === item.id ? { ...value, ...values } : value))

  const purchase = () => {
    if (purchaseState !== "READY") return

    setPurchaseState("PENDING")
    window.setTimeout(() => {
      patch({ owned: true })
      setPurchaseState("CONFIRMED")
    }, 900)
  }

  return (
    <div className="screen personalization-detail personalization-detail--redesign">
      <PageHeader title={item.name} back={back} actions={<ScopeMark target={item.target} compact />} />
      <Preview item={item} detail />
      <div className="detail-meta detail-meta--redesign">
        <span><small>Тип</small>{personalizationLabels[item.type]}</span>
        <span><small>Автор</small>{item.creator}</span>
        <span><small>Стан</small>{personalizationStatusLabels[item.status]}</span>
      </div>
      <div className="detail-action">
        {!item.owned ? (
          <button className="primary-action" disabled={purchaseState === "PENDING"} onClick={purchase}>
            {purchaseState === "PENDING" ? <><Glyph name="clock" size={15} /><span>Обробка</span></> : <><span>Купити</span><CurrencyValue currency={item.currency} value={item.price} compact /></>}
          </button>
        ) : (
          <button className={item.active ? "secondary-action is-active" : "primary-action"} onClick={() => patch({ active: !item.active })}>{item.active ? "Вимкнути" : "Активувати"}</button>
        )}
      </div>
    </div>
  )
}

function ScopeMark({ target, compact = false }: { target: PersonalizationTarget; compact?: boolean }) {
  return (
    <span className={`scope-symbol ${compact ? "scope-symbol--compact" : ""}`} title={target === "USER" ? "Для користувача" : "Для аккаунта"}>
      <Glyph name={target === "USER" ? "person" : "account"} size={compact ? 13 : 15} />
      <span>{target === "USER" ? "User" : "Account"}</span>
    </span>
  )
}

function TypeGlyph({ type }: { type: PersonalizationType }) {
  if (type === "NOTIFICATION_PACK") return <Glyph name="bell" size={16} />
  if (type === "NICK_STYLE") return <span className="type-aa">Aa</span>
  if (type === "PROFILE_THEME") return <Glyph name="account" size={16} />
  if (type === "AVATAR_FRAME") return <span className="type-frame" />

  return <Glyph name="spark" size={16} />
}

function Preview({ item, small, detail }: { item: Personalization; small?: boolean; detail?: boolean }) {
  return (
    <div className={`personalization-preview personalization-preview--${item.type.toLowerCase()} ${small ? "is-small" : ""} ${detail ? "is-detail" : ""}`}>
      <span className="preview-orbit" />
      <span className="preview-core">
        {item.type === "NOTIFICATION_PACK"
          ? <Glyph name="bell" size={small ? 17 : 26} />
          : item.type === "NICK_STYLE"
            ? <b>Aa</b>
            : item.type === "PROFILE_THEME"
              ? <span className="theme-window" />
              : item.type === "AVATAR_FRAME"
                ? <span className="frame-sample" />
                : <Glyph name="spark" size={small ? 17 : 27} />}
      </span>
    </div>
  )
}
