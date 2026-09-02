package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.pack.NotificationPackCallbackData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackDetailResult
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuMode
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuScreenData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackStoreResult
import com.mnemos.telegram.bot.application.pack.model.OwnedNotificationPacksResult
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackDetail
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackPreview
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackStoreItem
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackStorePage
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplate
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplateCondition
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplateVariant
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationValueForms
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
            screenData(
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
    fun `detail renders standard and aggregate previews together`() {
        val pack =
            detailPack(
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
        assertTrue(message.text.contains("✨ Дроп Вечный клинок"))
        assertTrue(message.text.contains("north выбил Мифический меч"))
        assertTrue(message.text.contains("🧩 <b>Агрегированное сообщение</b>"))
        assertTrue(message.text.contains("✨ Дропы"))
        assertTrue(message.text.contains("✨ north: Вечный клинок"))
        assertTrue(message.text.contains("25 PRIME"))
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, purchase.style)
    }

    @Test
    fun `detail exposes the same event type and variant controls as Mini App`() {
        val message = NotificationPackMenuMessageRenderer.render(detailData(detailPack()))
        val callbacks = message.callbacks()

        NotificationPackTemplateType.entries.forEach { type ->
            assertTrue(
                callbacks.any { action -> action.data == NotificationPackCallbackData.template(type) },
                "Missing Telegram preview switch for $type",
            )
        }

        assertTrue(callbacks.any { action -> action.data == NotificationPackCallbackData.BASE_VARIANT })
        assertTrue(callbacks.any { action -> action.data == NotificationPackCallbackData.variant(SECRET_VARIANT_ID) })
    }

    @Test
    fun `selected conditional variant changes both preview formats`() {
        val data =
            detailData(
                pack = detailPack(),
                selectedVariantId = SECRET_VARIANT_ID,
            )

        val message = NotificationPackMenuMessageRenderer.render(data)

        assertTrue(message.text.contains("Вариант: <b>Секретный</b>"))
        assertTrue(message.text.contains("<b>🗝 Секрет: Вечный клинок</b>"))
        assertTrue(message.text.contains("north нашёл Секретный предмет"))
        assertTrue(message.text.contains("<b>🗝 Секретные дропы</b>"))
        assertTrue(message.text.contains("🗝 north: секрет Вечный клинок"))
    }

    @Test
    fun `switching event type keeps both preview formats on selected type`() {
        val data =
            detailData(
                pack = detailPack(),
                selectedTemplateType = NotificationPackTemplateType.GLOBAL,
            )

        val message = NotificationPackMenuMessageRenderer.render(data)

        assertTrue(message.text.contains("Событие: <b>Глобальное событие</b>"))
        assertTrue(message.text.contains("<b>🌌 Глобальное: Затмение</b>"))
        assertTrue(message.text.contains("Началось событие Затмение"))
        assertTrue(message.text.contains("<b>🌌 Глобальные</b>"))
        assertTrue(message.text.contains("🌌 Затмение"))
    }

    @Test
    fun `owned pack detail renders success apply action`() {
        val pack = detailPack(owned = true)
        val data = detailData(pack)

        val message = NotificationPackMenuMessageRenderer.render(data)
        val apply = message.callbacks().single { action -> action.data == NotificationPackCallbackData.apply(PACK_ID) }

        assertTrue(message.text.contains("Статус: <b>куплен</b>"))
        assertEquals("✅ Применить", apply.label)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, apply.style)
    }

    @Test
    fun `missing detail returns to store`() {
        val data =
            screenData(
                mode = NotificationPackMenuMode.DETAIL,
                detail = NotificationPackDetailResult.NotFound,
                selectedPersonalizationId = PACK_ID,
            )

        val message = NotificationPackMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Этот пак больше недоступен в магазине"))
        assertTrue(callbacks.any { action -> action.data == NotificationPackCallbackData.STORE })
    }

    @Test
    fun `owned list highlights active pack and keeps apply callback`() {
        val data =
            screenData(
                mode = NotificationPackMenuMode.OWNED,
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
            screenData(
                mode = NotificationPackMenuMode.STORE,
                store = NotificationPackStoreResult.Unavailable,
            )

        val message = NotificationPackMenuMessageRenderer.render(data)
        val callbacks = message.callbacks()

        assertTrue(message.text.contains("Магазин паков временно недоступен"))
        assertTrue(callbacks.any { action -> action.data == NotificationPackCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
    }

    private fun detailData(
        pack: TelegramNotificationPackDetail,
        selectedTemplateType: NotificationPackTemplateType = NotificationPackTemplateType.ITEM_DROP,
        selectedVariantId: String? = null,
    ): TelegramMenuScreenData.Packs =
        screenData(
            mode = NotificationPackMenuMode.DETAIL,
            detail = NotificationPackDetailResult.Available(pack),
            selectedPersonalizationId = PACK_ID,
            selectedTemplateType = selectedTemplateType,
            selectedVariantId = selectedVariantId,
        )

    private fun screenData(
        mode: NotificationPackMenuMode,
        store: NotificationPackStoreResult? = null,
        detail: NotificationPackDetailResult? = null,
        owned: OwnedNotificationPacksResult? = null,
        selectedPersonalizationId: UUID? = null,
        selectedTemplateType: NotificationPackTemplateType = NotificationPackTemplateType.ITEM_DROP,
        selectedVariantId: String? = null,
    ): TelegramMenuScreenData.Packs =
        TelegramMenuScreenData.Packs(
            NotificationPackMenuScreenData(
                mode = mode,
                store = store,
                detail = detail,
                owned = owned,
                selectedPersonalizationId = selectedPersonalizationId,
                selectedTemplateType = selectedTemplateType,
                selectedVariantId = selectedVariantId,
                feedback = null,
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
        )

    private fun detailPack(
        name: String = "Crystal Pack",
        author: String = "Mnemos",
        owned: Boolean = false,
        active: Boolean = false,
    ): TelegramNotificationPackDetail =
        TelegramNotificationPackDetail(
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
            mixedAggregateTitle = "Сводка Mnemos",
            templates =
                mapOf(
                    NotificationPackTemplateType.ITEM_DROP to itemDropTemplate(),
                    NotificationPackTemplateType.BOOSTER to
                        template(
                            emoji = "⚡",
                            soloTitle = "Бустер {booster}",
                            soloBody = "{player} активировал {booster}",
                            compact = "{player}: {booster}",
                            aggregateTitle = "Бустеры",
                        ),
                    NotificationPackTemplateType.DUNGEON to
                        template(
                            emoji = "🏰",
                            soloTitle = "Данж {dungeon}",
                            soloBody = "Открыт {dungeon}",
                            compact = "{dungeon}",
                            aggregateTitle = "Данжи",
                        ),
                    NotificationPackTemplateType.RAID to
                        template(
                            emoji = "🛡",
                            soloTitle = "Рейд",
                            soloBody = "Локации {locations}",
                            compact = "Рейд · {locations}",
                            aggregateTitle = "Рейды",
                        ),
                    NotificationPackTemplateType.GLOBAL to
                        template(
                            emoji = "🌌",
                            soloTitle = "Глобальное: {event}",
                            soloBody = "Началось событие {event}",
                            compact = "{event}",
                            aggregateTitle = "Глобальные",
                        ),
                    NotificationPackTemplateType.PLAYER_OFFLINE to
                        template(
                            emoji = "👤",
                            soloTitle = "{player} вышел",
                            soloBody = "{realm} · {lastSeen}",
                            compact = "{player} · {realm}",
                            aggregateTitle = "Оффлайн",
                        ),
                ),
            valueMappings =
                mapOf(
                    NotificationPackValueKey.ITEM_RARITY to
                        mapOf(
                            "MYTHICAL" to TelegramNotificationValueForms("Мифический", emptyMap()),
                            "SECRET" to TelegramNotificationValueForms("Секретный", emptyMap()),
                        ),
                ),
        )

    private fun itemDropTemplate(): TelegramNotificationTemplate =
        TelegramNotificationTemplate(
            emoji = "✨",
            soloTitle = "Дроп {item}",
            soloBody = "{player} выбил {rarity} {itemType}",
            compact = "{player}: {item}",
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
                        emoji = "🗝",
                        soloTitle = "Секрет: {item}",
                        soloBody = "{player} нашёл {rarity} предмет",
                        compact = "{player}: секрет {item}",
                        aggregateTitle = "Секретные дропы",
                    ),
                ),
        )

    private fun template(
        emoji: String,
        soloTitle: String,
        soloBody: String,
        compact: String,
        aggregateTitle: String,
    ): TelegramNotificationTemplate =
        TelegramNotificationTemplate(
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
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
