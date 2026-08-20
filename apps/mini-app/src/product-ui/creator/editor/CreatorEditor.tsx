import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  publishCreatorPersonalization,
  submitCreatorPersonalization,
} from "@/shared/api/creatorPersonalizationApi"
import { getPersonalization, type PersonalizationResponse } from "@/shared/api/personalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { PersonalizationVisual } from "../../components/personalization/PersonalizationPreview"
import { useProductData } from "../../data/ProductDataProvider"
import type { Currency } from "../../model"
import { CurrencyMark, Glyph } from "../../shared/MnemosIcon"
import { Field, PageHeader } from "../../shared/Primitives"
import { CreatorLoadingState } from "../CreatorLoadingState"
import {
  creatorStatusLabel,
  creatorTypeGlyph,
  creatorTypeLabel,
  creatorTypeOrder,
} from "../creatorPresentation"
import {
  buildCreatorPreview,
  createDefaultCreatorEditorForm,
  creatorResponseToEditorForm,
  saveCreatorForm,
} from "./creatorEditorModel"

type Props = {
  params?: Record<string, string>
  back: () => void
}

export function CreatorEditor({ params, back }: Props) {
  const { t } = useTranslation()
  const product = useProductData()
  const personalizationId = params?.personalizationId ?? "new"
  const isNew = personalizationId === "new"
  const [source, setSource] = useState<PersonalizationResponse | null>(null)
  const [form, setForm] = useState(() => createDefaultCreatorEditorForm(t))
  const [loading, setLoading] = useState(!isNew)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (isNew) {
      return
    }

    const frame = window.requestAnimationFrame(() => {
      void getPersonalization(personalizationId)
        .then((response) => {
          setSource(response)
          setForm(creatorResponseToEditorForm(response, t))
        })
        .catch(handleError)
        .finally(() => setLoading(false))
    })

    return () => window.cancelAnimationFrame(frame)
  }, [isNew, personalizationId, t])

  const numericPrice = Number(form.price)
  const valid = form.name.trim().length >= 3 && Number.isInteger(numericPrice) && numericPrice > 0
  const editable = !source || ["DRAFT", "REJECTED", "PRIVATE"].includes(source.status)
  const preview = buildCreatorPreview(
    form,
    source?.id ?? "preview",
    source?.status ?? "DRAFT",
    product.user.username,
    t("creator.editor.fallbackName"),
  )

  const save = async (): Promise<PersonalizationResponse | null> => {
    if (!valid || !editable || busy) {
      return null
    }

    setBusy(true)

    try {
      const saved = await saveCreatorForm(source?.id ?? null, form)

      setSource(saved)
      setForm(creatorResponseToEditorForm(saved, t))

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

    if (saved) {
      back()
    }
  }

  const submit = async () => {
    const saved = await save()

    if (!saved) {
      return
    }

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
    if (!source || source.status !== "APPROVED" || busy) {
      return
    }

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
        <CreatorLoadingState />
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
          <small>{creatorTypeLabel(t, form.type)}</small>
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
        {creatorTypeOrder.map((value) => (
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
            <Glyph name={creatorTypeGlyph[value]} size={18} />
            <span>{creatorTypeLabel(t, value)}</span>
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
                setForm((current) => ({
                  ...current,
                  accent: event.target.value.toUpperCase(),
                }))
              }
            />
          </Field>

          <Field label={t("creator.editor.secondary")}>
            <input
              type="color"
              value={form.secondary}
              disabled={!editable}
              onChange={(event) =>
                setForm((current) => ({
                  ...current,
                  secondary: event.target.value.toUpperCase(),
                }))
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
                setForm((current) => ({
                  ...current,
                  intensity: Number(event.target.value),
                }))
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
                setForm((current) => ({
                  ...current,
                  message: event.target.value,
                }))
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
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                price: event.target.value,
              }))
            }
          />
        </div>

        {source ? (
          <p className="inline-hint inline-hint--left">
            {t("creator.editor.status", {
              status: creatorStatusLabel(t, source.status),
            })}
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
