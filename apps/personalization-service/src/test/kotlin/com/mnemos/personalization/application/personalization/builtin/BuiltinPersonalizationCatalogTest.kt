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
    fun `built in notification pack variants stay sparse so base templates remain reachable`() {
        val packs =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }

        packs.forEach { definition ->
            val content = definition.content as PersonalizationContent.NotificationPack
            val key = definition.catalogKey.value

            val itemRarities =
                content.templates
                    .getValue(NotificationTemplateKey.ITEM_DROP)
                    .variants
                    .conditionValues(NotificationPackVariantField.ITEM_RARITY)
            val boosters =
                content.templates
                    .getValue(NotificationTemplateKey.BOOSTER)
                    .variants
                    .conditionValues(NotificationPackVariantField.BOOSTER)
            val dungeons =
                content.templates
                    .getValue(NotificationTemplateKey.DUNGEON)
                    .variants
                    .conditionValues(NotificationPackVariantField.DUNGEON)

            assertTrue(
                itemRarities != setOf("MYTHICAL", "SECRET"),
                "Item-drop variants must not shadow the base template for $key",
            )
            assertTrue(
                boosters != setOf("LUCK", "MONEY", "DAMAGE", "POWER"),
                "Booster variants must not shadow the base template for $key",
            )
            assertTrue(
                dungeons != setOf("EASY", "MEDIUM", "HARD", "EXPERT"),
                "Dungeon variants must not shadow the base template for $key",
            )
        }
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
        const val COMET_CHAOS = "COMET_CHAOS"
    }
}
