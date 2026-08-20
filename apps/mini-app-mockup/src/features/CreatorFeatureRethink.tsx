import { AnimatePresence, motion } from "framer-motion"
import { useMemo, useState } from "react"
import { creatorItems as seed } from "../mockData"
import type {
  Currency,
  Personalization,
  PersonalizationStatus,
  PersonalizationType,
  Route,
} from "../model"
import { personalizationLabels, personalizationStatusLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, Field, PageHeader } from "../shared/Primitives"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type StudioFilter = "ALL" | "DRAFT" | "REVIEW" | "PUBLISHED"

const typeOrder: PersonalizationType[] = [
  "NICK_STYLE",
  "PROFILE_THEME",
  "AVATAR_FRAME",
  "PROFILE_EFFECT",
  "NOTIFICATION_PACK",
]

const targetByType: Record<PersonalizationType, "USER" | "ACCOUNT"> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

const typeGlyph: Record<PersonalizationType, "spark" | "creator" | "profile-mark" | "events" | "bell"> = {
  NICK_STYLE: "spark",
  PROFILE_THEME: "creator",
  AVATAR_FRAME: "profile-mark",
  PROFILE_EFFECT: "events",
  NOTIFICATION_PACK: "bell",
}

export function CreatorFeature(props: Props) {
  const [items, setItems] = useState<Personalization[]>(seed)

  if (props.routeId === "creator-revenue") {
    return <CreatorRevenueRethink back={props.back} />
  }

  if (props.routeId === "personalization-editor") {
    return (
      <CreatorEditorRethink
        {...props}
        items={items}
        setItems={setItems}
      />
    )
  }

  return <CreatorStudioRethink {...props} items={items} />
}

function CreatorStudioRethink({ navigate, back, items }: Props & { items: Personalization[] }) {
  const [filter, setFilter] = useState<StudioFilter>("ALL")
  const counts = useMemo(() => ({
    draft: items.filter((item) => item.status === "DRAFT" || item.status === "REJECTED").length,
    review: items.filter((item) => item.status === "IN_REVIEW").length,
    published: items.filter((item) => item.status === "PUBLISHED").length,
  }), [items])

  const visible = items.filter((item) => {
    if (filter === "ALL") return true
    if (filter === "DRAFT") return item.status === "DRAFT" || item.status === "REJECTED"
    if (filter === "REVIEW") return item.status === "IN_REVIEW" || item.status === "APPROVED"
    return item.status === "PUBLISHED"
  })

  return (
    <div className="screen creator-r2-screen">
      <PageHeader title="Creator Studio" back={back} />

      <motion.section
        className="creator-r2-hero"
        initial={{ opacity: 0, y: 8 }}
        animate={{ opacity: 1, y: 0 }}
      >
        <div className="creator-r2-hero__copy">
          <small>MNEMOS CREATOR</small>
          <strong>Твоя майстерня</strong>
          <p>Створюй персоналізації, перевіряй стан публікації та керуй доходом в одному просторі.</p>
        </div>
        <div className="creator-r2-hero__art" aria-hidden="true" />
        <div className="creator-r2-hero__actions">
          <button onClick={() => navigate({ id: "personalization-editor", params: { personalizationId: "new" } })}>
            <Glyph name="plus" size={16} />
            <span>Нова робота</span>
          </button>
          <button className="is-revenue" onClick={() => navigate({ id: "creator-revenue" })}>
            <CurrencyMark currency="PRIME" size={15} />
            <span>128</span>
            <small>дохід</small>
          </button>
        </div>
      </motion.section>

      <section className="creator-r2-dashboard">
        <button className={filter === "DRAFT" ? "is-active" : ""} onClick={() => setFilter(filter === "DRAFT" ? "ALL" : "DRAFT")}>
          <i className="is-draft" />
          <strong>{counts.draft}</strong>
          <span>Чернетки</span>
        </button>
        <button className={filter === "REVIEW" ? "is-active" : ""} onClick={() => setFilter(filter === "REVIEW" ? "ALL" : "REVIEW")}>
          <i className="is-review" />
          <strong>{counts.review}</strong>
          <span>Перевірка</span>
        </button>
        <button className={filter === "PUBLISHED" ? "is-active" : ""} onClick={() => setFilter(filter === "PUBLISHED" ? "ALL" : "PUBLISHED")}>
          <i className="is-published" />
          <strong>{counts.published}</strong>
          <span>Опубліковано</span>
        </button>
      </section>

      <header className="creator-r2-section-title">
        <span>
          <small>{filter === "ALL" ? "ВСІ РОБОТИ" : "ВІДФІЛЬТРОВАНО"}</small>
          <strong>Персоналізації</strong>
        </span>
        <b>{visible.length}</b>
      </header>

      <section className="creator-r2-gallery">
        <AnimatePresence initial={false}>
          {visible.map((item, index) => (
            <motion.button
              layout
              key={item.id}
              className={`creator-r2-card creator-r2-card--${item.type.toLowerCase()}`}
              initial={{ opacity: 0, y: 6 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.98 }}
              transition={{ delay: Math.min(index * 0.035, 0.12) }}
              onClick={() => navigate({ id: "personalization-editor", params: { personalizationId: item.id } })}
            >
              <span className="creator-r2-card__visual">
                <Glyph name={typeGlyph[item.type]} size={22} />
                <i />
              </span>
              <span className="creator-r2-card__copy">
                <small>{personalizationLabels[item.type]}</small>
                <strong>{item.name}</strong>
                <em>{item.target === "USER" ? "Для User" : "Для Account"}</em>
              </span>
              <span className={`creator-r2-status is-${item.status.toLowerCase()}`}>
                {personalizationStatusLabels[item.status]}
              </span>
            </motion.button>
          ))}
        </AnimatePresence>
      </section>
    </div>
  )
}

