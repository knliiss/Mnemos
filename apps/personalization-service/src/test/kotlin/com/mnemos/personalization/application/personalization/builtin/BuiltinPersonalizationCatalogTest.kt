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
    fun `authored notification packs cover every supported event variant`() {
        val authored =
            BuiltinPersonalizationCatalog.definitions
                .filter { definition -> definition.type == PersonalizationType.NOTIFICATION_PACK }
                .mapNotNull { definition ->
                    BuiltinNotificationPackVariantSets.forPack(definition.catalogKey.value)
                        ?.let { variants -> definition.catalogKey.value to variants }
                }

        assertEquals(AUTHORED_PACK_COUNT, authored.size)

        authored.forEach { (key, variants) ->
            assertEquals(
                setOf("MYTHICAL", "SECRET"),
                variants.itemDrop
                    .flatMap { variant -> variant.conditions }
                    .filter { condition -> condition.field == NotificationPackVariantField.ITEM_RARITY }
                    .map { condition -> condition.value }
                    .toSet(),
                "Unexpected rarity coverage for $key",
            )

            val secretVariants =
                variants.itemDrop.filter { variant ->
                    variant.conditions.any { condition ->
                        condition.field == NotificationPackVariantField.ITEM_RARITY &&
                            condition.value == "SECRET"
                    }
                }

            assertEquals(2, secretVariants.size, "Expected sword and pet secret variants for $key")
            assertEquals(
                setOf("SWORD", "PET"),
                secretVariants
                    .flatMap { variant -> variant.conditions }
                    .filter { condition -> condition.field == NotificationPackVariantField.ITEM_TYPE }
                    .map { condition -> condition.value }
                    .toSet(),
                "Secret variants must be limited to swords and pets for $key",
            )
            assertTrue(
                secretVariants.all { variant ->
                    variant.conditions.any { condition -> condition.field == NotificationPackVariantField.ITEM_TYPE }
                },
                "Generic SECRET variant is not allowed for $key",
            )

            assertEquals(
                setOf("LUCK", "MONEY", "DAMAGE", "POWER"),
                variants.booster.singleConditionValues(NotificationPackVariantField.BOOSTER),
                "Unexpected booster coverage for $key",
            )
            assertEquals(
                setOf("EASY", "MEDIUM", "HARD", "EXPERT"),
                variants.dungeon.singleConditionValues(NotificationPackVariantField.DUNGEON),
                "Unexpected dungeon coverage for $key",
            )
            assertEquals(
                setOf("DARKNESS", "MOON", "ECLIPSE", "EXPLOSION"),
                variants.global.singleConditionValues(NotificationPackVariantField.GLOBAL_EVENT),
                "Unexpected global event coverage for $key",
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

    private fun List<com.mnemos.personalization.domain.personalization.model.NotificationTemplateVariant>.singleConditionValues(
        field: NotificationPackVariantField,
    ): Set<String> =
        map { variant ->
            variant.conditions.single { condition -> condition.field == field }.value
        }.toSet()

    private companion object {
        const val MIN_DEFINITIONS_PER_TYPE = 20
        const val AUTHORED_PACK_COUNT = 18
        const val COMET_CHAOS = "COMET_CHAOS"
    }
}
