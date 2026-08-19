import { useCallback, useEffect, useMemo, useState } from "react"
import {
  approvePersonalization,
  listPersonalizationReviewQueue,
  rejectPersonalization,
} from "@/shared/api/personalizationModerationApi"
import { handleError } from "@/shared/errors/errorHandler"
import {
  PersonalizationDetails,
  PersonalizationVisual,
} from "../components/PersonalizationPreview"
import { useProductData } from "../data/ProductDataProvider"
import { toProductPersonalization } from "../data/personalizationMappers"
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

export function PersonalizationReviewProduction({ back }: { back: () => void }) {
  const product = useProductData()
  const [items, setItems] = useState<Personalization[]>([])
  const [selectedId, setSelectedId] = useState<string | null>(null)
  const [price, setPrice] = useState("")
  const [reason, setReason] = useState("")
  const [query, setQuery] = useState("")
  const [scope, setScope] = useState<ReviewScope>("ALL")
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)

  const refresh = useCallback(async () => {
    try {
      const responses = await listPersonalizationReviewQueue()
      const next = responses.map((response) => toProductPersonalization(
        response,
        null,
        product.user.id,
        product.user.username,
      ))

      setItems(next)
      setSelectedId((current) => current && next.some((item) => item.id === current)
        ? current
        : next[0]?.id ?? null)
    } catch (error) {
      handleError(error)
    } finally {
      setLoading(false)
    }
  }, [product.user.id, product.user.username])

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void refresh()
    })

    return () => window.cancelAnimationFrame(frame)
  }, [refresh])

  const selected = items.find((item) => item.id === selectedId) ?? null

  useEffect(() => {
    if (!selected) return

    const frame = window.requestAnimationFrame(() => {
      setPrice(String(selected.price))
      setReason("")
    })

    return () => window.cancelAnimationFrame(frame)
  }, [selected])

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

  const decide = async (decision: "APPROVE" | "REJECT") => {
    if (!selected || busy) return
    if (decision === "APPROVE" && !priceValid) return
    if (decision === "REJECT" && !reason.trim()) return

    setBusy(true)

    try {
      if (decision === "APPROVE") {
        await approvePersonalization(
          selected.id,
          selected.currency,
          numericPrice,
        )
      } else {
        await rejectPersonalization(selected.id, reason.trim())
      }

      await refresh()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="screen review-overhaul-screen">
      <PageHeader title="Перевірка" back={back} />

      <section className="review-overview">
        <span className="review-overview__mark"><Glyph name="creator" size={20} /></span>
        <span>
          <small>Черга персоналізацій</small>
          <strong>{items.length} очікують рішення</strong>
          <em>Черга, preview та рішення завантажуються з personalization-service.</em>
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

      {loading ? <LoadingState /> : (
        <div className="review-queue-grid">
          {visible.map((item) => (
            <button
              key={item.id}
              className={selectedId === item.id ? "review-queue-card is-selected" : "review-queue-card"}
              onClick={() => setSelectedId(item.id)}
            >
              <PersonalizationVisual item={item} small />
              <span className="review-queue-card__copy">
                <span className="review-queue-card__meta">
                  <small>{personalizationLabels[item.type]}</small>
                  <i className={`is-${item.target.toLowerCase()}`}>{item.target}</i>
                </span>
                <strong>{item.name}</strong>
                <em>by {item.creator}</em>
              </span>
              <span className="review-queue-card__side">
                <CurrencyValue currency={item.currency} value={item.price} compact />
                <b className={`status-${item.status.toLowerCase()}`}>
                  {personalizationStatusLabels[item.status]}
                </b>
              </span>
            </button>
          ))}
          {!visible.length ? <p className="inline-hint">Черга порожня</p> : null}
        </div>
      )}

      {selected ? (
        <section className="review-workbench">
          <header>
            <span>
              <small>SELECTED</small>
              <strong>{selected.name}</strong>
              <em>{personalizationLabels[selected.type]} · {selected.target}</em>
            </span>
            <button aria-label="Закрити" onClick={() => setSelectedId(null)}>
              <Glyph name="close" size={16} />
            </button>
          </header>

          <PersonalizationVisual item={selected} detail />
          <PersonalizationDetails item={selected} />

          <div className="review-metadata">
            <article><small>Автор</small><strong>{selected.creator}</strong></article>
            <article><small>Scope</small><strong>{selected.target}</strong></article>
            <article><small>Статус</small><strong>{personalizationStatusLabels[selected.status]}</strong></article>
          </div>

          <div className="review-decision-grid">
            <section className="review-approve-panel">
              <Field label="Ціна після перевірки" hint={!priceValid && price.length ? "Додатне ціле" : undefined}>
                <div className="amount-input">
                  <span><CurrencyMark currency={selected.currency} size={18} /></span>
                  <input
                    type="number"
                    min={1}
                    step={1}
                    value={price}
                    disabled={busy}
                    onChange={(event) => setPrice(event.target.value)}
                  />
                </div>
              </Field>
              <button
                className="primary-action"
                disabled={!priceValid || busy}
                onClick={() => void decide("APPROVE")}
              >
                <Glyph name="check" size={16} />
                {busy ? "Зберігаємо…" : "Схвалити"}
              </button>
            </section>

            <section className="review-reject-panel">
              <Field label="Причина відмови">
                <textarea
                  value={reason}
                  maxLength={500}
                  disabled={busy}
                  onChange={(event) => setReason(event.target.value)}
                  placeholder="Що саме треба виправити"
                />
              </Field>
              <button
                className="destructive-action"
                disabled={!reason.trim() || busy}
                onClick={() => void decide("REJECT")}
              >
                <Glyph name="close" size={16} />
                {busy ? "Зберігаємо…" : "Відхилити"}
              </button>
            </section>
          </div>
        </section>
      ) : (
        <section className="review-empty-workbench">
          <Glyph name="creator" size={22} />
          <span><strong>Обери заявку</strong><small>Зʼявиться preview та інструменти рішення.</small></span>
        </section>
      )}
    </div>
  )
}

function LoadingState() {
  return <div className="events-r2-loading" aria-label="Завантаження"><i /><i /><i /></div>
}
