package com.mnemos.notification.application.notification.personalization.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.application.notification.model.NotificationType
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import java.util.UUID

data class ActiveNotificationPack(
    val userId: UserId,
    val personalizationId: UUID,
    val mixedAggregateTitle: String,
    val templates: Map<NotificationType, NotificationPackTemplate>,
    val valueMappings: Map<NotificationPackValueKey, Map<String, NotificationPackValueForms>> = emptyMap(),
) {
    init {
        val requiredTypes = NotificationType.entries.toSet() - NotificationType.PING
        require(templates.keys.containsAll(requiredTypes)) {
            "Active notification pack must define every required notification type"
        }
    }
}
