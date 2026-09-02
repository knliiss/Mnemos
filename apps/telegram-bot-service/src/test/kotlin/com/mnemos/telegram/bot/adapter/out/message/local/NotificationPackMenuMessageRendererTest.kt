package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.pack.NotificationPackCallbackData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackAggregatePreviewMode
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuMode
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuScreenData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackStoreResult
import com.mnemos.telegram.bot.application.pack.model.OwnedNotificationPacksResult
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackPreview
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackStoreItem
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackStorePage
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplate
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplateCondition
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplateVariant
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
    fun `pack detail shows standard and aggregate previews together with all template switches`() {
        val pack =
            storePack(
                name = "Crystal <Pack>",
                author = "Creator & Co",
            )
        val data = detailData(pack)

        val message = NotificationPackMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val purchase = callbacks.single { action -> action.data == NotificationPackCallbackData.purchase(PACK_ID) }

        assertTrue(message.text.contains("Crystal &lt;Pack&gt;"))
        assertTrue(message.text.contains("Creator &amp; Co"))
        assertTrue(message.text.contains("📨 <b>Обычное сообщение</b>"))
        assertTrue(message.text.contains("🧩 <b>Агрегированное · один тип</b>"))
        assertTrue(message.text.contains("📦 Дроп Вечный клинок"))
        assertTrue(message.text.contains("north получил Вечный клинок"))
        assertTrue(message.text.contains("📦 Дропы"))
        assertTrue(message.text.contains("📦 Вечный клинок"))
        assertTrue(message.text.contains("25 PRIME"))
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, purchase.style)

        NotificationPackPreviewFormatter.templateTypes.forEach { type ->
            assertTrue(
                callbacks.any { action -> action.data == NotificationPackCallbackData.template(type) },
                "Missing template switch for $type",
            )
        }
    }

    @Test
    fun `selected conditional variant updates both standard and aggregate previews`() {
        val pack = storePack()
        val data =
            detailData(
                pack = pack,
                selectedTemplateType = NotificationPackTemplateType.ITEM_DROP,
                selectedVariantId = SECRET_VARIANT_ID,
            )

        val message = NotificationPackMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()
        val variantAction =
            callbacks.single { action ->
                action.data == NotificationPackCallbackData.variant(SECRET_VARIANT_ID)
            }

        assertTrue(message.text.contains("Вариант: <b>Секретный</b>"))
        assertTrue(message.text.contains("🔐 Секретный дроп Вечный клинок"))
        assertTrue(message.text.contains("north нашёл секретный предмет"))
        assertTrue(message.text.contains("🔐 Секретные дропы"))
        assertTrue(message.text.contains("🔐 Вечный клинок · секрет"))
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, variantAction.style)
        assertTrue(callbacks.any { action -> action.data == NotificationPackCallbackData.BASE_VARIANT })
    }

    @Test
    fun `mixed aggregate preview keeps standard message visible and renders multiple compact types`() {
        val data =
            detailData(
                pack = storePack(),
                selectedTemplateType = NotificationPackTemplateType.GLOBAL,
                aggregatePreviewMode = NotificationPackAggregatePreviewMode.MIXED,
            )

        val message = NotificationPackMenuMessageRenderer.render(data)
        val mixedAction =
            message.callbacks().single { action ->
                action.data == NotificationPackCallbackData.aggregateMode(NotificationPackAggregatePreviewMode.MIXED)
            }

        assertTrue(message.text.contains("📨 <b>Обычное сообщение</b>"))
        assertTrue(message.text.contains("🌌 Затмение началось"))
        assertTrue(message.text.contains("🧩 <b>Агрегированное · смешанный</b>"))
        assertTrue(message.text.contains("События Mnemos"))
        assertTrue(message.text.contains("📦 Вечный клинок"))
        assertTrue(message.text.contains("⚡ Удача"))
        assertTrue(message.text.contains("🏰 Экспертное подземелье"))
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, mixedAction.style)
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
                ),
            )

        val message = NotificationPackMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Магазин паков временно недоступен"))
        assertTrue(callbacks.any { action -> action.data == NotificationPackCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
    }

    private fun detailData(
        pack: TelegramNotificationPackStoreItem,
        selectedTemplateType: NotificationPackTemplateType = NotificationPackTemplateType.ITEM_DROP,
        selectedVariantId: String? = null,
        aggregatePreviewMode: NotificationPackAggregatePreviewMode = NotificationPackAggregatePreviewMode.SAME_TYPE,
    ): TelegramMenuScreenData.Packs =
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
                selectedTemplateType = selectedTemplateType,
                selectedVariantId = selectedVariantId,
                aggregatePreviewMode = aggregatePreviewMode,
            ),
        )

    private fun storePack(
        name: String = "Crystal Pack",
        author: String = "Mnemos",
        owned: Boolean = false,
        active: Boolean = false,
    ): TelegramNotificationPackStoreItem =
        TelegramNotificationPackStoreItem(
            personalizationId = PACK_ID,
            name = name,
            author = author,
            priceAmount = 25,
            currency = "PRIME",
            owned = owned,
            active = active,
            preview = preview(),
        )

    private fun preview(): TelegramNotificationPackPreview =
        TelegramNotificationPackPreview(
            mixedAggregateTitle = "События Mnemos",
            templates =
                mapOf(
                    NotificationPackTemplateType.ITEM_DROP to
                        TelegramNotificationTemplate(
                            emoji = "📦",
                            soloTitle = "Дроп {item}",
                            soloBody = "{player} получил {item}",
                            compact = "{item}",
                            aggregateTitle = "Дропы",
                            variants =
                                listOf(
                                    TelegramNotificationTemplateVariant(
                                        id = SECRET_VARIANT_ID,
                                        conditions =
                                            listOf(
                                                TelegramNotificationTemplateCondition(
                                                    field = NotificationPackVariantField.ITEM_RARITY,
                                                    value = "SECRET",
                                                ),
                                            ),
                                        emoji = "🔐",
                                        soloTitle = "Секретный дроп {item}",
                                        soloBody = "{player} нашёл секретный предмет",
                                        compact = "{item} · секрет",
                                        aggregateTitle = "Секретные дропы",
                                    ),
                                ),
                        ),
                    NotificationPackTemplateType.BOOSTER to
                        template(
                            emoji = "⚡",
                            title = "Бустер {booster}",
                            body = "{player} активировал {booster}",
                            compact = "{booster}",
                            aggregateTitle = "Бустеры",
                        ),
                    NotificationPackTemplateType.DUNGEON to
                        template(
                            emoji = "🏰",
                            title = "Dungeon {dungeon}",
                            body = "Открыт {dungeon}",
                            compact = "{dungeon}",
                            aggregateTitle = "Dungeon",
                        ),
                    NotificationPackTemplateType.RAID to
                        template(
                            emoji = "🛡",
                            title = "RAID",
                            body = "Локации {locations}",
                            compact = "RAID · {locations}",
                            aggregateTitle = "RAID",
                        ),
                    NotificationPackTemplateType.GLOBAL to
                        template(
                            emoji = "🌌",
                            title = "{event} началось",
                            body = "Глобальное событие {event}",
                            compact = "{event}",
                            aggregateTitle = "Глобальные события",
                        ),
                    NotificationPackTemplateType.PLAYER_OFFLINE to
                        template(
                            emoji = "👤",
                            title = "{player} вышел",
                            body = "{realm} · {lastSeen}",
                            compact = "{player} · offline",
                            aggregateTitle = "Игроки offline",
                        ),
                ),
            valueMappings = emptyMap(),
        )

    private fun template(
        emoji: String,
        title: String,
        body: String,
        compact: String,
        aggregateTitle: String,
    ): TelegramNotificationTemplate =
        TelegramNotificationTemplate(
            emoji = emoji,
            soloTitle = title,
            soloBody = body,
            compact = compact,
            aggregateTitle = aggregateTitle,
            variants = emptyList(),
        )

    private fun OutboundTelegramMessage.callbacks(): List<OutboundTelegramMessageAction.Callback> =
        actionRows
            .flatten()
            .filterIsInstance<OutboundTelegramMessageAction.Callback>()

    private companion object {
        const val SECRET_VARIANT_ID = "secret"
        val PACK_ID: UUID = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b74")
    }
}
