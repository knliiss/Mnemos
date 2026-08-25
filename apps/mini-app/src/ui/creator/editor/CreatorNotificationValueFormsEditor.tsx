import type { TFunction } from "i18next"
import type { Dispatch, SetStateAction } from "react"
import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import type { NotificationPackEditorDescriptorResponse } from "@/shared/api/creatorPersonalizationApi"
import type {
  NotificationPackValueKey,
  NotificationValueForms,
} from "../../model"
import { Glyph } from "../../shared/MnemosIcon"
import type { CreatorEditorForm } from "./creatorEditorModel"

type Props = {
  form: CreatorEditorForm
  descriptor: NotificationPackEditorDescriptorResponse
  editable: boolean
  setForm: Dispatch<SetStateAction<CreatorEditorForm>>
}

type FormEntry = {
  key: string
  value: string
}

const valueKeyOrder: NotificationPackValueKey[] = [
  "BOOSTER",
  "DUNGEON",
  "ITEM_TYPE",
  "ITEM_RARITY",
  "GLOBAL_EVENT",
]

const placeholderByValueKey: Record<NotificationPackValueKey, string> = {
  ITEM_RARITY: "rarity",
  ITEM_TYPE: "itemType",
  BOOSTER: "booster",
  DUNGEON: "dungeon",
  GLOBAL_EVENT: "event",
}

export function CreatorNotificationValueFormsEditor({
  form,
  descriptor,
  editable,
  setForm,
}: Props) {
  const { t } = useTranslation()
  const availableKeys = useMemo(
    () =>
      valueKeyOrder.filter((key) => {
        const mappings = form.notificationPack.valueMappings?.[key] ?? descriptor.valueMappings?.[key]

        return Boolean(mappings && Object.keys(mappings).length)
      }),
    [descriptor.valueMappings, form.notificationPack.valueMappings],
  )
  const [selectedKey, setSelectedKey] = useState<NotificationPackValueKey>(
    availableKeys[0] ?? "BOOSTER",
  )
  const activeKey = availableKeys.includes(selectedKey) ? selectedKey : availableKeys[0]

  if (!activeKey) {
    return null
  }

  const mappings =
    form.notificationPack.valueMappings?.[activeKey] ?? descriptor.valueMappings?.[activeKey] ?? {}

  const updateForms = (
    rawValue: string,
    next: NotificationValueForms,
  ) => {
    setForm((current) => ({
      ...current,
      notificationPack: {
        ...current.notificationPack,
        valueMappings: {
          ...(current.notificationPack.valueMappings ?? descriptor.valueMappings ?? {}),
          [activeKey]: {
            ...(current.notificationPack.valueMappings?.[activeKey] ??
              descriptor.valueMappings?.[activeKey] ??
              {}),
            [rawValue]: next,
          },
        },
      },
    }))
  }

  return (
    <section className="notification-word-forms">
      <header className="notification-word-forms__header">
        <span>
          <small>{t("creator.editor.wordForms.eyebrow")}</small>
          <strong>{t("creator.editor.wordForms.title")}</strong>
        </span>
        <em>{t("creator.editor.wordForms.hint")}</em>
      </header>

      <nav className="notification-word-forms__tabs" aria-label={t("creator.editor.wordForms.title")}>
        {availableKeys.map((key) => (
          <button
            type="button"
            key={key}
            className={activeKey === key ? "is-active" : ""}
            onClick={() => setSelectedKey(key)}
          >
            {t(`creator.editor.wordForms.valueKeys.${key}`)}
          </button>
        ))}
      </nav>

      <div className="notification-word-forms__guide">
        <Glyph name="spark" size={15} />
        <span>
          <strong>{t("creator.editor.wordForms.guideTitle")}</strong>
          <small>{t("creator.editor.wordForms.guideCopy")}</small>
        </span>
      </div>

      <div className="notification-word-forms__values">
        {Object.entries(mappings).map(([rawValue, valueForms]) => (
          <ValueFormsCard
            key={rawValue}
            valueKey={activeKey}
            rawValue={rawValue}
            forms={valueForms}
            editable={editable}
            onChange={(next) => updateForms(rawValue, next)}
          />
        ))}
      </div>
    </section>
  )
}

function ValueFormsCard({
  valueKey,
  rawValue,
  forms,
  editable,
  onChange,
}: {
  valueKey: NotificationPackValueKey
  rawValue: string
  forms: NotificationValueForms
  editable: boolean
  onChange: (next: NotificationValueForms) => void
}) {
  const { t } = useTranslation()
  const placeholder = placeholderByValueKey[valueKey]
  const entries = formEntries(forms)

  const updateEntry = (formKey: string, value: string) => {
    if (formKey === "default") {
      onChange({
        ...forms,
        default: value,
      })
      return
    }

    onChange({
      ...forms,
      forms: {
        ...forms.forms,
        [formKey]: value,
      },
    })
  }

  return (
    <details className="notification-word-form-card">
      <summary>
        <span>
          <small>{valueLabel(t, valueKey, rawValue)}</small>
          <strong>{forms.default}</strong>
        </span>
        <span className="notification-word-form-card__count">
          {t("creator.editor.wordForms.formsCount", { count: entries.length })}
          <i className="notification-word-form-card__chevron" aria-hidden="true">
            ⌄
          </i>
        </span>
      </summary>

      <div className="notification-word-form-card__body">
        {entries.map((entry) => {
          const placeholderValue =
            entry.key === "default" ? `{${placeholder}}` : `{${placeholder}.${entry.key}}`

          return (
            <label key={entry.key} className="notification-word-form-field">
              <span className="notification-word-form-field__label">
                <span>
                  <strong>{formName(t, entry.key)}</strong>
                  <small>{formQuestion(t, entry.key)}</small>
                </span>
                <code>{placeholderValue}</code>
              </span>
              <input
                value={entry.value}
                disabled={!editable}
                onChange={(event) => updateEntry(entry.key, event.target.value)}
              />
              <small className="notification-word-form-field__example">
                {t("creator.editor.wordForms.preview", {
                  placeholder: placeholderValue,
                  value: entry.value,
                })}
              </small>
            </label>
          )
        })}
      </div>
    </details>
  )
}

function formEntries(forms: NotificationValueForms): FormEntry[] {
  return [
    {
      key: "default",
      value: forms.default,
    },
    ...Object.entries(forms.forms).map(([key, value]) => ({ key, value })),
  ]
}

function valueLabel(
  t: TFunction,
  valueKey: NotificationPackValueKey,
  rawValue: string,
): string {
  if (valueKey === "ITEM_RARITY") {
    return t(`events.rarities.${rawValue}`)
  }

  if (valueKey === "ITEM_TYPE") {
    return t(`events.itemTypes.${rawValue}`)
  }

  if (valueKey === "BOOSTER") {
    return t(`events.boosters.${rawValue}`)
  }

  if (valueKey === "DUNGEON") {
    return t(`events.dungeons.${rawValue}`)
  }

  return t(`events.globals.${rawValue}`)
}

function formName(t: TFunction, formKey: string): string {
  return t(`creator.editor.wordForms.formNames.${formKey}`, {
    defaultValue: formKey,
  })
}

function formQuestion(t: TFunction, formKey: string): string {
  return t(`creator.editor.wordForms.formQuestions.${formKey}`, {
    defaultValue: "",
  })
}
