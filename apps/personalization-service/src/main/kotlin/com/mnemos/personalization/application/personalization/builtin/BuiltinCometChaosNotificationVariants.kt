package com.mnemos.personalization.application.personalization.builtin

object BuiltinCometChaosNotificationVariants {
    fun forPack(catalogKey: String): Copy =
        requireNotNull(copies[catalogKey]) {
            "Built-in notification pack $catalogKey must define Comet Chaos copy"
        }

    data class Copy(
        val emoji: String,
        val title: String,
        val body: String,
        val compact: String,
        val aggregateTitle: String,
    )

    private val copies: Map<String, Copy> =
        mapOf(
            "notification.mnemos-archive" to
                copy(
                    title = "Кометный хаос",
                    body = "Комета расчертила небо. Хроника открыла новую запись: начался Кометный хаос.",
                    compact = "Кометный хаос занесён в хронику",
                    aggregateTitle = "Записи о Кометном хаосе",
                ),
            "notification.protocol" to
                copy(
                    title = "WORLD // COMET CHAOS",
                    body = "COMET TRAJECTORY DETECTED // CHAOS STATE ACTIVE",
                    compact = "COMET CHAOS // ACTIVE",
                    aggregateTitle = "COMET CHAOS EVENTS",
                ),
            "notification.rng" to
                copy(
                    title = "Небо выбило комету",
                    body = "RNG прокнул Кометный хаос. Даже мир решил сыграть по шансам.",
                    compact = "WORLD PROC → Кометный хаос",
                    aggregateTitle = "Кометные броски мира",
                ),
            "notification.night-shift" to
                copy(
                    emoji = "🌌",
                    title = "Небо тоже не спит",
                    body = "Комета пронеслась над миром. Ночная смена получила ещё одну причину не ложиться.",
                    compact = "Кометный хаос вместо сна",
                    aggregateTitle = "Кометная смена",
                ),
            "notification.dep" to
                copy(
                    emoji = "📉🔥",
                    title = "Комета пробила сопротивление",
                    body = "Начался Кометный хаос. Волатильность теперь буквально падает с неба.",
                    compact = "Кометная волатильность",
                    aggregateTitle = "Кометные движения",
                ),
            "notification.deadpan" to
                copy(
                    title = "Пролетела комета",
                    body = "Начался Кометный хаос. Это тоже глобальное событие.",
                    compact = "Кометный хаос начался",
                    aggregateTitle = "Кометные события",
                ),
            "notification.anomaly" to
                copy(
                    title = "Обнаружен объект вне нормы",
                    body = "Комета пересекла небо. Система классифицировала состояние как Кометный хаос.",
                    compact = "COMET_CHAOS_DETECTED",
                    aggregateTitle = "Кометные аномалии",
                ),
            "notification.frontier" to
                copy(
                    title = "Комета над фронтиром",
                    body = "Над фронтиром прошла комета. Начался Кометный хаос.",
                    compact = "Кометный хаос на фронтире",
                    aggregateTitle = "Кометные события фронтира",
                ),
            "notification.suspicious-luck" to
                copy(
                    emoji = "🪐",
                    title = "Даже комета вовремя",
                    body = "Начался Кометный хаос. Конечно, именно сейчас. Совпадение выглядит всё подозрительнее.",
                    compact = "Комета тоже прокнула",
                    aggregateTitle = "Подозрительно своевременные кометы",
                ),
            "notification.operator" to
                copy(
                    title = "Кометный сигнал принят",
                    body = "Оператор зафиксировал комету: начался Кометный хаос.",
                    compact = "Кометный хаос · принято",
                    aggregateTitle = "Кометные сигналы",
                ),
            "notification.clean-log" to
                copy(
                    title = "GLOBAL // COMET_CHAOS",
                    body = "EVENT=COMET_CHAOS // STATUS=ACTIVE",
                    compact = "COMET_CHAOS // ACTIVE",
                    aggregateTitle = "COMET_CHAOS EVENTS",
                ),
            "notification.hunter" to
                copy(
                    emoji = "🦅",
                    title = "След в небе",
                    body = "Комета прошла над миром. Охота входит в Кометный хаос.",
                    compact = "След кометы найден",
                    aggregateTitle = "Кометные следы",
                ),
            "notification.calm" to
                copy(
                    title = "Пролетела комета",
                    body = "Начался Кометный хаос. Без паники, уведомление уже пришло.",
                    compact = "Кометный хаос начался",
                    aggregateTitle = "Кометные события",
                ),
            "notification.ultra" to
                copy(
                    title = "КОМЕТНЫЙ ХАОС!",
                    body = "КОМЕТА В НЕБЕ — КОМЕТНЫЙ ХАОС АКТИВЕН!",
                    compact = "COMET CHAOS!",
                    aggregateTitle = "КОМЕТНЫЙ ХАОС",
                ),
            "notification.archivist" to
                copy(
                    title = "Комета внесена в реестр",
                    body = "Хроника отметила пролёт кометы. Начался Кометный хаос.",
                    compact = "Запись: Кометный хаос",
                    aggregateTitle = "Кометные записи",
                ),
            "notification.karma" to
                copy(
                    title = "Баланс сорвался с орбиты",
                    body = "Комета прошла по небу. Карма объявляет Кометный хаос.",
                    compact = "Карма → Кометный хаос",
                    aggregateTitle = "Кометные сдвиги баланса",
                ),
            "notification.critical" to
                copy(
                    emoji = "☄️",
                    title = "Критическое небесное событие",
                    body = "Комета пересекла небо. Критический статус: Кометный хаос.",
                    compact = "CRIT → Кометный хаос",
                    aggregateTitle = "Критические кометные события",
                ),
            "notification.night-city" to
                copy(
                    emoji = "🛰️",
                    title = "Комета над ночным городом",
                    body = "Неон встретился с кометным следом. Начался Кометный хаос.",
                    compact = "Комета над городом",
                    aggregateTitle = "Кометное небо",
                ),
            "notification.signal" to
                copy(
                    title = "Кометный сигнал",
                    body = "Получен небесный сигнал: начался Кометный хаос.",
                    compact = "SIGNAL → COMET_CHAOS",
                    aggregateTitle = "Кометные сигналы",
                ),
            "notification.guild" to
                copy(
                    emoji = "🔭",
                    title = "Вестник увидел комету",
                    body = "Над миром прошла комета. Гильдия встречает Кометный хаос.",
                    compact = "Гильдия → Кометный хаос",
                    aggregateTitle = "Кометные вести гильдии",
                ),
            "notification.dzhamshut" to
                copy(
                    emoji = "🏚️",
                    title = "Объекту пиздец",
                    body = "Начался Кометный хаос. Теперь даже небо участвует в стройке без проекта.",
                    compact = "Кометы над объектом",
                    aggregateTitle = "Кометные происшествия на объекте",
                ),
            "notification.shbshnik" to
                copy(
                    emoji = "🫣",
                    title = "Теперь прятаться поздно",
                    body = "Начался Кометный хаос. Под столом от этого уже не спастись.",
                    compact = "Кометы нашли ШБ",
                    aggregateTitle = "Кометные проблемы ШБ",
                ),
            "notification.kaif" to
                copy(
                    emoji = "🌠",
                    title = "Кайф из космоса",
                    body = "Начался Кометный хаос. Даже небо решило добавить эффектов.",
                    compact = "Космический кайф",
                    aggregateTitle = "Космический кайф",
                ),
            "notification.gospodin" to
                copy(
                    emoji = "✨",
                    title = "Небеса приветствуют господина",
                    body = "Начался Кометный хаос. Небо подготовило для господина отдельное представление.",
                    compact = "Небесное представление",
                    aggregateTitle = "Небесные знамения",
                ),
            "notification.lavash" to
                copy(
                    emoji = "🛸",
                    title = "Доставка прямо с неба",
                    body = "Начался Кометный хаос. Похоже, курьер выбрал очень странный маршрут.",
                    compact = "Космическая доставка",
                    aggregateTitle = "Доставки с неба",
                ),
            "notification.zaebalo" to
                copy(
                    emoji = "🤯",
                    title = "А КОМЕТЫ-ТО НАХУЯ",
                    body = "Начался Кометный хаос. Потому что обычных проблем, видимо, было мало.",
                    compact = "Ещё и кометы",
                    aggregateTitle = "Кометный пиздец",
                ),
            "notification.casino" to
                copy(
                    emoji = "🪐",
                    title = "Космический джекпот",
                    body = "Начался Кометный хаос. Колесо мира докрутилось до космического сектора.",
                    compact = "COSMIC JACKPOT",
                    aggregateTitle = "Космические джекпоты",
                ),
            "notification.support" to
                copy(
                    emoji = "🛰️",
                    title = "INCIDENT CASCADE",
                    body = "Начался Кометный хаос. Инциденты падают с неба, созвон уже создан.",
                    compact = "INCIDENT CASCADE",
                    aggregateTitle = "Каскадные инциденты",
                ),
            "notification.gossip" to
                copy(
                    emoji = "📡",
                    title = "ВСЕ УЖЕ ОБ ЭТОМ ГОВОРЯТ",
                    body = "Начался Кометный хаос. Версий произошедшего уже больше, чем комет.",
                    compact = "Главная сплетня дня",
                    aggregateTitle = "Кометные слухи",
                ),
            "notification.commentator" to
                copy(
                    emoji = "🛰️",
                    title = "НЕБО ВСТУПАЕТ В ИГРУ!",
                    body = "Начался Кометный хаос! Даже космос подключился к сегодняшнему матчу!",
                    compact = "КОСМОС В ИГРЕ",
                    aggregateTitle = "Космические моменты эфира",
                ),
            "notification.deadline" to
                copy(
                    emoji = "📥",
                    title = "Задачи посыпались с неба",
                    body = "Начался Кометный хаос. Бэклог теперь буквально падает сверху.",
                    compact = "BACKLOG FROM SKY",
                    aggregateTitle = "Космический бэклог",
                ),
        )

    private fun copy(
        emoji: String = "☄️",
        title: String,
        body: String,
        compact: String,
        aggregateTitle: String,
    ): Copy =
        Copy(
            emoji = emoji,
            title = title,
            body = body,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )
}
