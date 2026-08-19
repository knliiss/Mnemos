import type { TFunction } from "i18next"
import { AnimatePresence, motion } from "framer-motion"
import { useCallback, useEffect, useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  createCreatorPersonalization,
  getCreatorRevenueOperations,
  getCreatorRevenueSummary,
  listCreatorPersonalizations,
  publishCreatorPersonalization,
  submitCreatorPersonalization,
  updateCreatorPersonalization,
  type CreatorRevenueOperationResponse,
  type CreatorRevenueSummaryResponse,
} from "@/shared/api/creatorPersonalizationApi"
import { getPersonalization, type PersonalizationResponse } from "@/shared/api/personalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { PersonalizationVisual } from "../components/PersonalizationPreview"
import { useProductData } from "../data/ProductDataProvider"
import { toProductPersonalization } from "../data/personalizationMappers"
import type {
  Currency,
  Personalization,
  PersonalizationContent,
  PersonalizationStatus,
  PersonalizationType,
  Route,
} from "../model"
import { CurrencyMark, Glyph } from "../shared/MnemosIcon"
import { CurrencyValue, Field, PageHeader } from "../shared/Primitives"

type Props = {
  routeId: Route["id"]
  params?: Record<string, string>
  navigate: (route: Route) => void
  back: () => void
}

type StudioFilter = "ALL" | "DRAFT" | "REVIEW" | "PUBLISHED"

type EditorForm = {
  type: PersonalizationType
  name: string
  currency: Currency
  price: string
  accent: string
  secondary: string
  intensity: number
  message: string
}

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

const typeGlyph: Record<
  PersonalizationType,
  "spark" | "creator" | "profile-mark" | "events" | "bell"
> = {
  NICK_STYLE: "spark",
  PROFILE_THEME: "creator",
  AVATAR_FRAME: "profile-mark",
  PROFILE_EFFECT: "events",
  NOTIFICATION_PACK: "bell",
}

export function CreatorFeature(props: Props) {
  if (props.routeId === "creator-revenue") {
    return <CreatorRevenueProduction back={props.back} />
  }

  if (props.routeId === "personalization-editor") {
    return <CreatorEditorProduction {...props} />
  }

  return <CreatorStudioProduction {...props} />
}

