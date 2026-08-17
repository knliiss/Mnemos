import { useMemo, useState } from "react"
import { CURRENCIES, PERSONALIZATION_TYPES } from "../contracts"
import { currentUser, personalizations as seed } from "../mockData"
import { personalizationContentById } from "../personalizationPreviewData"
import type {
  AvatarFrameContent,
  Currency,
  NickStyleContent,
  NotificationPackContent,
  Personalization,
  PersonalizationContent,
  PersonalizationTarget,
  PersonalizationType,
  ProfileEffectContent,
  ProfileThemeContent,
  Route,
} from "../model"
import { personalizationLabels, personalizationStatusLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, PageHeader, Segmented, Toggle } from "../shared/Primitives"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type StoreType = "ALL" | PersonalizationType
type StoreView = "CATALOG" | "OWNED"
type PurchaseState = "READY" | "PENDING" | "CONFIRMED"

const targets: Record<PersonalizationType, PersonalizationTarget> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

function withContent(item: Personalization): Personalization {
  return {
    ...item,
    content: item.content ?? personalizationContentById[item.id],
  }
}

export function PersonalizationFeatureFinal(props: Props) {
  const [items, setItems] = useState<Personalization[]>(() => seed.map(withContent))
  const [storeType, setStoreType] = useState<StoreType>("ALL")
  const [storeCurrency, setStoreCurrency] = useState<"ALL" | Currency>("ALL")
  const [storeView, setStoreView] = useState<StoreView>("CATALOG")

  if (props.routeId === "personalization-owned") {
    return <OwnedPage {...props} items={items} setItems={setItems} />
  }

  if (props.routeId === "personalization-detail") {
    return <DetailPage {...props} items={items} setItems={setItems} />
  }

  return (
    <StoreRoot
      {...props}
      items={items}
      setItems={setItems}
      type={storeType}
      setType={setStoreType}
      currency={storeCurrency}
      setCurrency={setStoreCurrency}
      view={storeView}
      setView={setStoreView}
    />
  )
}

function StoreRoot({ navigate, items, setItems, type, setType, currency, setCurrency, view, setView }: Props & {
  items: Personalization[]
  setItems: (items: Personalization[]) => void
  type: StoreType
  setType: (type: StoreType) => void
  currency: "ALL" | Currency
  setCurrency: (currency: "ALL" | Currency) => void
  view: StoreView
  setView: (view: StoreView) => void
}) {
  const visible = useMemo(() => {
    const published = items.filter((item) => item.status === "PUBLISHED")
    const byView = view === "OWNED" ? published.filter((item) => item.owned) : published

    return byView.filter((item) =>
      (type === "ALL" || item.type === type) &&
      (currency === "ALL" || item.currency === currency),
    )
  }, [items, type, currency, view])

  return (
    <div className="screen store-final-screen root-screen">
      <header className="store-final-header">
        <span>
          <small>Персоналізація</small>
          <h1>Магазин</h1>
        </span>
        <button className="store-prime-pill" onClick={() => navigate({ id: "user-wallet" })}>
          <CurrencyMark currency="PRIME" size={16} />
          <strong>{currentUser.prime.toLocaleString("uk-UA")}</strong>
        </button>
      </header>

      <div className="store-view-switch" role="tablist" aria-label="Розділ магазину">
        <button className={view === "CATALOG" ? "is-active" : ""} onClick={() => setView("CATALOG")}>Вітрина</button>
        <button className={view === "OWNED" ? "is-active" : ""} onClick={() => setView("OWNED")}>
          Мої
          <span>{items.filter((item) => item.owned).length}</span>
        </button>
      </div>

      <div className="store-type-rail store-type-rail--final">
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

      <div className="store-final-toolbar">
        <span>{type === "ALL" ? (view === "OWNED" ? "Придбані" : "Усі товари") : personalizationLabels[type]}</span>
        <Segmented
          value={currency}
          options={["ALL", ...CURRENCIES] as const}
          onChange={setCurrency}
          label={(value) => value === "ALL" ? "Усі" : value}
        />
      </div>

      <div className="catalog-final-list">
        {visible.map((item) => (
          <button
            className={`catalog-final-card catalog-final-card--${item.type.toLowerCase()}`}
            key={item.id}
            onClick={() => navigate({ id: "personalization-detail", params: { personalizationId: item.id } })}
          >
            <Preview item={item} />
            <span className="catalog-final-card__copy">
              <span className="catalog-final-card__topline">
                <small>{personalizationLabels[item.type]}</small>
                <ScopeMark target={item.target} />
              </span>
              <strong>{item.name}</strong>
              <em>{item.creator}</em>
              <PreviewCaption item={item} />
            </span>
            <span className="catalog-final-card__price">
              {item.owned ? (
                <span className="catalog-state"><Glyph name="check" size={13} />{item.active ? "Активна" : "Придбано"}</span>
              ) : <CurrencyValue currency={item.currency} value={item.price} compact />}
              <b>›</b>
            </span>
          </button>
        ))}
      </div>

      {!visible.length ? <p className="inline-hint">У цьому розділі поки порожньо</p> : null}
    </div>
  )
}

