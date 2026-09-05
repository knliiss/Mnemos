package com.mnemos.event.application.saocatalog.service

import com.mnemos.event.application.localization.model.SaoLocalizationPack
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemRarity
import com.mnemos.event.application.saocatalog.model.SaoCatalogItemType
import com.mnemos.event.domain.item.model.DomainItemType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class SaoCatalogParserTest {
    private val parser = SaoCatalogParser()
    private val now = Instant.parse("2026-09-05T00:00:00Z")

    @Test
    fun `catalog derives explicit rarity style color and safe locations without guessing`() {
        val parsed =
            parser.parse(
                packs =
                    listOf(
                        pack(
                            locale = "ru_RU",
                            properties =
                                mapOf(
                                    "sao.location.17.name" to "Подводный храм",
                                    "sao.item.l17_1.name" to "¨37efefКлинок глубин",
                                    "sao.item.pet_l17_1.name" to "¨a91925Морской дракон",
                                    "sao.item.book_l17_1.name" to "¨efac00Книга прилива",
                                    "sao.item.auraDeep.name" to "¨707070Глубокая аура",
                                    "sao.item.d1_1.name" to "¨2a66bdРеликвия",
                                ),
                        ),
                    ),
                now = now,
            )

        val weapon = parsed.items.single { item -> item.gameKey == "sao.item.l17_1.name" }
        val pet = parsed.items.single { item -> item.gameKey == "sao.item.pet_l17_1.name" }
        val book = parsed.items.single { item -> item.gameKey == "sao.item.book_l17_1.name" }
        val aura = parsed.items.single { item -> item.gameKey == "sao.item.auraDeep.name" }
        val jewelry = parsed.items.single { item -> item.gameKey == "sao.item.d1_1.name" }

        assertEquals(SaoCatalogItemType.WEAPON, weapon.type)
        assertEquals("Клинок глубин", weapon.defaultName)
        assertEquals(SaoCatalogItemRarity.SECRET, weapon.rarity)
        assertEquals("#37efef", weapon.styleColor)
        assertEquals(setOf(17), weapon.locationIds)

        assertEquals(SaoCatalogItemType.PET, pet.type)
        assertEquals(SaoCatalogItemRarity.MYTHICAL, pet.rarity)
        assertEquals("#a91925", pet.styleColor)
        assertEquals(setOf(17), pet.locationIds)

        assertEquals(SaoCatalogItemRarity.LEGENDARY, book.rarity)
        assertEquals("#efac00", book.styleColor)
        assertEquals(setOf(17), book.locationIds)
        assertEquals("#707070", aura.styleColor)
        assertEquals("#2a66bd", jewelry.styleColor)
        assertTrue(aura.locationIds.isEmpty())
        assertTrue(jewelry.locationIds.isEmpty())
    }

    @Test
    fun `location-like ids are ignored when localization does not define that location`() {
        val parsed =
            parser.parse(
                packs =
                    listOf(
                        pack(
                            locale = "ru_RU",
                            properties =
                                mapOf(
                                    "sao.location.17.name" to "Подводный храм",
                                    "sao.item.l999_1.name" to "¨37efefНеизвестный клинок",
                                    "sao.item.book_l999_1.name" to "¨a91925Неизвестная книга",
                                ),
                        ),
                    ),
                now = now,
            )

        assertTrue(parsed.items.all { item -> item.locationIds.isEmpty() })
    }

    @Test
    fun `unknown rarity stays unresolved while known style color is preserved`() {
        val parsed =
            parser.parse(
                packs =
                    listOf(
                        pack(
                            locale = "ru_RU",
                            properties = mapOf("sao.item.l1_1.name" to "¨123456Без известной редкости"),
                        ),
                    ),
                now = now,
            )

        val item = parsed.items.single()

        assertNull(item.rarity)
        assertEquals("#123456", item.styleColor)
        assertEquals(setOf(item.gameKey), parsed.unresolvedRarityKeys)
    }

    @Test
    fun `historical aliases resolve names from every localization`() {
        val parsed =
            parser.parse(
                packs =
                    listOf(
                        pack(
                            locale = "ru_RU",
                            properties = mapOf("sao.item.l1_1.name" to "¨37efefТень леса"),
                        ),
                        pack(
                            locale = "en_US",
                            properties = mapOf("sao.item.l1_1.name" to "¨37efefForest Shadow"),
                        ),
                    ),
                now = now,
            )

        assertEquals(
            "sao.item.l1_1.name",
            parsed.aliases[
                LegacyItemAlias(
                    normalizedName = "тень леса",
                    type = DomainItemType.SWORD,
                ),
            ],
        )
        assertEquals(
            "sao.item.l1_1.name",
            parsed.aliases[
                LegacyItemAlias(
                    normalizedName = "forest shadow",
                    type = DomainItemType.SWORD,
                ),
            ],
        )
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
}