function CreatorStudioProduction({ navigate, back }: Props) {
  const { t } = useTranslation()
  const product = useProductData()
  const [items, setItems] = useState<Personalization[]>([])
  const [filter, setFilter] = useState<StudioFilter>("ALL")
  const [loading, setLoading] = useState(true)
  const [revenue, setRevenue] = useState<CreatorRevenueSummaryResponse | null>(null)

  const refresh = useCallback(async () => {
    setLoading(true)

    try {
      const [responses, summary] = await Promise.all([
        listCreatorPersonalizations(),
        getCreatorRevenueSummary(),
      ])

      setItems(
        responses.map((response) =>
          toProductPersonalization(response, null, product.user.id, product.user.username),
        ),
      )
      setRevenue(summary)
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

  const counts = useMemo(
    () => ({
      draft: items.filter((item) => item.status === "DRAFT" || item.status === "REJECTED").length,
      review: items.filter((item) => item.status === "IN_REVIEW" || item.status === "APPROVED")
        .length,
      published: items.filter((item) => item.status === "PUBLISHED" || item.status === "UNLISTED")
        .length,
    }),
    [items],
  )

  const visible = items.filter((item) => {
    if (filter === "ALL") return true
    if (filter === "DRAFT") return item.status === "DRAFT" || item.status === "REJECTED"
    if (filter === "REVIEW") return item.status === "IN_REVIEW" || item.status === "APPROVED"
    return item.status === "PUBLISHED" || item.status === "UNLISTED"
  })

  return (
    <div className="screen creator-r2-screen">
      <PageHeader title={t("creator.title")} back={back} />

      <section className="creator-r2-hero">
        <div className="creator-r2-hero__copy">
          <small>MNEMOS CREATOR</small>
          <strong>{t("creator.workshopTitle")}</strong>
          <p>{t("creator.workshopCopy")}</p>
        </div>
        <div className="creator-r2-hero__art" aria-hidden="true" />
        <div className="creator-r2-hero__actions">
          <button
            onClick={() =>
              navigate({ id: "personalization-editor", params: { personalizationId: "new" } })
            }
          >
            <Glyph name="plus" size={16} />
            <span>{t("creator.newWork")}</span>
          </button>
          <button className="is-revenue" onClick={() => navigate({ id: "creator-revenue" })}>
            <CurrencyMark currency="PRIME" size={15} />
            <span>{revenue?.confirmedPrime ?? 0}</span>
            <small>{t("creator.revenue")}</small>
          </button>
        </div>
      </section>

      <section className="creator-r2-dashboard">
        <button
          className={filter === "DRAFT" ? "is-active" : ""}
          onClick={() => setFilter(filter === "DRAFT" ? "ALL" : "DRAFT")}
        >
          <i className="is-draft" />
          <strong>{counts.draft}</strong>
          <span>{t("creator.drafts")}</span>
        </button>
        <button
          className={filter === "REVIEW" ? "is-active" : ""}
          onClick={() => setFilter(filter === "REVIEW" ? "ALL" : "REVIEW")}
        >
          <i className="is-review" />
          <strong>{counts.review}</strong>
          <span>{t("creator.review")}</span>
        </button>
        <button
          className={filter === "PUBLISHED" ? "is-active" : ""}
          onClick={() => setFilter(filter === "PUBLISHED" ? "ALL" : "PUBLISHED")}
        >
          <i className="is-published" />
          <strong>{counts.published}</strong>
          <span>{t("creator.published")}</span>
        </button>
      </section>

      <header className="creator-r2-section-title">
        <span>
          <small>{filter === "ALL" ? t("creator.allWorks") : t("creator.filtered")}</small>
          <strong>{t("creator.personalizations")}</strong>
        </span>
        <b>{visible.length}</b>
      </header>

      {loading ? (
        <LoadingState />
      ) : (
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
                onClick={() =>
                  navigate({ id: "personalization-editor", params: { personalizationId: item.id } })
                }
              >
                <span className="creator-r2-card__visual">
                  <PersonalizationVisual item={item} small />
                </span>
                <span className="creator-r2-card__copy">
                  <small>{typeLabel(t, item.type)}</small>
                  <strong>{item.name}</strong>
                  <em>{item.target === "USER" ? t("creator.forUser") : t("creator.forAccount")}</em>
                </span>
                <span className={`creator-r2-status is-${item.status.toLowerCase()}`}>
                  {statusLabel(t, item.status)}
                </span>
              </motion.button>
            ))}
          </AnimatePresence>
          {!visible.length ? <p className="inline-hint">{t("creator.emptySection")}</p> : null}
        </section>
      )}
    </div>
  )
}

