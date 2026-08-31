package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuMessageRenderer
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.online.SaoOnlineCallbackData
import com.mnemos.telegram.bot.application.online.model.SaoOnlineMenuScreenData
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Component
class LocalTelegramMenuMessageRenderer : TelegramMenuMessageRenderer {
    override fun render(
        screen: TelegramMenuScreen,
        data: TelegramMenuScreenData,
        language: TelegramSettingsLanguage,
    ): OutboundTelegramMessage {
        val rawMessage =
            when (screen) {
                TelegramMenuScreen.HOME -> homeMessage()
                TelegramMenuScreen.NOTIFICATIONS -> NotificationMenuMessageRenderer.render(data)
                TelegramMenuScreen.ONLINE -> onlineMessage(data)
                TelegramMenuScreen.EVENTS -> EventHistoryMenuMessageRenderer.render(data)
                TelegramMenuScreen.DAILY -> DailyMenuMessageRenderer.render(data)
                TelegramMenuScreen.ACCOUNT -> AccountMenuMessageRenderer.render(data)
                TelegramMenuScreen.BETS -> BetMenuMessageRenderer.render(data)
                TelegramMenuScreen.PRIME -> PrimeMenuMessageRenderer.render(data)
                TelegramMenuScreen.SETTINGS -> SettingsMenuMessageRenderer.render(data)
            }

        return rawMessage
            .localized(language)
            .withStructuredKeyboard()
    }

    private fun homeMessage(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                """
                🧠 <b>Mnemos</b>

                Основные функции Mnemos доступны прямо в Telegram. Выберите раздел ниже.
                """.trimIndent(),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = homeActions,
        )