function OwnedPage({ navigate, back, params, items, setItems }: Props & {
  items: Personalization[]
  setItems: (items: Personalization[]) => void
}) {
  const target = params?.target === "ACCOUNT" || params?.target === "USER" ? params.target : null
  const compatibleTypes = target ? PERSONALIZATION_TYPES.filter((value) => targets[value] === target) : PERSONALIZATION_TYPES
  const [type, setType] = useState<StoreType>("ALL")
  const visible = items.filter((item) => item.owned && (!target || item.target === target) && (type === "ALL" || item.type === type))

  const toggle = (id: string, active: boolean) => {
    setItems(items.map((item) => item.id === id ? { ...item, active } : item))
  }

  return (
    <div className="screen owned-final-screen">
      <PageHeader title={target === "ACCOUNT" ? "Персоналізації аккаунта" : target === "USER" ? "Персоналізації User" : "Мої персоналізації"} back={back} />
      <div className="compact-filter compact-filter--redesign">
        {(["ALL", ...compatibleTypes] as const).map((value) => (
          <button key={value} className={type === value ? "is-active" : ""} onClick={() => setType(value)}>
            {value === "ALL" ? "Усі" : personalizationLabels[value]}
          </button>
        ))}
      </div>
      <div className="owned-final-list">
        {visible.map((item) => (
          <article key={item.id}>
            <button className="owned-final-list__main" onClick={() => navigate({ id: "personalization-detail", params: { personalizationId: item.id } })}>
              <Preview item={item} small />
              <span><strong>{item.name}</strong><small>{personalizationLabels[item.type]}</small></span>
            </button>
            <Toggle checked={item.active} onChange={(active) => toggle(item.id, active)} label={item.name} />
          </article>
        ))}
      </div>
    </div>
  )
}

function DetailPage({ params, back, items, setItems }: Props & { items: Personalization[]; setItems: (items: Personalization[]) => void }) {
  const item = items.find((value) => value.id === params?.personalizationId) ?? items[0]
  const [purchaseState, setPurchaseState] = useState<PurchaseState>(item.owned ? "CONFIRMED" : "READY")

  const patch = (values: Partial<Personalization>) => {
    setItems(items.map((value) => value.id === item.id ? { ...value, ...values } : value))
  }

  const purchase = () => {
    if (purchaseState !== "READY") return

    setPurchaseState("PENDING")
    window.setTimeout(() => {
      patch({ owned: true })
      setPurchaseState("CONFIRMED")
    }, 900)
  }

  return (
    <div className="screen personalization-final-detail">
      <PageHeader title={item.name} back={back} actions={<ScopeMark target={item.target} compact />} />
      <Preview item={item} detail />
      <PreviewDetails item={item} />
      <div className="detail-meta detail-meta--redesign">
        <span><small>Тип</small>{personalizationLabels[item.type]}</span>
        <span><small>Автор</small>{item.creator}</span>
        <span><small>Стан</small>{personalizationStatusLabels[item.status]}</span>
      </div>
      <div className="detail-action">
        {!item.owned ? (
          <button className="primary-action" disabled={purchaseState === "PENDING"} onClick={purchase}>
            {purchaseState === "PENDING" ? (
              <><Glyph name="clock" size={15} /><span>Обробка</span></>
            ) : <><span>Купити</span><CurrencyValue currency={item.currency} value={item.price} compact /></>}
          </button>
        ) : (
          <button className={item.active ? "secondary-action is-active" : "primary-action"} onClick={() => patch({ active: !item.active })}>
            {item.active ? "Вимкнути" : "Активувати"}
          </button>
        )}
      </div>
    </div>
  )
}

function Preview({ item, small = false, detail = false }: { item: Personalization; small?: boolean; detail?: boolean }) {
  const content = item.content
  const className = `real-preview real-preview--${item.type.toLowerCase()} ${small ? "is-small" : ""} ${detail ? "is-detail" : ""}`

  if (!content) {
    return <div className={className}><Glyph name="spark" size={small ? 18 : 28} /></div>
  }

  if (content.kind === "NOTIFICATION_PACK") {
    return <NotificationPreview content={content} className={className} small={small} />
  }

  if (content.kind === "NICK_STYLE") {
    return <NickPreview content={content} className={className} />
  }

  if (content.kind === "PROFILE_THEME") {
    return <ThemePreview content={content} className={className} />
  }

  if (content.kind === "AVATAR_FRAME") {
    return <FramePreview content={content} className={className} />
  }

  return <EffectPreview content={content} className={className} />
}

function NotificationPreview({ content, className, small }: { content: NotificationPackContent; className: string; small: boolean }) {
  const template = content.templates.ITEM_DROP
  const body = renderTemplate(template.soloBody, {
    player: "north",
    item: "Eternal Blade",
    rarity: "Mythical",
    itemType: "Sword",
  })

  return (
    <div className={`${className} notification-real-preview`}>
      <span className="notification-real-preview__emoji">{template.emoji ?? "•"}</span>
      <span>
        <strong>{template.soloTitle}</strong>
        {!small ? <small>{body}</small> : null}
      </span>
    </div>
  )
}

