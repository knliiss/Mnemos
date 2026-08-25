package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.condition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.definition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.template
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.variant
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import com.mnemos.personalization.domain.personalization.model.NotificationValueForms

object BuiltinNotificationPacksCore {
    val definitions: List<BuiltinPersonalizationDefinition> =
        listOf(
            mnemosArchive(),
            protocol(),
            rng(),
        )

    private fun mnemosArchive(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.mnemos-archive",
            name = "Mnemos Archive",
            pricePrime = 15,
            mixedAggregateTitle = "Хроника пополнилась сразу несколькими событиями",
            itemDrop =
                template(
                    emoji = "📜",
                    title = "Запись добавлена",
                    body = "{player} получил «{item}». Находка сохранена в хронике.",
                    compact = "{player} → «{item}»",
                    aggregateTitle = "Новые записи о находках",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "✦",
                                title = "Особая запись",
                                body = "Хроника отмечает редкую находку: {player} получил «{item}».",
                                compact = "Особая запись · {player} → «{item}»",
                                aggregateTitle = "Особые записи о находках",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "⚡",
                    title = "Состояние изменилось",
                    body = "{player} активировал эффект {booster.genitive}. Изменение сохранено.",
                    compact = "{player} · {booster}",
                    aggregateTitle = "Новые записи об усилениях",
                    variants =
                        listOf(
                            variant(
                                id = "luck",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "LUCK")),
                                emoji = "🍀",
                                title = "Запись об удаче",
                                body = "{player} активировал эффект Удачи. Хроника отметила изменение шансов.",
                                compact = "Удача · {player}",
                                aggregateTitle = "Записи об удаче",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "🗝",
                    title = "Проход открыт",
                    body = "{dungeon} теперь доступно. Момент открытия сохранён.",
                    compact = "Открыто: {dungeon}",
                    aggregateTitle = "Новые записи о подземельях",
                    variants =
                        listOf(
                            variant(
                                id = "expert",
                                conditions = listOf(condition(NotificationPackVariantField.DUNGEON, "EXPERT")),
                                emoji = "🔐",
                                title = "Особый проход открыт",
                                body = "Экспертное подземелье доступно. Хроника выделила эту запись отдельно.",
                                compact = "Особый проход · Expert",
                                aggregateTitle = "Особые проходы",
                            ),
                        ),
                ),
            raid =
                template(
                    emoji = "⚔️",
                    title = "Зафиксирован рейд",
                    body = "Активность отмечена на локациях: {locations}.",
                    compact = "Рейд → {locations}",
                    aggregateTitle = "Новые записи о рейдах",
                ),
            global =
                template(
                    emoji = "◈",
                    title = "Мир изменился",
                    body = "Хроника отмечает событие: {event}.",
                    compact = "Событие: {event}",
                    aggregateTitle = "Записи об изменениях мира",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🌑",
                                title = "Тьма",
                                body = "Свет исчезает. Тьма наступает, и новая запись открывается в хронике.",
                                compact = "Тьма накрыла мир",
                                aggregateTitle = "Записи о Тьме",
                            ),
                            variant(
                                id = "eclipse",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "ECLIPSE")),
                                emoji = "🌒",
                                title = "Затмение",
                                body = "Небо темнеет. Начало затмения сохранено в хронике.",
                                compact = "Началось затмение",
                                aggregateTitle = "Записи о затмениях",
                            ),
                            variant(
                                id = "blood-moon",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "MOON")),
                                emoji = "🩸",
                                title = "Кровавая луна",
                                body = "Кровавая луна поднялась над миром. Хроника запомнила этот момент.",
                                compact = "Взошла кровавая луна",
                                aggregateTitle = "Записи о кровавой луне",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "Взрыв",
                                body = "Мир содрогнулся. В хронике появилась запись о взрыве.",
                                compact = "Зафиксирован взрыв",
                                aggregateTitle = "Записи о взрывах",
                            ),
                        ),
                ),
        )

    private fun protocol(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.protocol",
            name = "Протокол",
            pricePrime = 25,
            mixedAggregateTitle = "SYSTEM // MULTIPLE EVENTS DETECTED",
            valueMappings = protocolValues(),
            itemDrop =
                template(
                    emoji = "▣",
                    title = "DROP // CONFIRMED",
                    body = "USER {player} RECEIVED «{item}» // RARITY: {rarity}",
                    compact = "DROP // {player} → «{item}»",
                    aggregateTitle = "DROP EVENTS",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "◆",
                                title = "DROP // SECRET",
                                body = "HIGH-RARITY PAYLOAD // {player} → «{item}»",
                                compact = "SECRET // {player} → «{item}»",
                                aggregateTitle = "SECRET DROPS",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "⚡",
                    title = "BOOST // ONLINE",
                    body = "USER {player} ENABLED {booster}",
                    compact = "BOOST // {booster} // {player}",
                    aggregateTitle = "ACTIVE BOOSTS",
                    variants =
                        listOf(
                            variant(
                                id = "luck",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "LUCK")),
                                emoji = "⌁",
                                title = "PROBABILITY // MODIFIED",
                                body = "USER {player} ENABLED LUCK // STATE UPDATED",
                                compact = "LUCK // ACTIVE // {player}",
                                aggregateTitle = "PROBABILITY MODIFIERS",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "◇",
                    title = "INSTANCE // OPEN",
                    body = "ACCESS GRANTED // {dungeon}",
                    compact = "INSTANCE // {dungeon}",
                    aggregateTitle = "OPEN INSTANCES",
                ),
            raid =
                template(
                    emoji = "⚔",
                    title = "RAID // START",
                    body = "ACTIVE LOCATIONS // {locations}",
                    compact = "RAID // {locations}",
                    aggregateTitle = "RAID ACTIVITY",
                ),
            global =
                template(
                    emoji = "◎",
                    title = "WORLD // EVENT",
                    body = "EVENT DETECTED // {event}",
                    compact = "WORLD // {event}",
                    aggregateTitle = "WORLD EVENTS",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🌑",
                                title = "WORLD // DARKNESS",
                                body = "LIGHT LEVEL CRITICAL // DARKNESS ACTIVE",
                                compact = "DARKNESS // ACTIVE",
                                aggregateTitle = "DARKNESS EVENTS",
                            ),
                            variant(
                                id = "eclipse",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "ECLIPSE")),
                                emoji = "🌒",
                                title = "WORLD // ECLIPSE",
                                body = "SOLAR VISIBILITY REDUCED // ECLIPSE ACTIVE",
                                compact = "ECLIPSE // ACTIVE",
                                aggregateTitle = "ECLIPSE EVENTS",
                            ),
                            variant(
                                id = "blood-moon",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "MOON")),
                                emoji = "🩸",
                                title = "WORLD // BLOOD MOON",
                                body = "SKY STATE CHANGED // BLOOD MOON ACTIVE",
                                compact = "BLOOD MOON // ACTIVE",
                                aggregateTitle = "BLOOD MOON EVENTS",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "WORLD // EXPLOSION",
                                body = "HIGH-ENERGY EVENT DETECTED",
                                compact = "EXPLOSION // DETECTED",
                                aggregateTitle = "EXPLOSION EVENTS",
                            ),
                        ),
                ),
        )

    private fun rng(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.rng",
            name = "RNG",
            pricePrime = 30,
            mixedAggregateTitle = "Похоже, RNG сегодня решил не скучать",
            itemDrop =
                template(
                    emoji = "🎲",
                    title = "Шанс сработал",
                    body = "RNG выбрал {player}. В инвентаре появился «{item}».",
                    compact = "{player} выиграл «{item}» у вероятности",
                    aggregateTitle = "RNG раздал дроп",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🃏",
                                title = "Это уже подозрительно",
                                body = "{player} получил «{item}». Таблицу шансов лучше пока не открывать.",
                                compact = "{player} сломал шанс → «{item}»",
                                aggregateTitle = "Подозрительно удачные дропы",
                            ),
                            variant(
                                id = "secret-pet",
                                conditions =
                                    listOf(
                                        condition(NotificationPackVariantField.ITEM_RARITY, "SECRET"),
                                        condition(NotificationPackVariantField.ITEM_TYPE, "PET"),
                                    ),
                                emoji = "🐾",
                                title = "RNG окончательно сдался",
                                body = "У {player} появился секретный питомец «{item}». Вероятность решила не спорить.",
                                compact = "Секретный питомец → {player}",
                                aggregateTitle = "RNG раздал секретных питомцев",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🍀",
                    title = "Вероятности изменились",
                    body = "{player} активировал эффект {booster.genitive}. Теперь можно делать вид, что всё рассчитано.",
                    compact = "{player} подкрутил {booster}",
                    aggregateTitle = "Вероятности подкрутили",
                    variants =
                        listOf(
                            variant(
                                id = "luck",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "LUCK")),
                                emoji = "🍀",
                                title = "RNG получил конкурента",
                                body = "{player} активировал Удачу. Честный рандом временно под вопросом.",
                                compact = "{player} подкрутил удачу",
                                aggregateTitle = "RNG под давлением",
                            ),
                            variant(
                                id = "money",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "MONEY")),
                                emoji = "💰",
                                title = "Шансы теперь ещё и оплачиваются",
                                body = "{player} активировал Доход. Вероятность внезапно стала коммерческой.",
                                compact = "RNG монетизирован · {player}",
                                aggregateTitle = "Коммерческий RNG",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "⏱",
                    title = "Тайминг пойман",
                    body = "{dungeon} открылось. На этот раз шанс опоздать не сработал.",
                    compact = "Поймано окно: {dungeon}",
                    aggregateTitle = "Пойманные окна",
                    variants =
                        listOf(
                            variant(
                                id = "expert",
                                conditions = listOf(condition(NotificationPackVariantField.DUNGEON, "EXPERT")),
                                emoji = "🎯",
                                title = "Попал ровно в окно",
                                body = "Экспертное подземелье открыто. Хотя бы один тайминг сегодня выпал как надо.",
                                compact = "Экспертное окно поймано",
                                aggregateTitle = "Экспертные окна",
                            ),
                        ),
                ),
            raid =
                template(
                    emoji = "🎯",
                    title = "Ещё одна проверка на удачу",
                    body = "Рейд стартовал на локациях: {locations}.",
                    compact = "Рейд → {locations}",
                    aggregateTitle = "Новые проверки RNG",
                ),
            global =
                template(
                    emoji = "🎲",
                    title = "Мир бросил кубик",
                    body = "Выпало событие: {event}.",
                    compact = "Мир выбрал {event}",
                    aggregateTitle = "Мировой RNG",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🌑",
                                title = "RNG выключил свет",
                                body = "Выпала Тьма. Обычного рандома, видимо, было недостаточно.",
                                compact = "RNG включил Тьму",
                                aggregateTitle = "Тёмные броски",
                            ),
                            variant(
                                id = "eclipse",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "ECLIPSE")),
                                emoji = "🌒",
                                title = "Шанс увидеть солнце снизился",
                                body = "Выпало затмение. Вероятность света временно не 100%.",
                                compact = "Солнце проиграло шансам",
                                aggregateTitle = "Затмения по RNG",
                            ),
                            variant(
                                id = "blood-moon",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "MOON")),
                                emoji = "🩸",
                                title = "Небо выбило редкий скин",
                                body = "Взошла кровавая луна. Обычная сегодня, видимо, не прокнула.",
                                compact = "Небо выбило Blood Moon",
                                aggregateTitle = "Редкие небесные проки",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "Выпал крит",
                                body = "Мир только что прокнул на взрыв.",
                                compact = "WORLD CRIT → взрыв",
                                aggregateTitle = "Критические броски мира",
                            ),
                        ),
                ),
        )

    private fun protocolValues(): Map<NotificationPackValueKey, Map<String, NotificationValueForms>> =
        RussianNotificationValues.all +
            mapOf(
                NotificationPackValueKey.BOOSTER to
                    mapOf(
                        "LUCK" to NotificationValueForms("LUCK"),
                        "MONEY" to NotificationValueForms("MONEY"),
                        "DAMAGE" to NotificationValueForms("DAMAGE"),
                        "POWER" to NotificationValueForms("POWER"),
                    ),
                NotificationPackValueKey.DUNGEON to
                    mapOf(
                        "EASY" to NotificationValueForms("EASY INSTANCE"),
                        "MEDIUM" to NotificationValueForms("MEDIUM INSTANCE"),
                        "HARD" to NotificationValueForms("HARD INSTANCE"),
                        "EXPERT" to NotificationValueForms("EXPERT INSTANCE"),
                    ),
                NotificationPackValueKey.GLOBAL_EVENT to
                    mapOf(
                        "DARKNESS" to NotificationValueForms("DARKNESS"),
                        "MOON" to NotificationValueForms("BLOOD MOON"),
                        "ECLIPSE" to NotificationValueForms("ECLIPSE"),
                        "EXPLOSION" to NotificationValueForms("EXPLOSION"),
                    ),
                NotificationPackValueKey.ITEM_RARITY to
                    mapOf(
                        "MYTHICAL" to NotificationValueForms("MYTHICAL"),
                        "SECRET" to NotificationValueForms("SECRET"),
                    ),
            )
}
