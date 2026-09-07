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
    economyNote: "Calculated from this regular case price.",
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
    itemStats: "Stats",
    spawnType: "Spawn",
    health: "Health",
    moneyDrop: "Money drop",
    killTime: "Kill time",
    kills: "Kills",
    itemStat: {
      POWER: "Power",
      SELL_PRICE: "Sell price",
      LEVEL: "Level",
      SATIETY: "Satiety",
      SATIETY_LEVEL: "Satiety level",
      PET_POWER: "Pet power",
      DAMAGE_BOOST: "Damage boost",
      POWER_BOOST: "Power boost",
      MONEY_BOOST: "Money boost",
      CRIT_CHANCE: "Critical chance",
      MULTICRIT: "Multicrit",
    },
    mobSpawn: {
      ALWAYS: "Always",
      DARKNESS: "Darkness",
      BLOOD_MOON: "Blood Moon",
    },
    specialZone: "Special zone",
    locationNumber: (id: number) => `Location #${id}`,
    caseIndex: (index: number) => `Case ${index}`,
    count: (value: number) => String(value),
    formatKillTime: (value: string) => formatGuideKillTime(value, "en"),
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
    economyNote: "Расчет от цены этого обычного кейса.",
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
    itemStats: "Характеристики",
    spawnType: "Спавн",
    health: "Здоровье",
    moneyDrop: "Дроп денег",
    killTime: "Время убийства",
    kills: "Убийств",
    itemStat: {
      POWER: "Сила",
      SELL_PRICE: "Цена продажи",
      LEVEL: "Уровень",
      SATIETY: "Сытость",
      SATIETY_LEVEL: "Уровень сытости",
      PET_POWER: "Мощь",
      DAMAGE_BOOST: "Бустер урона",
      POWER_BOOST: "Бустер силы",
      MONEY_BOOST: "Бустер денег",
      CRIT_CHANCE: "Шанс крита",
      MULTICRIT: "Мультикрит",
    },
    mobSpawn: {
      ALWAYS: "Всегда",
      DARKNESS: "Тьма",
      BLOOD_MOON: "Кровавая луна",
    },
    specialZone: "Особая зона",
    locationNumber: (id: number) => `Локация #${id}`,
    caseIndex: (index: number) => `Кейс ${index}`,
    count: (value: number) => String(value),
    formatKillTime: (value: string) => formatGuideKillTime(value, "ru"),
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
    economyNote: "Розрахунок від ціни цього звичайного кейса.",
    rebirth: "РБ на цій локації",
    requirement: "Потрібно сили",
    growth: "Приріст",
    boosts: "Бусти",
    noBoosts: "Без постійного бусту",
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
    itemStats: "Характеристики",
    spawnType: "Спавн",
    health: "Здоров'я",
    moneyDrop: "Дроп грошей",
    killTime: "Час убивства",
    kills: "Убивств",
    itemStat: {
      POWER: "Сила",
      SELL_PRICE: "Ціна продажу",
      LEVEL: "Рівень",
      SATIETY: "Ситість",
      SATIETY_LEVEL: "Рівень ситості",
      PET_POWER: "Міць",
      DAMAGE_BOOST: "Бустер шкоди",
      POWER_BOOST: "Бустер сили",
      MONEY_BOOST: "Бустер грошей",
      CRIT_CHANCE: "Шанс криту",
      MULTICRIT: "Мультикрит",
    },
    mobSpawn: {
      ALWAYS: "Завжди",
      DARKNESS: "Темрява",
      BLOOD_MOON: "Кривавий місяць",
    },
    specialZone: "Особлива зона",
    locationNumber: (id: number) => `Локація #${id}`,
    caseIndex: (index: number) => `Кейс ${index}`,
    count: (value: number) => String(value),
    formatKillTime: (value: string) => formatGuideKillTime(value, "uk"),
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

function formatGuideKillTime(value: string, language: GuideLocationUiLanguage) {
  if (language === "ru") {
    return value
  }

  const units =
    language === "uk"
      ? { day: "д", hour: "год", minute: "хв", second: "с" }
      : { day: "d", hour: "h", minute: "m", second: "s" }

  return value
    .replace(/(\d+)д/g, `$1${units.day}`)
    .replace(/(\d+)ч/g, `$1${units.hour}`)
    .replace(/(\d+)м/g, `$1${units.minute}`)
    .replace(/(\d+)с/g, `$1${units.second}`)
}