    private fun onlineMessage(data: TelegramMenuScreenData): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = renderSaoOnline(data),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = renderSaoOnlineActions(data),
        )

    private fun renderSaoOnline(data: TelegramMenuScreenData): String {
        val onlineData =
            data as? TelegramMenuScreenData.SaoOnline
                ?: return renderSaoOnlineUnavailable()

        return when (val screenData = onlineData.data) {
            is SaoOnlineMenuScreenData.RealmList -> renderSaoRealmList(screenData.snapshot)
            is SaoOnlineMenuScreenData.RealmDetail -> renderSaoRealmDetail(screenData)
            is SaoOnlineMenuScreenData.SearchPrompt -> renderSearchPrompt(screenData)
            is SaoOnlineMenuScreenData.SearchResults -> renderSearchResults(screenData)
            SaoOnlineMenuScreenData.Unavailable -> renderSaoOnlineUnavailable()
        }
    }

    private fun renderSaoRealmList(snapshot: SaoOnlineSnapshot): String {
        val realmLines =
            snapshot.realms
                .joinToString("\n") { realm ->
                    "${escapeHtml(realm.name)} — <b>${realm.playerCount}</b>"
                }.ifBlank {
                    "Нет активных реалмов."
                }
        val generatedAt = onlineTimestampFormatter.format(snapshot.generatedAt)

        return listOf(
            "🌐 <b>Онлайн SAO</b>",
            "",
            "Сейчас отслеживается: <b>${snapshot.trackedOnline}</b>",
            "",
            realmLines,
            "",
            "Обновлено: <code>$generatedAt</code>",
        ).joinToString("\n")
    }

    private fun renderSaoRealmDetail(data: SaoOnlineMenuScreenData.RealmDetail): String {
        val firstPlayerNumber = data.page * ITEMS_PER_PAGE + 1
        val playerLines =
            data.players
                .mapIndexed { index, player ->
                    "${firstPlayerNumber + index}. <code>${escapeHtml(player.nickname)}</code>"
                }.joinToString("\n")
                .ifBlank {
                    "Сейчас на этом реалме никого нет."
                }
        val generatedAt = onlineTimestampFormatter.format(data.generatedAt)
        val totalPages = data.totalPages.coerceAtLeast(1)

        return listOf(
            "🌐 <b>${escapeHtml(data.realmName)}</b>",
            "",
            "Онлайн: <b>${data.playerCount}</b>",
            "Страница: <b>${data.page + 1}/$totalPages</b>",
            "",
            playerLines,
            "",
            "Обновлено: <code>$generatedAt</code>",
        ).joinToString("\n")
    }

    private fun renderSearchPrompt(data: SaoOnlineMenuScreenData.SearchPrompt): String {
        val lines =
            mutableListOf(
                "🔎 <b>Поиск игрока</b>",
                "",
                "Отправьте никнейм или его часть одним сообщением.",
                "От 2 до 32 символов, без пробелов.",
            )

        data.error?.let { error ->
            lines += ""
            lines += "⚠️ ${escapeHtml(error)}"
        }

        return lines.joinToString("\n")
    }

    private fun renderSearchResults(data: SaoOnlineMenuScreenData.SearchResults): String {
        val firstMatchNumber = data.page * ITEMS_PER_PAGE + 1
        val matchLines =
            data.matches
                .mapIndexed { index, match ->
                    val nickname = escapeHtml(match.nickname)
                    val realmName = escapeHtml(match.realmName)

                    "${firstMatchNumber + index}. <code>$nickname</code> — $realmName"
                }.joinToString("\n")
                .ifBlank {
                    "Онлайн-игроки по этому запросу не найдены."
                }
        val generatedAt = onlineTimestampFormatter.format(data.generatedAt)
        val totalPages = data.totalPages.coerceAtLeast(1)

        return listOf(
            "🔎 <b>Поиск игрока</b>",
            "",
            "Запрос: <code>${escapeHtml(data.query)}</code>",
            "Найдено: <b>${data.totalMatches}</b>",
            "Страница: <b>${data.page + 1}/$totalPages</b>",
            "",
            matchLines,
            "",
            "Обновлено: <code>$generatedAt</code>",
        ).joinToString("\n")
    }

    private fun renderSaoOnlineUnavailable(): String =
        listOf(
            "🌐 <b>Онлайн SAO</b>",
            "",
            "Не удалось получить актуальный онлайн. Попробуйте обновить через несколько секунд.",
        ).joinToString("\n")

    private fun renderSaoOnlineActions(data: TelegramMenuScreenData): List<OutboundTelegramMessageAction> {
        val onlineData =
            data as? TelegramMenuScreenData.SaoOnline
                ?: return searchAndRefreshActions + navigationActions

        return when (val screenData = onlineData.data) {
            is SaoOnlineMenuScreenData.RealmList -> renderRealmListActions(screenData.snapshot)
            is SaoOnlineMenuScreenData.RealmDetail -> renderRealmDetailActions(screenData)
            is SaoOnlineMenuScreenData.SearchPrompt -> searchPromptActions
            is SaoOnlineMenuScreenData.SearchResults -> renderSearchResultsActions(screenData)
            SaoOnlineMenuScreenData.Unavailable -> searchAndRefreshActions + navigationActions
        }
    }

    private fun renderRealmListActions(snapshot: SaoOnlineSnapshot): List<OutboundTelegramMessageAction> {
        val realmActions =
            snapshot.realms.map { realm ->
                OutboundTelegramMessageAction.Callback(
                    label = "${realm.name} · ${realm.playerCount}",
                    data = SaoOnlineCallbackData.realm(realm.number),
                    style = OutboundTelegramButtonStyle.PRIMARY,
                )
            }

        return searchAndRefreshActions + realmActions + navigationActions
    }

    private fun renderRealmDetailActions(data: SaoOnlineMenuScreenData.RealmDetail): List<OutboundTelegramMessageAction> =
        searchAndRefreshActions + pageActions(data.page, data.totalPages) + realmListAction + navigationActions

    private fun renderSearchResultsActions(data: SaoOnlineMenuScreenData.SearchResults): List<OutboundTelegramMessageAction> =
        searchAndRefreshActions + pageActions(data.page, data.totalPages) + realmListAction + navigationActions

    private fun pageActions(
        page: Int,
        totalPages: Int,
    ): List<OutboundTelegramMessageAction> {
        val normalizedTotalPages = totalPages.coerceAtLeast(1)
        val actions = mutableListOf<OutboundTelegramMessageAction>()

        if (page > 0) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "← $page/$normalizedTotalPages",
                    data = SaoOnlineCallbackData.PREVIOUS_PAGE,
                )
        }

        if (page + 1 < normalizedTotalPages) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "${page + 2}/$normalizedTotalPages →",
                    data = SaoOnlineCallbackData.NEXT_PAGE,
                )
        }

        return actions
    }

    private fun OutboundTelegramMessage.localized(language: TelegramSettingsLanguage): OutboundTelegramMessage {
        if (language == TelegramSettingsLanguage.RU) {
            return this
        }

        return copy(
            text = BotMenuTranslations.translate(text, language),
            actions = actions.map { action -> action.localized(language) },
            actionRows =
                actionRows.map { row ->
                    row.map { action -> action.localized(language) }
                },
        )
    }

    private fun OutboundTelegramMessageAction.localized(language: TelegramSettingsLanguage): OutboundTelegramMessageAction =
        when (this) {
            is OutboundTelegramMessageAction.Callback ->
                copy(label = BotMenuTranslations.translate(label, language))

            is OutboundTelegramMessageAction.OpenMiniApp ->
                copy(label = BotMenuTranslations.translate(label, language))

            is OutboundTelegramMessageAction.OpenUrl ->
                copy(label = BotMenuTranslations.translate(label, language))
        }

    private fun OutboundTelegramMessage.withStructuredKeyboard(): OutboundTelegramMessage {
        if (actionRows.isNotEmpty() || actions.isEmpty()) {
            return this
        }

        val standaloneRows = mutableListOf<List<OutboundTelegramMessageAction>>()
        val mainActions = mutableListOf<OutboundTelegramMessageAction>()
        val pageActions = mutableListOf<OutboundTelegramMessageAction>()
        val refreshActions = mutableListOf<OutboundTelegramMessageAction>()
        val navigationActions = mutableListOf<OutboundTelegramMessageAction>()

        actions.forEach { action ->
            when {
                action is OutboundTelegramMessageAction.OpenMiniApp ||
                    action is OutboundTelegramMessageAction.OpenUrl -> standaloneRows += listOf(action)

                action.isNavigationAction() -> navigationActions += action
                action.isPaginationAction() -> pageActions += action
                action.isRefreshAction() -> refreshActions += action
                else -> mainActions += action
            }
        }

        val rows =
            buildList {
                addAll(mainActions.adaptiveRows())

                if (pageActions.isNotEmpty()) {
                    add(pageActions)
                }

                if (refreshActions.isNotEmpty()) {
                    add(refreshActions)
                }

                addAll(standaloneRows)

                if (navigationActions.isNotEmpty()) {
                    add(navigationActions)
                }
            }

        return copy(
            actions = emptyList(),
            actionRows = rows,
        )
    }

    private fun OutboundTelegramMessageAction.isNavigationAction(): Boolean =
        this is OutboundTelegramMessageAction.Callback &&
            (data == TelegramMenuScreen.BACK_CALLBACK_DATA || data == TelegramMenuScreen.HOME.callbackData)

    private fun OutboundTelegramMessageAction.isPaginationAction(): Boolean =
        this is OutboundTelegramMessageAction.Callback &&
            (label.contains('←') || label.contains('→'))

    private fun OutboundTelegramMessageAction.isRefreshAction(): Boolean = label.startsWith("🔄")

    private fun List<OutboundTelegramMessageAction>.adaptiveRows(): List<List<OutboundTelegramMessageAction>> {
        if (isEmpty()) {
            return emptyList()
        }

        val result = mutableListOf<List<OutboundTelegramMessageAction>>()
        val buffer = mutableListOf<OutboundTelegramMessageAction>()

        fun flush() {
            if (buffer.isEmpty()) {
                return
            }

            val longestLabel = buffer.maxOf { action -> action.label.length }
            val size =
                when {
                    longestLabel <= 6 -> 5
                    longestLabel <= 12 -> 4
                    longestLabel <= 20 -> 3
                    longestLabel <= 30 -> 2
                    else -> 1
                }

            result += buffer.chunked(size)
            buffer.clear()
        }

        forEach { action ->
            if (action is OutboundTelegramMessageAction.OpenMiniApp || action is OutboundTelegramMessageAction.OpenUrl) {
                flush()
                result += listOf(action)
            } else {
                buffer += action
            }
        }

        flush()

        return result
    }

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private companion object {
        const val ITEMS_PER_PAGE = 10

        val searchAction =
            OutboundTelegramMessageAction.Callback(
                label = "🔎 Найти игрока",
                data = SaoOnlineCallbackData.SEARCH,
                style = OutboundTelegramButtonStyle.PRIMARY,
            )

        val refreshAction =
            OutboundTelegramMessageAction.Callback(
                "🔄 Обновить",
                SaoOnlineCallbackData.REFRESH,
            )

        val searchAndRefreshActions = listOf(searchAction, refreshAction)

        val realmListAction =
            OutboundTelegramMessageAction.Callback(
                "← К реалмам",
                SaoOnlineCallbackData.REALM_LIST,
            )

        val searchPromptActions =
            listOf(
                OutboundTelegramMessageAction.Callback(
                    label = "✖ Отмена",
                    data = SaoOnlineCallbackData.SEARCH_CANCEL,
                    style = OutboundTelegramButtonStyle.DANGER,
                ),
                OutboundTelegramMessageAction.Callback(
                    "⌂ Меню",
                    TelegramMenuScreen.HOME.callbackData,
                ),
            )

        val navigationActions =
            listOf(
                OutboundTelegramMessageAction.Callback(
                    "← Назад",
                    TelegramMenuScreen.BACK_CALLBACK_DATA,
                ),
                OutboundTelegramMessageAction.Callback(
                    "⌂ Меню",
                    TelegramMenuScreen.HOME.callbackData,
                ),
            )

        val homeActions =
            listOf(
                OutboundTelegramMessageAction.Callback(
                    "🔔 Уведомления",
                    TelegramMenuScreen.NOTIFICATIONS.callbackData,
                    OutboundTelegramButtonStyle.PRIMARY,
                ),
                OutboundTelegramMessageAction.Callback(
                    "🌐 Онлайн",
                    TelegramMenuScreen.ONLINE.callbackData,
                    OutboundTelegramButtonStyle.PRIMARY,
                ),
                OutboundTelegramMessageAction.Callback(
                    "🕘 История",
                    TelegramMenuScreen.EVENTS.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "🎁 Daily",
                    TelegramMenuScreen.DAILY.callbackData,
                    OutboundTelegramButtonStyle.SUCCESS,
                ),
                OutboundTelegramMessageAction.Callback(
                    "👤 Аккаунт",
                    TelegramMenuScreen.ACCOUNT.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "🎲 Ставки",
                    TelegramMenuScreen.BETS.callbackData,
                ),
                OutboundTelegramMessageAction.Callback(
                    "💎 PRIME",
                    TelegramMenuScreen.PRIME.callbackData,
                    OutboundTelegramButtonStyle.SUCCESS,
                ),
                OutboundTelegramMessageAction.Callback(
                    "⚙️ Настройки",
                    TelegramMenuScreen.SETTINGS.callbackData,
                ),
                OutboundTelegramMessageAction.OpenMiniApp("↗️ Открыть Mini App"),
            )

        val onlineTimestampFormatter: DateTimeFormatter =
            DateTimeFormatter
                .ofPattern("dd.MM HH:mm 'UTC'")
                .withZone(ZoneOffset.UTC)
    }
}

