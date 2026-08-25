import { useEffect, useLayoutEffect, useRef, useState } from "react"
import { useTranslation } from "react-i18next"
import {
  getCreatorPersonalization,
  getNotificationPackEditorDescriptor,
  publishCreatorPersonalization,
  submitCreatorPersonalization,
  type NotificationPackEditorDescriptorResponse,
} from "@/shared/api/creatorPersonalizationApi"
import {
  getPersonalizationPricing,
  type PersonalizationPricingResponse,
  type PersonalizationResponse,
} from "@/shared/api/personalizationApi"
import { handleError } from "@/shared/errors/errorHandler"
import { PersonalizationVisual } from "../../components/personalization/PersonalizationPreview"
import { useMnemosData } from "../../data/MnemosDataProvider"
import type { Currency } from "../../model"
import { CurrencyMark, Glyph } from "../../shared/MnemosIcon"
import { Field, PageHeader } from "../../shared/Primitives"
import { NotificationPackSpotlightPreview } from "../../personalization/components/NotificationPackSpotlightPreview"
import { FeatureEmpty } from "../../system/FeatureEmpty"
import { CreatorLoadingState } from "../CreatorLoadingState"
import {
  creatorStatusLabel,
  creatorTypeGlyph,
  creatorTypeLabel,
  creatorTypeOrder,
} from "../creatorPresentation"
import { CreatorDesignPresets } from "./CreatorDesignPresets"
import { CreatorNotificationPackStudio } from "./CreatorNotificationPackStudio"
import { CreatorPersonalizationControls } from "./CreatorPersonalizationControls"
import { CreatorVisualLayerStudio } from "./CreatorVisualLayerStudio"
import {
  buildCreatorPreview,
  createDefaultCreatorEditorForm,
  creatorResponseToEditorForm,
  saveCreatorForm,
} from "./creatorEditorModel"
import { isCreatorVisualFormValid, isNotificationPackFormValid } from "./creatorEditorValidation"
import "./creator-notification-studio.css"
import "./creator-visual-editor.css"
import {
  isCreatorPriceValid,
  normalizeCreatorPrice,
  resolveCreatorPriceRange,
} from "./creatorPricing"

type Props = {
  params?: Record<string, string>
  back: () => void
}

