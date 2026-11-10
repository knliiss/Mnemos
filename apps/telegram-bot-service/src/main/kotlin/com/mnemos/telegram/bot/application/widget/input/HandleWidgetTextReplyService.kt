package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.events.EventHistorySearchQuery
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.online.SaoOnlineMenuContext
import com.mnemos.telegram.bot.application.online.SaoOnlineSearchQuery
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
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
) : HandleWidgetTextReplyUseCase {
    override fun handle(telegramId: Long, promptMessageId: Int, text: String): TelegramDeliveryResult? {
        if (!properties.enabled) return null
        val session = pending.find(telegramId, promptMessageId) ?: return null
        val widget = widgets.find(telegramId, session.widgetMessageId)

        val screen =
            when (session.purpose) {
                WidgetTextInputPurpose.EVENT_SEARCH -> TelegramMenuScreen.EVENTS
                WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH -> TelegramMenuScreen.ONLINE
            }
        val result =
            if (widget == null || widget.screen != session.purpose.screen || widget.accountId != session.accountId ||
                text.trim().equals("скасувати", ignoreCase = true)
            ) {
                menu.open(telegramId, screen)
            } else {
                val context =
                    when (session.purpose) {
                        WidgetTextInputPurpose.EVENT_SEARCH -> {
                            val query = EventHistorySearchQuery.normalize(text)
                            if (query == null) {
                                EventHistoryMenuContext.withSearchError(emptyMap(), EventHistorySearchQuery.INVALID_MESSAGE)
                            } else {
                                EventHistoryMenuContext.search(emptyMap(), query)
                            }
                        }
                        WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH -> {
                            val query = SaoOnlineSearchQuery.normalize(text)
                            if (query == null) {
                                mapOf(SaoOnlineMenuContext.SEARCH_ERROR_KEY to SaoOnlineSearchQuery.INVALID_MESSAGE)
                            } else {
                                SaoOnlineMenuContext.search(query)
                            }
                        }
                    }
                menu.open(telegramId, screen, screenContext = context)
            }

        if (result != TelegramDeliveryResult.Retryable) {
            pending.delete(telegramId, promptMessageId)
        }
        return result
    }
}
