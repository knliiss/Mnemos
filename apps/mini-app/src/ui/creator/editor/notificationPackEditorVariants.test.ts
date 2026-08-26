import { describe, expect, it } from "vitest"
import type { NotificationPackEditorDescriptorResponse } from "@/shared/api/creatorPersonalizationApi"
import type {
  NotificationPackContent,
  NotificationTemplate,
  NotificationTemplateKey,
} from "../../model"
import {
  renderNotificationTemplate,
  valuesForVariant,
} from "../../personalization/components/notificationPackPreview"
import { createDefaultCreatorEditorForm } from "./creatorEditorModel"
import { isNotificationPackFormValid } from "./creatorEditorValidation"

const template: NotificationTemplate = {
  emoji: "⚡",
  soloTitle: "Booster",
  soloBody: "{player} активировал {booster.accusative}",
  compact: "{booster}",
  aggregateTitle: "Boosters",
  variants: [],
}

const content: NotificationPackContent = {
  kind: "NOTIFICATION_PACK",
  mixedAggregateTitle: "Several events",
  templates: Object.fromEntries(
    (["ITEM_DROP", "BOOSTER", "DUNGEON", "RAID", "GLOBAL"] as NotificationTemplateKey[]).map(
      (key) => [key, template],
    ),
  ) as Record<NotificationTemplateKey, NotificationTemplate>,
  valueMappings: {
    BOOSTER: {
      LUCK: {
        default: "Удача",
        forms: {
          accusative: "Удачу",
        },
      },
    },
  },
}

const descriptor: NotificationPackEditorDescriptorResponse = {
  mixedAggregateTitleDefault: "Several events",
  maxTextLength: 512,
  maxEmojiLength: 16,
  fields: [
    { key: "SOLO_TITLE", variablesAllowed: true },
    { key: "SOLO_BODY", variablesAllowed: true },
    { key: "COMPACT", variablesAllowed: true },
    { key: "AGGREGATE_TITLE", variablesAllowed: false },
  ],
  templates: [
    {
      type: "BOOSTER",
      variables: [
        {
          key: "player",
          sampleValue: "vex",
          forms: [
            {
              key: "default",
              placeholder: "{player}",
              sampleValue: "vex",
            },
          ],
        },
        {
          key: "booster",
          sampleValue: "Удача",
          forms: [
            {
              key: "default",
              placeholder: "{booster}",
              sampleValue: "Удача",
            },
            {
              key: "accusative",
              placeholder: "{booster.accusative}",
              sampleValue: "Удачу",
            },
          ],
        },
      ],
      sampleValues: {
        player: "vex",
        booster: "Удача",
      },
      conditionFields: [
        {
          field: "BOOSTER",
          placeholder: "booster",
          values: [
            { value: "LUCK", sampleValue: "Удача" },
            { value: "MONEY", sampleValue: "Доход" },
          ],
        },
      ],
      defaults: {
        emoji: "⚡",
        soloTitle: "Booster",
        soloBody: "{player} активировал {booster.accusative}",
        compact: "{booster}",
        aggregateTitle: "Boosters",
      },
    },
  ],
  valueMappings: content.valueMappings,
}

describe("notification pack variants", () => {
  it("renders named value forms from the pack mapping", () => {
    expect(
      renderNotificationTemplate(content, "{booster} → {booster.accusative}", {
        booster: "LUCK",
      }),
    ).toBe("Удача → Удачу")
  })

  it("applies variant conditions to preview values", () => {
    const values = valuesForVariant("BOOSTER", {
      id: "money",
      conditions: [{ field: "BOOSTER", value: "MONEY" }],
      emoji: null,
      soloTitle: "Money",
      soloBody: "{booster}",
      compact: "{booster}",
      aggregateTitle: "Money boosters",
    })

    expect(values.booster).toBe("MONEY")
    expect(values.player).toBe("vex")
  })

  it("validates named forms and typed variant conditions", () => {
    const form = createDefaultCreatorEditorForm(descriptor)

    form.type = "NOTIFICATION_PACK"
    form.name = "Booster Pack"
    form.notificationPack.templates.BOOSTER.variants = [
      {
        id: "luck",
        conditions: [{ field: "BOOSTER", value: "LUCK" }],
        emoji: "🍀",
        soloTitle: "Luck",
        soloBody: "{player} активировал {booster.accusative}",
        compact: "{booster}",
        aggregateTitle: "Luck boosters",
      },
    ]

    expect(isNotificationPackFormValid(form, descriptor)).toBe(true)

    form.notificationPack.templates.BOOSTER.variants[0].conditions[0].value = "UNKNOWN"
    expect(isNotificationPackFormValid(form, descriptor)).toBe(false)

    form.notificationPack.templates.BOOSTER.variants[0].conditions[0].value = "LUCK"
    form.notificationPack.templates.BOOSTER.variants[0].soloBody = "{booster.unknownForm}"
    expect(isNotificationPackFormValid(form, descriptor)).toBe(false)
  })
})