private object BotMenuTranslations {
    fun translate(
        value: String,
        language: TelegramSettingsLanguage,
    ): String {
        val replacements =
            when (language) {
                TelegramSettingsLanguage.UK -> ukrainian
                TelegramSettingsLanguage.EN -> english
                TelegramSettingsLanguage.RU -> return value
            }

        return replacements.fold(value) { translated, replacement ->
            translated.replace(replacement.first, replacement.second)
        }
    }

    private val ukrainian =
        listOf(
            "Основные функции Mnemos доступны прямо в Telegram. Выберите раздел ниже." to
                "Основні функції Mnemos доступні прямо в Telegram. Оберіть розділ нижче.",
            "Не удалось получить актуальный онлайн. Попробуйте обновить через несколько секунд." to
                "Не вдалося отримати актуальний онлайн. Спробуйте оновити за кілька секунд.",
            "Не удалось получить данные аккаунта. Попробуйте обновить через несколько секунд." to
                "Не вдалося отримати дані акаунта. Спробуйте оновити за кілька секунд.",
            "Не удалось получить состояние Daily. Попробуйте обновить через несколько секунд." to
                "Не вдалося отримати стан Daily. Спробуйте оновити за кілька секунд.",
            "Не удалось получить подписки. Попробуйте обновить через несколько секунд." to
                "Не вдалося отримати підписки. Спробуйте оновити за кілька секунд.",
            "Не удалось загрузить персональные настройки. Попробуйте позже." to
                "Не вдалося завантажити персональні налаштування. Спробуйте пізніше.",
            "Не удалось загрузить варианты пополнения. Попробуйте позже." to
                "Не вдалося завантажити варіанти поповнення. Спробуйте пізніше.",
            "Сервис уведомлений временно недоступен. Попробуйте еще раз." to
                "Сервіс сповіщень тимчасово недоступний. Спробуйте ще раз.",
            "Сервис ставок временно недоступен. Попробуйте обновить." to
                "Сервіс ставок тимчасово недоступний. Спробуйте оновити.",
            "Активных подписок пока нет." to "Активних підписок поки немає.",
            "Для удаления нажмите ✖ с номером подписки." to "Для видалення натисніть ✖ з номером підписки.",
            "Выберите тип события, о котором хотите получать уведомления." to
                "Оберіть тип події, про яку хочете отримувати сповіщення.",
            "Укажите хотя бы один фильтр. Можно использовать оба одновременно." to
                "Вкажіть щонайменше один фільтр. Можна використати обидва одночасно.",
            "Если ничего не выбрано, уведомления будут приходить для рейда на любой локации." to
                "Якщо нічого не вибрано, сповіщення надходитимуть для рейду на будь-якій локації.",
            "Cristalix-аккаунт ещё не подключён." to "Cristalix-акаунт ще не підключено.",
            "Его можно полностью настроить прямо здесь, без Mini App." to
                "Його можна повністю налаштувати прямо тут, без Mini App.",
            "Для Daily нужен активный Cristalix-аккаунт." to "Для Daily потрібен активний Cristalix-акаунт.",
            "Для ставок нужен активный Cristalix-аккаунт Mnemos." to "Для ставок потрібен активний Cristalix-акаунт Mnemos.",
            "Создать аккаунт или присоединиться по приглашению можно прямо в боте." to
                "Створити акаунт або приєднатися за запрошенням можна прямо в боті.",
            "Пополнение PRIME через Telegram Stars." to "Поповнення PRIME через Telegram Stars.",
            "Нажмите кнопку оплаты ниже." to "Натисніть кнопку оплати нижче.",
            "Отправьте никнейм или его часть одним сообщением." to "Надішліть нікнейм або його частину одним повідомленням.",
            "Онлайн-игроки по этому запросу не найдены." to "Онлайн-гравців за цим запитом не знайдено.",
            "Сейчас на этом реалме никого нет." to "Зараз на цьому реалмі нікого немає.",
            "Нет активных реалмов." to "Немає активних реалмів.",
            "Событий по выбранным условиям пока нет." to "Подій за вибраними умовами поки немає.",
            "Отправьте название предмета, никнейм или часть запроса одним сообщением." to
                "Надішліть назву предмета, нікнейм або частину запиту одним повідомленням.",
            "Сейчас нет доступных раундов событий." to "Зараз немає доступних раундів подій.",
            "Сейчас нет активных item race." to "Зараз немає активних item race.",
            "История ставок пока пустая." to "Історія ставок поки порожня.",
            "Отправьте сумму в COIN одним сообщением." to "Надішліть суму в COIN одним повідомленням.",
            "Отправьте сумму COIN, на которую хотите увеличить свою позицию." to
                "Надішліть суму COIN, на яку хочете збільшити свою позицію.",
            "Операция подтверждена. Баланс и позиция обновлены." to "Операцію підтверджено. Баланс і позицію оновлено.",
            "Транзакция не прошла. Ставка не была подтверждена." to "Транзакція не пройшла. Ставку не підтверджено.",
            "Операция ещё обрабатывается. Нажмите «Обновить», чтобы проверить статус." to
                "Операція ще обробляється. Натисніть «Оновити», щоб перевірити статус.",
            "Подписка активирована." to "Підписку активовано.",
            "Активный аккаунт переключён." to "Активний акаунт перемкнено.",
            "Cristalix-аккаунт создан и выбран активным." to "Cristalix-акаунт створено та вибрано активним.",
            "Вы присоединились к аккаунту, он выбран активным." to "Ви приєдналися до акаунта, його вибрано активним.",
            "Настройка сохранена." to "Налаштування збережено.",
            "Награда получена" to "Нагороду отримано",
            "награда получена" to "нагороду отримано",
            "награда доступна" to "нагорода доступна",
            "Можно восстановить серию" to "Можна відновити серію",
            "Восстановлений в этом месяце" to "Відновлень цього місяця",
            "Следующая награда" to "Наступна нагорода",
            "Активных подписок" to "Активних підписок",
            "Доступно аккаунтов" to "Доступно акаунтів",
            "Активный" to "Активний",
            "владелец" to "власник",
            "участник" to "учасник",
            "деактивирован" to "деактивовано",
            "Баланс" to "Баланс",
            "Страница" to "Сторінка",
            "Сейчас отслеживается" to "Зараз відстежується",
            "Обновлено" to "Оновлено",
            "Найдено" to "Знайдено",
            "Запрос" to "Запит",
            "Фильтр" to "Фільтр",
            "Сортировка" to "Сортування",
            "Редкость" to "Рідкість",
            "Тип предмета" to "Тип предмета",
            "Локация" to "Локація",
            "Текущий поиск" to "Поточний пошук",
            "Поиск" to "Пошук",
            "Сначала новые" to "Спочатку нові",
            "Сначала старые" to "Спочатку старі",
            "Любая локация" to "Будь-яка локація",
            "Любая редкость" to "Будь-яка рідкість",
            "Любой тип" to "Будь-який тип",
            "Любой бустер" to "Будь-який бустер",
            "Любое событие" to "Будь-яка подія",
            "Любой вайп" to "Будь-який вайп",
            "Текущий" to "Поточний",
            "Предыдущий" to "Попередній",
            "Глобальные" to "Глобальні",
            "Рейды" to "Рейди",
            "Бустеры" to "Бустери",
            "Дропы" to "Дропи",
            "Все" to "Усі",
            "События" to "Події",
            "Событие" to "Подія",
            "История ставок" to "Історія ставок",
            "История событий" to "Історія подій",
            "Уведомления" to "Сповіщення",
            "Новая подписка" to "Нова підписка",
            "Подписка на дроп" to "Підписка на дроп",
            "Подписка на бустер" to "Підписка на бустер",
            "Подписка на данж" to "Підписка на данж",
            "Подписка на рейд" to "Підписка на рейд",
            "Подписка на глобальное событие" to "Підписка на глобальну подію",
            "Глобальное" to "Глобальна",
            "Аккаунт" to "Акаунт",
            "Настройки" to "Налаштування",
            "Язык" to "Мова",
            "Пак уведомлений" to "Пак сповіщень",
            "Не выбран" to "Не вибрано",
            "Недоступен" to "Недоступно",
            "Поиск игрока" to "Пошук гравця",
            "Онлайн" to "Онлайн",
            "Обновить" to "Оновити",
            "Найти игрока" to "Знайти гравця",
            "К реалмам" to "До реалмів",
            "Назад" to "Назад",
            "Меню" to "Меню",
            "Открыть Mini App" to "Відкрити Mini App",
            "Добавить подписку" to "Додати підписку",
            "Создать" to "Створити",
            "Присоединиться" to "Приєднатися",
            "Настроить аккаунт" to "Налаштувати акаунт",
            "Забрать награду" to "Забрати нагороду",
            "Восстановить серию" to "Відновити серію",
            "Отмена" to "Скасувати",
            "Отменить ввод" to "Скасувати введення",
            "Сбросить поиск" to "Скинути пошук",
            "Дальше" to "Далі",
            "Луна" to "Місяць",
            "Затмение" to "Затемнення",
            "Взрыв" to "Вибух",
            "Тьма" to "Темрява",
            "Удача" to "Удача",
            "Деньги" to "Гроші",
            "Урон" to "Шкода",
            "Сила" to "Сила",
            "Легкий" to "Легкий",
            "Средний" to "Середній",
            "Сложный" to "Складний",
            "Эксперт" to "Експерт",
            "Да" to "Так",
            "Нет" to "Ні",
            "Увеличить" to "Збільшити",
            "Вступить" to "Вступити",
            "Время" to "Час",
            "Закрытие" to "Закриття",
            "Результат" to "Результат",
            "Моя ставка" to "Моя ставка",
            "Моя позиция" to "Моя позиція",
            "Статус" to "Статус",
            "Вход" to "Вхід",
            "комиссия" to "комісія",
            "Регистрация до" to "Реєстрація до",
            "Старт" to "Старт",
            "ожидает" to "очікує",
        ).sortedByDescending { replacement -> replacement.first.length }

