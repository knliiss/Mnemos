package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.notification.NotificationMenuContext
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import java.util.UUID

class NotificationMenuScreenDataProviderTest {
    @Test
    fun `loads requested notification page from menu context`() {
        val result = availablePage(page = 2, totalPages = 4)
        val gateway = RecordingGateway(mapOf(2 to result))
        val provider = NotificationMenuScreenDataProvider(gateway)
        val session =
            TelegramMenuSession(
                telegramId = TELEGRAM_ID,
                currentScreen = TelegramMenuScreen.NOTIFICATIONS,
                screenContext = NotificationMenuContext.withPage(emptyMap(), 2),
            )

        val data = provider.load(session) as TelegramMenuScreenData.Notifications

        assertSame(result, data.data.result)
        assertEquals(listOf(PageRequest(page = 2, size = 8)), gateway.requests)
    }

    @Test
    fun `falls back to last page when notification page shrinks`() {
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
        val session =
            TelegramMenuSession(
                telegramId = TELEGRAM_ID,
                currentScreen = TelegramMenuScreen.NOTIFICATIONS,
                screenContext = NotificationMenuContext.withPage(emptyMap(), 8),
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
