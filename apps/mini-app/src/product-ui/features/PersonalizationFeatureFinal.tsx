import { motion } from "framer-motion"
import { useMemo, useState } from "react"
import type { CSSProperties } from "react"
import {
  PersonalizationDetails,
  PersonalizationVisual,
  hydratePersonalization,
} from "../components/PersonalizationPreview"
import { CURRENCIES, PERSONALIZATION_TYPES } from "../contracts"
import { currentUser, personalizations as seed } from "../mockData"
import type {
  Currency,
  Personalization,
  PersonalizationTarget,
  PersonalizationType,
  Route,
} from "../model"
import { personalizationLabels, personalizationStatusLabels } from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import {
  CurrencyValue,
  PageHeader,
  Segmented,
} from "../shared/Primitives"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type PurchaseState = "READY" | "PENDING" | "CONFIRMED"

type TypePresentation = {
  title: string
  note: string
  impact: string
}

const targets: Record<PersonalizationType, PersonalizationTarget> = {
  NOTIFICATION_PACK: "USER",
  NICK_STYLE: "ACCOUNT",
  PROFILE_THEME: "ACCOUNT",
  AVATAR_FRAME: "ACCOUNT",
  PROFILE_EFFECT: "ACCOUNT",
}

const typePresentation: Record<PersonalizationType, TypePresentation> = {
  NOTIFICATION_PACK: {
    title: "Сповіщення",
    note: "Оформлення повідомлень Mnemos",
    impact: "Змінює тексти, emoji та подачу сповіщень про події.",
  },
  NICK_STYLE: {
    title: "Нік",
    note: "Колір, градієнт і glow",
    impact: "Змінює відображення ніка аккаунта у публічному профілі та інших account-поверхнях.",
  },
  PROFILE_THEME: {
    title: "Тема",
    note: "Фон, поверхні, рамки й акцент",
    impact: "Змінює фон публічного профілю аккаунта, поверхні карток, рамки, текст і акцентний колір.",
  },
  AVATAR_FRAME: {
    title: "Рамка",
    note: "Обрамлення голови аккаунта",
    impact: "Змінює рамку навколо голови аккаунта: колір, товщину, форму та glow.",
  },
  PROFILE_EFFECT: {
    title: "Ефект",
    note: "Декоративний ефект профілю",
    impact: "Додає анімований декоративний ефект до публічного профілю аккаунта.",
  },
}

const accountPersonalizationTypes = PERSONALIZATION_TYPES.filter(
  (type) => targets[type] === "ACCOUNT",
)

export function PersonalizationFeatureFinal(props: Props) {
  const [items, setItems] = useState<Personalization[]>(() => seed.map(hydratePersonalization))

  if (props.routeId === "personalization-owned") {
    return <OwnedPage {...props} items={items} setItems={setItems} />
  }

  if (props.routeId === "personalization-store-category") {
    return <StoreCategoryPage {...props} items={items} />
  }

  if (props.routeId === "personalization-detail") {
    return <DetailPage {...props} items={items} setItems={setItems} />
  }

  return <StoreRoot {...props} items={items} />
}

