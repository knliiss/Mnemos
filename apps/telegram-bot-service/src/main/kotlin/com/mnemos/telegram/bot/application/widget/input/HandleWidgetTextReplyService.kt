package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.events.EventHistorySearchQuery
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.online.SaoOnlineMenuContext
import com.mnemos.telegram.bot.application.online.SaoOnlineSearchQuery
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.input.ApplyWidgetTextSearchUseCase
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class HandleWidgetTextReplyService(
    private val properties: TelegramWidgetProperties,
    private val pending: WidgetTextInputSessionRepository,
    private val widgets: TelegramWidgetSessionRepository,
    private val menu: TelegramMenuService,
    private val search: ApplyWidgetTextSearchUseCase,
    private val accountInput: ApplyWidgetAccountInputUseCase,
) : HandleWidgetTextReplyUseCase {
    override fun handle(telegramId: Long, promptMessageId: Int, text: String): TelegramDeliveryResult? {
        if (!properties.enabled) return null
        val session = pending.find(telegramId, promptMessageId) ?: return null
        val widget = widgets.find(telegramId, session.widgetMessageId)

        val screen =
            when (session.purpose) {
                WidgetTextInputPurpose.EVENT_SEARCH -> TelegramMenuScreen.EVENTS
                WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH -> TelegramMenuScreen.ONLINE
                WidgetTextInputPurpose.ACCOUNT_CREATE, WidgetTextInputPurpose.ACCOUNT_JOIN -> TelegramMenuScreen.ACCOUNT
            }
        val result =
            when {
                text.trim().equals("скасувати", ignoreCase = true) -> TelegramDeliveryResult.Delivered
                widget == null || widget.screen != session.purpose.screen || widget.accountId != session.accountId ->
                    menu.open(telegramId, screen)
                session.purpose == WidgetTextInputPurpose.ACCOUNT_CREATE ||
                    session.purpose == WidgetTextInputPurpose.ACCOUNT_JOIN ->
                    accountInput.apply(session, widget, text.trim())
                else -> {
                    val normalized =
                        when (session.purpose) {
                            WidgetTextInputPurpose.EVENT_SEARCH -> EventHistorySearchQuery.normalize(text)
                            WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH -> SaoOnlineSearchQuery.normalize(text)
                            else -> null
                        }

                    if (normalized == null) {
                        val context =
                            when (session.purpose) {
                                WidgetTextInputPurpose.EVENT_SEARCH ->
                                    EventHistoryMenuContext.withSearchError(emptyMap(), EventHistorySearchQuery.INVALID_MESSAGE)
                                WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH ->
                                    mapOf(SaoOnlineMenuContext.SEARCH_ERROR_KEY to SaoOnlineSearchQuery.INVALID_MESSAGE)
                                else -> emptyMap()
                            }
                        menu.open(telegramId, screen, screenContext = context)
                    } else {
                        search.apply(widget, normalized)
                    }
                }
            }

        if (result != TelegramDeliveryResult.Retryable) {
            pending.delete(telegramId, promptMessageId)
        }
        return result
    }
}
