package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.condition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.definition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.template
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.variant
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

object BuiltinNotificationPacksMeme {
    val definitions: List<BuiltinPersonalizationDefinition> =
        listOf(
            dzhamshut(),
            shbshnik(),
            kaif(),
            gospodin(),
            lavash(),
            zaebalo(),
        )

    private fun dzhamshut(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.dzhamshut",
            name = "Джамшут",
            pricePrime = 35,
            mixedAggregateTitle = "Стройка встала: событий слишком много",
            itemDrop =
                template(
                    emoji = "🧱",
                    title = "Я строить, я копать, я дроп выбивать!",
                    body = "{player} добыл «{item}». Кирпичей много, дропа мало — сегодня повезло.",
                    compact = "{player} накопал «{item}»",
                    aggregateTitle = "Накопанные дропы",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🏗️",
                                title = "Прораб такое не согласовывал",
                                body = "{player} выкопал секретный «{item}». Теперь работу точно никто не продолжит.",
                                compact = "Секретный клад → «{item}»",
                                aggregateTitle = "Незаконно хорошие находки",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🔨",
                    title = "Инструмент выдали",
                    body = "{player} активировал {booster.accusative}. Работа пошла быстрее.",
                    compact = "{player} получил {booster}",
                    aggregateTitle = "Усиленная смена",
                ),
            dungeon =
                template(
                    emoji = "🚧",
                    title = "Проход раскопан",
                    body = "{dungeon} открылось. Табличку «ремонт» уже можно убирать.",
                    compact = "Раскопано: {dungeon}",
                    aggregateTitle = "Раскопанные проходы",
                ),
            raid =
                template(
                    emoji = "🏗️",
                    title = "Бригада выехала",
                    body = "Работы начались на локациях: {locations}.",
                    compact = "Бригада → {locations}",
                    aggregateTitle = "Объекты бригады",
                ),
            global =
                template(
                    emoji = "🧱",
                    title = "Объект снова с сюрпризом",
                    body = "Началось событие {event}. По смете этого не было.",
                    compact = "Вне сметы: {event}",
                    aggregateTitle = "События вне сметы",
                ),
            playerOffline =
                template(
                    emoji = "🧹",
                    title = "Смена закончилась",
                    body = "{player} ушёл с объекта. Плитку пока класть некому.",
                    compact = "{player} ушёл со смены",
                    aggregateTitle = "Смена закончилась",
                ),
        )

    private fun shbshnik(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.shbshnik",
            name = "ШБшник",
            pricePrime = 40,
            mixedAggregateTitle = "ШБ снова устроило целую серию событий",
            itemDrop =
                template(
                    emoji = "⚡",
                    title = "Как всегда ШБ",
                    body = "{player} снова выбил «{item}». Пора уже выбираться из-под стола Битошки?",
                    compact = "ШБ · {player} → «{item}»",
                    aggregateTitle = "Очередное ШБ",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🤨",
                                title = "ШБ, но уже подозрительно",
                                body = "{player} забрал секретный «{item}». Битошка требует объяснений.",
                                compact = "Секретное ШБ → «{item}»",
                                aggregateTitle = "Подозрительные ШБ-дропы",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🔌",
                    title = "ШБ подключило усиление",
                    body = "{player} активировал {booster.accusative}. Теперь оправданий ещё меньше.",
                    compact = "ШБ + {booster} · {player}",
                    aggregateTitle = "ШБ под усилением",
                ),
            dungeon =
                template(
                    emoji = "🚪",
                    title = "Выход из-под стола найден",
                    body = "{dungeon} открылось. Есть шанс наконец-то выбраться.",
                    compact = "ШБ нашло {dungeon}",
                    aggregateTitle = "Маршруты эвакуации ШБ",
                ),
            raid =
                template(
                    emoji = "⚔️",
                    title = "ШБ вышло в люди",
                    body = "Рейд на локациях: {locations}. Да, оно реально вышло.",
                    compact = "ШБ → {locations}",
                    aggregateTitle = "Выходы ШБ",
                ),
            global =
                template(
                    emoji = "⚡",
                    title = "Даже мир теперь ШБ",
                    body = "Началось событие {event}. Совпадение слишком удобное.",
                    compact = "WORLD = ШБ · {event}",
                    aggregateTitle = "Глобальное ШБ",
                ),
            playerOffline =
                template(
                    emoji = "🪑",
                    title = "Вернулся под стол",
                    body = "{player} вышел из игры. Место у Битошки снова занято.",
                    compact = "{player} снова под столом",
                    aggregateTitle = "ШБ ушли в офлайн",
                ),
        )

