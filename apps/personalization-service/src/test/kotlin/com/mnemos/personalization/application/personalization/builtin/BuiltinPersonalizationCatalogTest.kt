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
    fun `item drop variants keep mythical on base and type every secret variant`() {
        val packs =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }

        packs.forEach { definition ->
            val content = definition.content as PersonalizationContent.NotificationPack
            val itemVariants = content.templates.getValue(NotificationTemplateKey.ITEM_DROP).variants
            val key = definition.catalogKey.value

            assertTrue(
                "MYTHICAL" !in itemVariants.conditionValues(NotificationPackVariantField.ITEM_RARITY),
                "MYTHICAL drops must keep using the base template for $key",
            )

            val secretVariants =
                itemVariants.filter { variant ->
                    variant.conditions.any { condition ->
                        condition.field == NotificationPackVariantField.ITEM_RARITY &&
                            condition.value == "SECRET"
                    }
                }

            assertTrue(
                secretVariants.all { variant ->
                    val itemTypes =
                        variant.conditions
                            .filter { condition -> condition.field == NotificationPackVariantField.ITEM_TYPE }
                            .map { condition -> condition.value }
                            .toSet()

                    itemTypes.size == 1 && itemTypes.single() in SECRET_ITEM_TYPES
                },
                "SECRET variants must target exactly SWORD or PET for $key",
            )
        }
    }

    @Test
    fun `booster dungeon and global variants keep their base templates reachable`() {
        val packs =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }

        packs.forEach { definition ->
            val content = definition.content as PersonalizationContent.NotificationPack
            val key = definition.catalogKey.value
            val boosterVariants = content.templates.getValue(NotificationTemplateKey.BOOSTER).variants
            val dungeonVariants = content.templates.getValue(NotificationTemplateKey.DUNGEON).variants
            val globalVariants = content.templates.getValue(NotificationTemplateKey.GLOBAL).variants

            assertTrue(
                boosterVariants.conditionValues(NotificationPackVariantField.BOOSTER) != ALL_BOOSTERS,
                "Booster variants must not shadow the base template for $key",
            )
            assertTrue(
                dungeonVariants.conditionValues(NotificationPackVariantField.DUNGEON) != ALL_DUNGEONS,
                "Dungeon variants must not shadow the base template for $key",
            )
            assertTrue(
                globalVariants.conditionValues(NotificationPackVariantField.GLOBAL_EVENT) != ALL_GLOBAL_EVENTS,
                "Global variants must not shadow the base template for $key",
            )
        }
    }

    @Test
    fun `legacy drop identities stay intact`() {
        val packs =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }
                .associateBy { definition -> definition.catalogKey.value }

        val dep = packs.getValue("notification.dep").content as PersonalizationContent.NotificationPack
        val depDrop = dep.templates.getValue(NotificationTemplateKey.ITEM_DROP)
        assertEquals("💸", depDrop.emoji)
        assertEquals("Деп окупился", depDrop.soloTitle)
        assertTrue(depDrop.soloBody.contains("Последний деп ради дропа того стоил! 💸"))
        assertTrue(depDrop.variants.all { variant -> variant.emoji == "💸" })

        val gospodin = packs.getValue("notification.gospodin").content as PersonalizationContent.NotificationPack
        val gospodinDrop = gospodin.templates.getValue(NotificationTemplateKey.ITEM_DROP)
        assertEquals("👑", gospodinDrop.emoji)
        assertEquals("Ваша удача безгранична", gospodinDrop.soloTitle)
        assertTrue(gospodinDrop.variants.all { variant -> variant.emoji == "👑" })
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
        val SECRET_ITEM_TYPES = setOf("SWORD", "PET")
        val ALL_BOOSTERS = setOf("LUCK", "MONEY", "DAMAGE", "POWER")
        val ALL_DUNGEONS = setOf("EASY", "MEDIUM", "HARD", "EXPERT")
        val ALL_GLOBAL_EVENTS = setOf("DARKNESS", "MOON", "ECLIPSE", "EXPLOSION", COMET_CHAOS)
        const val COMET_CHAOS = "COMET_CHAOS"
    }
}