    private val english =
        listOf(
            "Основные функции Mnemos доступны прямо в Telegram. Выберите раздел ниже." to
                "Mnemos core features are available directly in Telegram. Choose a section below.",
            "Не удалось получить актуальный онлайн. Попробуйте обновить через несколько секунд." to
                "Could not load current online data. Try refreshing in a few seconds.",
            "Не удалось получить данные аккаунта. Попробуйте обновить через несколько секунд." to
                "Could not load account data. Try refreshing in a few seconds.",
            "Не удалось получить состояние Daily. Попробуйте обновить через несколько секунд." to
                "Could not load Daily status. Try refreshing in a few seconds.",
            "Не удалось получить подписки. Попробуйте обновить через несколько секунд." to
                "Could not load subscriptions. Try refreshing in a few seconds.",
            "Не удалось загрузить персональные настройки. Попробуйте позже." to
                "Could not load personal settings. Try again later.",
            "Не удалось загрузить варианты пополнения. Попробуйте позже." to
                "Could not load purchase options. Try again later.",
            "Сервис уведомлений временно недоступен. Попробуйте еще раз." to
                "Notification service is temporarily unavailable. Try again.",
            "Сервис ставок временно недоступен. Попробуйте обновить." to
                "Bet service is temporarily unavailable. Try refreshing.",
            "Активных подписок пока нет." to "There are no active subscriptions yet.",
            "Для удаления нажмите ✖ с номером подписки." to "To delete one, press ✖ with its subscription number.",
            "Выберите тип события, о котором хотите получать уведомления." to
                "Choose the event type you want to be notified about.",
            "Укажите хотя бы один фильтр. Можно использовать оба одновременно." to
                "Set at least one filter. You can use both together.",
            "Если ничего не выбрано, уведомления будут приходить для рейда на любой локации." to
                "With no locations selected, raid notifications apply to every location.",
            "Cristalix-аккаунт ещё не подключён." to "No Cristalix account is connected yet.",
            "Его можно полностью настроить прямо здесь, без Mini App." to
                "You can configure it completely here without opening the Mini App.",
            "Для Daily нужен активный Cristalix-аккаунт." to "Daily requires an active Cristalix account.",
            "Для ставок нужен активный Cristalix-аккаунт Mnemos." to "Bets require an active Mnemos Cristalix account.",
            "Создать аккаунт или присоединиться по приглашению можно прямо в боте." to
                "You can create an account or join by invite directly in the bot.",
            "Пополнение PRIME через Telegram Stars." to "Top up PRIME with Telegram Stars.",
            "Нажмите кнопку оплаты ниже." to "Use the payment button below.",
            "Отправьте никнейм или его часть одним сообщением." to "Send a nickname or part of it in one message.",
            "Онлайн-игроки по этому запросу не найдены." to "No online players matched this query.",
            "Сейчас на этом реалме никого нет." to "There is nobody on this realm right now.",
            "Нет активных реалмов." to "There are no active realms.",
            "Событий по выбранным условиям пока нет." to "No events match the selected filters yet.",
            "Отправьте название предмета, никнейм или часть запроса одним сообщением." to
                "Send an item name, nickname, or part of the query in one message.",
            "Сейчас нет доступных раундов событий." to "There are no available event rounds right now.",
            "Сейчас нет активных item race." to "There are no active item races right now.",
            "История ставок пока пустая." to "Bet history is empty.",
            "Отправьте сумму в COIN одним сообщением." to "Send the COIN amount in one message.",
            "Отправьте сумму COIN, на которую хотите увеличить свою позицию." to
                "Send the COIN amount you want to add to your position.",
            "Операция подтверждена. Баланс и позиция обновлены." to "Operation confirmed. Balance and position were updated.",
            "Транзакция не прошла. Ставка не была подтверждена." to "Transaction failed. The bet was not confirmed.",
            "Операция ещё обрабатывается. Нажмите «Обновить», чтобы проверить статус." to
                "The operation is still processing. Press “Refresh” to check its status.",
            "Подписка активирована." to "Subscription enabled.",
            "Активный аккаунт переключён." to "Active account switched.",
            "Cristalix-аккаунт создан и выбран активным." to "Cristalix account created and selected as active.",
            "Вы присоединились к аккаунту, он выбран активным." to "You joined the account and it is now active.",
            "Настройка сохранена." to "Setting saved.",
            "награда получена" to "reward claimed",
            "награда доступна" to "reward available",
            "Можно восстановить серию" to "You can restore streak",
            "Восстановлений в этом месяце" to "Restores this month",
            "Следующая награда" to "Next reward",
            "Активных подписок" to "Active subscriptions",
            "Доступно аккаунтов" to "Available accounts",
            "Активный" to "Active",
            "владелец" to "owner",
            "участник" to "member",
            "деактивирован" to "deactivated",
            "Страница" to "Page",
            "Сейчас отслеживается" to "Tracked online",
            "Обновлено" to "Updated",
            "Найдено" to "Found",
            "Запрос" to "Query",
            "Фильтр" to "Filter",
            "Сортировка" to "Sort",
            "Редкость" to "Rarity",
            "Тип предмета" to "Item type",
            "Локация" to "Location",
            "Текущий поиск" to "Current search",
            "Поиск" to "Search",
            "Сначала новые" to "Newest first",
            "Сначала старые" to "Oldest first",
            "Любая локация" to "Any location",
            "Любая редкость" to "Any rarity",
            "Любой тип" to "Any type",
            "Любой бустер" to "Any booster",
            "Любое событие" to "Any event",
            "Любой вайп" to "Any wipe",
            "Текущий" to "Current",
            "Предыдущий" to "Previous",
            "Глобальные" to "Global",
            "Рейды" to "Raids",
            "Бустеры" to "Boosters",
            "Дропы" to "Drops",
            "Все" to "All",
            "События" to "Events",
            "Событие" to "Event",
            "История ставок" to "Bet history",
            "История событий" to "Event history",
            "Уведомления" to "Notifications",
            "Новая подписка" to "New subscription",
            "Подписка на дроп" to "Drop subscription",
            "Подписка на бустер" to "Booster subscription",
            "Подписка на данж" to "Dungeon subscription",
            "Подписка на рейд" to "Raid subscription",
            "Подписка на глобальное событие" to "Global event subscription",
            "Глобальное" to "Global",
            "Аккаунт" to "Account",
            "Настройки" to "Settings",
            "Язык" to "Language",
            "Пак уведомлений" to "Notification pack",
            "Не выбран" to "Not selected",
            "Недоступен" to "Unavailable",
            "Поиск игрока" to "Player search",
            "Онлайн" to "Online",
            "Обновить" to "Refresh",
            "Найти игрока" to "Find player",
            "К реалмам" to "Realms",
            "Назад" to "Back",
            "Меню" to "Menu",
            "Открыть Mini App" to "Open Mini App",
            "Добавить подписку" to "Add subscription",
            "Создать" to "Create",
            "Присоединиться" to "Join",
            "Настроить аккаунт" to "Set up account",
            "Забрать награду" to "Claim reward",
            "Восстановить серию" to "Restore streak",
            "Отмена" to "Cancel",
            "Отменить ввод" to "Cancel input",
            "Сбросить поиск" to "Clear search",
            "Дальше" to "Next",
            "Луна" to "Moon",
            "Затмение" to "Eclipse",
            "Взрыв" to "Explosion",
            "Тьма" to "Darkness",
            "Удача" to "Luck",
            "Деньги" to "Money",
            "Урон" to "Damage",
            "Сила" to "Power",
            "Легкий" to "Easy",
            "Средний" to "Medium",
            "Сложный" to "Hard",
            "Эксперт" to "Expert",
            "Да" to "Yes",
            "Нет" to "No",
            "Увеличить" to "Increase",
            "Вступить" to "Join",
            "Время" to "Time",
            "Закрытие" to "Closes",
            "Результат" to "Result",
            "Моя ставка" to "My bet",
            "Моя позиция" to "My position",
            "Статус" to "Status",
            "Вход" to "Entry",
            "комиссия" to "fee",
            "Регистрация до" to "Registration closes",
            "Старт" to "Start",
            "ожидает" to "pending",
        ).sortedByDescending { replacement -> replacement.first.length }
}
