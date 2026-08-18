import { useMemo, useState } from "react"
import {
  PersonalizationDetails,
  PersonalizationVisual,
  hydratePersonalization,
} from "../components/PersonalizationPreview"
import { reviewQueue } from "../mockData"
import type {
  Personalization,
  PersonalizationTarget,
} from "../model"
import {
  personalizationLabels,
  personalizationStatusLabels,
} from "../presentation"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import {
  CurrencyValue,
  Field,
  PageHeader,
} from "../shared/Primitives"

type ReviewScope = "ALL" | PersonalizationTarget

const reviewScopeLabels: Record<ReviewScope, string> = {
  ALL: "Усі",
  USER: "User",
  ACCOUNT: "Account",
}

export function PersonalizationReviewOverhaul({ back }: { back: () => void }) {
  const [items, setItems] = useState<Personalization[]>(
    () => reviewQueue.map(hydratePersonalization),
  )
  const [selectedId, setSelectedId] = useState<string | null>(
    reviewQueue[0]?.id ?? null,
  )
  const [price, setPrice] = useState(
    String(reviewQueue[0]?.price ?? 25),
  )
  const [reason, setReason] = useState("")
  const [query, setQuery] = useState("")
  const [scope, setScope] = useState<ReviewScope>("ALL")

  const selected = items.find((item) => item.id === selectedId) ?? null
  const numericPrice = Number(price)
  const priceValid = Number.isInteger(numericPrice) && numericPrice > 0

  const visible = useMemo(() => {
    const normalized = query.trim().toLowerCase()

    return items.filter((item) => {
      const scopeMatches = scope === "ALL" || item.target === scope
      const queryMatches = !normalized
        || item.name.toLowerCase().includes(normalized)
        || item.creator.toLowerCase().includes(normalized)
        || personalizationLabels[item.type].toLowerCase().includes(normalized)

      return scopeMatches && queryMatches
    })
  }, [items, query, scope])

  const pendingCount = items.filter((item) => item.status === "IN_REVIEW").length

  const select = (item: Personalization) => {
    setSelectedId(item.id)
    setPrice(String(item.price))
    setReason("")
  }

  const patch = (
    id: string,
    values: Partial<Personalization>,
  ) => {
    setItems(items.map((item) =>
      item.id === id
        ? { ...item, ...values }
        : item,
    ))
  }

  const moveToNextPending = (currentId: string) => {
    const next = items.find((item) =>
      item.id !== currentId && item.status === "IN_REVIEW",
    )

    if (!next) {
      setSelectedId(null)
      return
    }

    setSelectedId(next.id)
    setPrice(String(next.price))
    setReason("")
  }

  const approve = () => {
    if (!selected || !priceValid) return

    patch(selected.id, {
      status: "APPROVED",
      price: numericPrice,
    })
    moveToNextPending(selected.id)
  }

  const reject = () => {
    if (!selected || !reason.trim()) return

    patch(selected.id, {
      status: "REJECTED",
    })
    moveToNextPending(selected.id)
  }

  return (
    <div className="screen review-overhaul-screen">
      <PageHeader title="Перевірка" back={back} />

      <section className="review-overview">
        <span className="review-overview__mark">
          <Glyph name="creator" size={20} />
        </span>
        <span>
          <small>Черга персоналізацій</small>
          <strong>{pendingCount} очікують рішення</strong>
          <em>Перевіряй вигляд, scope, ціну і контент перед схваленням.</em>
        </span>
      </section>

      <div className="review-toolbar">
        <label>
          <Glyph name="search" size={16} />
          <input
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            placeholder="Назва, автор або тип"
          />
        </label>

        <div className="review-scope">
          {(["ALL", "USER", "ACCOUNT"] as const).map((value) => (
            <button
              key={value}
              className={scope === value ? "is-active" : ""}
              onClick={() => setScope(value)}
            >
              {reviewScopeLabels[value]}
            </button>
          ))}
        </div>
      </div>

      <div className="review-queue-grid">
        {visible.map((item) => (
          <button
            key={item.id}
            className={
              selectedId === item.id
                ? "review-queue-card is-selected"
                : "review-queue-card"
            }
            onClick={() => select(item)}
          >
            <PersonalizationVisual item={item} small />

            <span className="review-queue-card__copy">
              <span className="review-queue-card__meta">
                <small>{personalizationLabels[item.type]}</small>
                <i className={`is-${item.target.toLowerCase()}`}>
                  {item.target}
                </i>
              </span>
              <strong>{item.name}</strong>
              <em>by {item.creator}</em>
            </span>

            <span className="review-queue-card__side">
              <CurrencyValue
                currency={item.currency}
                value={item.price}
                compact
              />
              <b className={`status-${item.status.toLowerCase()}`}>
                {personalizationStatusLabels[item.status]}
              </b>
            </span>
          </button>
        ))}
      </div>

      {selected ? (
        <section className="review-workbench">
          <header>
            <span>
              <small>SELECTED</small>
              <strong>{selected.name}</strong>
              <em>
                {personalizationLabels[selected.type]} · {selected.target}
              </em>
            </span>
            <button
              aria-label="Закрити"
              onClick={() => setSelectedId(null)}
            >
              <Glyph name="close" size={16} />
            </button>
          </header>

          <PersonalizationVisual item={selected} detail />
          <PersonalizationDetails item={selected} />

          <div className="review-metadata">
            <article>
              <small>Автор</small>
              <strong>{selected.creator}</strong>
            </article>
            <article>
              <small>Scope</small>
              <strong>{selected.target}</strong>
            </article>
            <article>
              <small>Статус</small>
              <strong>{personalizationStatusLabels[selected.status]}</strong>
            </article>
          </div>

          <div className="review-decision-grid">
            <section className="review-approve-panel">
              <Field
                label="Ціна після перевірки"
                hint={
                  !priceValid && price.length
                    ? "Додатне ціле"
                    : undefined
                }
              >
                <div className="amount-input">
                  <span>
                    <CurrencyMark
                      currency={selected.currency}
                      size={18}
                    />
                  </span>
                  <input
                    type="number"
                    min={1}
                    step={1}
                    value={price}
                    onChange={(event) => setPrice(event.target.value)}
                  />
                </div>
              </Field>

              <button
                className="primary-action"
                disabled={!priceValid || selected.status !== "IN_REVIEW"}
                onClick={approve}
              >
                <Glyph name="check" size={16} />
                Схвалити
              </button>
            </section>

            <section className="review-reject-panel">
              <Field label="Причина відмови">
                <textarea
                  value={reason}
                  maxLength={300}
                  onChange={(event) => setReason(event.target.value)}
                  placeholder="Що саме треба виправити"
                />
              </Field>

              <button
                className="destructive-action"
                disabled={!reason.trim() || selected.status !== "IN_REVIEW"}
                onClick={reject}
              >
                <Glyph name="close" size={16} />
                Відхилити
              </button>
            </section>
          </div>
        </section>
      ) : (
        <section className="review-empty-workbench">
          <Glyph name="creator" size={22} />
          <span>
            <strong>Обери заявку</strong>
            <small>З'явиться повний preview та інструменти рішення.</small>
          </span>
        </section>
      )}
    </div>
  )
}
