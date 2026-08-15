package com.mnemos.personalization.contracts.event

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType

data class NotificationPackSnapshotV1(
    val mixedAggregateTitle: String,
    val templates: List<NotificationPackTemplateV1>,
) {
    init {
        val templateTypes = templates.map(NotificationPackTemplateV1::type)

        require(templateTypes.size == NotificationPackTemplateType.entries.size) {
            "Notification pack snapshot must define every template exactly once"
        }

        require(templateTypes.toSet() == NotificationPackTemplateType.entries.toSet()) {
            "Notification pack snapshot must define every template exactly once"
        }
    }
}
