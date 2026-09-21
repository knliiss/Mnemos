package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.condition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.definition
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.template
import com.mnemos.personalization.application.personalization.builtin.BuiltinNotificationPackSupport.variant
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

object BuiltinNotificationPacksFresh {
    val definitions: List<BuiltinPersonalizationDefinition> =
        listOf(
            casino(),
            support(),
            gossip(),
            commentator(),
            deadline(),
        )

    private fun casino(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.casino",
            name = "Казино",
            pricePrime = 36,
            mixedAggregateTitle = "Крупье едва успевает объявлять результаты",
            itemDrop =
                template(
                    emoji = "🎰",
                    title = "Ставка сыграла",
                    body = "{player} сорвал «{item}». Казино сегодня решило отдать часть кассы.",
                    compact = "{player} выиграл «{item}»",
                    aggregateTitle = "Выигрышные ставки",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "💎",
                                title = "ДЖЕКПОТ",
                                body = "{player} выбил секретный «{item}». Крупье делает вид, что так и было задумано.",
                                compact = "JACKPOT → «{item}»",
                                aggregateTitle = "Джекпоты",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🃏",
                    title = "Ставки повышены",
                    body = "{player} активировал {booster.accusative}. Осторожная игра официально закончилась.",
                    compact = "ALL IN · {booster} · {player}",
                    aggregateTitle = "Повышенные ставки",
                    variants =
                        listOf(
                            variant(
                                id = "luck",
                                conditions = listOf(condition(NotificationPackVariantField.BOOSTER, "LUCK")),
                                emoji = "🍀",
                                title = "Крупье нервничает",
                                body = "{player} активировал Удачу. У заведения внезапно появились вопросы к математике.",
                                compact = "Удача против казино · {player}",
                                aggregateTitle = "Казино против удачи",
                            ),
                        ),
                ),
            dungeon =
                template(
                    emoji = "🎟️",
                    title = "Стол открыт",
                    body = "{dungeon} доступно. Делайте ваши ставки.",
                    compact = "Стол открыт → {dungeon}",
                    aggregateTitle = "Открытые столы",
                ),
            raid =
                template(
                    emoji = "♠️",
                    title = "Большая игра началась",
                    body = "Рейд идёт на локациях: {locations}. Выход из-за стола уже поздно искать.",
                    compact = "Большая игра → {locations}",
                    aggregateTitle = "Большие игры",
                ),
            global =
                template(
                    emoji = "🎲",
                    title = "Колесо мира крутится",
                    body = "Выпало событие {event}. Ставки приняты.",
                    compact = "Мировая ставка → {event}",
                    aggregateTitle = "Ставки мирового масштаба",
                    variants =
                        listOf(
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "Казино взорвало банк",
                                body = "Произошёл взрыв. Похоже, кто-то действительно поставил всё.",
                                compact = "БАНК ВЗОРВАН",
                                aggregateTitle = "Взорванные банки",
                            ),
                        ),
                ),
            playerOffline =
                template(
                    emoji = "💵",
                    title = "Игрок забрал фишки",
                    body = "{player} вышел из игры. Главное — вовремя уйти из казино.",
                    compact = "{player} вышел из-за стола",
                    aggregateTitle = "Игроки забрали фишки",
                ),
        )

    private fun support(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.support",
            name = "Техподдержка",
            pricePrime = 32,
            mixedAggregateTitle = "Очередь тикетов внезапно выросла",
            itemDrop =
                template(
                    emoji = "🎫",
                    title = "Тикет закрыт сам собой",
                    body = "{player} получил «{item}». Причина успеха неизвестна, но статус уже RESOLVED.",
                    compact = "RESOLVED · {player} → «{item}»",
                    aggregateTitle = "Закрытые тикеты",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🚨",
                                title = "Эскалация не понадобилась",
                                body = "{player} выбил секретный «{item}». L3 просит больше так не пугать.",
                                compact = "P0 RESOLVED → «{item}»",
                                aggregateTitle = "Закрытые P0",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🛠️",
                    title = "Временное решение применено",
                    body = "{player} активировал {booster.accusative}. Не трогайте, пока работает.",
                    compact = "WORKAROUND · {booster} · {player}",
                    aggregateTitle = "Применённые workaround'ы",
                ),
            dungeon =
                template(
                    emoji = "🔧",
                    title = "Техническое окно открыто",
                    body = "{dungeon} доступно. Работы желательно завершить до следующего инцидента.",
                    compact = "MAINTENANCE → {dungeon}",
                    aggregateTitle = "Технические окна",
                    variants =
                        listOf(
                            variant(
                                id = "expert",
                                conditions = listOf(condition(NotificationPackVariantField.DUNGEON, "EXPERT")),
                                emoji = "🧯",
                                title = "Только для L3",
                                body = "Экспертное подземелье открыто. Первая линия туда не полезет.",
                                compact = "L3 ONLY → Expert",
                                aggregateTitle = "Эскалации на L3",
                            ),
                        ),
                ),
            raid =
                template(
                    emoji = "📟",
                    title = "Массовый инцидент",
                    body = "Затронуты локации: {locations}. Статус-страница уже красная.",
                    compact = "INCIDENT → {locations}",
                    aggregateTitle = "Массовые инциденты",
                ),
            global =
                template(
                    emoji = "🚨",
                    title = "P1: мир работает нестабильно",
                    body = "Зафиксировано событие {event}. Ответственный уже якобы подключается.",
                    compact = "P1 → {event}",
                    aggregateTitle = "Критические инциденты",
                    variants =
                        listOf(
                            variant(
                                id = "darkness",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "DARKNESS")),
                                emoji = "🔌",
                                title = "Кажется, выключили прод",
                                body = "Наступила Тьма. Кто последний трогал освещение?",
                                compact = "PROD DARK",
                                aggregateTitle = "Падения освещения",
                            ),
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "🔥",
                                title = "Это уже не warning",
                                body = "Произошёл взрыв. Severity автоматически повышен до «ну всё».",
                                compact = "SEV: НУ ВСЁ",
                                aggregateTitle = "Инциденты уровня «ну всё»",
                            ),
                        ),
                ),
            playerOffline =
                template(
                    emoji = "✅",
                    title = "Пользователь отключился",
                    body = "{player} вышел из игры. Тикет можно закрывать до повторного обращения.",
                    compact = "{player} · DISCONNECTED",
                    aggregateTitle = "Закрытые сессии",
                ),
        )

    private fun gossip(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.gossip",
            name = "Сплетни",
            pricePrime = 27,
            mixedAggregateTitle = "Так, тут сразу несколько новостей — садитесь ближе",
            itemDrop =
                template(
                    emoji = "👀",
                    title = "Ты только никому",
                    body = "Говорят, {player} выбил «{item}». Я этого не говорила.",
                    compact = "Есть инфа: {player} → «{item}»",
                    aggregateTitle = "Свежие слухи о дропах",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🤫",
                                title = "Вот это уже секретик",
                                body = "{player} выбил секретный «{item}». Через пять минут об этом знает весь сервер.",
                                compact = "Секретик → «{item}» у {player}",
                                aggregateTitle = "Секреты, которые уже все знают",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "🗣️",
                    title = "По слухам, стал сильнее",
                    body = "Мне тут сказали, что {player} активировал {booster.accusative}. Но это не точно.",
                    compact = "Слух: {booster} у {player}",
                    aggregateTitle = "Слухи об усилениях",
                ),
            dungeon =
                template(
                    emoji = "📍",
                    title = "Есть одно место",
                    body = "Только между нами: {dungeon} открылось.",
                    compact = "Шёпотом → {dungeon}",
                    aggregateTitle = "Места, о которых все уже знают",
                ),
            raid =
                template(
                    emoji = "📣",
                    title = "Там уже все собираются",
                    body = "На локациях {locations} начинается движ. Я просто передаю.",
                    compact = "Все идут → {locations}",
                    aggregateTitle = "Главные обсуждения дня",
                ),
            global =
                template(
                    emoji = "📰",
                    title = "Это обсуждает весь сервер",
                    body = "Началось событие {event}. Через минуту у каждого будет своя версия произошедшего.",
                    compact = "Главная сплетня → {event}",
                    aggregateTitle = "Главные сплетни мира",
                ),
            playerOffline =
                template(
                    emoji = "🤐",
                    title = "После такого я бы тоже вышел",
                    body = "{player} больше не в сети. Причины мы, конечно же, придумывать не будем.",
                    compact = "{player} ушёл. Подозрительно.",
                    aggregateTitle = "Подозрительные выходы",
                ),
        )

    private fun commentator(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.commentator",
            name = "Комментатор",
            pricePrime = 35,
            mixedAggregateTitle = "События идут одно за другим — эфир не успевает выдохнуть",
            itemDrop =
                template(
                    emoji = "🎙️",
                    title = "ЕСТЬ ДРОП!",
                    body = "{player} забирает «{item}»! Какой момент, какая реализация!",
                    compact = "ГОООЛ... то есть ДРОП → «{item}»",
                    aggregateTitle = "Лучшие моменты дропа",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🏆",
                                title = "НЕВЕРОЯТНО!",
                                body = "{player} выбивает секретный «{item}»! Трибуны такого сегодня не ожидали!",
                                compact = "СЕНСАЦИЯ → «{item}»",
                                aggregateTitle = "Сенсации эфира",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "📈",
                    title = "Темп растёт!",
                    body = "{player} активирует {booster.accusative}! Посмотрим, как это изменит игру.",
                    compact = "УСИЛЕНИЕ → {booster} · {player}",
                    aggregateTitle = "Ключевые усиления",
                ),
            dungeon =
                template(
                    emoji = "🚪",
                    title = "И вот оно окно!",
                    body = "{dungeon} открывается прямо сейчас. Команды уже готовы заходить.",
                    compact = "ОКНО → {dungeon}",
                    aggregateTitle = "Открытые игровые окна",
                ),
            raid =
                template(
                    emoji = "⚔️",
                    title = "ПОЕХАЛИ!",
                    body = "Рейд стартует на локациях: {locations}. Начинается главное противостояние!",
                    compact = "СТАРТ → {locations}",
                    aggregateTitle = "Главные противостояния",
                ),
            global =
                template(
                    emoji = "📺",
                    title = "Вот это поворот!",
                    body = "Мир встречает событие {event}. Эфир становится только интереснее!",
                    compact = "LIVE → {event}",
                    aggregateTitle = "Главные события эфира",
                    variants =
                        listOf(
                            variant(
                                id = "explosion",
                                conditions = listOf(condition(NotificationPackVariantField.GLOBAL_EVENT, "EXPLOSION")),
                                emoji = "💥",
                                title = "КАКОЙ ВЗРЫВ!",
                                body = "Мир содрогнулся! Повтор этого момента точно покажут ещё не раз.",
                                compact = "МОМЕНТ МАТЧА → ВЗРЫВ",
                                aggregateTitle = "Самые громкие моменты",
                            ),
                        ),
                ),
            playerOffline =
                template(
                    emoji = "🏁",
                    title = "На сегодня всё",
                    body = "{player} покидает игру. Спасибо за этот перформанс.",
                    compact = "{player} завершил матч",
                    aggregateTitle = "Завершившие выступление",
                ),
        )

    private fun deadline(): BuiltinPersonalizationDefinition =
        definition(
            key = "notification.deadline",
            name = "Дедлайн",
            pricePrime = 29,
            mixedAggregateTitle = "Задач стало больше, а дедлайн почему-то не сдвинулся",
            itemDrop =
                template(
                    emoji = "⏳",
                    title = "Сдано за минуту до дедлайна",
                    body = "{player} всё-таки получил «{item}». Планирование снова чудом не понадобилось.",
                    compact = "Успел → «{item}»",
                    aggregateTitle = "Сданное в последнюю минуту",
                    variants =
                        listOf(
                            variant(
                                id = "secret",
                                conditions = listOf(condition(NotificationPackVariantField.ITEM_RARITY, "SECRET")),
                                emoji = "🫨",
                                title = "ЭТО БЫЛО БЛИЗКО",
                                body = "{player} выбил секретный «{item}». Ещё секунда — и пришлось бы просить перенос.",
                                compact = "В последнюю секунду → «{item}»",
                                aggregateTitle = "Спасённые дедлайны",
                            ),
                        ),
                ),
            booster =
                template(
                    emoji = "☕",
                    title = "Добавили ещё одну чашку",
                    body = "{player} активировал {booster.accusative}. Сон снова исключён из плана.",
                    compact = "Дедлайн + {booster} · {player}",
                    aggregateTitle = "Ресурсы перед дедлайном",
                ),
            dungeon =
                template(
                    emoji = "📅",
                    title = "Созвон уже начался",
                    body = "{dungeon} открылось. Да, приглашение было в календаре.",
                    compact = "MEETING NOW → {dungeon}",
                    aggregateTitle = "Пропущенные напоминания",
                    variants =
                        listOf(
                            variant(
                                id = "expert",
                                conditions = listOf(condition(NotificationPackVariantField.DUNGEON, "EXPERT")),
                                emoji = "🔥",
                                title = "Срочно на прод",
                                body = "Экспертное подземелье открыто. Конечно же, без времени на подготовку.",
                                compact = "HOTFIX → Expert",
                                aggregateTitle = "Срочные задачи",
                            ),
                        ),
                ),
            raid =
                template(
                    emoji = "📋",
                    title = "Спринт внезапно закончился",
                    body = "Работа уже кипит на локациях: {locations}. Кто не успел оценить задачу — поздно.",
                    compact = "SPRINT → {locations}",
                    aggregateTitle = "Задачи текущего спринта",
                ),
            global =
                template(
                    emoji = "🚨",
                    title = "Внеплановая задача",
                    body = "Началось событие {event}. Приоритет, разумеется, «ещё вчера».",
                    compact = "URGENT → {event}",
                    aggregateTitle = "Внеплановые задачи",
                ),
            playerOffline =
                template(
                    emoji = "💻",
                    title = "Ноутбук закрыт",
                    body = "{player} вышел из игры. Сообщения после этого момента считаются завтрашними.",
                    compact = "{player} закрыл ноутбук",
                    aggregateTitle = "Рабочий день закончен",
                ),
        )
}
