type GuideLocationUiLanguage = "en" | "ru" | "uk"

const TEXT = {
  en: {
    locationArchive: "Locations",
    locationArchiveCopy:
      "Current names and descriptions with source-backed cases, mobs, drops and rebirth progression.",
    searchPlaceholder: "Location, mob, case or drop",
    purchasePrice: "Unlock price",
    cases: "Cases",
    mobs: "Mobs",
    drops: "Drops",
    normalCase: "Regular case",
    donateCase: "Donate case",
    specialCase: "Special case",
    casePrice: "Case price",
    fifteenMinutesAndDarkness: "15 min / Darkness",
    explosion: "Explosion",
    oneHour: "1 hour",
    eightHours: "8 hours",
    twelveHours: "12 hours",
    economy: "Opening cost",
    economyNote:
      "Calculated from this regular case price. Drop chances are account-specific and are not shown.",
    rebirth: "Rebirths on this location",
    requirement: "Power required",
    growth: "Growth",
    boosts: "Boosts",
    noBoosts: "No permanent boost",
    npcs: "NPCs",
    raidNpc: "Raid portal",
    questNpc: "Quest giver",
    detailsUnavailable: "Detailed data is not available",
    detailsUnavailableCopy:
      "The dump was recorded while this location was locked. The current localized name, description and unlock price are shown, but mobs, cases and drops are intentionally not guessed.",
    sourceUnavailable: "No verified details in the source dump",
    openMob: "Show drops",
    closeMob: "Hide drops",
    itemDetails: "Drop details",
    sourceCase: "Drops from case",
    sourceMob: "Drops from mob",
    rarity: "Rarity",
    quantity: "Quantity",
    noChance: "Drop chance is not shown because it depends on the account.",
    specialZone: "Special zone",
    locationNumber: (id: number) => `Location #${id}`,
    caseIndex: (index: number) => `Case ${index}`,
    count: (value: number) => String(value),
  },
  ru: {
    locationArchive: "Локации",
    locationArchiveCopy:
      "Актуальные названия и описания, кейсы, мобы, дропы и прогрессия РБ из подтвержденных источников.",
    searchPlaceholder: "Локация, моб, кейс или дроп",
    purchasePrice: "Цена открытия",
    cases: "Кейсы",
    mobs: "Мобы",
    drops: "Дропы",
    normalCase: "Обычный кейс",
    donateCase: "Донатный кейс",
    specialCase: "Особый кейс",
    casePrice: "Цена кейса",
    fifteenMinutesAndDarkness: "15 минут / Тьма",
    explosion: "Взрыв",
    oneHour: "1 час",
    eightHours: "8 часов",
    twelveHours: "12 часов",
    economy: "Стоимость открытия",
    economyNote:
      "Расчет от цены этого обычного кейса. Шансы дропа индивидуальны для аккаунта и не показываются.",
    rebirth: "РБ на этой локации",
    requirement: "Нужно силы",
    growth: "Прирост",
    boosts: "Бусты",
    noBoosts: "Без постоянного буста",
    npcs: "NPC",
    raidNpc: "Портал в рейд",
    questNpc: "Квестовый NPC",
    detailsUnavailable: "Подробные данные пока недоступны",
    detailsUnavailableCopy:
      "Дамп был снят, когда эта локация была заблокирована. Показываем актуальное локализованное название, описание и цену открытия, но не выдумываем мобов, кейсы и дропы.",
    sourceUnavailable: "В исходном дампе нет подтвержденных деталей",
    openMob: "Показать дропы",
    closeMob: "Скрыть дропы",
    itemDetails: "Информация о дропе",
    sourceCase: "Выпадает из кейса",
    sourceMob: "Выпадает из моба",
    rarity: "Редкость",
    quantity: "Количество",
    noChance: "Шанс дропа не показывается, потому что он зависит от аккаунта.",
    specialZone: "Особая зона",
    locationNumber: (id: number) => `Локация #${id}`,
    caseIndex: (index: number) => `Кейс ${index}`,
    count: (value: number) => String(value),
  },
  uk: {
    locationArchive: "Локації",
    locationArchiveCopy:
      "Актуальні назви й описи, кейси, моби, дропи та прогресія РБ із підтверджених джерел.",
    searchPlaceholder: "Локація, моб, кейс або дроп",
    purchasePrice: "Ціна відкриття",
    cases: "Кейси",
    mobs: "Моби",
    drops: "Дропи",
    normalCase: "Звичайний кейс",
    donateCase: "Донатний кейс",
    specialCase: "Особливий кейс",
    casePrice: "Ціна кейса",
    fifteenMinutesAndDarkness: "15 хвилин / Темрява",
    explosion: "Вибух",
    oneHour: "1 година",
    eightHours: "8 годин",
    twelveHours: "12 годин",
    economy: "Вартість відкриття",
    economyNote:
      "Розрахунок від ціни цього звичайного кейса. Шанси дропу індивідуальні для акаунта й не показуються.",
    rebirth: "РБ на цій локації",
    requirement: "Потрібно сили",
    growth: "Приріст",
    boosts: "Бусти",
    noBoosts: "Без постійного буста",
    npcs: "NPC",
    raidNpc: "Портал у рейд",
    questNpc: "Квестовий NPC",
    detailsUnavailable: "Детальні дані поки недоступні",
    detailsUnavailableCopy:
      "Дамп було знято, коли ця локація була заблокована. Показуємо актуальну локалізовану назву, опис і ціну відкриття, але не вигадуємо мобів, кейси та дропи.",
    sourceUnavailable: "У вихідному дампі немає підтверджених деталей",
    openMob: "Показати дропи",
    closeMob: "Сховати дропи",
    itemDetails: "Інформація про дроп",
    sourceCase: "Випадає з кейса",
    sourceMob: "Випадає з моба",
    rarity: "Рідкість",
    quantity: "Кількість",
    noChance: "Шанс дропу не показується, тому що він залежить від акаунта.",
    specialZone: "Особлива зона",
    locationNumber: (id: number) => `Локація #${id}`,
    caseIndex: (index: number) => `Кейс ${index}`,
    count: (value: number) => String(value),
  },
} as const

export function guideLocationUiText(language: string | undefined) {
  return TEXT[guideLocationUiLanguage(language)]
}

function guideLocationUiLanguage(language: string | undefined): GuideLocationUiLanguage {
  const normalized = language?.toLowerCase() ?? ""

  if (normalized.startsWith("uk")) {
    return "uk"
  }

  if (normalized.startsWith("ru")) {
    return "ru"
  }

  return "en"
}
