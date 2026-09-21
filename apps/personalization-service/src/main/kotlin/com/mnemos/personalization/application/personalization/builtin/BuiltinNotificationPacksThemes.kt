package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.condition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.definition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.template
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.variant
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

object BuiltinNotificationPacksThemes {
    val definitions: List<BuiltinPersonalizationDefinition> =
        listOf(
            hunter(),
            critical(),
            nightCity(),
            guild(),
        )

    private fun hunter(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.hunter",
            name = "Охотник",
            pricePrime = 28,
            mixedAggregateTitle = "Следов слишком много — охота в самом разгаре",
            itemDrop =
                template(
                    emoji = "🏹",
                    title = "Добыча взята",
                    body = "{player} выследил «{item}». На этот раз цель не ушла.",
                    compact = "{player} добыл «{item}»",
                    aggregateTitle = "Взятая добыча",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🐺",
                                title = "Редкий зверь пал",
                                body = "{player} забрал секретный «{item}». За таким следом стоило идти.",
                                compact = "Редкая добыча → «{item}»",
                                aggregateTitle = "Редкая добыча",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🩸",
                    title = "Чутьё обострилось",
                    body = "{player} активировал {booster.accusative}. След теперь видно отчётливее.",
                    compact = "Чутьё: {booster} · {player}",
                    aggregateTitle = "Охотничьи усиления",
                    variants =
                        listOf(
                            variant(
                                id = "damage",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "DAMAGE")),
                                emoji = "🗡️",
                                title = "Оружие заточено",
                                body = "{player} активировал Урон. Следующая цель это почувствует.",
                                compact = "Урон готов · {player}",
                                aggregateTitle = "Заточенное оружие",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "🐾",
                    title = "След ведёт в логово",
                    body = "{dungeon} открылось. Добыча где-то внутри.",
                    compact = "След → {dungeon}",
                    aggregateTitle = "Открытые логова",
                    variants =
                        listOf(
                            variant(
                                id = "expert",
                                conditions = listOf(condition(NotificationPackVariantField.DUNGEON, "EXPERT")),
                                emoji = "☠️",
                                title = "След слишком свежий",
                                body = "Экспертное подземелье открыто. Здесь охотник легко может стать добычей.",
                                compact = "Опасное логово → Expert",
                                aggregateTitle = "Опасные логова",
                            ),
                        ),
                ),
            raid =
                template(
                    emoji = "🎯",
                    title = "Цели обнаружены",
                    body = "Охота началась на локациях: {locations}.",
                    compact = "Цели → {locations}",
                    aggregateTitle = "Рейдовая охота",
                ),
            global =
                template(
                    emoji = "🌲",
                    title = "Мир сменил след",
                    body = "Началось событие {event}. Условия охоты изменились.",
                    compact = "Новый след: {event}",
                    aggregateTitle = "Перемены на охоте",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🌑",
                                title = "Охота без света",
                                body = "Тьма наступила. Теперь остаётся полагаться только на чутьё.",
                                compact = "Ночная охота",
                                aggregateTitle = "Охота во тьме",
                            ),
                            variant(
                                id = "blood-moon",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "MOON")),
                                emoji = "🩸",
                                title = "Кровавая луна зовёт",
                                body = "Взошла кровавая луна. Ночь обещает быть хищной.",
                                compact = "Луна вышла на охоту",
                                aggregateTitle = "Кровавая охота",
                            ),
                        ),
                ),
            playerOffline =
                template(
                    emoji = "🏕️",
                    title = "Охотник снялся со следа",
                    body = "{player} вышел из игры. Охота продолжится позже.",
                    compact = "{player} ушёл с охоты",
                    aggregateTitle = "Охотники ушли",
                ),
        )

    private fun critical(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.critical",
            name = "Крит",
            pricePrime = 34,
            mixedAggregateTitle = "Система прокнула сразу несколько критов",
            itemDrop =
                template(
                    emoji = "💢",
                    title = "КРИТИЧЕСКИЙ ДРОП",
                    body = "{player} выбил «{item}». Обычного попадания игре показалось мало.",
                    compact = "CRIT → «{item}» у {player}",
                    aggregateTitle = "Критические дропы",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "💥",
                                title = "OVERCRIT",
                                body = "{player} выбил секретный «{item}». Цифры урона уже не помещаются.",
                                compact = "OVERCRIT → «{item}»",
                                aggregateTitle = "Криты за пределами нормы",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "⚡",
                    title = "Модификатор крита",
                    body = "{player} активировал {booster.accusative}. Базового урона событиям уже мало.",
                    compact = "CRIT MOD · {booster} · {player}",
                    aggregateTitle = "Критические усиления",
                ),
            dungeon =
                template(
                    emoji = "🎯",
                    title = "Окно прокнуло",
                    body = "{dungeon} открылось. Попадание точно в тайминг.",
                    compact = "CRIT WINDOW → {dungeon}",
                    aggregateTitle = "Критические окна",
                ),
            raid =
                template(
                    emoji = "⚔️",
                    title = "Массовый крит",
                    body = "Рейд начался на локациях: {locations}. Пора раздавать цифры.",
                    compact = "AOE CRIT → {locations}",
                    aggregateTitle = "Массовые криты",
                ),
            global =
                template(
                    emoji = "💥",
                    title = "Мир получил крит",
                    body = "Событие {event} прошло с критическим модификатором.",
                    compact = "WORLD CRIT → {event}",
                    aggregateTitle = "Критические события мира",
                    variants =
                        listOf(
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "☢️",
                                title = "КРИТ ПО МИРУ",
                                body = "Произошёл взрыв. Похоже, модификатор урона был лишним.",
                                compact = "WORLD OVERCRIT",
                                aggregateTitle = "Мировые оверкриты",
                            ),
                        ),
                ),
            playerOffline =
                template(
                    emoji = "💤",
                    title = "Комбо прервано",
                    body = "{player} вышел из игры. Серия критов закончилась.",
                    compact = "{player} сбросил комбо",
                    aggregateTitle = "Сброшенные комбо",
                ),
        )

    private fun nightCity(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.night-city",
            name = "Ночной город",
            pricePrime = 38,
            mixedAggregateTitle = "Ночной эфир переполнен новыми сигналами",
            itemDrop =
                template(
                    emoji = "🌃",
                    title = "Новый трофей на улицах",
                    body = "{player} забрал «{item}». Неон всё видел, но ничего не скажет.",
                    compact = "NEON DROP → «{item}»",
                    aggregateTitle = "Ночные трофеи",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "💜",
                                title = "Контрабанда высшего класса",
                                body = "{player} получил секретный «{item}». В открытом эфире об этом не говорят.",
                                compact = "BLACK MARKET → «{item}»",
                                aggregateTitle = "Трофеи чёрного рынка",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🔋",
                    title = "Неон стал ярче",
                    body = "{player} активировал {booster.accusative}. Город подал больше напряжения.",
                    compact = "POWER GRID · {booster} · {player}",
                    aggregateTitle = "Перегруженная сеть",
                ),
            dungeon =
                template(
                    emoji = "🚇",
                    title = "Открылась ночная точка",
                    body = "{dungeon} доступно. Вход отмечен только на подпольной карте.",
                    compact = "UNDERGROUND → {dungeon}",
                    aggregateTitle = "Подпольные точки",
                ),
            raid =
                template(
                    emoji = "🚨",
                    title = "Сирены в квартале",
                    body = "Рейд начался на локациях: {locations}. Город уже не спит.",
                    compact = "ALERT → {locations}",
                    aggregateTitle = "Ночные тревоги",
                ),
            global =
                template(
                    emoji = "📺",
                    title = "Экстренный эфир",
                    body = "Город сообщает: началось событие {event}.",
                    compact = "LIVE → {event}",
                    aggregateTitle = "Экстренные эфиры",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🌑",
                                title = "Неон остался один",
                                body = "Тьма накрыла город. Теперь улицы держатся только на вывесках.",
                                compact = "NEON ONLY",
                                aggregateTitle = "Город без света",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "Канал потерял сигнал",
                                body = "Произошёл взрыв. Эфир вернётся, когда перестанет трясти.",
                                compact = "SIGNAL LOST",
                                aggregateTitle = "Потерянные сигналы",
                            ),
                        ),
                ),
            playerOffline =
                template(
                    emoji = "🚕",
                    title = "Уехал из ночного города",
                    body = "{player} вышел из игры. Неон остался гореть без него.",
                    compact = "{player} покинул город",
                    aggregateTitle = "Город пустеет",
                ),
        )

    private fun guild(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.guild",
            name = "Гильдия",
            pricePrime = 36,
            mixedAggregateTitle = "Вестник гильдии принёс сразу несколько свитков",
            itemDrop =
                template(
                    emoji = "🛡️",
                    title = "Трофей занесён в летопись",
                    body = "{player} принёс в гильдию «{item}». Писарь уже открыл новую страницу.",
                    compact = "{player} принёс «{item}»",
                    aggregateTitle = "Трофеи гильдии",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🏆",
                                title = "Реликвия для зала славы",
                                body = "{player} добыл секретный «{item}». Для такого трофея придётся освободить витрину.",
                                compact = "Реликвия → «{item}»",
                                aggregateTitle = "Реликвии гильдии",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "📯",
                    title = "Знамёна подняты",
                    body = "{player} активировал {booster.accusative}. Гильдия получила преимущество.",
                    compact = "Бафф гильдии · {booster}",
                    aggregateTitle = "Усиления гильдии",
                ),
            dungeon =
                template(
                    emoji = "🏰",
                    title = "Открыт путь для группы",
                    body = "{dungeon} доступно. Сбор у ворот.",
                    compact = "Сбор → {dungeon}",
                    aggregateTitle = "Походы в подземелья",
                    variants =
                        listOf(
                            variant(
                                id = "expert",
                                conditions = listOf(condition(NotificationPackVariantField.DUNGEON, "EXPERT")),
                                emoji = "⚜️",
                                title = "Испытание для ветеранов",
                                body = "Экспертное подземелье открыто. Новичков лучше оставить в таверне.",
                                compact = "Ветераны → Expert",
                                aggregateTitle = "Испытания ветеранов",
                            ),
                        ),
                ),
            raid =
                template(
                    emoji = "⚔️",
                    title = "Общий сбор",
                    body = "Гильдия собирается на рейд. Локации: {locations}.",
                    compact = "Сбор гильдии → {locations}",
                    aggregateTitle = "Походы гильдии",
                ),
            global =
                template(
                    emoji = "📜",
                    title = "Королевский указ",
                    body = "Мир бросил новый вызов: {event}.",
                    compact = "Указ: {event}",
                    aggregateTitle = "Указы и события",
                ),
            playerOffline =
                template(
                    emoji = "🍺",
                    title = "Вернулся в таверну",
                    body = "{player} вышел из игры. В гильдии его будут ждать.",
                    compact = "{player} ушёл в таверну",
                    aggregateTitle = "Вернулись в таверну",
                ),
        )
}
