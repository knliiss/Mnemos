package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

data class NotificationPackEditorDescriptorResponse(
    val mixedAggregateTitleDefault: String,
    val maxTextLength: Int,
    val maxEmojiLength: Int,
    val fields: List<Field>,
    val templates: List<Template>,
) {
    data class Field(
        val key: String,
        val variablesAllowed: Boolean,
    )

    data class Template(
        val type: NotificationPackTemplateType,
        val variables: List<Variable>,
        val sampleValues: Map<String, String>,
        val conditionFields: List<ConditionField>,
        val defaults: Defaults,
    )

    data class Variable(
        val key: String,
        val sampleValue: String,
        val forms: List<VariableForm>,
    )

    data class VariableForm(
        val key: String,
        val placeholder: String,
        val sampleValue: String,
    )

    data class ConditionField(
        val field: NotificationPackVariantField,
        val placeholder: String,
        val values: List<ConditionValue>,
    )

    data class ConditionValue(
        val value: String,
        val sampleValue: String,
    )

    data class Defaults(
        val emoji: String?,
        val soloTitle: String,
        val soloBody: String,
        val compact: String,
        val aggregateTitle: String,
    )
}
