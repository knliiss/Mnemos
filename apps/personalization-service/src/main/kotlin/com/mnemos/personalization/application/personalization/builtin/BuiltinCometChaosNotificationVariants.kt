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
                    title = "Небо тоже не спит",
                    body = "Комета пронеслась над миром. Ночная смена получила ещё одну причину не ложиться.",
                    compact = "Кометный хаос вместо сна",
                    aggregateTitle = "Кометная смена",
                ),
            "notification.dep" to
                copy(
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
                    title = "Критическое небесное событие",
                    body = "Комета пересекла небо. Критический статус: Кометный хаос.",
                    compact = "CRIT → Кометный хаос",
                    aggregateTitle = "Критические кометные события",
                ),
            "notification.night-city" to
                copy(
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
                    title = "Вестник увидел комету",
                    body = "Над миром прошла комета. Гильдия встречает Кометный хаос.",
                    compact = "Гильдия → Кометный хаос",
                    aggregateTitle = "Кометные вести гильдии",
                ),
        )

    private fun copy(
        title: String,
        body: String,
        compact: String,
        aggregateTitle: String,
    ): Copy =
        Copy(
            emoji = "☄️",
            title = title,
            body = body,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )
}