function CreatorRevenueRethink({ back }: { back: () => void }) {
  const operations = [
    { id: "rev-1", status: "CONFIRMED", amount: 12, time: "сьогодні · 14:20", item: "Slate" },
    { id: "rev-2", status: "PENDING", amount: 8, time: "сьогодні · 13:04", item: "Dense" },
    { id: "rev-3", status: "FAILED", amount: 5, time: "вчора · 22:41", item: "Mono Frame" },
  ] as const

  return (
    <div className="screen creator-r2-screen creator-r2-revenue">
      <PageHeader title="Дохід" back={back} />

      <section className="creator-r2-revenue-hero">
        <span className="creator-r2-revenue-hero__mark"><CurrencyMark currency="PRIME" size={22} /></span>
        <span className="creator-r2-revenue-hero__copy">
          <small>ДОСТУПНО</small>
          <CurrencyValue currency="PRIME" value={128} />
          <em>+36 PRIME за останні 7 днів</em>
        </span>
        <div className="creator-r2-sparkline" aria-hidden="true">
          <i /><i /><i /><i /><i /><i /><i />
        </div>
      </section>

      <section className="creator-r2-revenue-stats">
        <article><small>Очікує</small><strong>18</strong><em>PRIME</em></article>
        <article><small>Продажів</small><strong>24</strong><em>за місяць</em></article>
        <article><small>Середнє</small><strong>7.4</strong><em>PRIME</em></article>
      </section>

      <header className="creator-r2-section-title"><span><small>ОПЕРАЦІЇ</small><strong>Остання активність</strong></span></header>
      <section className="creator-r2-revenue-log">
        {operations.map((operation) => (
          <article key={operation.id}>
            <i className={`is-${operation.status.toLowerCase()}`} />
            <span><strong>{operation.item}</strong><small>{operation.time}</small></span>
            <span><CurrencyValue currency="PRIME" value={operation.amount} compact /><small>{operation.status}</small></span>
          </article>
        ))}
      </section>
    </div>
  )
}

