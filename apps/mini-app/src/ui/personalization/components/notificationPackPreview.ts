import type {
  NotificationPackContent,
  NotificationPackValueKey,
  NotificationPackVariantField,
  NotificationTemplate,
  NotificationTemplateKey,
  NotificationTemplateVariant,
} from "@/ui/model"

export const notificationTemplateKeys: NotificationTemplateKey[] = [
  "ITEM_DROP",
  "BOOSTER",
  "DUNGEON",
  "RAID",
  "GLOBAL",
  "PLAYER_OFFLINE",
]

const valueKeyByPlaceholder: Partial<Record<string, NotificationPackValueKey>> = {
  rarity: "ITEM_RARITY",
  itemType: "ITEM_TYPE",
  booster: "BOOSTER",
  dungeon: "DUNGEON",
  event: "GLOBAL_EVENT",
}

const placeholderByConditionField: Record<NotificationPackVariantField, string> = {
  ITEM_RARITY: "rarity",
  ITEM_TYPE: "itemType",
  BOOSTER: "booster",
  DUNGEON: "dungeon",
  GLOBAL_EVENT: "event",
}

const fallbackValueLabels: Partial<Record<NotificationPackValueKey, Record<string, string>>> = {
  ITEM_RARITY: {
    MYTHICAL: "Мифический",
    SECRET: "Секретный",
  },
  ITEM_TYPE: {
    SWORD: "меч",
    AURA: "аура",
    PET: "питомец",
    BOOK: "книга",
    JEWELRY: "реликвия",
  },
  BOOSTER: {
    LUCK: "Удача",
    MONEY: "Доход",
    DAMAGE: "Урон",
    POWER: "Сила",
  },
  DUNGEON: {
    EASY: "Лёгкое подземелье",
    MEDIUM: "Среднее подземелье",
    HARD: "Сложное подземелье",
    EXPERT: "Экспертное подземелье",
  },
  GLOBAL_EVENT: {
    DARKNESS: "Тьма",
    MOON: "Кровавая луна",
    ECLIPSE: "Затмение",
    EXPLOSION: "Взрыв",
  },
}

export function notificationPreviewValues(type: NotificationTemplateKey): Record<string, string> {
  if (type === "ITEM_DROP") {
    return {
      player: "north",
      item: "Вечный клинок",
      rarity: "MYTHICAL",
      itemType: "SWORD",
    }
  }

  if (type === "BOOSTER") {
    return {
      player: "vex",
      booster: "LUCK",
    }
  }

  if (type === "DUNGEON") {
    return {
      dungeon: "EXPERT",
    }
  }

  if (type === "RAID") {
    return {
      locations: "2 · 4 · 7",
    }
  }

  if (type === "PLAYER_OFFLINE") {
    return {
      player: "KoTuK_PvP",
      realm: "SAO-4",
      lastSeen: "00:37",
    }
  }

  return {
    event: "ECLIPSE",
  }
}

export function valuesForVariant(
  type: NotificationTemplateKey,
  variant: NotificationTemplateVariant | null,
): Record<string, string> {
  const values = notificationPreviewValues(type)

  variant?.conditions.forEach((condition) => {
    values[placeholderByConditionField[condition.field]] = condition.value
  })

  return values
}

export function selectedNotificationTemplate(
  template: NotificationTemplate,
  variantId: string | null,
): Omit<NotificationTemplate, "variants"> {
  const variant = variantId
    ? template.variants.find((candidate) => candidate.id === variantId)
    : null

  if (!variant) {
    return {
      emoji: template.emoji,
      soloTitle: template.soloTitle,
      soloBody: template.soloBody,
      compact: template.compact,
      aggregateTitle: template.aggregateTitle,
    }
  }

  return {
    emoji: variant.emoji,
    soloTitle: variant.soloTitle,
    soloBody: variant.soloBody,
    compact: variant.compact,
    aggregateTitle: variant.aggregateTitle,
  }
}

export function renderNotificationTemplate(
  content: NotificationPackContent,
  template: string,
  values: Record<string, string>,
): string {
  return template.replace(
    /\{([a-zA-Z][a-zA-Z0-9_]*)(?:\.([a-zA-Z][a-zA-Z0-9_]*))?}/g,
    (placeholder, key: string, form?: string) => {
      const rawValue = values[key]

      if (!rawValue) {
        return placeholder
      }

      const valueKey = valueKeyByPlaceholder[key]

      if (!valueKey) {
        return rawValue
      }

      const mapped = content.valueMappings[valueKey]?.[rawValue]

      if (mapped) {
        if (!form || form === "default") {
          return mapped.default
        }

        return mapped.forms[form] ?? mapped.default
      }

      return fallbackValueLabels[valueKey]?.[rawValue] ?? rawValue
    },
  )
}

export function variantLabel(variant: NotificationTemplateVariant): string {
  return variant.conditions
    .map((condition) => {
      const valueKey = valueKeyByPlaceholder[placeholderByConditionField[condition.field]]
      const value = valueKey
        ? (fallbackValueLabels[valueKey]?.[condition.value] ?? condition.value)
        : condition.value

      return value
    })
    .join(" · ")
}

export function withNotificationEmoji(emoji: string | null, value: string): string {
  const normalized = emoji?.trim()

  return normalized ? `${normalized} ${value}` : value
}
