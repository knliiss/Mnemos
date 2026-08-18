import type { PersonalizationContent } from "./model"

export const personalizationContentById: Record<string, PersonalizationContent> = {
  "p-1": {
    kind: "NOTIFICATION_PACK",
    mixedAggregateTitle: "Нові події",
    templates: {
      ITEM_DROP: {
        emoji: "✦",
        soloTitle: "Новий дроп",
        soloBody: "{player} вибив {item} · {rarity}",
        compact: "{player} → {item}",
        aggregateTitle: "Дропи предметів",
      },
      BOOSTER: {
        emoji: "↑",
        soloTitle: "Активовано бустер",
        soloBody: "{player} активував {booster}",
        compact: "{booster} · {player}",
        aggregateTitle: "Активні бустери",
      },
      DUNGEON: {
        emoji: "◇",
        soloTitle: "Підземелля",
        soloBody: "Доступне підземелля {dungeon}",
        compact: "Підземелля · {dungeon}",
        aggregateTitle: "Підземелля",
      },
      RAID: {
        emoji: "△",
        soloTitle: "Рейд",
        soloBody: "Локації: {locations}",
        compact: "Рейд · {locations}",
        aggregateTitle: "Рейди",
      },
      GLOBAL: {
        emoji: "●",
        soloTitle: "Глобальна подія",
        soloBody: "Почалась подія {event}",
        compact: "{event}",
        aggregateTitle: "Глобальні події",
      },
    },
  },
  "p-2": {
    kind: "NICK_STYLE",
    colors: [
      { color: "#F1EEE6", position: 0 },
      { color: "#D6FF43", position: 100 },
    ],
    angleDegrees: 110,
    glow: {
      color: "#D6FF43",
      strength: 24,
    },
  },
  "p-3": {
    kind: "PROFILE_THEME",
    background: "#090907",
    surface: "#211F1B",
    accent: "#D6FF43",
    text: "#F4F0E8",
    border: "#4C493E",
    glow: {
      color: "#D6FF43",
      strength: 18,
    },
  },
  "p-4": {
    kind: "AVATAR_FRAME",
    borderColor: "#E1A95E",
    borderWidthPx: 3,
    radiusPx: 14,
    glow: {
      color: "#E1A95E",
      strength: 28,
    },
  },
  "p-5": {
    kind: "PROFILE_EFFECT",
    effect: "SHIMMER",
    intensity: 42,
    primaryColor: "#D6FF43",
    secondaryColor: "#C88BC5",
  },
  "review-1": {
    kind: "NICK_STYLE",
    colors: [
      { color: "#DDE8F2", position: 0 },
      { color: "#8BB4D5", position: 48 },
      { color: "#F0F5F8", position: 100 },
    ],
    angleDegrees: 118,
    glow: {
      color: "#79A9D0",
      strength: 18,
    },
  },
  "review-2": {
    kind: "PROFILE_EFFECT",
    effect: "PULSE",
    intensity: 56,
    primaryColor: "#C27FC7",
    secondaryColor: "#D6FF43",
  },
}
