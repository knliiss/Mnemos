package com.mnemos.personalization.application.personalization.builtin

import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BuiltinPersonalizationCatalogTest {
    @Test
    fun `catalog keeps at least twenty definitions per personalization type`() {
        PersonalizationType.entries.forEach { type ->
            val count = BuiltinPersonalizationCatalog.definitions.count { definition -> definition.type == type }

            assertTrue(
                count >= MIN_DEFINITIONS_PER_TYPE,
                "Expected at least $MIN_DEFINITIONS_PER_TYPE built-ins for $type but found $count",
            )
        }
    }

    @Test
    fun `catalog keys are unique`() {
        val definitions = BuiltinPersonalizationCatalog.definitions
        val uniqueKeys = definitions.map { definition -> definition.catalogKey }.toSet()

        assertEquals(definitions.size, uniqueKeys.size)
    }

    @Test
    fun `legacy inspired notification pack concepts are present`() {
        val packs =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }
                .associateBy { definition -> definition.catalogKey.value }

        val expected =
            mapOf(
                "notification.dzhamshut" to "Джамшут",
                "notification.shbshnik" to "ШБшник",
                "notification.dep" to "Деп",
                "notification.night-shift" to "Сон",
                "notification.suspicious-luck" to "Удача",
                "notification.kaif" to "Кайф",
                "notification.gospodin" to "Господин",
                "notification.lavash" to "Лаваш",
                "notification.zaebalo" to "Заебало",
            )

        expected.forEach { (key, name) ->
            assertEquals(name, packs[key]?.name, "Expected built-in notification pack $key")
        }
    }

    @Test
    fun `fresh notification pack concepts are present`() {
        val packs =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }
                .associateBy { definition -> definition.catalogKey.value }

        val expected =
            mapOf(
                "notification.casino" to "Казино",
                "notification.support" to "Техподдержка",
                "notification.gossip" to "Сплетни",
                "notification.commentator" to "Комментатор",
                "notification.deadline" to "Дедлайн",
            )

        expected.forEach { (key, name) ->
            assertEquals(name, packs[key]?.name, "Expected fresh notification pack $key")
        }
    }

    @Test
    fun `authored notification packs keep the complete event matrix while base drops stay reachable`() {
        val authored =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }
                .mapNotNull { definition ->
                    BuiltinNotificationPackVariantSets
                        .forPack(definition.catalogKey.value)
                        ?.let { variants -> definition to variants }
                }

        assertEquals(AUTHORED_PACK_COUNT, authored.size)

        authored.forEach { (definition, _) ->
            val key = definition.catalogKey.value
            val content = definition.content as PersonalizationContent.NotificationPack
            val itemVariants = content.templates.getValue(NotificationTemplateKey.ITEM_DROP).variants
            val boosterVariants = content.templates.getValue(NotificationTemplateKey.BOOSTER).variants
            val dungeonVariants = content.templates.getValue(NotificationTemplateKey.DUNGEON).variants
            val globalVariants = content.templates.getValue(NotificationTemplateKey.GLOBAL).variants

            assertTrue(
                itemVariants.none { variant ->
                    variant.conditions.any { condition ->
                        condition.field == NotificationPackVariantField.ITEM_RARITY &&
                            condition.value == "MYTHICAL"
                    } &&
                        variant.conditions.none { condition ->
                            condition.field == NotificationPackVariantField.ITEM_TYPE
                        }
                },
                "Generic MYTHICAL variants must not shadow the base drop copy for $key",
            )

            val secretVariants =
                itemVariants.filter { variant ->
                    variant.conditions.any { condition ->
                        condition.field == NotificationPackVariantField.ITEM_RARITY &&
                            condition.value == "SECRET"
                    }
                }

            assertEquals(2, secretVariants.size, "Expected SECRET+SWORD and SECRET+PET for $key")
            assertEquals(
                setOf("SWORD", "PET"),
                secretVariants.conditionValues(NotificationPackVariantField.ITEM_TYPE),
                "Secret variants must be limited to swords and pets for $key",
            )
            assertTrue(
                secretVariants.all { variant ->
                    variant.conditions.any { condition ->
                        condition.field == NotificationPackVariantField.ITEM_TYPE
                    }
                },
                "Generic SECRET variant is not allowed for $key",
            )

            assertEquals(
                setOf("LUCK", "MONEY", "DAMAGE", "POWER"),
                boosterVariants.conditionValues(NotificationPackVariantField.BOOSTER),
                "Unexpected booster coverage for $key",
            )
            assertEquals(
                setOf("EASY", "MEDIUM", "HARD", "EXPERT"),
                dungeonVariants.conditionValues(NotificationPackVariantField.DUNGEON),
                "Unexpected dungeon coverage for $key",
            )
            assertEquals(
                setOf("DARKNESS", "MOON", "ECLIPSE", "EXPLOSION", COMET_CHAOS),
                globalVariants.conditionValues(NotificationPackVariantField.GLOBAL_EVENT),
                "Unexpected global event coverage for $key",
            )
        }
    }

    @Test
    fun `secret drop variants are limited to sword and pet across every built in pack`() {
        val packs =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }

        packs.forEach { definition ->
            val content = definition.content as PersonalizationContent.NotificationPack
            val variants = content.templates.getValue(NotificationTemplateKey.ITEM_DROP).variants

            variants
                .filter { variant ->
                    variant.conditions.any { condition ->
                        condition.field == NotificationPackVariantField.ITEM_RARITY &&
                            condition.value == "SECRET"
                    }
                }
                .forEach { variant ->
                    val itemType =
                        variant.conditions
                            .singleOrNull { condition ->
                                condition.field == NotificationPackVariantField.ITEM_TYPE
                            }
                            ?.value

                    assertTrue(
                        itemType == "SWORD" || itemType == "PET",
                        "SECRET variant ${variant.id} must be SECRET+SWORD or SECRET+PET for ${definition.catalogKey.value}",
                    )
                }
        }
    }

    @Test
    fun `drop emoji inheritance is curated instead of forced for every pack`() {
        val packs =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }

        val dropVariants =
            packs.flatMap { definition ->
                val content = definition.content as PersonalizationContent.NotificationPack
                val template = content.templates.getValue(NotificationTemplateKey.ITEM_DROP)
                template.variants.map { variant -> template.emoji to variant.emoji }
            }

        val inherited = dropVariants.count { (base, variant) -> base == variant }
        val custom = dropVariants.count { (base, variant) -> base != variant }

        assertTrue(inherited > custom, "Most drop variants should keep the pack's signature emoji")
        assertTrue(custom > 0, "Some packs should intentionally keep subtype-specific drop emoji")

        listOf("notification.dep", "notification.gospodin").forEach { key ->
            val definition = packs.single { definition -> definition.catalogKey.value == key }
            val content = definition.content as PersonalizationContent.NotificationPack
            val template = content.templates.getValue(NotificationTemplateKey.ITEM_DROP)

            assertTrue(
                template.variants.all { variant -> variant.emoji == template.emoji },
                "$key must keep its signature drop emoji in sword/pet variants",
            )
        }
    }

    @Test
    fun `dep keeps its signature drop copy`() {
        val dep =
            BuiltinPersonalizationCatalog.definitions
                .single { definition -> definition.catalogKey.value == "notification.dep" }
        val content = dep.content as PersonalizationContent.NotificationPack
        val drop = content.templates.getValue(NotificationTemplateKey.ITEM_DROP)

        assertEquals("💸", drop.emoji)
        assertEquals("Деп окупился", drop.soloTitle)
    }

    @Test
    fun `every built-in notification pack defines comet chaos copy`() {
        val packs =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }

        packs.forEach { definition ->
            val content = definition.content as PersonalizationContent.NotificationPack
            val globalTemplate = content.templates.getValue(NotificationTemplateKey.GLOBAL)
            val cometVariant =
                globalTemplate.variants.singleOrNull { variant ->
                    variant.conditions.any { condition ->
                        condition.field == NotificationPackVariantField.GLOBAL_EVENT &&
                            condition.value == COMET_CHAOS
                    }
                }
            val cometValue =
                content.valueMappings[NotificationPackValueKey.GLOBAL_EVENT]
                    ?.get(COMET_CHAOS)

            assertNotNull(
                cometVariant,
                "Built-in pack ${definition.catalogKey.value} must define Comet Chaos copy",
            )
            assertNotNull(
                cometValue,
                "Built-in pack ${definition.catalogKey.value} must define Comet Chaos value mapping",
            )
        }
    }

    private fun List<com.mnemos.personalization.domain.personalization.model.NotificationTemplateVariant>.conditionValues(
        field: NotificationPackVariantField,
    ): Set<String> =
        flatMap { variant -> variant.conditions }
            .filter { condition -> condition.field == field }
            .map { condition -> condition.value }
            .toSet()

    private companion object {
        const val MIN_DEFINITIONS_PER_TYPE = 20
        const val AUTHORED_PACK_COUNT = 18
        const val COMET_CHAOS = "COMET_CHAOS"
    }
}