function CreatorEditorRethink({ params, back, items, setItems }: Props & {
  items: Personalization[]
  setItems: (items: Personalization[]) => void
}) {
  const existing = items.find((item) => item.id === params?.personalizationId)
  const [type, setType] = useState<PersonalizationType>(existing?.type ?? "NICK_STYLE")
  const [name, setName] = useState(existing?.name ?? "")
  const [currency, setCurrency] = useState<Currency>(existing?.currency ?? "PRIME")
  const [price, setPrice] = useState(String(existing?.price ?? 25))
  const [accent, setAccent] = useState("#C7F24A")
  const [secondary, setSecondary] = useState("#B98DC2")
  const [intensity, setIntensity] = useState(48)
  const [message, setMessage] = useState("{player} отримав {item}")

  const numericPrice = Number(price)
  const valid = name.trim().length >= 3 && Number.isInteger(numericPrice) && numericPrice > 0

  const persist = (status: PersonalizationStatus) => {
    if (!valid) return

    const next: Personalization = {
      ...(existing ?? {
        id: `creator-${Date.now()}`,
        creator: "knalis",
        owned: true,
        active: false,
        preview: "creator",
      }),
      type,
      target: targetByType[type],
      name: name.trim(),
      currency: type === "NOTIFICATION_PACK" ? "PRIME" : currency,
      price: numericPrice,
      status,
    }

    setItems(existing
      ? items.map((item) => item.id === existing.id ? next : item)
      : [next, ...items])
    back()
  }

  return (
    <div className="screen creator-r2-screen creator-r2-editor">
      <PageHeader title={existing ? "Редагування" : "Нова робота"} back={back} />

      <section className="creator-r2-editor-stage">
        <header><span><i />LIVE</span><small>{personalizationLabels[type]}</small></header>
        <div className={`creator-r2-preview creator-r2-preview--${type.toLowerCase()}`} style={{ "--creator-r2-accent": accent, "--creator-r2-secondary": secondary, "--creator-r2-intensity": `${intensity}%` } as React.CSSProperties}>
          <span className="creator-r2-preview__mark"><Glyph name={typeGlyph[type]} size={26} /></span>
          <span className="creator-r2-preview__copy">
            <small>{targetByType[type]}</small>
            <strong>{name.trim() || "Нова персоналізація"}</strong>
            <em>{type === "NOTIFICATION_PACK" ? message : "Попередній перегляд у стилі Mnemos"}</em>
          </span>
          <i className="creator-r2-preview__pixel" />
        </div>
      </section>

      <section className="creator-r2-type-picker">
        {typeOrder.map((value) => (
          <button key={value} className={type === value ? "is-active" : ""} onClick={() => setType(value)}>
            <Glyph name={typeGlyph[value]} size={18} />
            <span>{personalizationLabels[value]}</span>
          </button>
        ))}
      </section>

      <section className="creator-r2-form-block">
        <header><small>ОСНОВНЕ</small><strong>Ідентичність</strong></header>
        <Field label="Назва"><input value={name} maxLength={64} onChange={(event) => setName(event.target.value)} placeholder="Назва персоналізації" /></Field>

        <div className="creator-r2-color-row">
          <Field label="Основний"><input type="color" value={accent} onChange={(event) => setAccent(event.target.value.toUpperCase())} /></Field>
          <Field label="Другий"><input type="color" value={secondary} onChange={(event) => setSecondary(event.target.value.toUpperCase())} /></Field>
        </div>

        <Field label="Інтенсивність">
          <div className="creator-r2-range"><input type="range" min={0} max={100} value={intensity} onChange={(event) => setIntensity(Number(event.target.value))} /><output>{intensity}%</output></div>
        </Field>

        {type === "NOTIFICATION_PACK" ? (
          <Field label="Приклад повідомлення"><textarea maxLength={512} value={message} onChange={(event) => setMessage(event.target.value)} /></Field>
        ) : null}
      </section>

      <section className="creator-r2-form-block">
        <header><small>ПУБЛІКАЦІЯ</small><strong>Ціна та статус</strong></header>
        <div className="creator-r2-price-row">
          {type === "NOTIFICATION_PACK" ? (
            <span className="creator-r2-fixed-currency"><CurrencyMark currency="PRIME" size={15} /> PRIME</span>
          ) : (
            <div className="creator-r2-currency-switch">
              {(["COIN", "PRIME"] as Currency[]).map((value) => (
                <button key={value} className={currency === value ? "is-active" : ""} onClick={() => setCurrency(value)}>{value}</button>
              ))}
            </div>
          )}
          <input type="number" min={1} step={1} value={price} onChange={(event) => setPrice(event.target.value)} />
        </div>
      </section>

      <div className="creator-r2-editor-actions">
        <button disabled={!valid} onClick={() => persist(existing?.status ?? "DRAFT")}>Зберегти</button>
        <button className="is-primary" disabled={!valid} onClick={() => persist(existing?.status === "APPROVED" ? "PUBLISHED" : "IN_REVIEW")}>
          {existing?.status === "APPROVED" ? "Опублікувати" : "На перевірку"}
        </button>
      </div>
    </div>
  )
}