function CreatorRevenueProduction({ back }: { back: () => void }) {
  const { t, i18n } = useTranslation()
  const [summary, setSummary] = useState<CreatorRevenueSummaryResponse | null>(null)
  const [operations, setOperations] = useState<CreatorRevenueOperationResponse[]>([])
  const [loading, setLoading] = useState(true)
  const locale = localeForLanguage(i18n.resolvedLanguage)

  useEffect(() => {
    const frame = window.requestAnimationFrame(() => {
      void Promise.all([getCreatorRevenueSummary(), getCreatorRevenueOperations()])
        .then(([nextSummary, nextOperations]) => {
          setSummary(nextSummary)
          setOperations(nextOperations)
        })
        .catch(handleError)
        .finally(() => setLoading(false))
    })

    return () => window.cancelAnimationFrame(frame)
  }, [])

  return (
    <div className="screen creator-r2-screen creator-r2-revenue">
      <PageHeader title={t("creator.revenuePage.title")} back={back} />

      <section className="creator-r2-revenue-hero">
        <span className="creator-r2-revenue-hero__mark">
          <CurrencyMark currency="PRIME" size={22} />
        </span>
        <span className="creator-r2-revenue-hero__copy">
          <small>{t("creator.revenuePage.confirmed")}</small>
          <CurrencyValue currency="PRIME" value={summary?.confirmedPrime ?? 0} />
          <em>
            {t("creator.revenuePage.successfulOperations", {
              count: summary?.confirmedOperations ?? 0,
            })}
          </em>
        </span>
        <div className="creator-r2-sparkline" aria-hidden="true">
          <i />
          <i />
          <i />
          <i />
          <i />
          <i />
          <i />
        </div>
      </section>

      <section className="creator-r2-revenue-stats">
        <article>
          <small>{t("creator.revenuePage.pending")}</small>
          <strong>{summary?.pendingPrime ?? 0}</strong>
          <em>PRIME</em>
        </article>
        <article>
          <small>{t("creator.revenuePage.confirmedOperations")}</small>
          <strong>{summary?.confirmedOperations ?? 0}</strong>
          <em>{t("creator.revenuePage.operations")}</em>
        </article>
        <article>
          <small>{t("creator.revenuePage.errors")}</small>
          <strong>{summary?.failedOperations ?? 0}</strong>
          <em>{summary?.failedPrime ?? 0} PRIME</em>
        </article>
      </section>

      <header className="creator-r2-section-title">
        <span>
          <small>{t("creator.revenuePage.operationsEyebrow")}</small>
          <strong>{t("creator.revenuePage.recentActivity")}</strong>
        </span>
      </header>
      {loading ? (
        <LoadingState />
      ) : (
        <section className="creator-r2-revenue-log">
          {operations.map((operation) => (
            <article key={operation.id}>
              <i className={`is-${operation.status.toLowerCase()}`} />
              <span>
                <strong>{t("creator.revenuePage.sale")}</strong>
                <small>{formatDate(operation.createdAt, locale)}</small>
              </span>
              <span>
                <CurrencyValue currency="PRIME" value={operation.amountPrime} compact />
                <small>{operation.status}</small>
              </span>
            </article>
          ))}
          {!operations.length ? (
            <p className="inline-hint">{t("creator.revenuePage.empty")}</p>
          ) : null}
        </section>
      )}
    </div>
  )
}