function StoreRoot({ navigate, items }: Props & { items: Personalization[] }) {
  const published = items.filter((item) => item.status === "PUBLISHED")

  return (
    <div className="screen store-v5-screen root-screen">
      <header className="store-v5-header">
        <span>
          <small>ПЕРСОНАЛІЗАЦІЯ</small>
          <h1>Магазин</h1>
          <p>Обери розділ. Каталог кожного типу відкривається окремо.</p>
        </span>

        <button className="store-prime-pill" onClick={() => navigate({ id: "user-wallet" })}>
          <CurrencyMark currency="PRIME" size={17} />
          <strong>{currentUser.prime.toLocaleString("uk-UA")}</strong>
        </button>
      </header>

      <section className="store-v5-hero">
        <span>
          <small>КАТАЛОГ</small>
          <strong>Налаштуй Mnemos під себе</strong>
          <em>Кожен тип має власний каталог і preview.</em>
        </span>
        <img src="/ui-illustrations/personalization-studio.svg" alt="" aria-hidden="true" />
      </section>

      <section className="store-v5-categories">
        {PERSONALIZATION_TYPES.map((type) => {
          const typeItems = published.filter((item) => item.type === type)
          const owned = typeItems.filter((item) => item.owned).length
          const sample = typeItems[0]

          return (
            <motion.button
              key={type}
              className={`store-v5-category store-v5-category--${type.toLowerCase()}`}
              whileTap={{ scale: 0.988 }}
              onClick={() => navigate({
                id: "personalization-store-category",
                params: { type },
              })}
            >
              <span className="store-v5-category__icon"><TypeGlyph type={type} /></span>
              <span className="store-v5-category__copy">
                <small>{targets[type] === "ACCOUNT" ? "ACCOUNT" : "USER"}</small>
                <strong>{typePresentation[type].title}</strong>
                <em>{typePresentation[type].note}</em>
                <span>{typeItems.length} у каталозі{owned ? ` · ${owned} придбано` : ""}</span>
              </span>
              {sample ? (
                <span className="store-v5-category__preview">
                  <PersonalizationVisual item={sample} small />
                </span>
              ) : (
                <span className="store-v5-category__preview is-empty"><Glyph name="spark" size={20} /></span>
              )}
            </motion.button>
          )
        })}
      </section>
    </div>
  )
}

function StoreCategoryPage({
  params,
  navigate,
  back,
  items,
}: Props & {
  items: Personalization[]
}) {
  const type = readPersonalizationType(params?.type) ?? "PROFILE_THEME"
  const [currency, setCurrency] = useState<"ALL" | Currency>("ALL")
  const [page, setPage] = useState(0)
  const pageSize = 3

  const visible = useMemo(
    () => items.filter((item) =>
      item.status === "PUBLISHED"
      && item.type === type
      && (currency === "ALL" || item.currency === currency),
    ),
    [currency, items, type],
  )

  const pageCount = Math.max(1, Math.ceil(visible.length / pageSize))
  const safePage = Math.min(page, pageCount - 1)
  const pageItems = visible.slice(safePage * pageSize, safePage * pageSize + pageSize)

  const selectCurrency = (next: "ALL" | Currency) => {
    setCurrency(next)
    setPage(0)
  }

  return (
    <div className="screen store-category-v5">
      <PageHeader title={typePresentation[type].title} back={back} />

      <section className="store-category-v5__intro">
        <span className="store-category-v5__icon"><TypeGlyph type={type} /></span>
        <span>
          <small>{targets[type] === "ACCOUNT" ? "ДЛЯ АККАУНТА" : "ДЛЯ USER"}</small>
          <strong>{typePresentation[type].note}</strong>
          <em>{typePresentation[type].impact}</em>
        </span>
      </section>

      <div className="store-category-v5__toolbar">
        <span>{visible.length} позицій</span>
        <Segmented
          value={currency}
          options={["ALL", ...CURRENCIES] as const}
          onChange={selectCurrency}
          label={(value) => value === "ALL" ? "Усі" : value}
        />
      </div>

      <div className="store-category-v5__list">
        {pageItems.map((item) => (
          <motion.button
            key={item.id}
            className="store-category-v5__card"
            whileTap={{ scale: 0.992 }}
            onClick={() => navigate({
              id: "personalization-detail",
              params: { personalizationId: item.id },
            })}
          >
            <span className="store-category-v5__visual">
              <PersonalizationVisual item={item} />
            </span>
            <span className="store-category-v5__copy">
              <small>{personalizationLabels[item.type]}</small>
              <strong>{item.name}</strong>
              <em>{item.creator}</em>
              <PreviewCaption item={item} />
            </span>
            <span className="store-category-v5__price">
              {item.owned ? (
                <span className="catalog-state">
                  <Glyph name="check" size={13} />
                  {item.active ? "Активна" : "Придбано"}
                </span>
              ) : (
                <CurrencyValue currency={item.currency} value={item.price} compact />
              )}
            </span>
          </motion.button>
        ))}

        {!pageItems.length ? <p className="inline-hint">У цьому розділі поки порожньо</p> : null}
      </div>

      {pageCount > 1 ? (
        <Pager page={safePage} pageCount={pageCount} onChange={setPage} />
      ) : null}
    </div>
  )
}

