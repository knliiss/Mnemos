package com.mnemos.notification.application.notification.personalization.model

import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

data class NotificationPackTemplate(
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
    val variants: List<NotificationPackTemplateVariant> = emptyList(),
)

data class NotificationPackTemplateVariant(
    val id: String,
    val conditions: List<NotificationPackTemplateCondition>,
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
)

data class NotificationPackTemplateCondition(
    val field: NotificationPackVariantField,
    val value: String,
)

data class NotificationPackValueForms(
    val default: String,
    val forms: Map<String, String>,
) {
    fun resolve(form: String?): String? =
        if (form == null || form == "default") {
            default
        } else {
            forms[form]
        }
}
