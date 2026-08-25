import type { Dispatch, ReactElement, SetStateAction } from "react"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type {
  NotificationPackEditorDescriptorResponse,
  NotificationPackEditorFieldKey,
} from "@/shared/api/creatorPersonalizationApi"
import type {
  NotificationTemplateResponse,
  NotificationTemplateVariantResponse,
} from "@/shared/api/personalizationApi"
import {
  BoosterArt,
  DungeonArt,
  GlobalEventArt,
  ItemTypeArt,
  RaidArt,
} from "../../components/events/EventArt"
import type {
  NotificationPackContent,
  NotificationTemplate,
  NotificationTemplateKey,
  NotificationTemplateVariant,
} from "../../model"
import {
  renderNotificationTemplate,
  valuesForVariant,
  variantLabel,
  withNotificationEmoji,
} from "../../personalization/components/notificationPackPreview"
import { Field } from "../../shared/Primitives"
import type { CreatorEditorForm } from "./creatorEditorModel"

type EditorMode = "SOLO" | "AGGREGATE"
type AggregatePreviewMode = "SAME_TYPE" | "MIXED"
type EditableTemplateField = "soloTitle" | "soloBody" | "compact" | "aggregateTitle"
type EditableMessageFields = Pick<
  NotificationTemplateResponse,
  "emoji" | "soloTitle" | "soloBody" | "compact" | "aggregateTitle"
>

type Props = {
  form: CreatorEditorForm
  descriptor: NotificationPackEditorDescriptorResponse
  editable: boolean
  setForm: Dispatch<SetStateAction<CreatorEditorForm>>
}

const fieldContractKey: Record<EditableTemplateField, NotificationPackEditorFieldKey> = {
  soloTitle: "SOLO_TITLE",
  soloBody: "SOLO_BODY",
  compact: "COMPACT",
  aggregateTitle: "AGGREGATE_TITLE",
}