function NickPreview({ content, className }: { content: NickStyleContent; className: string }) {
  const stops = content.colors.map((stop) => `${stop.color} ${stop.position}%`).join(", ")
  const glow = content.glow ? `0 0 ${Math.max(5, content.glow.strength / 3)}px ${content.glow.color}` : "none"

  return (
    <div className={`${className} nick-real-preview`}>
      <strong style={{ backgroundImage: `linear-gradient(${content.angleDegrees}deg, ${stops})`, textShadow: glow }}>knalis</strong>
      <small>Приклад ніка</small>
    </div>
  )
}

function ThemePreview({ content, className }: { content: ProfileThemeContent; className: string }) {
  return (
    <div className={`${className} theme-real-preview`} style={{ background: content.background, borderColor: content.border, color: content.text }}>
      <span className="theme-real-preview__avatar" style={{ background: content.surface, borderColor: content.accent }}>K</span>
      <span style={{ background: content.surface }}><b style={{ color: content.text }}>knalis</b><i style={{ background: content.accent }} /></span>
    </div>
  )
}

function FramePreview({ content, className }: { content: AvatarFrameContent; className: string }) {
  return (
    <div className={`${className} frame-real-preview`}>
      <span
        style={{
          borderColor: content.borderColor,
          borderWidth: content.borderWidthPx,
          borderRadius: content.radiusPx,
          boxShadow: content.glow ? `0 0 ${Math.max(6, content.glow.strength / 2)}px color-mix(in srgb, ${content.glow.color} 55%, transparent)` : "none",
        }}
      >
        K
      </span>
      <small>Рамка аватара</small>
    </div>
  )
}

function EffectPreview({ content, className }: { content: ProfileEffectContent; className: string }) {
  return (
    <div
      className={`${className} effect-real-preview effect-real-preview--${content.effect.toLowerCase()}`}
      style={{ "--effect-primary": content.primaryColor, "--effect-secondary": content.secondaryColor ?? content.primaryColor, "--effect-intensity": `${content.intensity}%` } as React.CSSProperties}
    >
      <span>K</span>
      <small>{content.effect}</small>
    </div>
  )
}

function PreviewCaption({ item }: { item: Personalization }) {
  const content = item.content

  if (!content) return null
  if (content.kind === "NOTIFICATION_PACK") return <small className="preview-caption">{content.templates.ITEM_DROP.compact.replace("{player}", "north").replace("{item}", "Eternal Blade")}</small>
  if (content.kind === "NICK_STYLE") return <small className="preview-caption">Градієнт · {content.angleDegrees}°</small>
  if (content.kind === "PROFILE_THEME") return <small className="preview-caption">Тема профілю</small>
  if (content.kind === "AVATAR_FRAME") return <small className="preview-caption">{content.borderWidthPx}px · radius {content.radiusPx}</small>

  return <small className="preview-caption">{content.effect} · {content.intensity}%</small>
}

function PreviewDetails({ item }: { item: Personalization }) {
  const content = item.content
  if (!content) return null

  if (content.kind === "NOTIFICATION_PACK") {
    const samples = [
      ["ITEM_DROP", content.templates.ITEM_DROP, { player: "north", item: "Eternal Blade", rarity: "Mythical", itemType: "Sword" }],
      ["BOOSTER", content.templates.BOOSTER, { player: "vex", booster: "Luck" }],
      ["GLOBAL", content.templates.GLOBAL, { event: "Затемнення" }],
    ] as const

    return (
      <section className="notification-samples">
        <div className="notification-samples__head"><h2>Як виглядатимуть сповіщення</h2></div>
        {samples.map(([key, template, values]) => (
          <article key={key}>
            <span>{template.emoji ?? "•"}</span>
            <div>
              <strong>{template.soloTitle}</strong>
              <p>{renderTemplate(template.soloBody, values)}</p>
              <small>{renderTemplate(template.compact, values)}</small>
            </div>
          </article>
        ))}
      </section>
    )
  }

  return (
    <section className="visual-preview-note">
      <Glyph name="spark" size={17} />
      <span><strong>Живий прев'ю</strong><small>Побудований із полів content, які повертає personalization-service.</small></span>
    </section>
  )
}

function renderTemplate(template: string, values: Record<string, string>) {
  return template.replace(/\{([a-zA-Z][a-zA-Z0-9_]*)}/g, (_, key: string) => values[key] ?? `{${key}}`)
}

function ScopeMark({ target, compact = false }: { target: PersonalizationTarget; compact?: boolean }) {
  return (
    <span className={`scope-symbol ${compact ? "scope-symbol--compact" : ""}`} title={target === "USER" ? "Для користувача" : "Для аккаунта"}>
      <Glyph name={target === "USER" ? "person" : "account"} size={compact ? 13 : 14} />
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