function OwnedPage({
  navigate,
  back,
  params,
  items,
  setItems,
}: Props & {
  items: Personalization[]
  setItems: (items: Personalization[]) => void
}) {
  const target = params?.target === "ACCOUNT" || params?.target === "USER"
    ? params.target
    : null
  const type = readPersonalizationType(params?.type)

  if (target === "ACCOUNT" && type && targets[type] === "ACCOUNT") {
    return (
      <AccountPersonalizationCategory
        type={type}
        navigate={navigate}
        back={back}
        items={items}
        setItems={setItems}
      />
    )
  }

  if (target === "ACCOUNT") {
    return <AccountPersonalizationHome navigate={navigate} back={back} items={items} />
  }

  const visible = items.filter((item) => item.owned && (!target || item.target === target))

  return (
    <div className="screen owned-v5-screen">
      <PageHeader title={target === "USER" ? "Персоналізації User" : "Мої персоналізації"} back={back} />

      <section className="owned-v5-list">
        {visible.map((item) => (
          <motion.button
            key={item.id}
            whileTap={{ scale: 0.992 }}
            onClick={() => navigate({
              id: "personalization-detail",
              params: { personalizationId: item.id },
            })}
          >
            <PersonalizationVisual item={item} small />
            <span>
              <small>{personalizationLabels[item.type]}</small>
              <strong>{item.name}</strong>
              <em>{item.active ? "Активна" : "Придбано"}</em>
            </span>
          </motion.button>
        ))}
      </section>
    </div>
  )
}

function AccountPersonalizationHome({
  navigate,
  back,
  items,
}: {
  navigate: (route: Route) => void
  back: () => void
  items: Personalization[]
}) {
  const owned = items.filter((item) => item.owned && item.target === "ACCOUNT")
  const active = owned.filter((item) => item.active)

  return (
    <div className="screen account-personalization-v5">
      <PageHeader title="Персоналізація аккаунта" back={back} />

      <section className="account-personalization-v5__hero">
        <span>
          <small>ОБРАЗ АККАУНТА</small>
          <strong>Кожен тип — окремо</strong>
          <em>{active.length} активні · {owned.length} придбані</em>
        </span>
        <img src="/ui-illustrations/personalization-studio.svg" alt="" aria-hidden="true" />
      </section>

      <section className="account-personalization-v5__categories">
        {accountPersonalizationTypes.map((type) => {
          const typeItems = owned.filter((item) => item.type === type)
          const selected = typeItems.find((item) => item.active) ?? null

          return (
            <motion.button
              key={type}
              className={selected ? "is-selected" : ""}
              whileTap={{ scale: 0.99 }}
              onClick={() => navigate({
                id: "personalization-owned",
                params: { target: "ACCOUNT", type },
              })}
            >
              <span className="account-personalization-v5__category-icon"><TypeGlyph type={type} /></span>
              <span className="account-personalization-v5__category-copy">
                <small>{typePresentation[type].title}</small>
                <strong>{selected?.name ?? "Не вибрано"}</strong>
                <em>{typePresentation[type].note}</em>
                <span>{typeItems.length} придбані</span>
              </span>
              <span className="account-personalization-v5__category-preview">
                {selected ? <PersonalizationVisual item={selected} small /> : <Glyph name="spark" size={18} />}
              </span>
            </motion.button>
          )
        })}
      </section>
    </div>
  )
}

