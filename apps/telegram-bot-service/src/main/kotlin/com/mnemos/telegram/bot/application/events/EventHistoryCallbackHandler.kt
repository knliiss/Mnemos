package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class EventHistoryCallbackHandler(
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = EventHistoryCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session = currentEventsSession(context) ?: return TelegramDeliveryResult.Delivered

        if (session.pendingInput == TelegramPendingInput.EVENT_SEARCH) {
            return if (context.data == EventHistoryCallbackData.SEARCH_CANCEL) {
                cancelSearch(context)
            } else {
                TelegramDeliveryResult.Delivered
            }
        }

        return when (context.data) {
            EventHistoryCallbackData.FILTERS -> openFilters(context, session)
            EventHistoryCallbackData.FILTERS_CLOSE -> closeFilters(context, session)
            EventHistoryCallbackData.REFRESH -> updateContext(context) { current -> current }
            EventHistoryCallbackData.PREVIOUS -> feedOnly(session) { updateContext(context, EventHistoryMenuContext::previous) }
            EventHistoryCallbackData.SEARCH -> feedOnly(session) { requestSearch(context) }
            EventHistoryCallbackData.SEARCH_CANCEL -> TelegramDeliveryResult.Delivered
            EventHistoryCallbackData.SEARCH_CLEAR ->
                feedOnly(session) { updateContext(context, EventHistoryMenuContext::clearSearch) }

            else -> handleParameterizedCallback(context, session)
        }
    }

    private fun openFilters(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        if (EventHistoryMenuContext.filtersOpen(session.screenContext)) {
            return TelegramDeliveryResult.Delivered
        }

        return updateContext(context, EventHistoryMenuContext::openFilters)
    }

    private fun closeFilters(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        if (!EventHistoryMenuContext.filtersOpen(session.screenContext)) {
            return TelegramDeliveryResult.Delivered
        }

        return updateContext(context, EventHistoryMenuContext::closeFilters)
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

    private fun handleParameterizedCallback(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        val filtersOpen = EventHistoryMenuContext.filtersOpen(session.screenContext)

        EventHistoryCallbackData.selectedFilter(context.data)?.let { filter ->
            return filtersOnly(filtersOpen) {
                updateContext(context) { current -> EventHistoryMenuContext.filter(current, filter) }
            }
        }

        EventHistoryCallbackData.selectedSort(context.data)?.let { sort ->
            return filtersOnly(filtersOpen) {
                updateContext(context) { current -> EventHistoryMenuContext.sort(current, sort) }
            }
        }

        EventHistoryCallbackData.selectedItemRarity(context.data)?.let { selection ->
            return filtersOnly(filtersOpen) {
                updateContext(context) { current -> EventHistoryMenuContext.itemRarity(current, selection.valueOrNull()) }
            }
        }

        EventHistoryCallbackData.selectedItemType(context.data)?.let { selection ->
            return filtersOnly(filtersOpen) {
                updateContext(context) { current -> EventHistoryMenuContext.itemType(current, selection.valueOrNull()) }
            }
        }

        EventHistoryCallbackData.selectedBoosterType(context.data)?.let { selection ->
            return filtersOnly(filtersOpen) {
                updateContext(context) { current -> EventHistoryMenuContext.boosterType(current, selection.valueOrNull()) }
            }
        }

        EventHistoryCallbackData.selectedGlobalType(context.data)?.let { selection ->
            return filtersOnly(filtersOpen) {
                updateContext(context) { current -> EventHistoryMenuContext.globalType(current, selection.valueOrNull()) }
            }
        }

        EventHistoryCallbackData.selectedRaidLocation(context.data)?.let { selection ->
            return filtersOnly(filtersOpen) {
                updateContext(context) { current -> EventHistoryMenuContext.raidLocation(current, selection.valueOrNull()) }
            }
        }

        EventHistoryCallbackData.selectedRaidLocationPage(context.data)?.let { page ->
            return filtersOnly(filtersOpen) {
                updateContext(context) { current -> EventHistoryMenuContext.raidLocationPage(current, page) }
            }
        }

        EventHistoryCallbackData.selectedWipe(context.data)?.let { selection ->
            return filtersOnly(filtersOpen) {
                updateContext(context) { current -> EventHistoryMenuContext.wipe(current, selection) }
            }
        }

        EventHistoryCallbackData.nextCursor(context.data)?.let { cursor ->
            return if (filtersOpen) {
                TelegramDeliveryResult.Delivered
            } else {
                updateContext(context) { current -> EventHistoryMenuContext.next(current, cursor) }
            }
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun feedOnly(
        session: TelegramMenuSession,
        action: () -> TelegramDeliveryResult,
    ): TelegramDeliveryResult =
        if (EventHistoryMenuContext.filtersOpen(session.screenContext)) {
            TelegramDeliveryResult.Delivered
        } else {
            action()
        }

    private fun filtersOnly(
        filtersOpen: Boolean,
        action: () -> TelegramDeliveryResult,
    ): TelegramDeliveryResult =
        if (filtersOpen) {
            action()
        } else {
            TelegramDeliveryResult.Delivered
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

    private fun currentEventsSession(context: TelegramCallbackContext): TelegramMenuSession? {
        val session = sessionRepository.find(context.telegramId) ?: return null

        if (session.currentScreen != TelegramMenuScreen.EVENTS || session.menuMessageId != context.messageId) {
            return null
        }

        return session
    }
}
