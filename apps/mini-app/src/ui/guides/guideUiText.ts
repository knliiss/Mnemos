type GuideUiLanguage = "en" | "ru" | "uk"

const GUIDE_UI_TEXT = {
  en: {
    title: "SAO Guides",
    back: "Back",
    clearSearch: "Clear search",
    eyebrow: "CARTOGRAPHER ARCHIVE",
    heroTitle: "Locations, NPCs and creatures",
    heroCopy:
      "The archive follows SAO localization and searches names in every language available to the client.",
    searchPlaceholder: "Location, NPC or creature",
    locations: "Locations",
    location: "Location",
    special: "Special",
    open: "Open location",
    noResults: "Nothing found",
    noResultsCopy: "Try a location, NPC, creature name or location number.",
    npc: "NPCs",
    mobs: "Creatures",
    cometMobs: "Comet creatures",
    noStaticPopulation:
      "This special area has no location-specific creature list in SAO localization.",
    creaturesCount: "{{count}} creatures",
    npcCount: "{{count}} NPCs",
    liveLanguages: "Multilingual SAO index",
  },
  ru: {
    title: "Гайды SAO",
    back: "Назад",
    clearSearch: "Очистить поиск",
    eyebrow: "АРХИВ КАРТОГРАФА",
    heroTitle: "Локации, NPC и существа",
    heroCopy:
      "Архив использует локализацию SAO и ищет названия на всех языках, доступных клиенту.",
    searchPlaceholder: "Локация, NPC или существо",
    locations: "Локации",
    location: "Локация",
    special: "Особая",
    open: "Открыть локацию",
    noResults: "Ничего не найдено",
    noResultsCopy: "Попробуйте название локации, NPC, существа или номер локации.",
    npc: "NPC",
    mobs: "Существа",
    cometMobs: "Кометные существа",
    noStaticPopulation:
      "Для этой особой зоны в локализации SAO нет отдельного списка существ.",
    creaturesCount: "Существ: {{count}}",
    npcCount: "NPC: {{count}}",
    liveLanguages: "Мультиязычный индекс SAO",
  },
  uk: {
    title: "Гайди SAO",
    back: "Назад",
    clearSearch: "Очистити пошук",
    eyebrow: "АРХІВ КАРТОГРАФА",
    heroTitle: "Локації, NPC та істоти",
    heroCopy:
      "Архів використовує локалізацію SAO та шукає назви всіма мовами, доступними клієнту.",
    searchPlaceholder: "Локація, NPC або істота",
    locations: "Локації",
    location: "Локація",
    special: "Особлива",
    open: "Відкрити локацію",
    noResults: "Нічого не знайдено",
    noResultsCopy: "Спробуйте назву локації, NPC, істоти або номер локації.",
    npc: "NPC",
    mobs: "Істоти",
    cometMobs: "Кометні істоти",
    noStaticPopulation:
      "Для цієї особливої зони в локалізації SAO немає окремого списку істот.",
    creaturesCount: "Істот: {{count}}",
    npcCount: "NPC: {{count}}",
    liveLanguages: "Багатомовний індекс SAO",
  },
} as const

export function guideUiText(language: string | undefined) {
  const resolvedLanguage: GuideUiLanguage = language?.toLowerCase().startsWith("uk")
    ? "uk"
    : language?.toLowerCase().startsWith("ru")
      ? "ru"
      : "en"

  const text = GUIDE_UI_TEXT[resolvedLanguage]

  return {
    ...text,
    creaturesCount: (count: number) => interpolate(text.creaturesCount, count),
    npcCount: (count: number) => interpolate(text.npcCount, count),
  }
}

function interpolate(template: string, count: number) {
  return template.replace("{{count}}", String(count))
}