function AccountPersonalizationCategory({
  type,
  navigate,
  back,
  items,
  setItems,
}: {
  type: PersonalizationType
  navigate: (route: Route) => void
  back: () => void
  items: Personalization[]
  setItems: (items: Personalization[]) => void
}) {
  const typeItems = items.filter((item) => item.owned && item.target === "ACCOUNT" && item.type === type)
  const selected = typeItems.find((item) => item.active) ?? null
  const [page, setPage] = useState(() => Math.max(0, typeItems.findIndex((item) => item.active)))
  const safePage = Math.min(page, Math.max(0, typeItems.length - 1))
  const current = typeItems[safePage] ?? null

  const choose = (item: Personalization) => {
    const removeSelection = item.active

    setItems(items.map((candidate) => {
      if (candidate.target !== "ACCOUNT" || candidate.type !== type) return candidate
      if (candidate.id === item.id) return { ...candidate, active: !removeSelection }

      return { ...candidate, active: false }
    }))
  }

  return (
    <div className="screen account-personalization-category-v5">
      <PageHeader title={typePresentation[type].title} back={back} />

      <section className="account-personalization-category-v5__intro">
        <span className="account-personalization-category-v5__icon"><TypeGlyph type={type} /></span>
        <span>
          <small>ЩО ЗМІНЮЄ</small>
          <strong>{typePresentation[type].note}</strong>
          <em>{typePresentation[type].impact}</em>
        </span>
      </section>

      <section className="account-personalization-category-v5__selected">
        <small>ЗАРАЗ ВИБРАНО</small>
        <strong>{selected?.name ?? "Нічого"}</strong>
        <span>{selected ? "Натисни на цю персоналізацію ще раз, щоб прибрати вибір." : "Обери одну з придбаних персоналізацій нижче."}</span>
      </section>

      {current ? (
        <section className="account-personalization-category-v5__pager">
          <motion.button
            className={`account-personalization-category-v5__choice ${current.active ? "is-active" : ""}`}
            whileTap={{ scale: 0.993 }}
            aria-pressed={current.active}
            onClick={() => choose(current)}
          >
            <span className="account-personalization-category-v5__choice-head">
              <span>
                <small>{personalizationLabels[current.type]}</small>
                <strong>{current.name}</strong>
                <em>by {current.creator}</em>
              </span>
              <span className={current.active ? "is-active" : ""}>
                {current.active ? <Glyph name="check" size={14} /> : null}
                {current.active ? "Вибрано" : "Вибрати"}
              </span>
            </span>

            <PersonalizationImpactPreview item={current} />
          </motion.button>

          <div className="account-personalization-category-v5__controls">
            <button
              disabled={safePage === 0}
              onClick={() => setPage((value) => Math.max(0, value - 1))}
              aria-label="Попередня персоналізація"
            >
              ‹
            </button>
            <span>{safePage + 1} / {typeItems.length}</span>
            <button
              disabled={safePage >= typeItems.length - 1}
              onClick={() => setPage((value) => Math.min(typeItems.length - 1, value + 1))}
              aria-label="Наступна персоналізація"
            >
              ›
            </button>
          </div>

          <button
            className="account-personalization-category-v5__details"
            onClick={() => navigate({
              id: "personalization-detail",
              params: { personalizationId: current.id },
            })}
          >
            Деталі персоналізації
          </button>
        </section>
      ) : (
        <section className="account-personalization-category-v5__empty">
          <TypeGlyph type={type} />
          <strong>Немає придбаних</strong>
          <span>Відкрий каталог цього типу, щоб додати варіанти.</span>
          <button onClick={() => navigate({ id: "personalization-store-category", params: { type } })}>
            <Glyph name="store" size={15} />
            До магазину
          </button>
        </section>
      )}
    </div>
  )
}

