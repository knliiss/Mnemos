import { useMemo, useState } from "react"
import { CURRENCIES, EFFECT_TYPES, NOTIFICATION_TEMPLATE_KEYS, PERSONALIZATION_TYPES } from "../contracts"
import { creatorItems as seed } from "../mockData"
import type {
  AvatarFrameContent,
  Currency,
  EffectType,
  GlowStyle,
  GradientStop,
  NickStyleContent,
  NotificationPackContent,
  NotificationTemplate,
  NotificationTemplateKey,
  Personalization,
  PersonalizationContent,
  PersonalizationStatus,
  PersonalizationType,
  ProfileEffectContent,
  ProfileThemeContent,
  Route,
} from "../model"
import { personalizationLabels, personalizationStatusLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { ChoiceGrid, CurrencyValue, Field, IconButton, PageHeader, Segmented, Toggle } from "../shared/Primitives"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type TemplateDraft = {
  emoji: string
  soloTitle: string
  soloBody: string
  compact: string
  aggregateTitle: string
}

type GradientStopDraft = {
  color: string
  position: number
}

const targets: Record<PersonalizationType, "USER" | "ACCOUNT"> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

const placeholders: Record<NotificationTemplateKey, string> = {
  ITEM_DROP: "{player} {item} {rarity} {itemType}",
  BOOSTER: "{player} {booster}",
  DUNGEON: "{dungeon}",
  RAID: "{locations}",
  GLOBAL: "{event}",
}

const blankTemplate = (): TemplateDraft => ({
  emoji: "",
  soloTitle: "",
  soloBody: "",
  compact: "",
  aggregateTitle: "",
})

export function CreatorFeature(props: Props) {
  const [items, setItems] = useState<Personalization[]>(seed)

  if (props.routeId === "creator-revenue") {
    return <RevenuePage back={props.back} />
  }

  if (props.routeId === "personalization-editor") {
    return <Editor {...props} items={items} setItems={setItems} />
  }

  return <Studio {...props} items={items} />
}

function Studio({ navigate, back, items }: Props & { items: Personalization[] }) {
  const counts = useMemo(() => ({
    draft: items.filter((item) => item.status === "DRAFT" || item.status === "REJECTED").length,
    review: items.filter((item) => item.status === "IN_REVIEW").length,
    published: items.filter((item) => item.status === "PUBLISHED").length,
  }), [items])

  return (
    <div className="screen creator-screen">
      <PageHeader
        title="Creator"
        back={back}
        actions={<IconButton icon="plus" label="Створити" onClick={() => navigate({ id: "personalization-editor", params: { personalizationId: "new" } })} />}
      />

      <button className="creator-revenue creator-revenue--button" onClick={() => navigate({ id: "creator-revenue" })}>
        <div><CurrencyValue currency="PRIME" value={128} /><small>підтверджено</small></div>
        <span><b>18</b><small>очікує</small></span>
        <span className="creator-revenue__arrow">›</span>
      </button>

      <div className="creator-status-strip">
        <span>{counts.draft}<small>чернетки</small></span>
        <span>{counts.review}<small>перевірка</small></span>
        <span>{counts.published}<small>опубліковано</small></span>
      </div>

      <div className="creator-list">
        {items.map((item) => (
          <button key={item.id} onClick={() => navigate({ id: "personalization-editor", params: { personalizationId: item.id } })}>
            <CreatorListPreview item={item} />
            <span><strong>{item.name}</strong><small>{personalizationLabels[item.type]}</small></span>
            <span className={`status-tag status-tag--${item.status.toLowerCase()}`}>{personalizationStatusLabels[item.status]}</span>
          </button>
        ))}
      </div>
    </div>
  )
}

function RevenuePage({ back }: { back: () => void }) {
  const operations = [
    { id: "rev-1", status: "CONFIRMED", amount: 12, time: "сьогодні · 14:20" },
    { id: "rev-2", status: "PENDING", amount: 8, time: "сьогодні · 13:04" },
    { id: "rev-3", status: "FAILED", amount: 5, time: "вчора · 22:41" },
  ] as const

  return (
    <div className="screen creator-revenue-screen">
      <PageHeader title="Дохід" back={back} />
      <section className="revenue-summary">
        <div><CurrencyValue currency="PRIME" value={128} /><small>підтверджено</small></div>
        <div><CurrencyValue currency="PRIME" value={18} compact /><small>очікує</small></div>
        <div><CurrencyValue currency="PRIME" value={5} compact /><small>помилка</small></div>
      </section>
      <div className="revenue-log">
        {operations.map((operation) => (
          <div key={operation.id}>
            <i className={`revenue-state revenue-state--${operation.status.toLowerCase()}`} />
            <span>
              <strong>{operation.status === "CONFIRMED" ? "Зараховано" : operation.status === "PENDING" ? "Очікує" : "Не зараховано"}</strong>
              <small>{operation.time}</small>
            </span>
            <CurrencyValue currency="PRIME" value={operation.amount} compact />
          </div>
        ))}
      </div>
    </div>
  )
}

function Editor({ params, back, items, setItems }: Props & { items: Personalization[]; setItems: (items: Personalization[]) => void }) {
  const existing = items.find((item) => item.id === params?.personalizationId)
  const existingContent = existing?.content

  const [type, setType] = useState<PersonalizationType>(existing?.type ?? "NICK_STYLE")
  const [name, setName] = useState(existing?.name ?? "")
  const [currency, setCurrency] = useState<Currency>(existing?.currency ?? "PRIME")
  const [price, setPrice] = useState(String(existing?.price ?? 25))

  const existingGlow = existingContent && "glow" in existingContent ? existingContent.glow : null
  const [glow, setGlow] = useState(Boolean(existingGlow))
  const [glowColor, setGlowColor] = useState(existingGlow?.color ?? "#D7FF32")
  const [glowStrength, setGlowStrength] = useState(existingGlow?.strength ?? 24)

  const existingNick = existingContent?.kind === "NICK_STYLE" ? existingContent : null
  const [stops, setStops] = useState<GradientStopDraft[]>(existingNick?.colors ?? [
    { color: "#F3EFE6", position: 0 },
    { color: "#D7FF32", position: 100 },
  ])
  const [angle, setAngle] = useState(existingNick?.angleDegrees ?? 90)

  const existingTheme = existingContent?.kind === "PROFILE_THEME" ? existingContent : null
  const [background, setBackground] = useState(existingTheme?.background ?? "#090907")
  const [surface, setSurface] = useState(existingTheme?.surface ?? "#211F1B")
  const [accent, setAccent] = useState(existingTheme?.accent ?? "#D7FF32")
  const [text, setText] = useState(existingTheme?.text ?? "#F4F0E8")
  const [border, setBorder] = useState(existingTheme?.border ?? "#4C493E")

  const existingFrame = existingContent?.kind === "AVATAR_FRAME" ? existingContent : null
  const [borderWidth, setBorderWidth] = useState(existingFrame?.borderWidthPx ?? 2)
  const [radius, setRadius] = useState(existingFrame?.radiusPx ?? 12)

  const existingEffect = existingContent?.kind === "PROFILE_EFFECT" ? existingContent : null
  const [effect, setEffect] = useState<EffectType>(existingEffect?.effect ?? "AURORA")
  const [intensity, setIntensity] = useState(existingEffect?.intensity ?? 42)
  const [primary, setPrimary] = useState(existingEffect?.primaryColor ?? "#D7FF32")
  const [secondary, setSecondary] = useState(existingEffect?.secondaryColor ?? "#C68FBD")

  const existingPack = existingContent?.kind === "NOTIFICATION_PACK" ? existingContent : null
  const [mixed, setMixed] = useState(existingPack?.mixedAggregateTitle ?? "")
  const [openTemplate, setOpenTemplate] = useState<NotificationTemplateKey>("ITEM_DROP")
  const [templates, setTemplates] = useState<Record<NotificationTemplateKey, TemplateDraft>>(() => {
    if (!existingPack) {
      return {
        ITEM_DROP: blankTemplate(),
        BOOSTER: blankTemplate(),
        DUNGEON: blankTemplate(),
        RAID: blankTemplate(),
        GLOBAL: blankTemplate(),
      }
    }

    return Object.fromEntries(NOTIFICATION_TEMPLATE_KEYS.map((key) => {
      const template = existingPack.templates[key]
      return [key, { ...template, emoji: template.emoji ?? "" }]
    })) as Record<NotificationTemplateKey, TemplateDraft>
  })

  const numericPrice = Number(price)
  const priceValid = Number.isInteger(numericPrice) && numericPrice > 0
  const gradientValid = stops.length >= 1 && stops.length <= 4 && stops.every((stop) => stop.position >= 0 && stop.position <= 100) && new Set(stops.map((stop) => stop.position)).size === stops.length
  const notificationValid = mixed.trim().length > 0 && NOTIFICATION_TEMPLATE_KEYS.every((key) => {
    const template = templates[key]
    return Boolean(template.soloTitle.trim() && template.soloBody.trim() && template.compact.trim() && template.aggregateTitle.trim())
  })
  const valid = name.trim().length >= 3 && name.trim().length <= 64 && priceValid && (type === "NICK_STYLE" ? gradientValid : type === "NOTIFICATION_PACK" ? notificationValid : true)

  const glowStyle: GlowStyle | null = glow ? { color: glowColor, strength: glowStrength } : null
  const content = buildContent({
    type,
    glow: glowStyle,
    stops,
    angle,
    background,
    surface,
    accent,
    text,
    border,
    borderWidth,
    radius,
    effect,
    intensity,
    primary,
    secondary,
    mixed,
    templates,
  })

  const preview: Personalization = {
    id: existing?.id ?? "preview",
    type,
    target: targets[type],
    name: name.trim() || "Нова персоналізація",
    creator: "knalis",
    currency,
    price: priceValid ? numericPrice : 0,
    status: existing?.status ?? "DRAFT",
    owned: true,
    active: false,
    preview: "editor",
    content,
  }

  const persist = (status?: PersonalizationStatus) => {
    if (!valid) return false

    const next: Personalization = {
      ...preview,
      id: existing?.id ?? `creator-${Date.now()}`,
      name: name.trim(),
      price: numericPrice,
      status: status ?? existing?.status ?? "DRAFT",
    }

    setItems(existing ? items.map((item) => item.id === existing.id ? next : item) : [...items, next])
    return true
  }

  const finish = (status?: PersonalizationStatus) => {
    if (persist(status)) back()
  }

  const updateTemplate = (patch: Partial<TemplateDraft>) => {
    setTemplates({
      ...templates,
      [openTemplate]: {
        ...templates[openTemplate],
        ...patch,
      },
    })
  }

  const changeType = (value: PersonalizationType) => {
    setType(value)
    if (value === "NOTIFICATION_PACK" && currency === "COIN") {
      setCurrency("PRIME")
    }
  }

  return (
    <div className="screen editor-screen creator-editor-final">
      <PageHeader title={existing ? "Редагування" : "Створення"} eyebrow={existing ? personalizationStatusLabels[existing.status] : undefined} back={back} />

      <section className="creator-live-preview">
        <header><span><i />LIVE PREVIEW</span><small>{personalizationLabels[type]}</small></header>
        <LivePreview item={preview} openTemplate={openTemplate} />
      </section>

      <div className="form-stack editor-form">
        {!existing ? (
          <Field label="Тип">
            <ChoiceGrid value={type} options={PERSONALIZATION_TYPES} onChange={changeType} labels={personalizationLabels} />
          </Field>
        ) : null}

        <Field label="Назва"><input value={name} minLength={3} maxLength={64} onChange={(event) => setName(event.target.value)} /></Field>

        {type === "NICK_STYLE" ? (
          <>
            <Field label="Градієнт" hint="1–4 точки, позиції не повторюються">
              <div className="gradient-editor">
                {stops.map((stop, index) => (
                  <div key={`${index}-${stop.position}`}>
                    <Color value={stop.color} set={(value) => setStops(stops.map((item, itemIndex) => itemIndex === index ? { ...item, color: value } : item))} />
                    <input type="number" min={0} max={100} step={1} value={stop.position} onChange={(event) => setStops(stops.map((item, itemIndex) => itemIndex === index ? { ...item, position: Number(event.target.value) } : item))} />
                    {stops.length > 1 ? <button type="button" aria-label="Видалити точку" onClick={() => setStops(stops.filter((_, itemIndex) => itemIndex !== index))}><Glyph name="close" size={14} /></button> : <span />}
                  </div>
                ))}
                {stops.length < 4 ? <button type="button" className="add-inline" onClick={() => setStops([...stops, { color: "#A0A3AA", position: Math.min(100, stops.length * 30 + 20) }])}><Glyph name="plus" size={15} />Точка</button> : null}
              </div>
            </Field>
            <Range label="Кут" min={0} max={360} value={angle} set={setAngle} suffix="°" />
          </>
        ) : null}

        {type === "PROFILE_THEME" ? (
          <div className="color-matrix">
            <ColorField label="Фон" value={background} set={setBackground} />
            <ColorField label="Поверхня" value={surface} set={setSurface} />
            <ColorField label="Акцент" value={accent} set={setAccent} />
            <ColorField label="Текст" value={text} set={setText} />
            <ColorField label="Контур" value={border} set={setBorder} />
          </div>
        ) : null}

        {type === "AVATAR_FRAME" ? (
          <>
            <ColorField label="Контур" value={border} set={setBorder} />
            <Range label="Товщина" min={1} max={8} value={borderWidth} set={setBorderWidth} suffix="px" />
            <Range label="Радіус" min={0} max={32} value={radius} set={setRadius} suffix="px" />
          </>
        ) : null}

        {type === "PROFILE_EFFECT" ? (
          <>
            <Field label="Ефект"><ChoiceGrid value={effect} options={EFFECT_TYPES} onChange={setEffect} /></Field>
            <Range label="Інтенсивність" min={1} max={100} value={intensity} set={setIntensity} />
            <ColorField label="Основний" value={primary} set={setPrimary} />
            <ColorField label="Другий" value={secondary} set={setSecondary} />
          </>
        ) : null}

        {type === "NOTIFICATION_PACK" ? (
          <>
            <Field label="Заголовок групи"><input value={mixed} maxLength={512} onChange={(event) => setMixed(event.target.value)} /></Field>
            <div className="template-editor template-editor--final">
              <div className="template-tabs">
                {NOTIFICATION_TEMPLATE_KEYS.map((key) => (
                  <button key={key} type="button" className={openTemplate === key ? "is-active" : ""} onClick={() => setOpenTemplate(key)}>{templateLabel(key)}</button>
                ))}
              </div>
              <small className="placeholder-hint">Доступні: {placeholders[openTemplate]}</small>
              <Field label="Emoji"><input maxLength={16} value={templates[openTemplate].emoji} onChange={(event) => updateTemplate({ emoji: event.target.value })} /></Field>
              <Field label="Заголовок"><input maxLength={512} value={templates[openTemplate].soloTitle} onChange={(event) => updateTemplate({ soloTitle: event.target.value })} /></Field>
              <Field label="Текст"><textarea maxLength={512} value={templates[openTemplate].soloBody} onChange={(event) => updateTemplate({ soloBody: event.target.value })} /></Field>
              <Field label="Компактно"><input maxLength={512} value={templates[openTemplate].compact} onChange={(event) => updateTemplate({ compact: event.target.value })} /></Field>
              <Field label="Група"><input maxLength={512} value={templates[openTemplate].aggregateTitle} onChange={(event) => updateTemplate({ aggregateTitle: event.target.value })} /></Field>
            </div>
          </>
        ) : null}

        {type !== "PROFILE_EFFECT" && type !== "NOTIFICATION_PACK" ? (
          <>
            <div className="inline-toggle"><strong>Glow</strong><Toggle checked={glow} onChange={setGlow} label="Glow" /></div>
            {glow ? (
              <>
                <ColorField label="Glow" value={glowColor} set={setGlowColor} />
                <Range label="Сила" min={1} max={100} value={glowStrength} set={setGlowStrength} />
              </>
            ) : null}
          </>
        ) : null}

        <Field label="Ціна" hint={!priceValid && price.length ? "Додатне ціле число" : undefined}>
          <div className="price-editor">
            {type === "NOTIFICATION_PACK" ? (
              <span className="creator-fixed-currency"><CurrencyMark currency="PRIME" size={16} /> PRIME</span>
            ) : (
              <Segmented value={currency} options={CURRENCIES} onChange={setCurrency} />
            )}
            <input type="number" min={1} step={1} value={price} onChange={(event) => setPrice(event.target.value)} />
          </div>
        </Field>
      </div>

      <div className="sticky-actions creator-actions">
        <button className="secondary-action" disabled={!valid} onClick={() => finish()}>Зберегти</button>
        {!existing || existing.status === "DRAFT" || existing.status === "REJECTED" ? <button className="primary-action" disabled={!valid} onClick={() => finish("IN_REVIEW")}>На перевірку</button> : null}
        {existing?.status === "APPROVED" ? <button className="primary-action" onClick={() => finish("PUBLISHED")}>Опублікувати</button> : null}
      </div>
    </div>
  )
}

type BuildContentArgs = {
  type: PersonalizationType
  glow: GlowStyle | null
  stops: GradientStopDraft[]
  angle: number
  background: string
  surface: string
  accent: string
  text: string
  border: string
  borderWidth: number
  radius: number
  effect: EffectType
  intensity: number
  primary: string
  secondary: string
  mixed: string
  templates: Record<NotificationTemplateKey, TemplateDraft>
}

function buildContent(args: BuildContentArgs): PersonalizationContent {
  if (args.type === "NOTIFICATION_PACK") {
    const templates = Object.fromEntries(NOTIFICATION_TEMPLATE_KEYS.map((key) => [key, toNotificationTemplate(args.templates[key])])) as Record<NotificationTemplateKey, NotificationTemplate>
    const content: NotificationPackContent = {
      kind: "NOTIFICATION_PACK",
      mixedAggregateTitle: args.mixed,
      templates,
    }
    return content
  }

  if (args.type === "NICK_STYLE") {
    const content: NickStyleContent = {
      kind: "NICK_STYLE",
      colors: args.stops.map((stop): GradientStop => ({ color: stop.color, position: stop.position })),
      angleDegrees: args.angle,
      glow: args.glow,
    }
    return content
  }

  if (args.type === "PROFILE_THEME") {
    const content: ProfileThemeContent = {
      kind: "PROFILE_THEME",
      background: args.background,
      surface: args.surface,
      accent: args.accent,
      text: args.text,
      border: args.border,
      glow: args.glow,
    }
    return content
  }

  if (args.type === "AVATAR_FRAME") {
    const content: AvatarFrameContent = {
      kind: "AVATAR_FRAME",
      borderColor: args.border,
      borderWidthPx: args.borderWidth,
      radiusPx: args.radius,
      glow: args.glow,
    }
    return content
  }

  const content: ProfileEffectContent = {
    kind: "PROFILE_EFFECT",
    effect: args.effect,
    intensity: args.intensity,
    primaryColor: args.primary,
    secondaryColor: args.secondary || null,
  }
  return content
}

function toNotificationTemplate(template: TemplateDraft): NotificationTemplate {
  return {
    emoji: template.emoji.trim() || null,
    soloTitle: template.soloTitle,
    soloBody: template.soloBody,
    compact: template.compact,
    aggregateTitle: template.aggregateTitle,
  }
}

function LivePreview({ item, openTemplate }: { item: Personalization; openTemplate: NotificationTemplateKey }) {
  const content = item.content

  if (!content) return null

  if (content.kind === "NOTIFICATION_PACK") {
    const template = content.templates[openTemplate]
    return (
      <div className="creator-preview-notification">
        <span>{template.emoji || "•"}</span>
        <div>
          <strong>{template.soloTitle || "Заголовок сповіщення"}</strong>
          <p>{renderTemplate(template.soloBody || "Текст із {player} та іншими placeholders", sampleValues(openTemplate))}</p>
          <small>{renderTemplate(template.compact || "Компактний текст", sampleValues(openTemplate))}</small>
        </div>
      </div>
    )
  }

  if (content.kind === "NICK_STYLE") {
    const stops = content.colors.map((stop) => `${stop.color} ${stop.position}%`).join(", ")
    return (
      <div className="creator-preview-nick">
        <strong style={{ backgroundImage: `linear-gradient(${content.angleDegrees}deg, ${stops})`, textShadow: content.glow ? `0 0 ${Math.max(5, content.glow.strength / 3)}px ${content.glow.color}` : "none" }}>knalis</strong>
        <small>Так виглядатиме нік</small>
      </div>
    )
  }

  if (content.kind === "PROFILE_THEME") {
    return (
      <div className="creator-preview-theme" style={{ background: content.background, borderColor: content.border, color: content.text }}>
        <span style={{ background: content.surface, borderColor: content.accent }}>K</span>
        <div style={{ background: content.surface, borderColor: content.border }}>
          <strong style={{ color: content.text }}>knalis</strong>
          <small style={{ color: content.accent }}>Профіль</small>
          <i style={{ background: content.accent }} />
        </div>
      </div>
    )
  }

  if (content.kind === "AVATAR_FRAME") {
    return (
      <div className="creator-preview-frame">
        <span style={{ borderColor: content.borderColor, borderWidth: content.borderWidthPx, borderRadius: content.radiusPx, boxShadow: content.glow ? `0 0 ${Math.max(7, content.glow.strength / 2)}px ${content.glow.color}66` : "none" }}>K</span>
        <small>{content.borderWidthPx}px · radius {content.radiusPx}</small>
      </div>
    )
  }

  return (
    <div className={`creator-preview-effect creator-preview-effect--${content.effect.toLowerCase()}`} style={{ "--creator-effect-primary": content.primaryColor, "--creator-effect-secondary": content.secondaryColor ?? content.primaryColor, "--creator-effect-alpha": Math.max(.08, content.intensity / 250) } as React.CSSProperties}>
      <span>K</span>
      <small>{content.effect} · {content.intensity}%</small>
    </div>
  )
}

function CreatorListPreview({ item }: { item: Personalization }) {
  if (!item.content) {
    return <div className="personalization-preview personalization-preview--creator is-small"><span className="preview-core"><Glyph name={item.type === "NOTIFICATION_PACK" ? "bell" : "spark"} size={16} /></span></div>
  }

  return <div className="creator-list-real-preview"><LivePreview item={item} openTemplate="ITEM_DROP" /></div>
}

function sampleValues(key: NotificationTemplateKey): Record<string, string> {
  if (key === "ITEM_DROP") return { player: "north", item: "Eternal Blade", rarity: "Mythical", itemType: "Sword" }
  if (key === "BOOSTER") return { player: "vex", booster: "Luck" }
  if (key === "DUNGEON") return { dungeon: "Експерт" }
  if (key === "RAID") return { locations: "2, 4, 7" }
  return { event: "Затемнення" }
}

function renderTemplate(template: string, values: Record<string, string>) {
  return template.replace(/\{([a-zA-Z][a-zA-Z0-9_]*)}/g, (_, key: string) => values[key] ?? `{${key}}`)
}

function templateLabel(key: NotificationTemplateKey) {
  if (key === "ITEM_DROP") return "Дроп"
  if (key === "BOOSTER") return "Бустер"
  if (key === "DUNGEON") return "Данж"
  if (key === "RAID") return "Рейд"
  return "Глобал"
}

function Color({ value, set }: { value: string; set: (value: string) => void }) {
  return <input className="bare-color" type="color" value={value} onChange={(event) => set(event.target.value.toUpperCase())} />
}

function ColorField({ label, value, set }: { label: string; value: string; set: (value: string) => void }) {
  return <Field label={label}><div className="color-field"><Color value={value} set={set} /><code>{value}</code></div></Field>
}

function Range({ label, min, max, value, set, suffix = "" }: { label: string; min: number; max: number; value: number; set: (value: number) => void; suffix?: string }) {
  return <Field label={label}><input type="range" min={min} max={max} value={value} onChange={(event) => set(Number(event.target.value))} /><output>{value}{suffix}</output></Field>
}