    private fun kaif(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.kaif",
            name = "Кайф",
            pricePrime = 28,
            mixedAggregateTitle = "Сегодня игра решила навалить кайфа",
            itemDrop =
                template(
                    emoji = "🌀",
                    title = "Абсолютный кайф",
                    body = "{player} выбил «{item}». Вот ради таких моментов и фармят.",
                    compact = "Кайф → «{item}» у {player}",
                    aggregateTitle = "Порция кайфа",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "😮‍💨",
                                title = "Вот теперь прям кайф",
                                body = "{player} выбил секретный «{item}». Можно просто молча наслаждаться.",
                                compact = "Секретный кайф → «{item}»",
                                aggregateTitle = "Кайф повышенной редкости",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "✨",
                    title = "Стало ещё приятнее",
                    body = "{player} активировал {booster.accusative}. Фарм официально перешёл в комфорт-режим.",
                    compact = "Кайф + {booster} · {player}",
                    aggregateTitle = "Комфортный фарм",
                ),
            dungeon =
                template(
                    emoji = "🚪",
                    title = "Идеальный тайминг",
                    body = "{dungeon} открылось ровно к моменту, когда захотелось зайти.",
                    compact = "Кайфовый тайминг → {dungeon}",
                    aggregateTitle = "Идеальные тайминги",
                ),
            raid =
                template(
                    emoji = "🌊",
                    title = "Волна пошла",
                    body = "Рейд начался на локациях: {locations}. Можно красиво залетать.",
                    compact = "На кайфе → {locations}",
                    aggregateTitle = "Кайфовые рейды",
                ),
            global =
                template(
                    emoji = "🌌",
                    title = "Атмосфера подъехала",
                    body = "Началось событие {event}. Мир сегодня умеет в настроение.",
                    compact = "Атмосфера: {event}",
                    aggregateTitle = "События с вайбом",
                ),
            playerOffline =
                template(
                    emoji = "😴",
                    title = "Хватит кайфовать",
                    body = "{player} вышел из игры. На сегодня достаточно.",
                    compact = "{player} ушёл отдыхать",
                    aggregateTitle = "Пошли отдыхать",
                ),
        )

    private fun gospodin(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.gospodin",
            name = "Господин",
            pricePrime = 45,
            mixedAggregateTitle = "Для господина подготовлено сразу несколько известий",
            itemDrop =
                template(
                    emoji = "👑",
                    title = "Ваша удача безгранична",
                    body = "Поздравляю, господин {player}. «{item}» теперь по праву принадлежит вам.",
                    compact = "Господин {player} получил «{item}»",
                    aggregateTitle = "Трофеи господина",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "💎",
                                title = "Достойно вашей коллекции",
                                body = "Господин {player}, секретный «{item}» доставлен. Остальным остаётся кланяться.",
                                compact = "Редкость для господина → «{item}»",
                                aggregateTitle = "Сокровища высшего порядка",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🪄",
                    title = "Ваше превосходство усилено",
                    body = "Господин {player} активировал {booster.accusative}. Как будто это было необходимо.",
                    compact = "{booster} для господина {player}",
                    aggregateTitle = "Усиления господина",
                ),
            dungeon =
                template(
                    emoji = "🏰",
                    title = "Врата открыты для вас",
                    body = "{dungeon} готово принять господина.",
                    compact = "Открыто для господина: {dungeon}",
                    aggregateTitle = "Открытые владения",
                ),
            raid =
                template(
                    emoji = "⚔️",
                    title = "Свита готова к походу",
                    body = "Цели ждут на локациях: {locations}.",
                    compact = "Поход → {locations}",
                    aggregateTitle = "Походы господина",
                ),
            global =
                template(
                    emoji = "🌍",
                    title = "Мир меняется по вашему расписанию",
                    body = "Началось событие {event}. Разумеется, именно сейчас.",
                    compact = "По воле господина: {event}",
                    aggregateTitle = "Мирские перемены",
                ),
            playerOffline =
                template(
                    emoji = "🛏️",
                    title = "Господин удалился",
                    body = "{player} покинул игру. Аудиенция окончена.",
                    compact = "Господин {player} ушёл",
                    aggregateTitle = "Аудиенции окончены",
                ),
        )

