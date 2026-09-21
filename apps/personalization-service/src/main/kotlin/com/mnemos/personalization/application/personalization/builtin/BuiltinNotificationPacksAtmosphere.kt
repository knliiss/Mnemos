package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.condition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.definition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.template
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.variant
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

object BuiltinNotificationPacksAtmosphere {
    val definitions: List<BuiltinPersonalizationDefinition> =
        listOf(
            anomaly(),
            frontier(),
            suspiciousLuck(),
        )

    private fun anomaly(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.anomaly",
            name = "Аномалия",
            pricePrime = 35,
            mixedAggregateTitle = "Система фиксирует сразу несколько аномалий",
            itemDrop =
                template(
                    emoji = "🧩",
                    title = "Инвентарь ведёт себя странно",
                    body = "У {player} появился «{item}». Источник происхождения не подтверждён.",
                    compact = "Аномалия → «{item}» у {player}",
                    aggregateTitle = "Аномалии инвентаря",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "⚠️",
                                title = "Это вообще должно было выпасть?",
                                body = "{player} получил «{item}». Секретный дроп прошёл проверку реальности не полностью.",
                                compact = "Невозможный дроп → «{item}»",
                                aggregateTitle = "Подозрительные находки",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "⚡",
                    title = "Параметры изменены",
                    body = "{player} активировал эффект {booster.genitive}. Значения вышли из штатного диапазона.",
                    compact = "Параметр изменён: {booster}",
                    aggregateTitle = "Изменения параметров",
                    variants =
                        listOf(
                            variant(
                                id = "luck",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "LUCK")),
                                emoji = "🍀",
                                title = "Вероятности вышли из нормы",
                                body = "{player} активировал Удачу. Система больше не считает распределение нейтральным.",
                                compact = "Вероятности нестабильны · {player}",
                                aggregateTitle = "Аномалии вероятности",
                            ),
                            variant(
                                id = "damage",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "DAMAGE")),
                                emoji = "💢",
                                title = "Лимиты перестали выглядеть обязательными",
                                body = "{player} активировал Урон. Кто-то снова трогал коэффициенты.",
                                compact = "Урон вышел за норму",
                                aggregateTitle = "Боевые аномалии",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "🚪",
                    title = "Проход появился",
                    body = "{dungeon} открылось. Ещё минуту назад его здесь будто не было.",
                    compact = "Появился проход: {dungeon}",
                    aggregateTitle = "Новые проходы",
                    variants =
                        listOf(
                            variant(
                                id = "expert",
                                conditions = listOf(condition(NotificationPackVariantField.DUNGEON, "EXPERT")),
                                emoji = "🕳",
                                title = "Найдена нестабильная зона",
                                body = "Экспертное подземелье открыто. Вход существует. Пока что.",
                                compact = "Нестабильная зона → Expert",
                                aggregateTitle = "Нестабильные зоны",
                            ),
                        ),
                ),
            raid =
                template(
                    emoji = "📡",
                    title = "Обнаружено движение",
                    body = "Аномальная активность на локациях: {locations}. Классификация: рейд.",
                    compact = "Активность → {locations}",
                    aggregateTitle = "Аномальная активность",
                ),
            global =
                template(
                    emoji = "⚠️",
                    title = "Состояние мира нестабильно",
                    body = "Зафиксировано событие {event}.",
                    compact = "WORLD_STATE → {event}",
                    aggregateTitle = "Системные аномалии",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🌑",
                                title = "Освещение перестало отвечать",
                                body = "Тьма накрыла мир. Рендер света временно недоступен.",
                                compact = "LIGHTING_ERROR",
                                aggregateTitle = "Ошибки освещения",
                            ),
                            variant(
                                id = "eclipse",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "ECLIPSE")),
                                emoji = "🌒",
                                title = "Источник света частично недоступен",
                                body = "Началось затмение. Небо работает в ограниченном режиме.",
                                compact = "SUN_OUTPUT_LIMITED",
                                aggregateTitle = "Ограничения источника света",
                            ),
                            variant(
                                id = "blood-moon",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "MOON")),
                                emoji = "🩸",
                                title = "Небо сменило палитру",
                                body = "Взошла кровавая луна. Причина изменения цвета не указана.",
                                compact = "SKY_PALETTE_CHANGED",
                                aggregateTitle = "Изменения палитры неба",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "Физика снова напомнила о себе",
                                body = "Произошёл взрыв. Стабильность мира слегка переоценили.",
                                compact = "WORLD_STABILITY_WARNING",
                                aggregateTitle = "Предупреждения стабильности",
                            ),
                        ),
                ),
        )

    private fun frontier(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.frontier",
            name = "Фронтир",
            pricePrime = 35,
            mixedAggregateTitle = "На фронтире произошло сразу несколько событий",
            itemDrop =
                template(
                    emoji = "🗡",
                    title = "Трофей найден",
                    body = "{player} вышел из боя с новым трофеем — «{item}».",
                    compact = "{player} забрал «{item}»",
                    aggregateTitle = "Собранные трофеи",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "✦",
                                title = "Редкий трофей",
                                body = "{player} получил «{item}». Такие находки редко остаются без внимания.",
                                compact = "Редкий трофей → «{item}»",
                                aggregateTitle = "Редкие трофеи",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "✦",
                    title = "Усиление группы",
                    body = "{player} активировал эффект {booster.genitive}. Окно для фарма открыто.",
                    compact = "Усиление {booster} · {player}",
                    aggregateTitle = "Активные усиления",
                    variants =
                        listOf(
                            variant(
                                id = "power",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "POWER")),
                                emoji = "🔥",
                                title = "Сила на пределе",
                                body = "{player} активировал Силу. Пора использовать окно по максимуму.",
                                compact = "Сила активна · {player}",
                                aggregateTitle = "Пиковые усиления",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "🏰",
                    title = "Врата открыты",
                    body = "{dungeon} ждёт новую группу.",
                    compact = "Врата открыты: {dungeon}",
                    aggregateTitle = "Открытые врата",
                    variants =
                        listOf(
                            variant(
                                id = "expert",
                                conditions = listOf(condition(NotificationPackVariantField.DUNGEON, "EXPERT")),
                                emoji = "🛡",
                                title = "Открыт путь для сильных",
                                body = "Экспертное подземелье доступно. Ошибки здесь стоят дороже обычного.",
                                compact = "Открыт Expert",
                                aggregateTitle = "Сложные маршруты",
                            ),
                        ),
                ),
            raid =
                template(
                    emoji = "⚔️",
                    title = "Сбор на рейд",
                    body = "Цели обнаружены. Локации: {locations}.",
                    compact = "Цели → {locations}",
                    aggregateTitle = "Рейдовые цели",
                ),
            global =
                template(
                    emoji = "◈",
                    title = "Фронтир изменился",
                    body = "Началось событие {event}.",
                    compact = "Фронтир: {event}",
                    aggregateTitle = "События фронтира",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🌑",
                                title = "Мир погрузился во тьму",
                                body = "Тьма наступила. Дальше придётся идти почти вслепую.",
                                compact = "Мир во тьме",
                                aggregateTitle = "Тьма на фронтире",
                            ),
                            variant(
                                id = "eclipse",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "ECLIPSE")),
                                emoji = "🌒",
                                title = "Свет меркнет",
                                body = "Началось затмение. Небо закрывается над фронтиром.",
                                compact = "Началось затмение",
                                aggregateTitle = "Затмения на фронтире",
                            ),
                            variant(
                                id = "blood-moon",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "MOON")),
                                emoji = "🩸",
                                title = "Кровавая луна взошла",
                                body = "Небо стало красным. Хороший знак это или нет — выяснится позже.",
                                compact = "Кровавая луна",
                                aggregateTitle = "Красное небо",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "Земля содрогнулась",
                                body = "Произошёл взрыв. На фронтире стало на одну проблему больше.",
                                compact = "Произошёл взрыв",
                                aggregateTitle = "Взрывы на фронтире",
                            ),
                        ),
                ),
        )

    private fun suspiciousLuck(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.suspicious-luck",
            name = "Удача",
            pricePrime = 30,
            mixedAggregateTitle = "Сегодня удача явно решила играть за одну сторону",
            itemDrop =
                template(
                    emoji = "🍀",
                    title = "Критическая удача",
                    body = "{player} выбил «{item}». Твоя удача сегодня уже выглядит как отдельная механика.",
                    compact = "Удача → {player} → «{item}»",
                    aggregateTitle = "Критически удачные находки",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🧐",
                                title = "Так, таблицу шансов сюда",
                                body = "{player} выбил секретный «{item}». Кажется, шанс выпадения просто забыл быть маленьким.",
                                compact = "Слишком везучий {player} → «{item}»",
                                aggregateTitle = "Удача вышла из-под контроля",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🎰",
                    title = "Удачу решили подкрутить ещё сильнее",
                    body = "{player} активировал эффект {booster.genitive}. Видимо, естественного везения уже мало.",
                    compact = "{player} добавил себе {booster}",
                    aggregateTitle = "Подкрученные шансы",
                    variants =
                        listOf(
                            variant(
                                id = "luck",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "LUCK")),
                                emoji = "🍀",
                                title = "Читы на удачу включены",
                                body = "{player} активировал Удачу. Теперь можно официально делать вид, что RNG честный.",
                                compact = "Ещё и Удача · {player}",
                                aggregateTitle = "Удачи стало неприлично много",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "⏱",
                    title = "И тайминг пойман",
                    body = "{dungeon} открылось ровно тогда, когда надо.",
                    compact = "Тайминг снова в пользу игрока",
                    aggregateTitle = "Слишком удобные тайминги",
                ),
            raid =
                template(
                    emoji = "🎯",
                    title = "Даже рейд пришёл куда надо",
                    body = "Активность началась на локациях: {locations}.",
                    compact = "Удачный рейд → {locations}",
                    aggregateTitle = "Подозрительно удобные рейды",
                ),
            global =
                template(
                    emoji = "🍀",
                    title = "Мир опять подыграл",
                    body = "Началось событие {event}. Всё складывается слишком удобно.",
                    compact = "Мир подыграл: {event}",
                    aggregateTitle = "Слишком удобные мировые события",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🌑",
                                title = "Даже свет решил уступить",
                                body = "Тьма наступила. Всё идёт слишком удобно.",
                                compact = "Тьма тоже в команде",
                                aggregateTitle = "Удобная Тьма",
                            ),
                            variant(
                                id = "eclipse",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "ECLIPSE")),
                                emoji = "🌒",
                                title = "Солнце тоже отошло",
                                body = "Началось затмение. Мир явно кому-то освобождает место.",
                                compact = "Даже солнце отошло",
                                aggregateTitle = "Слишком удобные затмения",
                            ),
                            variant(
                                id = "blood-moon",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "MOON")),
                                emoji = "🩸",
                                title = "Луна тоже сегодня особенная",
                                body = "Взошла кровавая луна. Обычной, видимо, было недостаточно.",
                                compact = "Даже луна необычная",
                                aggregateTitle = "Необычно удобное небо",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "Вот это уже перебор",
                                body = "Произошёл взрыв. Совпадений становится многовато.",
                                compact = "Совпадение стало громким",
                                aggregateTitle = "Слишком много совпадений",
                            ),
                        ),
                ),
            playerOffline =
                template(
                    emoji = "🍀",
                    title = "Удача вышла вместе с игроком",
                    body = "{player} вышел из игры. Остальным наконец-то вернули нормальные шансы.",
                    compact = "{player} унёс удачу с собой",
                    aggregateTitle = "Удача покинула сервер",
                ),
        )
}
