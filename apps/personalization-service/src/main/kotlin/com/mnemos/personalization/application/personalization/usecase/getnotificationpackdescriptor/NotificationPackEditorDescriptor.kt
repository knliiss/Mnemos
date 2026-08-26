package com.mnemos.personalization.application.personalization.usecase.getnotificationpackdescriptor

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import com.mnemos.personalization.domain.personalization.model.NotificationValueForms

data class NotificationPackEditorDescriptor(
    val mixedAggregateTitleDefault: String,
    val maxTextLength: Int,
    val maxEmojiLength: Int,
    val fields: List<Field>,
    val templates: List<Template>,
    val valueMappings: Map<NotificationPackValueKey, Map<String, NotificationValueForms>>,
) {
    data class Field(
        val key: FieldKey,
        val variablesAllowed: Boolean,
    )

    enum class FieldKey {
        SOLO_TITLE,
        SOLO_BODY,
        COMPACT,
        AGGREGATE_TITLE,
    }

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
