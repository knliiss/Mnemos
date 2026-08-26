package com.mnemos.notification.adapter.out.message.local

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.delivery.model.MessageAction
import com.mnemos.notification.application.notification.delivery.model.RenderedMessage
import com.mnemos.notification.application.notification.delivery.port.out.NotificationMessageRenderer
import com.mnemos.notification.application.notification.model.NotificationItem
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.port.out.ActiveNotificationPackRepository
import com.mnemos.notification.application.telegram.help.TelegramHelpSection
import com.mnemos.notification.application.telegram.start.TelegramStartContext
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(
    prefix = "mnemos.notification.message-pack",
    name = ["mode"],
    havingValue = "LOCAL",
    matchIfMissing = true,
)
class LocalNotificationMessageRenderer(
    private val resolver: StandardMessageResolver,
    private val personalizedResolver: PersonalizedMessageResolver,
    private val activeNotificationPackRepository: ActiveNotificationPackRepository,
) : NotificationMessageRenderer {
    override fun renderNotification(
        userId: UserId,
        items: List<NotificationItem>,
    ): RenderedMessage {
        require(items.isNotEmpty())

        val activePack = activeNotificationPackRepository.find(userId)
        val resolution = resolveNotifications(userId, items, activePack)

        return RenderedMessage(
            text =
                if (resolution.notifications.size == 1) {
                    renderSolo(resolution.notifications.single())
                } else {
                    renderAggregated(
                        notifications = resolution.notifications,
                        pack = resolution.pack,
                    )
                },
        )
    }

    override fun renderStart(context: TelegramStartContext): RenderedMessage =
        RenderedMessage(
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
            actions =
                listOf(
                    MessageAction.OpenMiniApp("Открыть Mnemos"),
                    MessageAction.Callback(
                        label = "Помощь",
                        data = TelegramHelpSection.OVERVIEW.callbackData,
                    ),
                ),
        )

    override fun renderHelp(section: TelegramHelpSection): RenderedMessage =
        RenderedMessage(
            text = renderHelpText(section),
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
                На дропы предметов, рейды по локациям, бустеры, данжи и глобальные события.

                <b>Как работают рейды?</b>
                У пользователя одна рейд-подписка. В ней можно включать и выключать нужные локации — Mnemos обновляет существующую подписку, а не создаёт новую на каждый набор локаций.

                <b>Почему уведомление может прийти не мгновенно?</b>
                Некоторые события агрегируются на короткое время, чтобы бот не отправлял несколько почти одинаковых сообщений подряд.
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
                Пак меняет оформление поддерживаемых уведомлений. Для доставки события используется активный пак, зафиксированный по правилам персонализации Mnemos.
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

    private fun renderHelpActions(section: TelegramHelpSection): List<MessageAction> =
        buildList {
            add(MessageAction.OpenMiniApp("Открыть Mnemos"))

            helpSections.forEach { (helpSection, label) ->
                add(
                    MessageAction.Callback(
                        label = if (helpSection == section) "• $label" else label,
                        data = helpSection.callbackData,
                    ),
                )
            }
        }

    private fun resolveNotifications(
        userId: UserId,
        items: List<NotificationItem>,
        pack: ActiveNotificationPack?,
    ): NotificationResolution {
        if (pack == null) {
            return NotificationResolution(
                notifications = items.map(resolver::resolve),
                pack = null,
            )
        }

        return try {
            NotificationResolution(
                notifications = items.map { item -> personalizedResolver.resolve(item, pack) },
                pack = pack,
            )
        } catch (exception: RuntimeException) {
            logger.warn(
                "Failed to render active notification pack, falling back to standard templates: userId={}, personalizationId={}",
                userId.id,
                pack.personalizationId,
                exception,
            )

            NotificationResolution(
                notifications = items.map(resolver::resolve),
                pack = null,
            )
        }
    }

    private fun renderSolo(notification: ResolvedNotification): String = "${notification.soloTitle}\n${notification.soloBody}"

    private fun renderAggregated(
        notifications: List<ResolvedNotification>,
        pack: ActiveNotificationPack?,
    ): String {
        val header = resolveAggregateHeader(notifications, pack)

        return buildString {
            append(header)

            notifications.forEach { notification ->
                val entry = "\n\n${notification.compact}"

                if (length + entry.length > MAX_MESSAGE_LENGTH - TRUNCATION_SUFFIX.length) {
                    append(TRUNCATION_SUFFIX)
                    return@buildString
                }

                append(entry)
            }
        }
    }

    private fun resolveAggregateHeader(
        notifications: List<ResolvedNotification>,
        pack: ActiveNotificationPack?,
    ): String {
        val types = notifications.map { it.type }.toSet()

        if (pack != null) {
            if (types.size > 1) {
                return TelegramHtmlEscaper.escape(pack.mixedAggregateTitle)
            }

            val variantIds = notifications.map { notification -> notification.variantId }.toSet()
            val first = notifications.first()

            return if (variantIds.size == 1) {
                requireNotNull(first.aggregateTitle)
            } else {
                requireNotNull(first.defaultAggregateTitle)
            }
        }

        if (types.size > 1) {
            return "🔔 <b>Сразу несколько событий</b>"
        }

        return when (types.single()) {
            NotificationType.ITEM_DROP ->
                "🎁 <b>Новые находки</b>"

            NotificationType.BOOSTER ->
                "⚡ <b>Активированы бустеры</b>"

            NotificationType.DUNGEON ->
                "🏰 <b>Открылись подземелья</b>"

            NotificationType.RAID ->
                "⚔️ <b>Начались рейды</b>"

            NotificationType.GLOBAL ->
                "🌍 <b>События мира</b>"
        }
    }

    private data class NotificationResolution(
        val notifications: List<ResolvedNotification>,
        val pack: ActiveNotificationPack?,
    )

    private companion object {
        const val MAX_MESSAGE_LENGTH = 4096
        const val TRUNCATION_SUFFIX = "\n\n…и ещё несколько событий"

        val helpSections =
            listOf(
                TelegramHelpSection.OVERVIEW to "Обзор",
                TelegramHelpSection.MINI_APP to "Mini App",
                TelegramHelpSection.SUBSCRIPTIONS to "Подписки",
                TelegramHelpSection.ACCOUNTS to "Аккаунты",
                TelegramHelpSection.NOTIFICATIONS to "Уведомления",
                TelegramHelpSection.TROUBLESHOOTING to "Проблемы",
            )

        val logger = LoggerFactory.getLogger(LocalNotificationMessageRenderer::class.java)
    }
}
