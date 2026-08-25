package com.mnemos.notification.adapter.out.persistence.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplate
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplateCondition
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplateVariant
import com.mnemos.notification.application.notification.personalization.model.NotificationPackValueForms
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

object ActiveNotificationPackPersistenceMapper {
    fun ActiveNotificationPack.toTemplateDocuments(): Map<String, Any?> =
        templates
            .mapKeys { (type, _) -> type.name }
            .mapValues { (_, template) -> template.toDocument() }

    fun ActiveNotificationPack.toValueMappingDocuments(): Map<String, Any?> =
        valueMappings
            .mapKeys { (key, _) -> key.name }
            .mapValues { (_, mappings) ->
                mappings.mapValues { (_, forms) -> forms.toDocument() }
            }

    fun ActiveNotificationPackEntity.toDomainOrNull(): ActiveNotificationPack? {
        val requiredSnapshotFields =
            listOf(
                personalizationId,
                mixedAggregateTitle,
                templates,
            )

        if (requiredSnapshotFields.all { it == null }) {
            return null
        }

        check(requiredSnapshotFields.all { it != null }) {
            "Persisted notification pack projection for user $userId contains a partial snapshot"
        }

        return try {
            ActiveNotificationPack(
                userId = UserId(userId),
                personalizationId = requireNotNull(personalizationId),
                mixedAggregateTitle = requireNotNull(mixedAggregateTitle),
                templates =
                    requireNotNull(templates)
                        .map { (rawType, rawTemplate) ->
                            NotificationType.valueOf(rawType) to requireDocument(rawTemplate).toDomainTemplate()
                        }.toMap(),
                valueMappings = valueMappings.orEmpty().toDomainValueMappings(),
            )
        } catch (exception: IllegalArgumentException) {
            throw IllegalStateException(
                "Persisted notification pack projection for user $userId contains invalid state",
                exception,
            )
        }
    }

    private fun NotificationPackTemplate.toDocument(): Map<String, Any?> =
        mapOf(
            "emoji" to emoji,
            "soloTitle" to soloTitle,
            "soloBody" to soloBody,
            "compact" to compact,
            "aggregateTitle" to aggregateTitle,
            "variants" to variants.map { variant -> variant.toDocument() },
        )

    private fun NotificationPackTemplateVariant.toDocument(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "conditions" to
                conditions.map { condition ->
                    mapOf(
                        "field" to condition.field.name,
                        "value" to condition.value,
                    )
                },
            "emoji" to emoji,
            "soloTitle" to soloTitle,
            "soloBody" to soloBody,
            "compact" to compact,
            "aggregateTitle" to aggregateTitle,
        )

    private fun NotificationPackValueForms.toDocument(): Map<String, Any?> =
        mapOf(
            "default" to default,
            "forms" to forms,
        )

    private fun Map<String, Any?>.toDomainTemplate(): NotificationPackTemplate =
        NotificationPackTemplate(
            emoji = stringOrNull("emoji"),
            soloTitle = requireString("soloTitle"),
            soloBody = requireString("soloBody"),
            compact = requireString("compact"),
            aggregateTitle = requireString("aggregateTitle"),
            variants =
                optionalList("variants").map { rawVariant ->
                    requireDocument(rawVariant).toDomainVariant()
                },
        )

    private fun Map<String, Any?>.toDomainVariant(): NotificationPackTemplateVariant =
        NotificationPackTemplateVariant(
            id = requireString("id"),
            conditions =
                requireList("conditions").map { rawCondition ->
                    val condition = requireDocument(rawCondition)

                    NotificationPackTemplateCondition(
                        field = NotificationPackVariantField.valueOf(condition.requireString("field")),
                        value = condition.requireString("value"),
                    )
                },
            emoji = stringOrNull("emoji"),
            soloTitle = requireString("soloTitle"),
            soloBody = requireString("soloBody"),
            compact = requireString("compact"),
            aggregateTitle = requireString("aggregateTitle"),
        )

    private fun Map<String, Any?>.toDomainValueMappings(): Map<NotificationPackValueKey, Map<String, NotificationPackValueForms>> =
        map { (rawKey, rawMappings) ->
            NotificationPackValueKey.valueOf(rawKey) to
                requireDocument(rawMappings).mapValues { (_, rawForms) ->
                    val forms = requireDocument(rawForms)

                    NotificationPackValueForms(
                        default = forms.requireString("default"),
                        forms =
                            forms.optionalDocument("forms").mapValues { (name, rawValue) ->
                                rawValue as? String
                                    ?: throw IllegalArgumentException("Notification pack value form $name must be a string")
                            },
                    )
                }
        }.toMap()

    private fun Map<String, Any?>.requireString(key: String): String =
        this[key] as? String
            ?: throw IllegalArgumentException("Notification pack field $key must be a string")

    private fun Map<String, Any?>.stringOrNull(key: String): String? =
        when (val value = this[key]) {
            null -> null
            is String -> value
            else -> throw IllegalArgumentException("Notification pack field $key must be a string")
        }

    private fun Map<String, Any?>.requireList(key: String): List<*> =
        this[key] as? List<*>
            ?: throw IllegalArgumentException("Notification pack field $key must be a list")

    private fun Map<String, Any?>.optionalList(key: String): List<*> =
        when (val value = this[key]) {
            null -> emptyList<Any?>()
            is List<*> -> value
            else -> throw IllegalArgumentException("Notification pack field $key must be a list")
        }

    private fun Map<String, Any?>.optionalDocument(key: String): Map<String, Any?> =
        when (val value = this[key]) {
            null -> emptyMap()
            else -> requireDocument(value)
        }

    @Suppress("UNCHECKED_CAST")
    private fun requireDocument(value: Any?): Map<String, Any?> =
        value as? Map<String, Any?>
            ?: throw IllegalArgumentException("Notification pack value must be an object")
}
