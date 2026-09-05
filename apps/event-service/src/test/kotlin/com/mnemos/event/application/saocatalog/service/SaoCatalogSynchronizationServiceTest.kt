package com.mnemos.event.application.saocatalog.service

import com.mnemos.event.application.localization.model.SaoLocalizationPack
import com.mnemos.event.application.localization.port.out.SaoLocalizationPackRepository
import com.mnemos.event.application.saocatalog.model.SaoCatalogItem
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemPage
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemRarity
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemType
import com.mnemos.event.application.saocatalog.port.out.LegacyItemIdentity
import com.mnemos.event.application.saocatalog.port.out.LegacyItemIdentityRepository
import com.mnemos.event.application.saocatalog.port.out.SaoCatalogItemRepository
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.item.model.Item
import com.mnemos.event.domain.item.model.ItemId
import com.mnemos.event.domain.item.repository.ItemRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class SaoCatalogSynchronizationServiceTest {
    private val now = Instant.parse("2026-09-05T00:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `synchronization backfills canonical identities from localized aliases and valid existing keys`() {
        val localizationRepository =
            FakeLocalizationRepository(
                listOf(
                    pack(
                        locale = "ru_RU",
                        properties = mapOf("sao.item.l1_1.name" to "¨37efefЛесной клинок"),
                    ),
                    pack(
                        locale = "en_US",
                        properties = mapOf("sao.item.l1_1.name" to "¨37efefForest Blade"),
                    ),
                ),
            )
        val catalogRepository = FakeCatalogRepository()
        val itemRepository = FakeItemRepository()
        val aliasItemId = ItemId.generate()
        val canonicalItemId = ItemId.generate()
        val legacyRepository =
            FakeLegacyItemIdentityRepository(
                listOf(
                    LegacyItemIdentity(
                        itemId = aliasItemId,
                        name = "Forest Blade",
                        type = DomainItemType.SWORD,
                        gameKey = null,
                    ),
                    LegacyItemIdentity(
                        itemId = canonicalItemId,
                        name = "Old localized value",
                        type = DomainItemType.SWORD,
                        gameKey = "sao.item.l1_1.name",
                    ),
                ),
            )
        val service =
            SaoCatalogSynchronizationService(
                localizationRepository = localizationRepository,
                catalogRepository = catalogRepository,
                legacyItemIdentityRepository = legacyRepository,
                itemRepository = itemRepository,
                parser = SaoCatalogParser(),
                clock = clock,
            )

        val result = service.synchronize()

        assertEquals(1, result.catalogItems)
        assertEquals(0, result.unresolvedRarityItems)
        assertEquals(2, result.resolvedHistoricalItems)
        assertEquals(0, result.unresolvedHistoricalItems)
        assertEquals("sao.item.l1_1.name", legacyRepository.savedCanonicalIdentities[aliasItemId])
        assertEquals("sao.item.l1_1.name", legacyRepository.savedCanonicalIdentities[canonicalItemId])
        assertEquals(setOf("sao.item.l1_1.name"), catalogRepository.replacedItems.map { item -> item.gameKey }.toSet())
    }

    @Test
    fun `synchronization seeds only mythical and secret domain items`() {
        val localizationRepository =
            FakeLocalizationRepository(
                listOf(
                    pack(
                        locale = "ru_RU",
                        properties =
                            mapOf(
                                "sao.item.l1_1.name" to "¨37efefСекретный клинок",
                                "sao.item.auraPumpkin.name" to "¨a91925Мифическая аура",
                                "sao.item.l1_2.name" to "¨707070Обычный клинок",
                                "sao.item.icon_service.name" to "¨37efefСлужебный предмет",
                            ),
                    ),
                ),
            )
        val itemRepository = FakeItemRepository()
        val service =
            SaoCatalogSynchronizationService(
                localizationRepository = localizationRepository,
                catalogRepository = FakeCatalogRepository(),
                legacyItemIdentityRepository = FakeLegacyItemIdentityRepository(emptyList()),
                itemRepository = itemRepository,
                parser = SaoCatalogParser(),
                clock = clock,
            )

        service.synchronize()

        assertEquals(2, itemRepository.savedItems.size)
        assertEquals(
            setOf(
                "sao.item.l1_1.name" to DomainItemRarity.SECRET,
                "sao.item.auraPumpkin.name" to DomainItemRarity.MYTHICAL,
            ),
            itemRepository.savedItems.map { item -> item.gameKey to item.rarity }.toSet(),
        )
    }

    @Test
    fun `ambiguous historical aliases stay unresolved instead of choosing an arbitrary item`() {
        val localizationRepository =
            FakeLocalizationRepository(
                listOf(
                    pack(
                        locale = "ru_RU",
                        properties =
                            mapOf(
                                "sao.item.l1_1.name" to "¨37efefОдинаковое имя",
                                "sao.item.l1_2.name" to "¨37efefОдинаковое имя",
                            ),
                    ),
                ),
            )
        val catalogRepository = FakeCatalogRepository()
        val legacyRepository =
            FakeLegacyItemIdentityRepository(
                listOf(
                    LegacyItemIdentity(
                        itemId = ItemId.generate(),
                        name = "Одинаковое имя",
                        type = DomainItemType.SWORD,
                        gameKey = null,
                    ),
                ),
            )
        val service =
            SaoCatalogSynchronizationService(
                localizationRepository = localizationRepository,
                catalogRepository = catalogRepository,
                legacyItemIdentityRepository = legacyRepository,
                itemRepository = FakeItemRepository(),
                parser = SaoCatalogParser(),
                clock = clock,
            )

        val result = service.synchronize()

        assertEquals(2, result.catalogItems)
        assertEquals(0, result.resolvedHistoricalItems)
        assertEquals(1, result.unresolvedHistoricalItems)
        assertTrue(legacyRepository.savedCanonicalIdentities.isEmpty())
    }

    @Test
    fun `empty localization storage does not replace catalog or touch historical identities`() {
        val localizationRepository = FakeLocalizationRepository(emptyList())
        val catalogRepository = FakeCatalogRepository()
        val legacyRepository =
            FakeLegacyItemIdentityRepository(
                listOf(
                    LegacyItemIdentity(
                        itemId = ItemId.generate(),
                        name = "Legacy item",
                        type = DomainItemType.SWORD,
                        gameKey = null,
                    ),
                ),
            )
        val service =
            SaoCatalogSynchronizationService(
                localizationRepository = localizationRepository,
                catalogRepository = catalogRepository,
                legacyItemIdentityRepository = legacyRepository,
                itemRepository = FakeItemRepository(),
                parser = SaoCatalogParser(),
                clock = clock,
            )

        val result = service.synchronize()

        assertEquals(SaoCatalogSynchronizationResult.EMPTY, result)
        assertFalse(catalogRepository.replaceCalled)
        assertTrue(legacyRepository.savedCanonicalIdentities.isEmpty())
    }

    private fun pack(
        locale: String,
        properties: Map<String, String>,
    ): SaoLocalizationPack =
        SaoLocalizationPack(
            locale = locale,
            hash = "hash-$locale",
            properties = properties,
            updatedAt = now,
        )

    private class FakeLocalizationRepository(
        private val packs: List<SaoLocalizationPack>,
    ) : SaoLocalizationPackRepository {
        override fun findByLocale(locale: String): SaoLocalizationPack? = packs.firstOrNull { pack -> pack.locale == locale }

        override fun findAll(): List<SaoLocalizationPack> = packs

        override fun save(pack: SaoLocalizationPack): SaoLocalizationPack = pack
    }

    private class FakeCatalogRepository : SaoCatalogItemRepository {
        var replaceCalled = false
        var replacedItems: List<SaoCatalogItem> = emptyList()

        override fun replaceAll(items: Collection<SaoCatalogItem>) {
            replaceCalled = true
            replacedItems = items.toList()
        }

        override fun search(
            search: String?,
            type: SaoCatalogItemType?,
            rarity: SaoCatalogItemRarity?,
            locationId: Int?,
            page: Int,
            size: Int,
        ): SaoCatalogItemPage =
            SaoCatalogItemPage(
                items = emptyList(),
                page = page,
                size = size,
                totalItems = 0,
                totalPages = 0,
            )
    }

    private class FakeItemRepository : ItemRepository {
        val savedItems = mutableListOf<Item>()

        override fun getOrCreate(
            itemKey: String?,
            itemName: String,
            type: DomainItemType,
            rarity: DomainItemRarity,
        ): Item {
            val item =
                Item(
                    gameKey = itemKey,
                    name = itemName,
                    type = type,
                    rarity = rarity,
                )

            savedItems += item

            return item
        }
    }

    private class FakeLegacyItemIdentityRepository(
        private val items: List<LegacyItemIdentity>,
    ) : LegacyItemIdentityRepository {
        val savedCanonicalIdentities = mutableMapOf<ItemId, String>()

        override fun findAll(): List<LegacyItemIdentity> = items

        override fun saveCanonicalIdentity(
            itemId: ItemId,
            gameKey: String,
        ) {
            savedCanonicalIdentities[itemId] = gameKey
        }
    }
}
