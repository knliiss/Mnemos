import type { Dispatch, ReactElement, SetStateAction } from "react"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type {
  NotificationPackEditorDescriptorResponse,
  NotificationPackEditorFieldKey,
} from "@/shared/api/creatorPersonalizationApi"
import type { NotificationTemplateResponse } from "@/shared/api/personalizationApi"
import {
  BoosterArt,
  DungeonArt,
  GlobalEventArt,
  ItemTypeArt,
  RaidArt,
} from "../../components/events/EventArt"
import type { NotificationTemplateKey } from "../../model"
import { Field } from "../../shared/Primitives"
import type { CreatorEditorForm } from "./creatorEditorModel"

type EditorMode = "SOLO" | "AGGREGATE"
type AggregatePreviewMode = "SAME_TYPE" | "MIXED"
type EditableTemplateField = "soloTitle" | "soloBody" | "compact" | "aggregateTitle"

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

function renderTemplate(template: string, values: Record<string, string>): string {
  return template.replace(
    /\{([a-zA-Z][a-zA-Z0-9_]*)}/g,
    (_, key: string) => values[key] ?? `{${key}}`,
  )
}

function withEmoji(emoji: string | null, value: string): string {
  return emoji?.trim() ? `${emoji.trim()} ${value}` : value
}

export function CreatorNotificationPackStudio({ form, descriptor, editable, setForm }: Props) {
  const { t } = useTranslation()
  const [selectedType, setSelectedType] = useState<NotificationTemplateKey>(
    descriptor.templates[0]?.type ?? "ITEM_DROP",
  )
  const [mode, setMode] = useState<EditorMode>("SOLO")
  const [aggregatePreviewMode, setAggregatePreviewMode] =
    useState<AggregatePreviewMode>("SAME_TYPE")
  const [activeField, setActiveField] = useState<EditableTemplateField>("soloBody")
  const templateDescriptor =
    descriptor.templates.find((template) => template.type === selectedType) ?? descriptor.templates[0]
  const template = templateDescriptor
    ? form.notificationPack.templates[templateDescriptor.type]
    : null

  const variableFields = useMemo(
    () =>
      new Set(
        descriptor.fields
          .filter((field) => field.variablesAllowed)
          .map((field) => field.key),
      ),
    [descriptor.fields],
  )

  if (!templateDescriptor || !template) {
    return null
  }

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

  const updateMixedAggregateTitle = (mixedAggregateTitle: string) => {
    setForm((current) => ({
      ...current,
      notificationPack: {
        ...current.notificationPack,
        mixedAggregateTitle,
      },
    }))
  }

  const insertVariable = (key: string) => {
    const contractKey = fieldContractKey[activeField]

    if (!editable || !variableFields.has(contractKey)) {
      return
    }

    const currentValue = template[activeField]
    const spacer = currentValue && !currentValue.endsWith(" ") ? " " : ""

    updateTemplate({
      [activeField]: `${currentValue}${spacer}{${key}}`,
    })
  }

  const soloText = [
    withEmoji(template.emoji, renderTemplate(template.soloTitle, templateDescriptor.sampleValues)),
    renderTemplate(template.soloBody, templateDescriptor.sampleValues),
  ].join("\n")
  const sameTypeAggregateText = [
    withEmoji(template.emoji, template.aggregateTitle),
    withEmoji(template.emoji, renderTemplate(template.compact, templateDescriptor.sampleValues)),
    withEmoji(template.emoji, renderTemplate(template.compact, templateDescriptor.sampleValues)),
  ].join("\n\n")
  const mixedTemplates = descriptor.templates.slice(0, Math.min(3, descriptor.templates.length))
  const mixedAggregateText = [
    form.notificationPack.mixedAggregateTitle,
    ...mixedTemplates.map((candidate) => {
      const candidateTemplate = form.notificationPack.templates[candidate.type]

      if (!candidateTemplate) {
        return ""
      }

      return withEmoji(
        candidateTemplate.emoji,
        renderTemplate(candidateTemplate.compact, candidate.sampleValues),
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
            onClick={() => setSelectedType(candidate.type)}
          >
            <span>{eventArt(candidate.type)}</span>
            <b>{t(`creator.editor.notificationTypes.${candidate.type}`)}</b>
          </button>
        ))}
      </nav>

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
                value={template.emoji ?? ""}
                maxLength={descriptor.maxEmojiLength}
                disabled={!editable}
                onChange={(event) => updateTemplate({ emoji: event.target.value || null })}
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
                  value={template.soloTitle}
                  maxLength={descriptor.maxTextLength}
                  disabled={!editable}
                  onFocus={() => setActiveField("soloTitle")}
                  onChange={(event) => updateTemplate({ soloTitle: event.target.value })}
                />
              </Field>

              <Field label={t("creator.editor.soloBody")}>
                <textarea
                  value={template.soloBody}
                  maxLength={descriptor.maxTextLength}
                  disabled={!editable}
                  onFocus={() => setActiveField("soloBody")}
                  onChange={(event) => updateTemplate({ soloBody: event.target.value })}
                />
              </Field>
            </>
          ) : (
            <>
              <Field label={t("creator.editor.aggregateTitle")}>
                <input
                  value={template.aggregateTitle}
                  maxLength={descriptor.maxTextLength}
                  disabled={!editable}
                  onFocus={() => setActiveField("aggregateTitle")}
                  onChange={(event) => updateTemplate({ aggregateTitle: event.target.value })}
                />
              </Field>

              <Field label={t("creator.editor.compactMessage")}>
                <textarea
                  value={template.compact}
                  maxLength={descriptor.maxTextLength}
                  disabled={!editable}
                  onFocus={() => setActiveField("compact")}
                  onChange={(event) => updateTemplate({ compact: event.target.value })}
                />
              </Field>

              <Field label={t("creator.editor.mixedAggregateTitle")}>
                <input
                  value={form.notificationPack.mixedAggregateTitle}
                  maxLength={descriptor.maxTextLength}
                  disabled={!editable}
                  onChange={(event) => updateMixedAggregateTitle(event.target.value)}
                />
              </Field>
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

              <div>
                {templateDescriptor.variables.map((variable) => (
                  <button
                    type="button"
                    key={variable.key}
                    disabled={!editable}
                    onClick={() => insertVariable(variable.key)}
                  >
                    <code>{`{${variable.key}}`}</code>
                    <small>{variable.sampleValue}</small>
                  </button>
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
                  {withEmoji(
                    template.emoji,
                    renderTemplate(template.soloTitle, templateDescriptor.sampleValues),
                  )}
                </small>
                <strong>{renderTemplate(template.soloBody, templateDescriptor.sampleValues)}</strong>
              </span>
            </div>
          </section>
        </div>
      </div>
    </section>
  )
}