export function CreatorEditor({ params, back }: Props) {
  const { t } = useTranslation()
  const data = useMnemosData()
  const personalizationId = params?.personalizationId ?? "new"
  const isNew = personalizationId === "new"
  const [source, setSource] = useState<PersonalizationResponse | null>(null)
  const [pricing, setPricing] = useState<PersonalizationPricingResponse | null>(null)
  const [descriptor, setDescriptor] = useState<NotificationPackEditorDescriptorResponse | null>(
    null,
  )
  const [form, setForm] = useState(() => createDefaultCreatorEditorForm())
  const [loading, setLoading] = useState(true)
  const [loadedId, setLoadedId] = useState<string | null>(null)
  const [loadError, setLoadError] = useState(false)
  const [reloadRevision, setReloadRevision] = useState(0)
  const [busy, setBusy] = useState(false)
  const busyScopeRef = useRef<string | null>(null)
  const mountedRef = useRef(true)
  const scopeRef = useRef(personalizationId)

  useLayoutEffect(() => {
    scopeRef.current = personalizationId
  }, [personalizationId])

  useEffect(() => {
    let cancelled = false
    const requestedId = personalizationId

    const frame = window.requestAnimationFrame(() => {
      mountedRef.current = true
      setLoading(true)
      setLoadError(false)

      const sourceRequest = isNew
        ? Promise.resolve(null)
        : getCreatorPersonalization(personalizationId)

      void Promise.all([
        getPersonalizationPricing(),
        getNotificationPackEditorDescriptor(),
        sourceRequest,
      ])
        .then(([pricingResponse, descriptorResponse, response]) => {
          if (cancelled || scopeRef.current !== requestedId) {
            return
          }

          const nextForm = response
            ? creatorResponseToEditorForm(response, descriptorResponse)
            : createDefaultCreatorEditorForm(descriptorResponse)

          setPricing(pricingResponse)
          setDescriptor(descriptorResponse)
          setSource(response)
          setForm(normalizeCreatorPrice(nextForm, pricingResponse))
          setLoadedId(requestedId)
        })
        .catch((error) => {
          if (!cancelled && scopeRef.current === requestedId) {
            setPricing(null)
            setDescriptor(null)
            setSource(null)
            setLoadError(true)
            setLoadedId(requestedId)
            handleError(error)
          }
        })
        .finally(() => {
          if (!cancelled && scopeRef.current === requestedId) {
            setLoading(false)
          }
        })
    })

    return () => {
      cancelled = true
      mountedRef.current = false
      window.cancelAnimationFrame(frame)
    }
  }, [isNew, personalizationId, reloadRevision])

  const currentSource = loadedId === personalizationId ? source : null
  const visibleLoading = loading || loadedId !== personalizationId
  const priceRange = pricing ? resolveCreatorPriceRange(pricing, form.type, form.currency) : null
  const priceValid = isCreatorPriceValid(form, pricing)
  const visualValid = isCreatorVisualFormValid(form)
  const notificationPackValid = isNotificationPackFormValid(form, descriptor)
  const valid = form.name.trim().length >= 3 && priceValid && visualValid && notificationPackValid
  const editable = !currentSource || ["DRAFT", "REJECTED", "PRIVATE"].includes(currentSource.status)

  const save = async (): Promise<PersonalizationResponse | null> => {
    const actionScope = personalizationId
    const currentPricing = pricing
    const currentDescriptor = descriptor

    if (
      !currentPricing ||
      !currentDescriptor ||
      !valid ||
      !editable ||
      busyScopeRef.current === actionScope
    ) {
      return null
    }

    busyScopeRef.current = actionScope
    setBusy(true)

    try {
      const saved = await saveCreatorForm(currentSource?.id ?? null, form, currentDescriptor)

      if (!mountedRef.current || scopeRef.current !== actionScope) {
        return null
      }

      setSource(saved)
      setLoadedId(actionScope)
      setForm(
        normalizeCreatorPrice(
          creatorResponseToEditorForm(saved, currentDescriptor),
          currentPricing,
        ),
      )

      return saved
    } catch (error) {
      if (mountedRef.current && scopeRef.current === actionScope) {
        handleError(error)
      }

      return null
    } finally {
      if (busyScopeRef.current === actionScope) {
        busyScopeRef.current = null

        if (mountedRef.current) {
          setBusy(false)
        }
      }
    }
  }

  const saveAndBack = async () => {
    const saved = await save()

    if (saved) {
      back()
    }
  }

  const submit = async () => {
    const actionScope = personalizationId
    const saved = await save()

    if (!saved || !mountedRef.current || scopeRef.current !== actionScope) {
      return
    }

    busyScopeRef.current = actionScope
    setBusy(true)

    try {
      await submitCreatorPersonalization(saved.id)

      if (mountedRef.current && scopeRef.current === actionScope) {
        back()
      }
    } catch (error) {
      if (mountedRef.current && scopeRef.current === actionScope) {
        handleError(error)
      }
    } finally {
      if (busyScopeRef.current === actionScope) {
        busyScopeRef.current = null

        if (mountedRef.current) {
          setBusy(false)
        }
      }
    }
  }

  const publish = async () => {
    const actionScope = personalizationId

    if (
      !currentSource ||
      currentSource.status !== "APPROVED" ||
      busyScopeRef.current === actionScope
    ) {
      return
    }

    busyScopeRef.current = actionScope
    setBusy(true)

    try {
      await publishCreatorPersonalization(currentSource.id)

      if (mountedRef.current && scopeRef.current === actionScope) {
        back()
      }
    } catch (error) {
      if (mountedRef.current && scopeRef.current === actionScope) {
        handleError(error)
      }
    } finally {
      if (busyScopeRef.current === actionScope) {
        busyScopeRef.current = null

        if (mountedRef.current) {
          setBusy(false)
        }
      }
    }
  }

  if (visibleLoading) {
    return (
      <div className="screen creator-screen">
        <PageHeader title={t("creator.editor.editing")} back={back} />
        <CreatorLoadingState />
      </div>
    )
  }

  if (loadError || !descriptor) {
    return (
      <div className="screen creator-screen">
        <PageHeader title={t("creator.editor.editing")} back={back} />
        <FeatureEmpty
          icon="creator"
          title={t("common.loadErrorTitle")}
          copy={t("common.loadErrorCopy")}
        >
          <button type="button" onClick={() => setReloadRevision((value) => value + 1)}>
            {t("common.retry")}
          </button>
        </FeatureEmpty>
      </div>
    )
  }

  const preview = buildCreatorPreview(
    form,
    descriptor,
    currentSource?.id ?? "preview",
    currentSource?.status ?? "DRAFT",
    data.user.username,
    t("creator.editor.fallbackName"),
  )

  return (
    <div className="screen creator-screen creator-editor">
      <PageHeader
        title={isNew ? t("creator.editor.newWork") : t("creator.editor.editing")}
        back={back}
      />

      <section className="creator-editor-stage">
        <header>
          <span>
            <i />
            {t("creator.editor.live")}
          </span>
          <small>{creatorTypeLabel(t, form.type)}</small>
        </header>

        {preview.type === "NOTIFICATION_PACK" ? (
          <NotificationPackSpotlightPreview item={preview} size="STUDIO" />
        ) : (
          <PersonalizationVisual item={preview} detail />
        )}
      </section>

      {currentSource?.rejectionReason ? (
        <section className="creator-form-block">
          <header>
            <small>{t("creator.editor.moderationEyebrow")}</small>
            <strong>{t("creator.editor.changesRequired")}</strong>
          </header>
          <p>{currentSource.rejectionReason}</p>
        </section>
      ) : null}

      <section className="creator-type-picker">
        {creatorTypeOrder.map((value) => (
          <button
            type="button"
            key={value}
            disabled={!editable || Boolean(currentSource)}
            className={form.type === value ? "is-active" : ""}
            onClick={() =>
              setForm((current) => {
                const next = {
                  ...current,
                  type: value,
                  currency: value === "NOTIFICATION_PACK" ? ("PRIME" as const) : current.currency,
                }

                return pricing ? normalizeCreatorPrice(next, pricing) : next
              })
            }
          >
            <Glyph name={creatorTypeGlyph[value]} size={18} />
            <span>{creatorTypeLabel(t, value)}</span>
          </button>
        ))}
      </section>

      <section className="creator-form-block">
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

        {form.type === "NOTIFICATION_PACK" ? (
          <CreatorNotificationPackStudio
            form={form}
            descriptor={descriptor}
            editable={editable}
            setForm={setForm}
          />
        ) : (
          <>
            <CreatorDesignPresets form={form} editable={editable} setForm={setForm} />
            <CreatorPersonalizationControls form={form} editable={editable} setForm={setForm} />
            <CreatorVisualLayerStudio form={form} editable={editable} setForm={setForm} />
          </>
        )}
      </section>

      <section className="creator-form-block">
        <header>
          <small>{t("creator.editor.publicationEyebrow")}</small>
          <strong>{t("creator.editor.priceAndStatus")}</strong>
        </header>

        <div className="creator-price-row">
          {form.type === "NOTIFICATION_PACK" ? (
            <span className="creator-fixed-currency">
              <CurrencyMark currency="PRIME" size={15} /> PRIME
            </span>
          ) : (
            <div className="creator-currency-switch">
              {(["COIN", "PRIME"] as Currency[]).map((value) => (
                <button
                  type="button"
                  key={value}
                  disabled={!editable}
                  className={form.currency === value ? "is-active" : ""}
                  onClick={() =>
                    setForm((current) => {
                      const next = { ...current, currency: value }

                      return pricing ? normalizeCreatorPrice(next, pricing) : next
                    })
                  }
                >
                  {value}
                </button>
              ))}
            </div>
          )}

          <input
            type="number"
            min={priceRange?.min ?? 1}
            max={priceRange?.max}
            step={1}
            value={form.price}
            disabled={!editable}
            aria-invalid={!priceValid}
            onChange={(event) =>
              setForm((current) => ({
                ...current,
                price: event.target.value,
              }))
            }
          />
        </div>

        {priceRange ? (
          <p className="inline-hint inline-hint--left">
            {priceRange.min}–{priceRange.max} {priceRange.currency}
          </p>
        ) : null}

        {currentSource ? (
          <p className="inline-hint inline-hint--left">
            {t("creator.editor.status", {
              status: creatorStatusLabel(t, currentSource.status),
            })}
          </p>
        ) : null}
      </section>

      <div className="creator-editor-actions">
        {editable ? (
          <>
            <button type="button" disabled={!valid || busy} onClick={() => void saveAndBack()}>
              {busy ? t("creator.editor.saving") : t("creator.editor.save")}
            </button>
            <button
              type="button"
              className="is-primary"
              disabled={!valid || busy}
              onClick={() => void submit()}
            >
              {busy ? t("creator.editor.submitting") : t("creator.editor.submit")}
            </button>
          </>
        ) : currentSource?.status === "APPROVED" ? (
          <button
            type="button"
            className="is-primary"
            disabled={busy}
            onClick={() => void publish()}
          >
            {busy ? t("creator.editor.publishing") : t("creator.editor.publish")}
          </button>
        ) : (
          <button type="button" disabled>
            {t("creator.editor.editingUnavailable")}
          </button>
        )}
      </div>
    </div>
  )
}