function CreatorEditorProduction({ params, back }: Props) {
  const { t } = useTranslation()
  const product = useProductData()
  const personalizationId = params?.personalizationId ?? "new"
  const isNew = personalizationId === "new"
  const [source, setSource] = useState<PersonalizationResponse | null>(null)
  const [form, setForm] = useState<EditorForm>(() => createDefaultForm(t))
  const [loading, setLoading] = useState(!isNew)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (isNew) return

    const frame = window.requestAnimationFrame(() => {
      void getPersonalization(personalizationId)
        .then((response) => {
          setSource(response)
          setForm(toEditorForm(response, t))
        })
        .catch(handleError)
        .finally(() => setLoading(false))
    })

    return () => window.cancelAnimationFrame(frame)
  }, [isNew, personalizationId, t])

  const numericPrice = Number(form.price)
  const valid = form.name.trim().length >= 3 && Number.isInteger(numericPrice) && numericPrice > 0
  const editable = !source || ["DRAFT", "REJECTED", "PRIVATE"].includes(source.status)
  const preview = buildPreview(
    form,
    source?.id ?? "preview",
    source?.status ?? "DRAFT",
    product.user.username,
    t("creator.editor.fallbackName"),
  )

  const save = async (): Promise<PersonalizationResponse | null> => {
    if (!valid || !editable || busy) return null

    setBusy(true)

    try {
      const saved = await saveForm(source?.id ?? null, form)
      setSource(saved)
      setForm(toEditorForm(saved, t))
      return saved
    } catch (error) {
      handleError(error)
      return null
    } finally {
      setBusy(false)
    }
  }

  const saveAndBack = async () => {
    const saved = await save()
    if (saved) back()
  }

  const submit = async () => {
    const saved = await save()
    if (!saved) return

    setBusy(true)

    try {
      await submitCreatorPersonalization(saved.id)
      back()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  const publish = async () => {
    if (!source || source.status !== "APPROVED" || busy) return

    setBusy(true)

    try {
      await publishCreatorPersonalization(source.id)
      back()
    } catch (error) {
      handleError(error)
    } finally {
      setBusy(false)
    }
  }

  if (loading) {
    return (
      <div className="screen creator-r2-screen">
        <PageHeader title={t("creator.editor.editing")} back={back} />
        <LoadingState />
      </div>
    )
  }

  return (
    <div className="screen creator-r2-screen creator-r2-editor">
      <PageHeader
        title={isNew ? t("creator.editor.newWork") : t("creator.editor.editing")}
        back={back}
      />

      <section className="creator-r2-editor-stage">
        <header>
          <span>
            <i />
            {t("creator.editor.live")}
          </span>
          <small>{typeLabel(t, form.type)}</small>
        </header>
        <PersonalizationVisual item={preview} detail />
      </section>

      {source?.rejectionReason ? (
        <section className="creator-r2-form-block">
          <header>
            <small>{t("creator.editor.moderationEyebrow")}</small>
            <strong>{t("creator.editor.changesRequired")}</strong>
          </header>
          <p>{source.rejectionReason}</p>
        </section>
      ) : null}

      <section className="creator-r2-type-picker">
        {typeOrder.map((value) => (
          <button
            key={value}
            disabled={!editable || Boolean(source)}
            className={form.type === value ? "is-active" : ""}
            onClick={() =>
              setForm((current) => ({
                ...current,
                type: value,
                currency: value === "NOTIFICATION_PACK" ? "PRIME" : current.currency,
              }))
            }
          >
            <Glyph name={typeGlyph[value]} size={18} />
            <span>{typeLabel(t, value)}</span>
          </button>
        ))}
      </section>

      <section className="creator-r2-form-block">
        <header>
          <small>{t("creator.editor.basicEyebrow")}</small>
          <strong>{t("creator.editor.identity")}</strong>
        </header>
        <Field label={t("creator.editor.name")}>
          <input
            value={form.name}
            maxLength={64}
            disabled={!editable}
            onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))}
            placeholder={t("creator.editor.namePlaceholder")}
          />
        </Field>

        <div className="creator-r2-color-row">
          <Field label={t("creator.editor.primary")}>
            <input
              type="color"
              value={form.accent}
              disabled={!editable}
              onChange={(event) =>
                setForm((current) => ({ ...current, accent: event.target.value.toUpperCase() }))
              }
            />
          </Field>
          <Field label={t("creator.editor.secondary")}>
            <input
              type="color"
              value={form.secondary}
              disabled={!editable}
              onChange={(event) =>
                setForm((current) => ({ ...current, secondary: event.target.value.toUpperCase() }))
              }
            />
          </Field>
        </div>

        <Field label={t("creator.editor.intensity")}>
          <div className="creator-r2-range">
            <input
              type="range"
              min={1}
              max={100}
              value={form.intensity}
              disabled={!editable}
              onChange={(event) =>
                setForm((current) => ({ ...current, intensity: Number(event.target.value) }))
              }
            />
            <output>{form.intensity}%</output>
          </div>
        </Field>

        {form.type === "NOTIFICATION_PACK" ? (
          <Field label={t("creator.editor.messageText")}>
            <textarea
              maxLength={512}
              value={form.message}
              disabled={!editable}
              onChange={(event) =>
                setForm((current) => ({ ...current, message: event.target.value }))
              }
            />
          </Field>
        ) : null}
      </section>

      <section className="creator-r2-form-block">
        <header>
          <small>{t("creator.editor.publicationEyebrow")}</small>
          <strong>{t("creator.editor.priceAndStatus")}</strong>
        </header>
        <div className="creator-r2-price-row">
          {form.type === "NOTIFICATION_PACK" ? (
            <span className="creator-r2-fixed-currency">
              <CurrencyMark currency="PRIME" size={15} /> PRIME
            </span>
          ) : (
            <div className="creator-r2-currency-switch">
              {(["COIN", "PRIME"] as Currency[]).map((value) => (
                <button
                  key={value}
                  disabled={!editable}
                  className={form.currency === value ? "is-active" : ""}
                  onClick={() => setForm((current) => ({ ...current, currency: value }))}
                >
                  {value}
                </button>
              ))}
            </div>
          )}
          <input
            type="number"
            min={1}
            step={1}
            value={form.price}
            disabled={!editable}
            onChange={(event) => setForm((current) => ({ ...current, price: event.target.value }))}
          />
        </div>
        {source ? (
          <p className="inline-hint inline-hint--left">
            {t("creator.editor.status", { status: statusLabel(t, source.status) })}
          </p>
        ) : null}
      </section>

      <div className="creator-r2-editor-actions">
        {editable ? (
          <>
            <button disabled={!valid || busy} onClick={() => void saveAndBack()}>
              {busy ? t("creator.editor.saving") : t("creator.editor.save")}
            </button>
            <button className="is-primary" disabled={!valid || busy} onClick={() => void submit()}>
              {busy ? t("creator.editor.submitting") : t("creator.editor.submit")}
            </button>
          </>
        ) : source?.status === "APPROVED" ? (
          <button className="is-primary" disabled={busy} onClick={() => void publish()}>
            {busy ? t("creator.editor.publishing") : t("creator.editor.publish")}
          </button>
        ) : (
          <button disabled>{t("creator.editor.editingUnavailable")}</button>
        )}
      </div>
    </div>
  )
}

