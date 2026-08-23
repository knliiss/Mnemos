import { describe, expect, it } from "vitest"
import type { NotificationPackEditorDescriptorResponse } from "@/shared/api/creatorPersonalizationApi"
import type { NotificationTemplateResponse } from "@/shared/api/personalizationApi"
import { createDefaultCreatorEditorForm } from "./creatorEditorModel"
import { isNotificationPackFormValid } from "./creatorEditorValidation"

const defaultTemplate: NotificationTemplateResponse = {
  emoji: "🔔",
  soloTitle: "Mnemos event",
  soloBody: "A new event happened",
  compact: "New event",
  aggregateTitle: "New events",
}

const descriptor: NotificationPackEditorDescriptorResponse = {
  mixedAggregateTitleDefault: "Several Mnemos events",
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
      type: "ITEM_DROP",
      variables: [
        { key: "player", sampleValue: "north" },
        { key: "item", sampleValue: "Eternal Blade" },
        { key: "rarity", sampleValue: "mythical" },
        { key: "itemType", sampleValue: "sword" },
      ],
      sampleValues: {
        player: "north",
        item: "Eternal Blade",
        rarity: "mythical",
        itemType: "sword",
      },
      defaults: {
        ...defaultTemplate,
        soloBody: "{player} found {item}",
        compact: "{player} — {item}",
      },
    },
    {
      type: "BOOSTER",
      variables: [
        { key: "player", sampleValue: "vex" },
        { key: "booster", sampleValue: "luck" },
      ],
      sampleValues: {
        player: "vex",
        booster: "luck",
      },
      defaults: {
        ...defaultTemplate,
        soloBody: "{player} activated {booster}",
        compact: "{booster} — {player}",
      },
    },
    {
      type: "DUNGEON",
      variables: [{ key: "dungeon", sampleValue: "expert" }],
      sampleValues: {
        dungeon: "expert",
      },
      defaults: {
        ...defaultTemplate,
        soloBody: "{dungeon} dungeon opened",
        compact: "{dungeon} dungeon",
      },
    },
    {
      type: "RAID",
      variables: [{ key: "locations", sampleValue: "2, 4, 7" }],
      sampleValues: {
        locations: "2, 4, 7",
      },
      defaults: {
        ...defaultTemplate,
        soloBody: "Raid at {locations}",
        compact: "Raid — {locations}",
      },
    },
    {
      type: "GLOBAL",
      variables: [{ key: "event", sampleValue: "eclipse" }],
      sampleValues: {
        event: "eclipse",
      },
      defaults: {
        ...defaultTemplate,
        soloBody: "Global event: {event}",
        compact: "{event}",
      },
    },
  ],
}

function notificationPackForm() {
  return {
    ...createDefaultCreatorEditorForm(descriptor),
    type: "NOTIFICATION_PACK" as const,
    name: "Signal Pack",
  }
}

describe("notification pack creator validation", () => {
  it("accepts descriptor defaults", () => {
    expect(isNotificationPackFormValid(notificationPackForm(), descriptor)).toBe(true)
  })

  it("rejects a placeholder that is not allowed for the selected event", () => {
    const form = notificationPackForm()

    form.notificationPack.templates.ITEM_DROP = {
      ...form.notificationPack.templates.ITEM_DROP,
      soloBody: "{booster}",
    }

    expect(isNotificationPackFormValid(form, descriptor)).toBe(false)
  })

  it("rejects raw Telegram HTML", () => {
    const form = notificationPackForm()

    form.notificationPack.templates.GLOBAL = {
      ...form.notificationPack.templates.GLOBAL,
      soloBody: "<b>{event}</b>",
    }

    expect(isNotificationPackFormValid(form, descriptor)).toBe(false)
  })

  it("rejects malformed placeholders", () => {
    const form = notificationPackForm()

    form.notificationPack.templates.RAID = {
      ...form.notificationPack.templates.RAID,
      compact: "Raid at {locations",
    }

    expect(isNotificationPackFormValid(form, descriptor)).toBe(false)
  })

  it("rejects placeholders in static aggregate titles", () => {
    const form = notificationPackForm()

    form.notificationPack.templates.DUNGEON = {
      ...form.notificationPack.templates.DUNGEON,
      aggregateTitle: "New {dungeon} dungeons",
    }

    expect(isNotificationPackFormValid(form, descriptor)).toBe(false)
  })

  it("rejects a pack with a missing descriptor template", () => {
    const form = notificationPackForm()

    delete form.notificationPack.templates.BOOSTER

    expect(isNotificationPackFormValid(form, descriptor)).toBe(false)
  })
})
