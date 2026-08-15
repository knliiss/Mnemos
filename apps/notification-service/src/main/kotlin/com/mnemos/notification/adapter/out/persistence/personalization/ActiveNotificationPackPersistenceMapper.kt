package com.mnemos.notification.adapter.out.persistence.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.notification.application.notification.personalization.model.ActiveNotificationPack
import com.mnemos.notification.application.notification.personalization.model.NotificationPackTemplate

object ActiveNotificationPackPersistenceMapper {
    fun ActiveNotificationPack.toTemplateDocuments(): Map<String, Any?> =
        templates
            .mapKeys { (type, _) -> type.name }
            .mapValues { (_, template) -> template.toDocument() }

    fun ActiveNotificationPackEntity.toDomainOrNull(): ActiveNotificationPack? {
        val snapshotFields =
            listOf(
                personalizationId,
                mixedAggregateTitle,
                templates,
            )

        if (snapshotFields.all { it == null }) {
            return null
        }

        check(snapshotFields.all { it != null }) {
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
                            NotificationType.valueOf(rawType) to requireDocument(rawTemplate).toDomain()
                        }.toMap(),
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
        )

    private fun Map<String, Any?>.toDomain(): NotificationPackTemplate =
        NotificationPackTemplate(
            emoji = stringOrNull("emoji"),
            soloTitle = requireString("soloTitle"),
            soloBody = requireString("soloBody"),
            compact = requireString("compact"),
            aggregateTitle = requireString("aggregateTitle"),
        )

    private fun Map<String, Any?>.requireString(key: String): String =
        this[key] as? String
            ?: throw IllegalArgumentException("Notification pack field $key must be a string")

    private fun Map<String, Any?>.stringOrNull(key: String): String? =
        when (val value = this[key]) {
            null -> null
            is String -> value
            else -> throw IllegalArgumentException("Notification pack field $key must be a string")
        }

    @Suppress("UNCHECKED_CAST")
    private fun requireDocument(value: Any?): Map<String, Any?> =
        value as? Map<String, Any?>
            ?: throw IllegalArgumentException("Notification pack template must be an object")
}
