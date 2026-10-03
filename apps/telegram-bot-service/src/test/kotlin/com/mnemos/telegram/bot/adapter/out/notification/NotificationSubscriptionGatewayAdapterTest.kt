package com.mnemos.telegram.bot.adapter.out.notification

import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationDungeonType
import com.mnemos.telegram.bot.application.notification.model.NotificationGlobalType
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramPingPreferenceResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionListResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramUpdatePingPreferenceRequest
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionResponse
import com.mnemos.telegram.bot.contracts.internal.subscription.TelegramSubscriptionType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class NotificationSubscriptionGatewayAdapterTest {
    @Test
    fun `mixed backend pages are filtered before item drop pagination`() {
        val client = mock<NotificationServiceClient>()
        val adapter = NotificationSubscriptionGatewayAdapter(client)
        val firstDrop = itemDrop("01a02e75-2603-7a67-b5f1-98b432273b71", "PlayerOne", "Sword")
        val secondDrop = itemDrop("01a02e75-2603-7a67-b5f1-98b432273b72", "PlayerTwo", "Shield")
        val thirdDrop = itemDrop("01a02e75-2603-7a67-b5f1-98b432273b73", "PlayerThree", "Ring")
        val fourthDrop = itemDrop("01a02e75-2603-7a67-b5f1-98b432273b74", "PlayerFour", "Potion")
        val raidId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b75")

        whenever(client.getPingPreference(TELEGRAM_ID)).thenReturn(
            TelegramPingPreferenceResponse(enabled = true),
        )
        whenever(
            client.listSubscriptions(
                telegramId = TELEGRAM_ID,
                page = 0,
                size = BACKEND_PAGE_SIZE,
            ),
        ).thenReturn(
            backendPage(
                page = 0,
                hasNextPage = true,
                items =
                    listOf(
                        simpleSubscription(
                            id = "01a02e75-2603-7a67-b5f1-98b432273b76",
                            type = TelegramSubscriptionType.GLOBAL,
                            globalType = "MOON",
                        ),
                        firstDrop,
                        TelegramSubscriptionResponse(
                            id = raidId,
                            type = TelegramSubscriptionType.RAID,
                            createdAt = CREATED_AT,
                            raidLocations = setOf(3, 12),
                        ),
                    ),
            ),
        )
        whenever(
            client.listSubscriptions(
                telegramId = TELEGRAM_ID,
                page = 1,
                size = BACKEND_PAGE_SIZE,
            ),
        ).thenReturn(
            backendPage(
                page = 1,
                hasNextPage = true,
                items =
                    listOf(
                        simpleSubscription(
                            id = "01a02e75-2603-7a67-b5f1-98b432273b77",
                            type = TelegramSubscriptionType.BOOSTER,
                            boosterType = "LUCK",
                        ),
                        secondDrop,
                        simpleSubscription(
                            id = "01a02e75-2603-7a67-b5f1-98b432273b78",
                            type = TelegramSubscriptionType.DUNGEON,
                            dungeonType = "HARD",
                        ),
                        thirdDrop,
                    ),
            ),
        )
        whenever(
            client.listSubscriptions(
                telegramId = TELEGRAM_ID,
                page = 2,
                size = BACKEND_PAGE_SIZE,
            ),
        ).thenReturn(
            backendPage(
                page = 2,
                hasNextPage = false,
                items =
                    listOf(
                        simpleSubscription(
                            id = "01a02e75-2603-7a67-b5f1-98b432273b79",
                            type = TelegramSubscriptionType.GLOBAL,
                            globalType = "DARKNESS",
                        ),
                        fourthDrop,
                    ),
            ),
        )

        val result =
            adapter.list(
                telegramId = TELEGRAM_ID,
                page = 1,
                size = 2,
            )

        assertTrue(result is NotificationSubscriptionResult.Available)

        val available = result as NotificationSubscriptionResult.Available

        assertEquals(listOf(thirdDrop.id, fourthDrop.id), available.page.items.map { item -> item.id })
        assertEquals(1, available.page.page)
        assertEquals(2, available.page.totalPages)
        assertEquals(4L, available.page.totalItems)
        assertFalse(available.page.hasNextPage)
        assertEquals(1, available.boosterSubscriptions.size)
        assertEquals(1, available.dungeonSubscriptions.size)
        assertEquals(2, available.globalSubscriptions.size)
        assertEquals(raidId, available.raidSubscription?.id)
        assertTrue(available.boosterSubscriptions.containsKey(NotificationBoosterType.LUCK))
        assertTrue(available.dungeonSubscriptions.containsKey(NotificationDungeonType.HARD))
        assertTrue(available.globalSubscriptions.containsKey(NotificationGlobalType.MOON))
        assertTrue(available.globalSubscriptions.containsKey(NotificationGlobalType.DARKNESS))
        assertTrue(available.pingEnabled)

        verify(client).getPingPreference(TELEGRAM_ID)
        verify(client).listSubscriptions(TELEGRAM_ID, 0, BACKEND_PAGE_SIZE)
        verify(client).listSubscriptions(TELEGRAM_ID, 1, BACKEND_PAGE_SIZE)
        verify(client).listSubscriptions(TELEGRAM_ID, 2, BACKEND_PAGE_SIZE)
    }

    @Test
    fun `ping preference toggle delegates to notification service`() {
        val client = mock<NotificationServiceClient>()
        val adapter = NotificationSubscriptionGatewayAdapter(client)

        whenever(
            client.updatePingPreference(
                telegramId = TELEGRAM_ID,
                request = TelegramUpdatePingPreferenceRequest(false),
            ),
        ).thenReturn(TelegramPingPreferenceResponse(enabled = false))

        assertTrue(adapter.setPingEnabled(TELEGRAM_ID, enabled = false))

        verify(client).updatePingPreference(
            telegramId = TELEGRAM_ID,
            request = TelegramUpdatePingPreferenceRequest(false),
        )
    }

    private fun backendPage(
        page: Int,
        hasNextPage: Boolean,
        items: List<TelegramSubscriptionResponse>,
    ): TelegramSubscriptionListResponse =
        TelegramSubscriptionListResponse(
            items = items,
            page = page,
            totalPages = 3,
            totalItems = 9,
            hasNextPage = hasNextPage,
        )

    private fun itemDrop(
        id: String,
        droppedFor: String,
        itemName: String,
    ): TelegramSubscriptionResponse =
        TelegramSubscriptionResponse(
            id = UUID.fromString(id),
            type = TelegramSubscriptionType.ITEM_DROP,
            createdAt = CREATED_AT,
            droppedFor = droppedFor,
            itemName = itemName,
        )

    private fun simpleSubscription(
        id: String,
        type: TelegramSubscriptionType,
        boosterType: String? = null,
        dungeonType: String? = null,
        globalType: String? = null,
    ): TelegramSubscriptionResponse =
        TelegramSubscriptionResponse(
            id = UUID.fromString(id),
            type = type,
            createdAt = CREATED_AT,
            boosterType = boosterType,
            dungeonType = dungeonType,
            globalType = globalType,
        )

    private companion object {
        const val TELEGRAM_ID = 931458258L
        const val BACKEND_PAGE_SIZE = 20
        val CREATED_AT: Instant = Instant.parse("2026-09-01T12:00:00Z")
    }
}
