package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage

object BotCommandTranslations {
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
            translation(
                source =
                    "Основными функциями Mnemos можно пользоваться прямо в этом чате — " +
                        "Mini App для этого не нужен.",
                target =
                    "Основними функціями Mnemos можна користуватися прямо в цьому чаті — " +
                        "Mini App для цього не потрібен.",
            ),
            translation(
                source =
                    "Нажмите «Меню Mnemos» или используйте /menu. Mini App ниже остаётся " +
                        "дополнительным интерфейсом, если он доступен в вашем регионе.",
                target =
                    "Натисніть «Меню Mnemos» або використайте /menu. Mini App нижче залишається " +
                        "додатковим інтерфейсом, якщо він доступний у вашому регіоні.",
            ),
            translation(
                source =
                    "Откройте /menu. Основные функции Mnemos работают прямо в Telegram-чате, " +
                        "включая настройку Cristalix-аккаунта.",
                target =
                    "Відкрийте /menu. Основні функції Mnemos працюють прямо в Telegram-чаті, " +
                        "включно з налаштуванням Cristalix-акаунта.",
            ),
            translation(
                source =
                    "Нет для основных сценариев. Он остаётся дополнительным визуальным интерфейсом " +
                        "и не должен блокировать использование Mnemos, если недоступен в вашем регионе.",
                target =
                    "Ні для основних сценаріїв. Він залишається додатковим візуальним інтерфейсом " +
                        "і не повинен блокувати використання Mnemos, якщо недоступний у вашому регіоні.",
            ),
            translation(
                source =
                    "Mini App — дополнительный интерфейс Mnemos. Если он открывается, используйте " +
                        "кнопку «Mini App» или стандартную кнопку Telegram.",
                target =
                    "Mini App — додатковий інтерфейс Mnemos. Якщо він відкривається, використовуйте " +
                        "кнопку «Mini App» або стандартну кнопку Telegram.",
            ),
            translation(
                source =
                    "Если Mini App не загружается из-за сети или региональных ограничений, " +
                        "продолжайте пользоваться /menu: аккаунт, уведомления, Daily, события, ставки, " +
                        "PRIME и настройки доступны через чат.",
                target =
                    "Якщо Mini App не завантажується через мережу або регіональні обмеження, " +
                        "продовжуйте користуватися /menu: акаунт, сповіщення, Daily, події, ставки, " +
                        "PRIME і налаштування доступні через чат.",
            ),
            translation(
                source =
                    "Откройте /notifications. Управление подписками повторяет Mini App: глобальные " +
                        "события, бустеры и данжи переключаются как настройки, RAID редактируется как " +
                        "одна подписка, а правила дропа управляются отдельно.",
                target =
                    "Відкрийте /notifications. Керування підписками повторює Mini App: глобальні події, " +
                        "бустери й данжі перемикаються як налаштування, RAID редагується як одна підписка, " +
                        "а правила дропа керуються окремо.",
            ),
            "Для фильтров по игроку или предмету бот сам запросит значение обычным сообщением." to
                "Для фільтрів за гравцем або предметом бот сам запросить значення звичайним повідомленням.",
            translation(
                source =
                    "Откройте /account. Там можно создать Mnemos/Cristalix-аккаунт, присоединиться " +
                        "по invite-коду, посмотреть доступные аккаунты и переключить активный.",
                target =
                    "Відкрийте /account. Там можна створити Mnemos/Cristalix-акаунт, приєднатися " +
                        "за invite-кодом, переглянути доступні акаунти й перемкнути активний.",
            ),
            "Daily, ставки и другие account-scoped функции используют выбранный активный аккаунт и его права." to
                "Daily, ставки та інші account-scoped функції використовують вибраний активний акаунт і його права.",
            "Откройте /bets. Бот показывает текущие раунды, коэффициенты и вашу ставку." to
                "Відкрийте /bets. Бот показує поточні раунди, коефіцієнти та вашу ставку.",
            translation(
                source =
                    "Выберите «Да» или «Нет», отправьте сумму COIN обычным сообщением. Уже созданную " +
                        "ставку можно увеличить. История доступна в этом же разделе.",
                target =
                    "Оберіть «Так» або «Ні», надішліть суму COIN звичайним повідомленням. Уже створену " +
                        "ставку можна збільшити. Історія доступна в цьому ж розділі.",
            ),
            translation(
                source =
                    "Для операции нужен активный аккаунт и право PLACE_BETS. Все ставки проходят " +
                        "через обычный Mnemos wallet/idempotency flow.",
                target =
                    "Для операції потрібен активний акаунт і право PLACE_BETS. Усі ставки проходять " +
                        "через звичайний Mnemos wallet/idempotency flow.",
            ),
            translation(
                source =
                    "Если бот был заблокирован, разблокируйте его и отправьте /start или /help — " +
                        "Mnemos снова активирует Telegram-получателя.",
                target =
                    "Якщо бот був заблокований, розблокуйте його й надішліть /start або /help — " +
                        "Mnemos знову активує Telegram-отримувача.",
            ),
            "Notification pack и доступные пользовательские настройки можно менять через /settings." to
                "Notification pack і доступні користувацькі налаштування можна змінювати через /settings.",
            "Используйте /menu. Для основных функций это не является блокирующей ошибкой." to
                "Використовуйте /menu. Для основних функцій це не є блокувальною помилкою.",
            translation(
                source =
                    "Нажмите «Обновить». Если соответствующий backend-сервис не отвечает, бот покажет " +
                        "локальную ошибку без требования открыть Mini App.",
                target =
                    "Натисніть «Оновити». Якщо відповідний backend-сервіс не відповідає, бот покаже " +
                        "локальну помилку без вимоги відкрити Mini App.",
            ),
            "Перейдите в /account и создайте, подключите или переключите его прямо в Telegram." to
                "Перейдіть у /account і створіть, підключіть або перемкніть його прямо в Telegram.",
            "• создавайте и подключайте Cristalix-аккаунты;" to "• створюйте та підключайте Cristalix-акаунти;",
            "• настраивайте уведомления и смотрите онлайн/историю событий;" to
                "• налаштовуйте сповіщення та переглядайте онлайн/історію подій;",
            "• забирайте Daily и управляйте балансами;" to "• забирайте Daily та керуйте балансами;",
            "• делайте ставки и покупайте PRIME через Telegram Stars;" to
                "• робіть ставки та купуйте PRIME через Telegram Stars;",
            "• меняйте доступные настройки Mnemos." to "• змінюйте доступні налаштування Mnemos.",
            "Mini App не открывается или не загружается." to "Mini App не відкривається або не завантажується.",
            "Отдельный раздел бота временно недоступен." to "Окремий розділ бота тимчасово недоступний.",
            "Нужен активный Cristalix-аккаунт." to "Потрібен активний Cristalix-акаунт.",
            "Добро пожаловать в Mnemos" to "Ласкаво просимо до Mnemos",
            "Проблемы и решения" to "Проблеми та рішення",
            "С чего начать?" to "З чого почати?",
            "Основные команды" to "Основні команди",
            "Нужен ли Mini App?" to "Чи потрібен Mini App?",
            "Помощь Mnemos" to "Допомога Mnemos",
            "Меню Mnemos" to "Меню Mnemos",
            "Помощь" to "Допомога",
            "Обзор" to "Огляд",
            "Подписки" to "Підписки",
            "Аккаунты" to "Акаунти",
            "Ставки" to "Ставки",
            "Уведомления" to "Сповіщення",
            "Проблемы" to "Проблеми",
        ).sortedByDescending { replacement -> replacement.first.length }

    private val english =
        listOf(
            translation(
                source =
                    "Основными функциями Mnemos можно пользоваться прямо в этом чате — " +
                        "Mini App для этого не нужен.",
                target =
                    "You can use Mnemos core features directly in this chat — " +
                        "the Mini App is not required.",
            ),
            translation(
                source =
                    "Нажмите «Меню Mnemos» или используйте /menu. Mini App ниже остаётся " +
                        "дополнительным интерфейсом, если он доступен в вашем регионе.",
                target =
                    "Press “Mnemos Menu” or use /menu. The Mini App below remains an optional interface " +
                        "when it is available in your region.",
            ),
            translation(
                source =
                    "Откройте /menu. Основные функции Mnemos работают прямо в Telegram-чате, " +
                        "включая настройку Cristalix-аккаунта.",
                target =
                    "Open /menu. Mnemos core features work directly in Telegram chat, " +
                        "including Cristalix account setup.",
            ),
            translation(
                source =
                    "Нет для основных сценариев. Он остаётся дополнительным визуальным интерфейсом " +
                        "и не должен блокировать использование Mnemos, если недоступен в вашем регионе.",
                target =
                    "Not for core flows. It remains an optional visual interface and should not block " +
                        "Mnemos when unavailable in your region.",
            ),
            translation(
                source =
                    "Mini App — дополнительный интерфейс Mnemos. Если он открывается, используйте " +
                        "кнопку «Mini App» или стандартную кнопку Telegram.",
                target =
                    "The Mini App is an optional Mnemos interface. If it opens, use the “Mini App” button " +
                        "or Telegram's standard app button.",
            ),
            translation(
                source =
                    "Если Mini App не загружается из-за сети или региональных ограничений, " +
                        "продолжайте пользоваться /menu: аккаунт, уведомления, Daily, события, ставки, " +
                        "PRIME и настройки доступны через чат.",
                target =
                    "If the Mini App cannot load because of network or regional restrictions, keep using " +
                        "/menu: account, notifications, Daily, events, bets, PRIME, and settings are available in chat.",
            ),
            translation(
                source =
                    "Откройте /notifications. Управление подписками повторяет Mini App: глобальные " +
                        "события, бустеры и данжи переключаются как настройки, RAID редактируется как " +
                        "одна подписка, а правила дропа управляются отдельно.",
                target =
                    "Open /notifications. Subscription controls match the Mini App: global events, " +
                        "boosters, and dungeons are toggles, RAID is edited as one subscription, and drop " +
                        "rules are managed separately.",
            ),
            "Для фильтров по игроку или предмету бот сам запросит значение обычным сообщением." to
                "For player or item filters, the bot will ask for the value in a regular message.",
            translation(
                source =
                    "Откройте /account. Там можно создать Mnemos/Cristalix-аккаунт, присоединиться " +
                        "по invite-коду, посмотреть доступные аккаунты и переключить активный.",
                target =
                    "Open /account. You can create a Mnemos/Cristalix account, join with an invite code, " +
                        "view available accounts, and switch the active one.",
            ),
            "Daily, ставки и другие account-scoped функции используют выбранный активный аккаунт и его права." to
                "Daily, bets, and other account-scoped features use the selected active account and its permissions.",
            "Откройте /bets. Бот показывает текущие раунды, коэффициенты и вашу ставку." to
                "Open /bets. The bot shows current rounds, odds, and your bet.",
            translation(
                source =
                    "Выберите «Да» или «Нет», отправьте сумму COIN обычным сообщением. Уже созданную " +
                        "ставку можно увеличить. История доступна в этом же разделе.",
                target =
                    "Choose “Yes” or “No” and send the COIN amount in a regular message. An existing bet " +
                        "can be increased. History is available in the same section.",
            ),
            translation(
                source =
                    "Для операции нужен активный аккаунт и право PLACE_BETS. Все ставки проходят " +
                        "через обычный Mnemos wallet/idempotency flow.",
                target =
                    "The operation requires an active account and PLACE_BETS permission. All bets use " +
                        "the standard Mnemos wallet/idempotency flow.",
            ),
            translation(
                source =
                    "Если бот был заблокирован, разблокируйте его и отправьте /start или /help — " +
                        "Mnemos снова активирует Telegram-получателя.",
                target =
                    "If the bot was blocked, unblock it and send /start or /help — Mnemos will reactivate " +
                        "the Telegram recipient.",
            ),
            "Notification pack и доступные пользовательские настройки можно менять через /settings." to
                "The notification pack and available user settings can be changed through /settings.",
            "Используйте /menu. Для основных функций это не является блокирующей ошибкой." to
                "Use /menu. This is not a blocking error for core features.",
            translation(
                source =
                    "Нажмите «Обновить». Если соответствующий backend-сервис не отвечает, бот покажет " +
                        "локальную ошибку без требования открыть Mini App.",
                target =
                    "Press “Refresh”. If the relevant backend service is unavailable, the bot will show " +
                        "a local error without requiring the Mini App.",
            ),
            "Перейдите в /account и создайте, подключите или переключите его прямо в Telegram." to
                "Open /account and create, connect, or switch it directly in Telegram.",
            "• создавайте и подключайте Cristalix-аккаунты;" to "• create and connect Cristalix accounts;",
            "• настраивайте уведомления и смотрите онлайн/историю событий;" to
                "• configure notifications and view online/event history;",
            "• забирайте Daily и управляйте балансами;" to "• claim Daily and manage balances;",
            "• делайте ставки и покупайте PRIME через Telegram Stars;" to
                "• place bets and buy PRIME with Telegram Stars;",
            "• меняйте доступные настройки Mnemos." to "• change available Mnemos settings.",
            "Mini App не открывается или не загружается." to "The Mini App does not open or load.",
            "Отдельный раздел бота временно недоступен." to "A bot section is temporarily unavailable.",
            "Нужен активный Cristalix-аккаунт." to "An active Cristalix account is required.",
            "Добро пожаловать в Mnemos" to "Welcome to Mnemos",
            "Проблемы и решения" to "Troubleshooting",
            "С чего начать?" to "Getting started",
            "Основные команды" to "Main commands",
            "Нужен ли Mini App?" to "Is the Mini App required?",
            "Помощь Mnemos" to "Mnemos Help",
            "Меню Mnemos" to "Mnemos Menu",
            "Помощь" to "Help",
            "Обзор" to "Overview",
            "Подписки" to "Subscriptions",
            "Аккаунты" to "Accounts",
            "Ставки" to "Bets",
            "Уведомления" to "Notifications",
            "Проблемы" to "Problems",
        ).sortedByDescending { replacement -> replacement.first.length }

    private fun translation(
        source: String,
        target: String,
    ): Pair<String, String> = source to target
}