export function CreatorNotificationPackStudio({ form, descriptor, editable, setForm }: Props) {
  const { t } = useTranslation()
  const [selectedType, setSelectedType] = useState<NotificationTemplateKey>(
    descriptor.templates[0]?.type ?? "ITEM_DROP",
  )
  const [selectedVariantId, setSelectedVariantId] = useState<string | null>(null)
  const [mode, setMode] = useState<EditorMode>("SOLO")
  const [aggregatePreviewMode, setAggregatePreviewMode] =
    useState<AggregatePreviewMode>("SAME_TYPE")
  const [activeField, setActiveField] = useState<EditableTemplateField>("soloBody")
  const templateDescriptor =
    descriptor.templates.find((template) => template.type === selectedType) ??
    descriptor.templates[0]
  const template = templateDescriptor
    ? form.notificationPack.templates[templateDescriptor.type]
    : null

  const variableFields = useMemo(
    () =>
      new Set(
        descriptor.fields.filter((field) => field.variablesAllowed).map((field) => field.key),
      ),
    [descriptor.fields],
  )

  if (!templateDescriptor || !template) {
    return null
  }

  const variants = template.variants ?? []
  const selectedVariant =
    selectedVariantId === null
      ? null
      : variants.find((variant) => variant.id === selectedVariantId) ?? null
  const message: EditableMessageFields = selectedVariant ?? template
  const previewContent = {
    kind: "NOTIFICATION_PACK",
    mixedAggregateTitle: form.notificationPack.mixedAggregateTitle,
    templates: form.notificationPack.templates as Record<NotificationTemplateKey, NotificationTemplate>,
    valueMappings: form.notificationPack.valueMappings,
  } satisfies NotificationPackContent
  const previewValues = valuesForVariant(
    templateDescriptor.type,
    selectedVariant as NotificationTemplateVariant | null,
  )

  const updateTemplate = (next: Partial<NotificationTemplateResponse>) => {
    setForm((current) => ({
      ...current,
      notificationPack: {
        ...current.notificationPack,
        templates: {
          ...current.notificationPack.templates,
          [templateDescriptor.type]: {
            ...current.notificationPack.templates[templateDescriptor.type],
            ...next,
          },
        },
      },
    }))
  }

  const updateSelectedMessage = (next: Partial<EditableMessageFields>) => {
    if (!selectedVariant) {
      updateTemplate(next)
      return
    }

    updateTemplate({
      variants: variants.map((variant) =>
        variant.id === selectedVariant.id ? { ...variant, ...next } : variant,
      ),
    })
  }

  const updateVariant = (next: Partial<NotificationTemplateVariantResponse>) => {
    if (!selectedVariant) {
      return
    }

    updateTemplate({
      variants: variants.map((variant) =>
        variant.id === selectedVariant.id ? { ...variant, ...next } : variant,
      ),
    })
  }

  const addVariant = () => {
    const firstField = templateDescriptor.conditionFields[0]
    const firstValue = firstField?.values[0]

    if (!editable || !firstField || !firstValue) {
      return
    }

    const variant: NotificationTemplateVariantResponse = {
      id: `variant_${Date.now().toString(36)}_${variants.length + 1}`,
      conditions: [
        {
          field: firstField.field,
          value: firstValue.value,
        },
      ],
      emoji: template.emoji,
      soloTitle: template.soloTitle,
      soloBody: template.soloBody,
      compact: template.compact,
      aggregateTitle: template.aggregateTitle,
    }

    updateTemplate({ variants: [...variants, variant] })
    setSelectedVariantId(variant.id)
  }

  const removeVariant = () => {
    if (!editable || !selectedVariant) {
      return
    }

    updateTemplate({
      variants: variants.filter((variant) => variant.id !== selectedVariant.id),
    })
    setSelectedVariantId(null)
  }

  const addCondition = () => {
    if (!selectedVariant) {
      return
    }

    const usedFields = new Set(selectedVariant.conditions.map((condition) => condition.field))
    const nextField = templateDescriptor.conditionFields.find(
      (field) => !usedFields.has(field.field),
    )
    const nextValue = nextField?.values[0]

    if (!nextField || !nextValue) {
      return
    }

    updateVariant({
      conditions: [
        ...selectedVariant.conditions,
        {
          field: nextField.field,
          value: nextValue.value,
        },
      ],
    })
  }

  const updateConditionField = (index: number, fieldName: string) => {
    if (!selectedVariant) {
      return
    }

    const field = templateDescriptor.conditionFields.find(
      (candidate) => candidate.field === fieldName,
    )
    const value = field?.values[0]

    if (!field || !value) {
      return
    }

    updateVariant({
      conditions: selectedVariant.conditions.map((condition, conditionIndex) =>
        conditionIndex === index
          ? {
              field: field.field,
              value: value.value,
            }
          : condition,
      ),
    })
  }

  const updateConditionValue = (index: number, value: string) => {
    if (!selectedVariant) {
      return
    }

    updateVariant({
      conditions: selectedVariant.conditions.map((condition, conditionIndex) =>
        conditionIndex === index ? { ...condition, value } : condition,
      ),
    })
  }

  const removeCondition = (index: number) => {
    if (!selectedVariant || selectedVariant.conditions.length === 1) {
      return
    }

    updateVariant({
      conditions: selectedVariant.conditions.filter((_, conditionIndex) => conditionIndex !== index),
    })
  }

  const updateMixedAggregateTitle = (mixedAggregateTitle: string) => {
    setForm((current) => ({
      ...current,
      notificationPack: {
        ...current.notificationPack,
        mixedAggregateTitle,
      },
    }))
  }

  const insertVariable = (placeholder: string) => {
    const contractKey = fieldContractKey[activeField]

    if (!editable || !variableFields.has(contractKey)) {
      return
    }

    const currentValue = message[activeField]
    const spacer = currentValue && !currentValue.endsWith(" ") ? " " : ""

    updateSelectedMessage({
      [activeField]: `${currentValue}${spacer}${placeholder}`,
    })
  }

  const soloText = [
    withNotificationEmoji(
      message.emoji,
      renderNotificationTemplate(previewContent, message.soloTitle, previewValues),
    ),
    renderNotificationTemplate(previewContent, message.soloBody, previewValues),
  ].join("\n")
  const sameTypeAggregateText = [
    withNotificationEmoji(message.emoji, message.aggregateTitle),
    withNotificationEmoji(
      message.emoji,
      renderNotificationTemplate(previewContent, message.compact, previewValues),
    ),
    withNotificationEmoji(
      message.emoji,
      renderNotificationTemplate(previewContent, message.compact, previewValues),
    ),
  ].join("\n\n")
  const mixedTemplates = descriptor.templates.slice(0, Math.min(3, descriptor.templates.length))
  const mixedAggregateText = [
    form.notificationPack.mixedAggregateTitle,
    ...mixedTemplates.map((candidate) => {
      const candidateTemplate = form.notificationPack.templates[candidate.type]

      if (!candidateTemplate) {
        return ""
      }

      return withNotificationEmoji(
        candidateTemplate.emoji,
        renderNotificationTemplate(
          previewContent,
          candidateTemplate.compact,
          valuesForVariant(candidate.type, null),
        ),
      )
    }),
  ]
    .filter(Boolean)
    .join("\n\n")
  const telegramText =
    mode === "SOLO"
      ? soloText
      : aggregatePreviewMode === "SAME_TYPE"
        ? sameTypeAggregateText
        : mixedAggregateText
  const canAddCondition = Boolean(
    selectedVariant && selectedVariant.conditions.length < templateDescriptor.conditionFields.length,
  )

  return (
    <section className="notification-pack-studio">
      <header className="notification-pack-studio__header">
        <span>
          <small>{t("creator.editor.notificationEyebrow")}</small>
          <strong>{t("creator.editor.notificationStudio")}</strong>
        </span>
        <em>{t("creator.editor.serverDriven")}</em>
      </header>

      <nav
        className="notification-pack-template-tabs"
        aria-label={t("creator.editor.eventTemplate")}
      >
        {descriptor.templates.map((candidate) => (
          <button
            type="button"
            key={candidate.type}
            className={candidate.type === templateDescriptor.type ? "is-active" : ""}
            onClick={() => {
              setSelectedType(candidate.type)
              setSelectedVariantId(null)
            }}
          >
            <span>{eventArt(candidate.type)}</span>
            <b>{t(`creator.editor.notificationTypes.${candidate.type}`)}</b>
          </button>
        ))}
      </nav>

      <section className="notification-variant-workspace">
        <header>
          <span>
            <small>{t("creator.editor.variantsEyebrow")}</small>
            <strong>{t("creator.editor.variantsTitle")}</strong>
          </span>

          {templateDescriptor.conditionFields.length ? (
            <button type="button" disabled={!editable} onClick={addVariant}>
              {t("creator.editor.addVariant")}
            </button>
          ) : null}
        </header>

        {variants.length ? (
          <nav className="notification-variant-switch" aria-label={t("creator.editor.variantsTitle")}>
            <button
              type="button"
              className={selectedVariant === null ? "is-active" : ""}
              onClick={() => setSelectedVariantId(null)}
            >
              {t("creator.editor.defaultVariant")}
            </button>

            {variants.map((variant) => (
              <button
                type="button"
                key={variant.id}
                className={selectedVariant?.id === variant.id ? "is-active" : ""}
                onClick={() => setSelectedVariantId(variant.id)}
              >
                {variantLabel(variant as NotificationTemplateVariant)}
              </button>
            ))}
          </nav>
        ) : (
          <p>{t("creator.editor.noVariants")}</p>
        )}

        {selectedVariant ? (
          <div className="notification-variant-conditions">
            <header>
              <span>
                <small>{t("creator.editor.conditionsEyebrow")}</small>
                <strong>{t("creator.editor.conditionsTitle")}</strong>
              </span>
              <button type="button" disabled={!editable} onClick={removeVariant}>
                {t("creator.editor.removeVariant")}
              </button>
            </header>

            {selectedVariant.conditions.map((condition, index) => {
              const field = templateDescriptor.conditionFields.find(
                (candidate) => candidate.field === condition.field,
              )
              const usedByOtherConditions = new Set(
                selectedVariant.conditions
                  .filter((_, conditionIndex) => conditionIndex !== index)
                  .map((candidate) => candidate.field),
              )

              return (
                <div className="notification-condition-row" key={`${condition.field}-${index}`}>
                  <select
                    value={condition.field}
                    disabled={!editable}
                    onChange={(event) => updateConditionField(index, event.target.value)}
                  >
                    {templateDescriptor.conditionFields.map((candidate) => (
                      <option
                        key={candidate.field}
                        value={candidate.field}
                        disabled={usedByOtherConditions.has(candidate.field)}
                      >
                        {candidate.placeholder}
                      </option>
                    ))}
                  </select>

                  <select
                    value={condition.value}
                    disabled={!editable}
                    onChange={(event) => updateConditionValue(index, event.target.value)}
                  >
                    {field?.values.map((candidate) => (
                      <option key={candidate.value} value={candidate.value}>
                        {candidate.sampleValue}
                      </option>
                    ))}
                  </select>

                  <button
                    type="button"
                    disabled={!editable || selectedVariant.conditions.length === 1}
                    onClick={() => removeCondition(index)}
                    aria-label={t("creator.editor.removeCondition")}
                  >
                    ×
                  </button>
                </div>
              )
            })}

            {canAddCondition ? (
              <button
                type="button"
                className="notification-add-condition"
                disabled={!editable}
                onClick={addCondition}
              >
                {t("creator.editor.addCondition")}
              </button>
            ) : null}
          </div>
        ) : null}
      </section>

      <div className="notification-pack-mode-switch">
        {(["SOLO", "AGGREGATE"] as EditorMode[]).map((value) => (
          <button
            type="button"
            key={value}
            className={mode === value ? "is-active" : ""}
            onClick={() => {
              setMode(value)
              setActiveField(value === "SOLO" ? "soloBody" : "compact")
            }}
          >
            {t(`creator.editor.notificationModes.${value}`)}
          </button>
        ))}
      </div>

      <div className="notification-pack-workspace">
        <div className="notification-pack-fields">
          <div className="notification-pack-fields__top">
            <Field label={t("creator.editor.emoji")}>
              <input
                value={message.emoji ?? ""}
                maxLength={descriptor.maxEmojiLength}
                disabled={!editable}
                onChange={(event) =>
                  updateSelectedMessage({ emoji: event.target.value || null })
                }
                placeholder="🎁"
              />
            </Field>

            <span className="notification-pack-limit">
              {t("creator.editor.textLimit", { count: descriptor.maxTextLength })}
            </span>
          </div>

          {mode === "SOLO" ? (
            <>
              <Field label={t("creator.editor.soloTitle")}>
                <input
                  value={message.soloTitle}
                  maxLength={descriptor.maxTextLength}
                  disabled={!editable}
                  onFocus={() => setActiveField("soloTitle")}
                  onChange={(event) => updateSelectedMessage({ soloTitle: event.target.value })}
                />
              </Field>

              <Field label={t("creator.editor.soloBody")}>
                <textarea
                  value={message.soloBody}
                  maxLength={descriptor.maxTextLength}
                  disabled={!editable}
                  onFocus={() => setActiveField("soloBody")}
                  onChange={(event) => updateSelectedMessage({ soloBody: event.target.value })}
                />
              </Field>
            </>
          ) : (
            <>
              <Field label={t("creator.editor.aggregateTitle")}>
                <input
                  value={message.aggregateTitle}
                  maxLength={descriptor.maxTextLength}
                  disabled={!editable}
                  onFocus={() => setActiveField("aggregateTitle")}
                  onChange={(event) =>
                    updateSelectedMessage({ aggregateTitle: event.target.value })
                  }
                />
              </Field>

              <Field label={t("creator.editor.compactMessage")}>
                <textarea
                  value={message.compact}
                  maxLength={descriptor.maxTextLength}
                  disabled={!editable}
                  onFocus={() => setActiveField("compact")}
                  onChange={(event) => updateSelectedMessage({ compact: event.target.value })}
                />
              </Field>

              {selectedVariant === null ? (
                <Field label={t("creator.editor.mixedAggregateTitle")}>
                  <input
                    value={form.notificationPack.mixedAggregateTitle}
                    maxLength={descriptor.maxTextLength}
                    disabled={!editable}
                    onChange={(event) => updateMixedAggregateTitle(event.target.value)}
                  />
                </Field>
              ) : null}
            </>
          )}

          {variableFields.has(fieldContractKey[activeField]) &&
          templateDescriptor.variables.length ? (
            <section className="notification-variable-palette">
              <header>
                <span>
                  <small>{t("creator.editor.variablesEyebrow")}</small>
                  <strong>{t("creator.editor.availableVariables")}</strong>
                </span>
                <em>{t("creator.editor.insertIntoActiveField")}</em>
              </header>

              <div className="notification-variable-groups">
                {templateDescriptor.variables.map((variable) => (
                  <section key={variable.key}>
                    <strong>{variable.key}</strong>
                    <div>
                      {variable.forms.map((variableForm) => (
                        <button
                          type="button"
                          key={variableForm.placeholder}
                          disabled={!editable}
                          onClick={() => insertVariable(variableForm.placeholder)}
                        >
                          <code>{variableForm.placeholder}</code>
                          <small>{variableForm.sampleValue}</small>
                        </button>
                      ))}
                    </div>
                  </section>
                ))}
              </div>
            </section>
          ) : null}
        </div>

        <div className="notification-preview-column">
          <section className="telegram-message-preview">
            <header>
              <span>
                <small>{t("creator.editor.telegramPreview")}</small>
                <strong>Mnemos</strong>
              </span>
              <i>now</i>
            </header>

            {mode === "AGGREGATE" ? (
              <div className="notification-aggregate-preview-switch">
                <button
                  type="button"
                  className={aggregatePreviewMode === "SAME_TYPE" ? "is-active" : ""}
                  onClick={() => setAggregatePreviewMode("SAME_TYPE")}
                >
                  {t("creator.editor.sameType")}
                </button>
                <button
                  type="button"
                  className={aggregatePreviewMode === "MIXED" ? "is-active" : ""}
                  onClick={() => setAggregatePreviewMode("MIXED")}
                >
                  {t("creator.editor.mixed")}
                </button>
              </div>
            ) : null}

            <div className="telegram-message-preview__chat">
              <div className="telegram-message-preview__bubble">
                {telegramText.split("\n").map((line, index) => (
                  <span key={`${index}-${line}`}>{line || "\u00a0"}</span>
                ))}
                <time>00:37</time>
              </div>
            </div>
          </section>

          <section className="mini-app-notification-preview">
            <header>
              <small>{t("creator.editor.miniAppPreview")}</small>
              <strong>{t(`creator.editor.notificationTypes.${templateDescriptor.type}`)}</strong>
            </header>

            <div>
              <span>{eventArt(templateDescriptor.type)}</span>
              <span>
                <small>
                  {withNotificationEmoji(
                    message.emoji,
                    renderNotificationTemplate(previewContent, message.soloTitle, previewValues),
                  )}
                </small>
                <strong>
                  {renderNotificationTemplate(previewContent, message.soloBody, previewValues)}
                </strong>
              </span>
            </div>
          </section>
        </div>
      </div>
    </section>
  )
}

function eventArt(type: NotificationTemplateKey): ReactElement {
  if (type === "ITEM_DROP") {
    return <ItemTypeArt itemType="SWORD" size={42} />
  }

  if (type === "BOOSTER") {
    return <BoosterArt boosterType="LUCK" size={42} />
  }

  if (type === "DUNGEON") {
    return <DungeonArt size={42} />
  }

  if (type === "RAID") {
    return <RaidArt size={42} />
  }

  return <GlobalEventArt eventType="ECLIPSE" size={42} />
}
