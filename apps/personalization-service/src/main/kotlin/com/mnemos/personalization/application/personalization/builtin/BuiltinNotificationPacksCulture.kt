package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.condition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.definition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.template
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.variant
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

object BuiltinNotificationPacksCulture {
    val definitions: List<BuiltinPersonalizationDefinition> =
        listOf(
            nightShift(),
            dep(),
            deadpan(),
        )

    private fun nightShift(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.night-shift",
            name = "Сон",
            pricePrime = 25,
            mixedAggregateTitle = "Сон снова откладывается",
            itemDrop =
                template(
                    emoji = "💤",
                    title = "И во сне дропает",
                    body = "{player} выбил «{item}» почти не просыпаясь. Сон фарму не помеха.",
                    compact = "{player} дропнул во сне → «{item}»",
                    aggregateTitle = "Дропы между снами",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "😶",
                                title = "Вот теперь проснулся",
                                body = "{player} выбил секретный «{item}». Такой будильник уже не проигнорируешь.",
                                compact = "Проснулся от секретки → {player}",
                                aggregateTitle = "Причины всё-таки проснуться",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "☕",
                    title = "Сон поставлен на паузу",
                    body = "{player} активировал эффект {booster.genitive}. Можно фармить с закрытыми глазами.",
                    compact = "Сквозь сон · {booster}",
                    aggregateTitle = "Сон снова отложили",
                    variants =
                        listOf(
                            variant(
                                id = "luck",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "LUCK")),
                                emoji = "🍀",
                                title = "Последняя попытка. Честно.",
                                body = "{player} включил Удачу. Теперь точно последний заход. Наверное.",
                                compact = "Удача включена · сон выключен",
                                aggregateTitle = "Последние попытки",
                            ),
                            variant(
                                id = "money",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "MONEY")),
                                emoji = "💸",
                                title = "Сон денег не приносит",
                                body = "{player} активировал Доход. Теперь отмену сна можно хотя бы оправдать экономически.",
                                compact = "Доход включён · сон выключен",
                                aggregateTitle = "Экономика без сна",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "⏰",
                    title = "Будильник сработал",
                    body = "{dungeon} открылось. Да, именно сейчас.",
                    compact = "Просыпаемся: {dungeon}",
                    aggregateTitle = "Ночные подземелья",
                    variants =
                        listOf(
                            variant(
                                id = "expert",
                                conditions = listOf(condition(NotificationPackVariantField.DUNGEON, "EXPERT")),
                                emoji = "🫠",
                                title = "Почему именно сейчас",
                                body = "Экспертное подземелье открыто. Сон был хорошей идеей примерно минуту назад.",
                                compact = "Экспертка. Сейчас.",
                                aggregateTitle = "Сон окончательно отменён",
                            ),
                        ),
                ),
            raid =
                template(
                    emoji = "🌙",
                    title = "Кто не спит — тот в рейде",
                    body = "Рейд начался. Локации: {locations}.",
                    compact = "Ночной рейд → {locations}",
                    aggregateTitle = "Рейды вместо сна",
                ),
            global =
                template(
                    emoji = "🌙",
                    title = "Сон — слабость",
                    body = "Началось событие {event}. Сон подождёт.",
                    compact = "Ночью началось: {event}",
                    aggregateTitle = "События между снами",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🌑",
                                title = "Наконец нормальное освещение",
                                body = "Тьма наступила. Отлично: теперь можно фармить с закрытыми глазами официально.",
                                compact = "Тьма. Всё по плану.",
                                aggregateTitle = "Тьма вместо сна",
                            ),
                            variant(
                                id = "eclipse",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "ECLIPSE")),
                                emoji = "🌒",
                                title = "Свет опять экономят",
                                body = "Началось затмение. Как будто ночью его и так было слишком много.",
                                compact = "Началось затмение",
                                aggregateTitle = "Экономия света",
                            ),
                            variant(
                                id = "blood-moon",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "MOON")),
                                emoji = "🩸",
                                title = "Сну добавили декорации",
                                body = "Взошла кровавая луна. Атмосфера подозрительно уместная.",
                                compact = "Взошла кровавая луна",
                                aggregateTitle = "Ночные декорации",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "Теперь не спят и соседи",
                                body = "Произошёл взрыв. Теперь проснулись вообще все.",
                                compact = "Сон отменён взрывом",
                                aggregateTitle = "Громкие будильники",
                            ),
                        ),
                ),
            playerOffline =
                template(
                    emoji = "😴",
                    title = "Наконец-то спать",
                    body = "{player} вышел из игры. Кажется, сон всё-таки победил.",
                    compact = "{player} ушёл спать",
                    aggregateTitle = "Сон победил",
                ),
        )

    private fun dep(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.dep",
            name = "Деп",
            pricePrime = 40,
            mixedAggregateTitle = "Депы снова начали принимать решения за игроков",
            itemDrop =
                template(
                    emoji = "📈",
                    title = "Последний деп был не зря",
                    body = "{player} выбил «{item}». Последний деп ради этого внезапно оказался не таким уж плохим решением.",
                    compact = "Последний деп → «{item}»",
                    aggregateTitle = "Окупившиеся депы",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "💸",
                                title = "Деп окупился",
                                body = "{player} выбил секретный «{item}». Финансовый отдел просит больше так не делать.",
                                compact = "Деп окупился → «{item}»",
                                aggregateTitle = "Депы, которые каким-то чудом окупились",
                            ),
                            variant(
                                id = "secret-pet",
                                conditions =
                                    listOf(
                                        condition(NotificationPackVariantField.ITEM_RARITY, "SECRET"),
                                        condition(NotificationPackVariantField.ITEM_TYPE, "PET"),
                                    ),
                                emoji = "🐾",
                                title = "Актив вырос сам",
                                body = "У {player} появился секретный питомец «{item}». Портфель доволен.",
                                compact = "Новый актив → «{item}»",
                                aggregateTitle = "Портфель пополнился",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "💹",
                    title = "Позиция усилена",
                    body = "{player} активировал эффект {booster.genitive}. Риск принят.",
                    compact = "{player} усилил позицию · {booster}",
                    aggregateTitle = "Новые позиции",
                    variants =
                        listOf(
                            variant(
                                id = "money",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "MONEY")),
                                emoji = "💰",
                                title = "Ликвидность пришла",
                                body = "{player} активировал Доход. Кэшфлоу снова выглядит прилично.",
                                compact = "Доход активирован",
                                aggregateTitle = "Ликвидность выросла",
                            ),
                            variant(
                                id = "luck",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "LUCK")),
                                emoji = "🎰",
                                title = "Риск-профиль повышен",
                                body = "{player} активировал Удачу. Фундаментальный анализ временно не нужен.",
                                compact = "Удача вместо анализа",
                                aggregateTitle = "Риск-профиль изменился",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "💰",
                    title = "Новая точка входа",
                    body = "{dungeon} открылось. Рынок фарма снова жив.",
                    compact = "Точка входа → {dungeon}",
                    aggregateTitle = "Новые точки входа",
                ),
            raid =
                template(
                    emoji = "📊",
                    title = "Пошёл объём",
                    body = "Рейд стартовал на локациях: {locations}. Кто ждал вход — момент настал.",
                    compact = "Объём → {locations}",
                    aggregateTitle = "Рейдовый объём",
                ),
            global =
                template(
                    emoji = "📊",
                    title = "Рынок мира двинулся",
                    body = "Событие {event} поменяло расклад.",
                    compact = "Мировой рынок: {event}",
                    aggregateTitle = "Движения рынка",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "📉",
                                title = "Рынок ушёл в тень",
                                body = "Тьма наступила. Видимость графика тоже.",
                                compact = "Рынок потемнел",
                                aggregateTitle = "Тёмный рынок",
                            ),
                            variant(
                                id = "eclipse",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "ECLIPSE")),
                                emoji = "🌒",
                                title = "Коррекция по солнцу",
                                body = "Началось затмение. Свет временно в дефиците.",
                                compact = "Солнце скорректировалось",
                                aggregateTitle = "Солнечная коррекция",
                            ),
                            variant(
                                id = "blood-moon",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "MOON")),
                                emoji = "🩸",
                                title = "Красная свеча на всё небо",
                                body = "Взошла кровавая луна. Технический анализ одобряет картинку.",
                                compact = "Небо закрылось красным",
                                aggregateTitle = "Красные свечи",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "Ликвидация",
                                body = "Произошёл взрыв. Кто держал позицию — уже поздно спрашивать.",
                                compact = "Ликвидация произошла",
                                aggregateTitle = "Массовые ликвидации",
                            ),
                        ),
                ),
            playerOffline =
                template(
                    emoji = "📉",
                    title = "Позиция закрыта",
                    body = "{player} вышел из игры. На сегодня торгов достаточно.",
                    compact = "{player} закрыл позицию",
                    aggregateTitle = "Позиции закрыты",
                ),
        )

    private fun deadpan(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.deadpan",
            name = "Без эмоций",
            pricePrime = 30,
            mixedAggregateTitle = "Произошло сразу несколько вещей",
            itemDrop =
                template(
                    emoji = "•",
                    title = "Предмет выпал",
                    body = "{player} получил «{item}». Такое иногда происходит.",
                    compact = "{player} получил «{item}»",
                    aggregateTitle = "Несколько предметов выпало",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "•",
                                title = "Редкий предмет выпал",
                                body = "{player} получил «{item}». Это довольно редко.",
                                compact = "{player} получил редкий «{item}»",
                                aggregateTitle = "Редкие предметы выпали",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "•",
                    title = "Бустер включён",
                    body = "{player} активировал {booster.accusative}. Он действительно работает.",
                    compact = "{player} включил {booster}",
                    aggregateTitle = "Несколько бустеров включено",
                    variants =
                        listOf(
                            variant(
                                id = "luck",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "LUCK")),
                                emoji = "•",
                                title = "Удача включена",
                                body = "{player} активировал Удачу. Теперь шансы немного другие.",
                                compact = "Удача включена",
                                aggregateTitle = "Удача была включена",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "•",
                    title = "Данж открыт",
                    body = "{dungeon} доступно. Можно заходить.",
                    compact = "{dungeon} открыто",
                    aggregateTitle = "Данжи открылись",
                ),
            raid =
                template(
                    emoji = "•",
                    title = "Рейд начался",
                    body = "Локации: {locations}. Да, это всё.",
                    compact = "Рейд: {locations}",
                    aggregateTitle = "Рейды начались",
                ),
            global =
                template(
                    emoji = "•",
                    title = "Событие началось",
                    body = "Сейчас происходит {event}.",
                    compact = "{event}",
                    aggregateTitle = "События произошли",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🌑",
                                title = "Стало темнее",
                                body = "Началась Тьма. Видимость снизилась.",
                                compact = "Сейчас темно",
                                aggregateTitle = "Стало темнее несколько раз",
                            ),
                            variant(
                                id = "eclipse",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "ECLIPSE")),
                                emoji = "🌒",
                                title = "Солнца стало меньше",
                                body = "Началось затмение.",
                                compact = "Идёт затмение",
                                aggregateTitle = "Затмения произошли",
                            ),
                            variant(
                                id = "blood-moon",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "MOON")),
                                emoji = "🩸",
                                title = "Луна покраснела",
                                body = "Взошла кровавая луна. Она действительно красная.",
                                compact = "Луна красная",
                                aggregateTitle = "Красные луны",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "Что-то взорвалось",
                                body = "Произошёл взрыв. Это было громко.",
                                compact = "Был взрыв",
                                aggregateTitle = "Что-то несколько раз взорвалось",
                            ),
                        ),
                ),
        )
}
