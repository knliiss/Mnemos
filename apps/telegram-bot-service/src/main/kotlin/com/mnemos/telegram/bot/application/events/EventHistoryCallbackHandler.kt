package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class EventHistoryCallbackHandler(
    private val menuService: TelegramMenuService,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = EventHistoryCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult =
        when (context.data) {
            EventHistoryCallbackData.REFRESH -> updateContext(context) { current -> current }
            EventHistoryCallbackData.PREVIOUS -> updateContext(context, EventHistoryMenuContext::previous)
            EventHistoryCallbackData.SEARCH -> requestSearch(context)
            EventHistoryCallbackData.SEARCH_CANCEL -> cancelSearch(context)
            EventHistoryCallbackData.SEARCH_CLEAR -> updateContext(context, EventHistoryMenuContext::clearSearch)
            else -> handleParameterizedCallback(context)
        }

    private fun requestSearch(context: TelegramCallbackContext): TelegramDeliveryResult =
        menuService.requestTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.EVENTS,
            pendingInput = TelegramPendingInput.EVENT_SEARCH,
            updateContext = EventHistoryMenuContext::clearSearchError,
        )

    private fun cancelSearch(context: TelegramCallbackContext): TelegramDeliveryResult =
        menuService.cancelTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.EVENTS,
        )

    private fun handleParameterizedCallback(context: TelegramCallbackContext): TelegramDeliveryResult {
        EventHistoryCallbackData.selectedFilter(context.data)?.let { filter ->
            return updateContext(context) { current ->
                EventHistoryMenuContext.filter(current, filter)
            }
        }

        EventHistoryCallbackData.selectedSort(context.data)?.let { sort ->
            return updateContext(context) { current ->
                EventHistoryMenuContext.sort(current, sort)
            }
        }

        EventHistoryCallbackData.selectedItemRarity(context.data)?.let { selection ->
            return updateContext(context) { current ->
                EventHistoryMenuContext.itemRarity(current, selection.valueOrNull())
            }
        }

        EventHistoryCallbackData.selectedItemType(context.data)?.let { selection ->
            return updateContext(context) { current ->
                EventHistoryMenuContext.itemType(current, selection.valueOrNull())
            }
        }

        EventHistoryCallbackData.selectedBoosterType(context.data)?.let { selection ->
            return updateContext(context) { current ->
                EventHistoryMenuContext.boosterType(current, selection.valueOrNull())
            }
        }

        EventHistoryCallbackData.selectedGlobalType(context.data)?.let { selection ->
            return updateContext(context) { current ->
                EventHistoryMenuContext.globalType(current, selection.valueOrNull())
            }
        }

        EventHistoryCallbackData.selectedRaidLocation(context.data)?.let { selection ->
            return updateContext(context) { current ->
                EventHistoryMenuContext.raidLocation(current, selection.valueOrNull())
            }
        }

        EventHistoryCallbackData.selectedRaidLocationPage(context.data)?.let { page ->
            return updateContext(context) { current ->
                EventHistoryMenuContext.raidLocationPage(current, page)
            }
        }

        EventHistoryCallbackData.nextCursor(context.data)?.let { cursor ->
            return updateContext(context) { current ->
                EventHistoryMenuContext.next(current, cursor)
            }
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun <T> FilterCallbackValue<T>.valueOrNull(): T? =
        when (this) {
            is FilterCallbackValue.Selected -> value
            FilterCallbackValue.Clear -> null
        }

    private fun updateContext(
        context: TelegramCallbackContext,
        update: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.EVENTS,
            update = update,
        )
}