function DetailPage({
  params,
  back,
  items,
  setItems,
}: Props & {
  items: Personalization[]
  setItems: (items: Personalization[]) => void
}) {
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

  const toggleSelection = () => {
    if (!item.owned) return

    setItems(items.map((candidate) => {
      if (candidate.target !== item.target || candidate.type !== item.type) return candidate
      if (candidate.id === item.id) return { ...candidate, active: !item.active }

      return item.active ? candidate : { ...candidate, active: false }
    }))
  }

  return (
    <div className="screen personalization-detail-v5">
      <PageHeader
        title={item.name}
        back={back}
        actions={<ScopeMark target={item.target} compact />}
      />

      <section className="personalization-detail-v5__hero">
        <span className="personalization-detail-v5__hero-copy">
          <small>{personalizationLabels[item.type]}</small>
          <strong>{item.name}</strong>
          <em>by {item.creator}</em>
          <p>{typePresentation[item.type].impact}</p>
        </span>

        <img src="/ui-illustrations/personalization-studio.svg" alt="" aria-hidden="true" />

        <span className="personalization-detail-v5__hero-price">
          {item.owned ? (
            <><Glyph name="check" size={14} />Придбано</>
          ) : (
            <CurrencyValue currency={item.currency} value={item.price} compact />
          )}
        </span>
      </section>

      <section className="personalization-detail-v5__impact">
        <header>
          <span>
            <small>LIVE PREVIEW</small>
            <strong>Що саме зміниться</strong>
          </span>
          <TypeGlyph type={item.type} />
        </header>

        <PersonalizationImpactPreview item={item} />
      </section>

      <PersonalizationDetails item={item} />

      <section className="personalization-detail-v5__meta">
        <span><small>Тип</small><strong>{personalizationLabels[item.type]}</strong></span>
        <span><small>Автор</small><strong>{item.creator}</strong></span>
        <span><small>Стан</small><strong>{personalizationStatusLabels[item.status]}</strong></span>
      </section>

      <div className="personalization-detail-v5__action">
        {!item.owned ? (
          <button disabled={purchaseState === "PENDING"} onClick={purchase}>
            {purchaseState === "PENDING" ? (
              <><Glyph name="clock" size={15} />Обробка</>
            ) : (
              <>Купити <CurrencyValue currency={item.currency} value={item.price} compact /></>
            )}
          </button>
        ) : (
          <button className={item.active ? "is-active" : ""} onClick={toggleSelection}>
            {item.active ? (
              <><Glyph name="close" size={15} />Прибрати вибір</>
            ) : (
              <><Glyph name="check" size={15} />Вибрати</>
            )}
          </button>
        )}
      </div>
    </div>
  )
}