    private fun lavash(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.lavash",
            name = "Лаваш",
            pricePrime = 34,
            mixedAggregateTitle = "Заказ получился большим: тут сразу несколько событий",
            itemDrop =
                template(
                    emoji = "🥙",
                    title = "Лаваш готов",
                    body = "{player} завернул «{item}» в лут. Горячее, свежее, только что выпало.",
                    compact = "Лаваш-дроп → «{item}»",
                    aggregateTitle = "Свежая партия дропа",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🌯",
                                title = "Шеф завернул секретное",
                                body = "{player} получил секретный «{item}». Такой заказ в меню не показывают.",
                                compact = "Секретный лаваш → «{item}»",
                                aggregateTitle = "Секретное меню",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🌶️",
                    title = "Добавили соус",
                    body = "{player} активировал {booster.accusative}. Теперь фарм с добавкой.",
                    compact = "Соус {booster} · {player}",
                    aggregateTitle = "Заказы с добавкой",
                ),
            dungeon =
                template(
                    emoji = "🚚",
                    title = "Доставка приехала",
                    body = "{dungeon} открылось. Забирайте, пока горячее.",
                    compact = "Доставка: {dungeon}",
                    aggregateTitle = "Горячие доставки",
                ),
            raid =
                template(
                    emoji = "🔪",
                    title = "Кухня вышла в рейд",
                    body = "Заказ готовится на локациях: {locations}.",
                    compact = "Кухня → {locations}",
                    aggregateTitle = "Большие заказы",
                ),
            global =
                template(
                    emoji = "🔥",
                    title = "Кухня горит, но всё по плану",
                    body = "Началось событие {event}. Главное — не перепутать с грилем.",
                    compact = "На кухне: {event}",
                    aggregateTitle = "События на кухне",
                ),
            playerOffline =
                template(
                    emoji = "🧾",
                    title = "Заказ закрыт",
                    body = "{player} вышел из игры. Смена закончена, лаваш съеден.",
                    compact = "{player} закрыл смену",
                    aggregateTitle = "Смены закрыты",
                ),
        )

    private fun zaebalo(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.zaebalo",
            name = "Заебало",
            pricePrime = 32,
            mixedAggregateTitle = "Да сколько можно, опять сразу несколько событий",
            itemDrop =
                template(
                    emoji = "🥵",
                    title = "Ну наконец-то, блядь",
                    body = "{player} выбил «{item}». Всё заебало, кроме того, что оно всё-таки выпало.",
                    compact = "Наконец → «{item}» у {player}",
                    aggregateTitle = "Наконец что-то выпало",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🤬",
                                title = "И ради этого столько фарма",
                                body = "{player} выбил секретный «{item}». Теперь можно злиться чуть тише.",
                                compact = "Секретка. Наконец-то.",
                                aggregateTitle = "Секретки через боль",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "💊",
                    title = "Ладно, ещё один заход",
                    body = "{player} активировал {booster.accusative}. Значит, домой пока никто не идёт.",
                    compact = "Ещё один заход · {booster}",
                    aggregateTitle = "Очередные «последние» заходы",
                ),
            dungeon =
                template(
                    emoji = "😩",
                    title = "Конечно, именно сейчас",
                    body = "{dungeon} открылось. Потому что спокойно закончить нельзя.",
                    compact = "Опять открыто: {dungeon}",
                    aggregateTitle = "Очередные окна",
                ),
            raid =
                template(
                    emoji = "🫠",
                    title = "Ну и куда теперь бежать",
                    body = "Рейд начался на локациях: {locations}. Отдых снова отменяется.",
                    compact = "Отдых отменён → {locations}",
                    aggregateTitle = "Причины не отдыхать",
                ),
            global =
                template(
                    emoji = "🙃",
                    title = "Миру тоже неймётся",
                    body = "Началось событие {event}. Как будто обычных проблем мало.",
                    compact = "Ещё и {event}",
                    aggregateTitle = "Проблемы мирового масштаба",
                ),
            playerOffline =
                template(
                    emoji = "🚪",
                    title = "Всё, я ушёл",
                    body = "{player} вышел из игры. И, если честно, правильно сделал.",
                    compact = "{player} не выдержал",
                    aggregateTitle = "Игроки не выдержали",
                ),
        )
}