async function saveForm(id: string | null, form: EditorForm): Promise<PersonalizationResponse> {
  const price = {
    currency: form.type === "NOTIFICATION_PACK" ? ("PRIME" as const) : form.currency,
    amount: Number(form.price),
  }

  if (form.type === "NICK_STYLE") {
    const request = {
      name: form.name.trim(),
      colors: [
        { color: form.accent, position: 0 },
        { color: form.secondary, position: 100 },
      ],
      angleDegrees: 45,
      glow: { color: form.accent, strength: form.intensity },
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "NICK_STYLE", request)
      : createCreatorPersonalization("NICK_STYLE", request)
  }

  if (form.type === "PROFILE_THEME") {
    const request = {
      name: form.name.trim(),
      background: "#11140F",
      surface: "#1C2119",
      accent: form.accent,
      text: "#F1F4E9",
      border: form.secondary,
      glow: { color: form.accent, strength: form.intensity },
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "PROFILE_THEME", request)
      : createCreatorPersonalization("PROFILE_THEME", request)
  }

  if (form.type === "AVATAR_FRAME") {
    const request = {
      name: form.name.trim(),
      borderColor: form.accent,
      borderWidthPx: Math.max(1, Math.min(8, Math.round(form.intensity / 20) + 1)),
      radiusPx: 8,
      glow: { color: form.secondary, strength: form.intensity },
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "AVATAR_FRAME", request)
      : createCreatorPersonalization("AVATAR_FRAME", request)
  }

  if (form.type === "PROFILE_EFFECT") {
    const request = {
      name: form.name.trim(),
      effect: "AURORA" as const,
      intensity: form.intensity,
      primaryColor: form.accent,
      secondaryColor: form.secondary,
      price,
    }

    return id
      ? updateCreatorPersonalization(id, "PROFILE_EFFECT", request)
      : createCreatorPersonalization("PROFILE_EFFECT", request)
  }

  const template = {
    emoji: null,
    soloTitle: form.name.trim(),
    soloBody: form.message.trim(),
    compact: form.message.trim(),
    aggregateTitle: form.name.trim(),
  }
  const request = {
    name: form.name.trim(),
    mixedAggregateTitle: form.name.trim(),
    templates: {
      ITEM_DROP: template,
      BOOSTER: template,
      DUNGEON: template,
      RAID: template,
      GLOBAL: template,
    },
    price,
  }

  return id
    ? updateCreatorPersonalization(id, "NOTIFICATION_PACK", request)
    : createCreatorPersonalization("NOTIFICATION_PACK", request)
}

function createDefaultForm(t: TFunction): EditorForm {
  return {
    type: "NICK_STYLE",
    name: "",
    currency: "PRIME",
    price: "25",
    accent: "#C7F24A",
    secondary: "#B98DC2",
    intensity: 48,
    message: t("creator.editor.defaultMessage"),
  }
}