function PersonalizationImpactPreview({ item }: { item: Personalization }) {
  const content = item.content

  if (!content) return <PersonalizationVisual item={item} detail />

  if (content.kind === "PROFILE_THEME") {
    return (
      <div
        className="impact-preview impact-preview--theme"
        style={{
          "--impact-background": content.background,
          "--impact-surface": content.surface,
          "--impact-accent": content.accent,
          "--impact-text": content.text,
          "--impact-border": content.border,
        } as CSSProperties}
      >
        <span className="impact-preview__caption">Публічний профіль аккаунта</span>
        <div className="impact-theme-card">
          <span className="impact-theme-card__avatar">K</span>
          <span className="impact-theme-card__copy">
            <strong>knalis</strong>
            <i />
            <i />
          </span>
          <span className="impact-theme-card__accent" />
        </div>
        <div className="impact-theme-parts">
          <span><i style={{ background: content.background }} />Фон</span>
          <span><i style={{ background: content.surface }} />Поверхні</span>
          <span><i style={{ background: content.accent }} />Акцент</span>
          <span><i style={{ background: content.border }} />Рамки</span>
        </div>
      </div>
    )
  }

  if (content.kind === "NICK_STYLE") {
    const stops = content.colors.map((stop) => `${stop.color} ${stop.position}%`).join(", ")

    return (
      <div className="impact-preview impact-preview--nick">
        <span><small>БЕЗ СТИЛЮ</small><strong>knalis</strong></span>
        <Glyph name="swap" size={17} />
        <span>
          <small>ЗІ СТИЛЕМ</small>
          <strong style={{
            backgroundImage: `linear-gradient(${content.angleDegrees}deg, ${stops})`,
            textShadow: content.glow ? `0 0 ${Math.max(5, content.glow.strength / 3)}px ${content.glow.color}` : "none",
          }}>knalis</strong>
        </span>
      </div>
    )
  }

  if (content.kind === "AVATAR_FRAME") {
    return (
      <div className="impact-preview impact-preview--frame">
        <span className="impact-preview__caption">Голова аккаунта</span>
        <span
          className="impact-frame-head"
          style={{
            borderColor: content.borderColor,
            borderWidth: content.borderWidthPx,
            borderRadius: content.radiusPx,
            boxShadow: content.glow
              ? `0 0 ${Math.max(6, content.glow.strength / 2)}px color-mix(in srgb, ${content.glow.color} 55%, transparent)`
              : "none",
          }}
        >K</span>
      </div>
    )
  }

  return (
    <div className="impact-preview impact-preview--generic">
      <PersonalizationVisual item={item} detail />
    </div>
  )
}

function Pager({
  page,
  pageCount,
  onChange,
}: {
  page: number
  pageCount: number
  onChange: (page: number) => void
}) {
  return (
    <nav className="personalization-pager" aria-label="Сторінки каталогу">
      <button disabled={page === 0} onClick={() => onChange(Math.max(0, page - 1))}>‹</button>
      <span>{page + 1} / {pageCount}</span>
      <button disabled={page >= pageCount - 1} onClick={() => onChange(Math.min(pageCount - 1, page + 1))}>›</button>
    </nav>
  )
}

function PreviewCaption({ item }: { item: Personalization }) {
  const content = item.content

  if (!content) return null

  if (content.kind === "NOTIFICATION_PACK") {
    return <small className="preview-caption">Шаблони для подій і агрегації</small>
  }

  if (content.kind === "NICK_STYLE") {
    return <small className="preview-caption">Градієнт · {content.angleDegrees}°</small>
  }

  if (content.kind === "PROFILE_THEME") {
    return <small className="preview-caption">Фон · поверхні · акцент · рамки</small>
  }

  if (content.kind === "AVATAR_FRAME") {
    return <small className="preview-caption">{content.borderWidthPx}px · radius {content.radiusPx}px</small>
  }

  return <small className="preview-caption">{content.effect} · {content.intensity}%</small>
}

function ScopeMark({
  target,
  compact = false,
}: {
  target: PersonalizationTarget
  compact?: boolean
}) {
  return (
    <span
      className={`scope-symbol ${compact ? "scope-symbol--compact" : ""}`}
      title={target === "USER" ? "Для користувача" : "Для аккаунта"}
    >
      <Glyph name={target === "USER" ? "person" : "account"} size={compact ? 13 : 14} />
      <span>{target === "USER" ? "User" : "Account"}</span>
    </span>
  )
}

function TypeGlyph({ type }: { type: PersonalizationType }) {
  if (type === "NOTIFICATION_PACK") return <Glyph name="bell" size={18} />
  if (type === "NICK_STYLE") return <span className="type-aa">Aa</span>
  if (type === "PROFILE_THEME") return <Glyph name="account" size={18} />
  if (type === "AVATAR_FRAME") return <span className="type-frame" />

  return <Glyph name="spark" size={18} />
}

function readPersonalizationType(value?: string): PersonalizationType | null {
  return PERSONALIZATION_TYPES.find((type) => type === value) ?? null
}
