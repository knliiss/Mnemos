package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.command.help.TelegramHelpSection
import com.mnemos.telegram.bot.application.command.port.out.TelegramCommandMessageRenderer
import com.mnemos.telegram.bot.application.command.start.TelegramStartContext
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import org.springframework.stereotype.Component

@Component
class LocalTelegramCommandMessageRenderer : TelegramCommandMessageRenderer {
    override fun renderStart(context: TelegramStartContext): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                "👋 <b>Добро пожаловать в Mnemos</b>\n\n" +
                    "Mnemos помогает следить за событиями SAO и управлять связанным Cristalix-аккаунтом из Telegram.\n\n" +
                    "• настраивайте подписки на рейды, дропы, бустеры и другие события;\n" +
                    "• смотрите ленту событий и статистику;\n" +
                    "• управляйте аккаунтами, наградами и персонализацией.\n\n" +
                    "<b>Как открыть Mini App?</b>\n" +
                    "Нажмите «Открыть Mnemos» ниже. Telegram откроет приложение как WebApp и передаст данные, " +
                    "необходимые для безопасного входа.\n\n" +
                    "Если что-то не работает или непонятно — используйте /help.",
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions =
                listOf(
                    OutboundTelegramMessageAction.OpenMiniApp("Открыть Mnemos"),
                    OutboundTelegramMessageAction.Callback(
                        label = "Помощь",
                        data = TelegramHelpSection.OVERVIEW.callbackData,
                    ),
                ),
        )

    override fun renderHelp(section: TelegramHelpSection): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = renderHelpText(section),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = renderHelpActions(section),
        )

    private fun renderHelpText(section: TelegramHelpSection): String =
        when (section) {
            TelegramHelpSection.OVERVIEW ->
                """
                ❓ <b>Помощь Mnemos</b>

                <b>Что такое Mnemos?</b>
                Telegram Mini App для событий SAO: подписки, лента событий, Cristalix-аккаунты, награды, ставки и персонализация.

                <b>С чего начать?</b>
                Нажмите «Открыть Mnemos», затем создайте или выберите Cristalix-аккаунт. После этого настройте нужные уведомления.

                <b>Где искать подробности?</b>
                Выберите раздел кнопками ниже. Сообщение обновится без создания новых сообщений.
                """.trimIndent()

            TelegramHelpSection.MINI_APP ->
                """
                📱 <b>Mini App</b>

                <b>Как правильно открыть Mnemos?</b>
                Используйте кнопку «Открыть Mnemos» в боте или стандартную кнопку Mini App Telegram. Не открывайте адрес приложения как обычную страницу браузера.

                <b>Почему это важно?</b>
                При WebApp-запуске Telegram передаёт подписанные данные авторизации. По ним Mnemos понимает, кто открыл приложение.

                <b>Появилось сообщение, что страница открыта не из Telegram. Что делать?</b>
                Закройте страницу и заново откройте Mnemos кнопкой в этом боте. Если приложение долго было открыто, обычное повторное открытие также обновит Telegram-контекст.
                """.trimIndent()

            TelegramHelpSection.SUBSCRIPTIONS ->
                """
                🔔 <b>Подписки</b>

                <b>На что можно подписаться?</b>
                На дропы предметов, рейды по локациям, бустеры, данжи, глобальные события и выход выбранных игроков из сети.

                <b>Как работают рейды?</b>
                У пользователя одна рейд-подписка. В ней можно включать и выключать нужные локации — Mnemos обновляет существующую подписку, а не создаёт новую на каждый набор локаций.

                <b>Как работает подписка на игрока?</b>
                Mnemos создаёт её только если прямо сейчас может достоверно проверить, онлайн игрок или офлайн. При недоступном или скрытом статусе подписка не создаётся.
                """.trimIndent()

            TelegramHelpSection.ACCOUNTS ->
                """
                👤 <b>Аккаунты</b>

                <b>Что такое Account в Mnemos?</b>
                Это связанный Cristalix-аккаунт. Один пользователь Mnemos может состоять в нескольких аккаунтах, но активным одновременно выбирается один.

                <b>Что происходит после деактивации?</b>
                Деактивированный аккаунт нельзя использовать как активный. Его владелец может снова активировать аккаунт, повторно зарегистрировав тот же Cristalix-профиль.

                <b>Почему я не вижу функции конкретного аккаунта?</b>
                Проверьте, что выбран правильный активный аккаунт и что у вас есть необходимые права внутри него.
                """.trimIndent()

            TelegramHelpSection.NOTIFICATIONS ->
                """
                ✉️ <b>Уведомления</b>

                <b>Почему бот ничего не присылает?</b>
                Проверьте подписки в Mini App и убедитесь, что бот не заблокирован в Telegram.

                <b>Я разблокировал бота. Нужно что-то ещё?</b>
                Отправьте /start или /help. Mnemos снова отметит Telegram-получателя активным.

                <b>Что меняет notification pack?</b>
                Пак меняет оформление поддерживаемых уведомлений, включая уведомление о выходе отслеживаемого игрока из сети.
                """.trimIndent()

            TelegramHelpSection.TROUBLESHOOTING ->
                """
                🛠 <b>Проблемы и решения</b>

                <b>Mini App временно недоступен.</b>
                Это обычно означает, что один из сервисов Mnemos сейчас не отвечает. Повторите открытие позже — не нужно очищать Telegram или вручную подменять ссылку.

                <b>После долгого простоя появилась ошибка авторизации.</b>
                Закройте Mini App и откройте его снова через Telegram. Приложение должно получить свежий WebApp-контекст и восстановить сессию.

                <b>Настройки изменились, но старое состояние ещё видно.</b>
                Обновите соответствующий экран или переоткройте Mini App. Если проблема повторяется, сообщите, какой экран и действие её вызывают.
                """.trimIndent()
        }

    private fun renderHelpActions(section: TelegramHelpSection): List<OutboundTelegramMessageAction> =
        buildList {
            add(OutboundTelegramMessageAction.OpenMiniApp("Открыть Mnemos"))

            helpSections.forEach { (helpSection, label) ->
                add(
                    OutboundTelegramMessageAction.Callback(
                        label = if (helpSection == section) "• $label" else label,
                        data = helpSection.callbackData,
                    ),
                )
            }
        }

    private companion object {
        val helpSections =
            listOf(
                TelegramHelpSection.OVERVIEW to "Обзор",
                TelegramHelpSection.MINI_APP to "Mini App",
                TelegramHelpSection.SUBSCRIPTIONS to "Подписки",
                TelegramHelpSection.ACCOUNTS to "Аккаунты",
                TelegramHelpSection.NOTIFICATIONS to "Уведомления",
                TelegramHelpSection.TROUBLESHOOTING to "Проблемы",
            )
    }
}
