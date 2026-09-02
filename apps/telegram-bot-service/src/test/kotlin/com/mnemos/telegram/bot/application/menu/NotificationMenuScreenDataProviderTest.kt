package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.notification.NotificationMenuContext
import com.mnemos.telegram.bot.application.notification.model.NotificationCreationMode
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.util.UUID

class NotificationMenuScreenDataProviderTest {
    @Test
    fun `loads requested drop page from menu context`() {
        val result = availablePage(page = 2, totalPages = 4)
        val gateway = RecordingGateway(mapOf(2 to result))
        val provider = NotificationMenuScreenDataProvider(gateway)
        val dropListContext = NotificationMenuContext.selectMode(NotificationCreationMode.ITEM_DROP_LIST)
        val session =
            TelegramMenuSession(
                telegramId = TELEGRAM_ID,
                currentScreen = TelegramMenuScreen.NOTIFICATIONS,
                screenContext = NotificationMenuContext.withPage(dropListContext, 2),
            )

        val data = provider.load(session) as TelegramMenuScreenData.Notifications

        assertSame(result, data.data.result)
        assertEquals(listOf(PageRequest(page = 2, size = 8)), gateway.requests)
    }

    @Test
    fun `falls back to last page when drop page shrinks`() {
        val stalePage = availablePage(page = 8, totalPages = 2)
        val lastPage = availablePage(page = 1, totalPages = 2)
        val gateway =
            RecordingGateway(
                mapOf(
                    8 to stalePage,
                    1 to lastPage,
                ),
            )
        val provider = NotificationMenuScreenDataProvider(gateway)
        val dropListContext = NotificationMenuContext.selectMode(NotificationCreationMode.ITEM_DROP_LIST)
        val session =
            TelegramMenuSession(
                telegramId = TELEGRAM_ID,
                currentScreen = TelegramMenuScreen.NOTIFICATIONS,
                screenContext = NotificationMenuContext.withPage(dropListContext, 8),
            )

        val data = provider.load(session) as TelegramMenuScreenData.Notifications

        assertSame(lastPage, data.data.result)
        assertEquals(
            listOf(
                PageRequest(page = 8, size = 8),
                PageRequest(page = 1, size = 8),
            ),
            gateway.requests,
        )
    }

    @Test
    fun `delete confirmation reloads selected drop page and id`() {
        val subscriptionId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val result = availablePage(page = 2, totalPages = 4)
        val gateway = RecordingGateway(mapOf(2 to result))
        val provider = NotificationMenuScreenDataProvider(gateway)
        val dropListContext = NotificationMenuContext.selectMode(NotificationCreationMode.ITEM_DROP_LIST)
        val secondPage = NotificationMenuContext.withPage(dropListContext, 2)
        val deleteContext = NotificationMenuContext.selectDelete(secondPage, subscriptionId)
        val session =
            TelegramMenuSession(
                telegramId = TELEGRAM_ID,
                currentScreen = TelegramMenuScreen.NOTIFICATIONS,
                screenContext = deleteContext,
            )

        val data = provider.load(session) as TelegramMenuScreenData.Notifications

        assertSame(result, data.data.result)
        assertEquals(NotificationCreationMode.ITEM_DROP_DELETE, data.data.mode)
        assertEquals(subscriptionId, data.data.deleteSubscriptionId)
        assertEquals(listOf(PageRequest(page = 2, size = 8)), gateway.requests)
    }

    @Test
    fun `does not retry unavailable notification page`() {
        val gateway = RecordingGateway(emptyMap())
        val provider = NotificationMenuScreenDataProvider(gateway)
        val session =
            TelegramMenuSession(
                telegramId = TELEGRAM_ID,
                currentScreen = TelegramMenuScreen.NOTIFICATIONS,
            )

        val data = provider.load(session) as TelegramMenuScreenData.Notifications

        assertEquals(NotificationSubscriptionResult.Unavailable, data.data.result)
        assertEquals(listOf(PageRequest(page = 0, size = 8)), gateway.requests)
    }

    @Test
    fun `creation screen uses session state without loading subscription list`() {
        val gateway = RecordingGateway(emptyMap())
        val provider = NotificationMenuScreenDataProvider(gateway)
        val itemDropContext =
            NotificationMenuContext.withDroppedFor(
                NotificationMenuContext.selectMode(NotificationCreationMode.ITEM_DROP),
                "knaliz",
            )
        val session =
            TelegramMenuSession(
                telegramId = TELEGRAM_ID,
                currentScreen = TelegramMenuScreen.NOTIFICATIONS,
                screenContext = itemDropContext,
            )

        val data = provider.load(session) as TelegramMenuScreenData.Notifications

        assertEquals(NotificationCreationMode.ITEM_DROP, data.data.mode)
        assertEquals("knaliz", data.data.droppedFor)
        assertEquals(NotificationSubscriptionResult.NotLoaded, data.data.result)
        assertEquals(emptyList<PageRequest>(), gateway.requests)
    }

    private fun availablePage(
        page: Int,
        totalPages: Int,
    ): NotificationSubscriptionResult.Available =
        NotificationSubscriptionResult.Available(
            NotificationSubscriptionPage(
                items = emptyList(),
                page = page,
                totalPages = totalPages,
                totalItems = totalPages.toLong(),
                hasNextPage = page + 1 < totalPages,
            ),
        )

    private data class PageRequest(
        val page: Int,
        val size: Int,
    )

    private class RecordingGateway(
        private val results: Map<Int, NotificationSubscriptionResult>,
    ) : NotificationSubscriptionGateway {
        val requests = mutableListOf<PageRequest>()

        override fun list(
            telegramId: Long,
            page: Int,
            size: Int,
        ): NotificationSubscriptionResult {
            assertEquals(TELEGRAM_ID, telegramId)
            requests += PageRequest(page = page, size = size)

            return results[page] ?: NotificationSubscriptionResult.Unavailable
        }

        override fun delete(
            telegramId: Long,
            subscriptionId: UUID,
        ): Boolean = false
    }

    private companion object {
        const val TELEGRAM_ID = 931458258L
    }
}