function toEditorForm(response: PersonalizationResponse, t: TFunction): EditorForm {
  const content = response.content
  const defaults = createDefaultForm(t)
  const base: EditorForm = {
    ...defaults,
    type: response.type,
    name: response.name,
    currency: response.price?.currency ?? "PRIME",
    price: String(response.price?.amount ?? 25),
  }

  if (response.type === "NICK_STYLE") {
    const value = content as
      | Extract<PersonalizationContent, { kind: "NICK_STYLE" }>
      | {
          colors: { color: string; position: number }[]
          glow: { color: string; strength: number } | null
        }

    return {
      ...base,
      accent: value.colors[0]?.color ?? base.accent,
      secondary: value.colors[1]?.color ?? value.colors[0]?.color ?? base.secondary,
      intensity: value.glow?.strength ?? base.intensity,
    }
  }

  if (response.type === "PROFILE_THEME") {
    const value = content as { accent: string; border: string; glow: { strength: number } | null }
    return {
      ...base,
      accent: value.accent,
      secondary: value.border,
      intensity: value.glow?.strength ?? base.intensity,
    }
  }

  if (response.type === "AVATAR_FRAME") {
    const value = content as {
      borderColor: string
      glow: { color: string; strength: number } | null
    }
    return {
      ...base,
      accent: value.borderColor,
      secondary: value.glow?.color ?? base.secondary,
      intensity: value.glow?.strength ?? base.intensity,
    }
  }

  if (response.type === "PROFILE_EFFECT") {
    const value = content as {
      primaryColor: string
      secondaryColor: string | null
      intensity: number
    }
    return {
      ...base,
      accent: value.primaryColor,
      secondary: value.secondaryColor ?? base.secondary,
      intensity: value.intensity,
    }
  }

  const value = content as { mixedAggregateTitle: string }
  return { ...base, currency: "PRIME", message: value.mixedAggregateTitle || base.message }
}

function buildPreview(
  form: EditorForm,
  id: string,
  status: PersonalizationStatus,
  username: string,
  fallbackName: string,
): Personalization {
  let content: PersonalizationContent

  if (form.type === "NICK_STYLE") {
    content = {
      kind: "NICK_STYLE",
      colors: [
        { color: form.accent, position: 0 },
        { color: form.secondary, position: 100 },
      ],
      angleDegrees: 45,
      glow: { color: form.accent, strength: form.intensity },
    }
  } else if (form.type === "PROFILE_THEME") {
    content = {
      kind: "PROFILE_THEME",
      background: "#11140F",
      surface: "#1C2119",
      accent: form.accent,
      text: "#F1F4E9",
      border: form.secondary,
      glow: { color: form.accent, strength: form.intensity },
    }
  } else if (form.type === "AVATAR_FRAME") {
    content = {
      kind: "AVATAR_FRAME",
      borderColor: form.accent,
      borderWidthPx: Math.max(1, Math.min(8, Math.round(form.intensity / 20) + 1)),
      radiusPx: 8,
      glow: { color: form.secondary, strength: form.intensity },
    }
  } else if (form.type === "PROFILE_EFFECT") {
    content = {
      kind: "PROFILE_EFFECT",
      effect: "AURORA",
      intensity: form.intensity,
      primaryColor: form.accent,
      secondaryColor: form.secondary,
    }
  } else {
    const template = {
      emoji: null,
      soloTitle: form.name || "Mnemos",
      soloBody: form.message,
      compact: form.message,
      aggregateTitle: form.name || "Mnemos",
    }
    content = {
      kind: "NOTIFICATION_PACK",
      mixedAggregateTitle: form.name || "Mnemos",
      templates: {
        ITEM_DROP: template,
        BOOSTER: template,
        DUNGEON: template,
        RAID: template,
        GLOBAL: template,
      },
    }
  }

  return {
    id,
    type: form.type,
    target: targetByType[form.type],
    name: form.name.trim() || fallbackName,
    creator: `@${username}`,
    currency: form.type === "NOTIFICATION_PACK" ? "PRIME" : form.currency,
    price: Number(form.price) || 0,
    status,
    owned: false,
    active: false,
    preview: "creator",
    content,
  }
}

function LoadingState() {
  const { t } = useTranslation()
  return (
    <div className="events-r2-loading" aria-label={t("creator.loading")}>
      <i />
      <i />
      <i />
    </div>
  )
}

function typeLabel(t: TFunction, type: PersonalizationType) {
  return t(`personalization.types.${type}.title`)
}

function statusLabel(t: TFunction, status: PersonalizationStatus) {
  return t(`creator.status.${status}`)
}

function formatDate(value: string, locale: string) {
  return new Intl.DateTimeFormat(locale, {
    day: "2-digit",
    month: "short",
    hour: "2-digit",
    minute: "2-digit",
  }).format(new Date(value))
}

function localeForLanguage(language?: string) {
  if (language === "en") return "en-US"
  if (language === "ru") return "ru-RU"
  return "uk-UA"
}
