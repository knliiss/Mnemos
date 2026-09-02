package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.pack.NotificationPackCallbackData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuMode
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuScreenData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackStoreResult
import com.mnemos.telegram.bot.application.pack.model.OwnedNotificationPacksResult
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackStoreItem
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackStorePage
import com.mnemos.telegram.bot.application.pack.model.TelegramOwnedNotificationPack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class NotificationPackMenuMessageRendererTest {
    @Test
    fun `store renders published packs and store navigation`() {
        val pack = storePack()
        val data =
            TelegramMenuScreenData.Packs(
                NotificationPackMenuScreenData(
                    mode = NotificationPackMenuMode.STORE,
                    store =
                        NotificationPackStoreResult.Available(
                            TelegramNotificationPackStorePage(
                                packs = listOf(pack),
                                page = 0,
                                totalItems = 1,
                                totalPages = 1,
                                hasNextPage = false,
                            ),
                        ),
                    owned = null,
                    selectedPersonalizationId = null,
                    feedback = null,
                ),
            )

        val message = NotificationPackMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val storeTab = callbacks.single { action -> action.data == NotificationPackCallbackData.STORE }
        val packAction = callbacks.single { action -> action.data == NotificationPackCallbackData.item(PACK_ID) }

        assertTrue(message.text.contains("🛍 <b>Магазин</b>"))
        assertTrue(message.text.contains("1 паков"))
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, storeTab.style)
        assertTrue(packAction.label.contains("Crystal Pack"))
        assertTrue(packAction.label.contains("25 PRIME"))
        assertTrue(callbacks.any { action -> action.data == NotificationPackCallbackData.OWNED })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
    }

    @Test
    fun `unowned pack detail renders escaped preview and primary purchase`() {
        val pack =
            storePack(
                name = "Crystal <Pack>",
                author = "Creator & Co",
                samples = listOf("✨ <Rare> & lucky"),
            )
        val data = detailData(pack)

        val message = NotificationPackMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val purchase = callbacks.single { action -> action.data == NotificationPackCallbackData.purchase(PACK_ID) }

        assertTrue(message.text.contains("Crystal &lt;Pack&gt;"))
        assertTrue(message.text.contains("Creator &amp; Co"))
        assertTrue(message.text.contains("✨ &lt;Rare&gt; &amp; lucky"))
        assertTrue(message.text.contains("25 PRIME"))
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, purchase.style)
    }

    @Test
    fun `owned pack detail renders success apply action`() {
        val pack = storePack(owned = true)
        val data = detailData(pack)

        val message = NotificationPackMenuMessageRenderer.render(data)
        val apply = message.callbacks().single { action -> action.data == NotificationPackCallbackData.apply(PACK_ID) }

        assertTrue(message.text.contains("Статус: <b>куплен</b>"))
        assertEquals("✅ Применить", apply.label)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, apply.style)
    }

    @Test
    fun `owned list highlights active pack and keeps apply callback`() {
        val data =
            TelegramMenuScreenData.Packs(
                NotificationPackMenuScreenData(
                    mode = NotificationPackMenuMode.OWNED,
                    store = null,
                    owned =
                        OwnedNotificationPacksResult.Available(
                            listOf(
                                TelegramOwnedNotificationPack(
                                    personalizationId = PACK_ID,
                                    name = "Crystal Pack",
                                    active = true,
                                ),
                            ),
                        ),
                    selectedPersonalizationId = null,
                    feedback = null,
                ),
            )

        val message = NotificationPackMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val ownedTab = callbacks.single { action -> action.data == NotificationPackCallbackData.OWNED }
        val activePack = callbacks.single { action -> action.data == NotificationPackCallbackData.apply(PACK_ID) }

        assertTrue(message.text.contains("🎒 <b>Мои паки</b>"))
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, ownedTab.style)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, activePack.style)
        assertTrue(activePack.label.startsWith("✅"))
    }

    @Test
    fun `unavailable store keeps refresh and back recovery`() {
        val data =
            TelegramMenuScreenData.Packs(
                NotificationPackMenuScreenData(
                    mode = NotificationPackMenuMode.STORE,
                    store = NotificationPackStoreResult.Unavailable,
                    owned = null,
                    selectedPersonalizationId = null,
                    feedback = null,
                ),
            )

        val message = NotificationPackMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Магазин паков временно недоступен"))
        assertTrue(callbacks.any { action -> action.data == NotificationPackCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
    }

    private fun detailData(pack: TelegramNotificationPackStoreItem): TelegramMenuScreenData.Packs =
        TelegramMenuScreenData.Packs(
            NotificationPackMenuScreenData(
                mode = NotificationPackMenuMode.DETAIL,
                store =
                    NotificationPackStoreResult.Available(
                        TelegramNotificationPackStorePage(
                            packs = listOf(pack),
                            page = 0,
                            totalItems = 1,
                            totalPages = 1,
                            hasNextPage = false,
                        ),
                    ),
                owned = null,
                selectedPersonalizationId = PACK_ID,
                feedback = null,
            ),
        )

    private fun storePack(
        name: String = "Crystal Pack",
        author: String = "Mnemos",
        owned: Boolean = false,
        active: Boolean = false,
        samples: List<String> = listOf("✨ Rare drop"),
    ): TelegramNotificationPackStoreItem =
        TelegramNotificationPackStoreItem(
            personalizationId = PACK_ID,
            name = name,
            author = author,
            priceAmount = 25,
            currency = "PRIME",
            owned = owned,
            active = active,
            samples = samples,
        )

    private fun OutboundTelegramMessage.callbacks(): List<OutboundTelegramMessageAction.Callback> =
        actionRows
            .flatten()
            .filterIsInstance<OutboundTelegramMessageAction.Callback>()

    private companion object {
        val PACK_ID: UUID = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b74")
    }
}
