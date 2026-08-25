package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.application.personalization.usecase.getnotificationpackdescriptor.NotificationPackEditorDescriptor
import com.mnemos.personalization.domain.personalization.model.NotificationValueForms

object NotificationPackEditorDescriptorApiMapper {
    fun NotificationPackEditorDescriptor.toResponse(): NotificationPackEditorDescriptorResponse =
        NotificationPackEditorDescriptorResponse(
            mixedAggregateTitleDefault = mixedAggregateTitleDefault,
            maxTextLength = maxTextLength,
            maxEmojiLength = maxEmojiLength,
            fields = fields.map { field -> field.toResponse() },
            templates = templates.map { template -> template.toResponse() },
            valueMappings =
                valueMappings.mapValues { (_, mappings) ->
                    mappings.mapValues { (_, forms) -> forms.toResponse() }
                },
        )

    private fun NotificationPackEditorDescriptor.Field.toResponse(): NotificationPackEditorDescriptorResponse.Field =
        NotificationPackEditorDescriptorResponse.Field(
            key = key.name,
            variablesAllowed = variablesAllowed,
        )

    private fun NotificationPackEditorDescriptor.Template.toResponse(): NotificationPackEditorDescriptorResponse.Template =
        NotificationPackEditorDescriptorResponse.Template(
            type = type,
            variables = variables.map { variable -> variable.toResponse() },
            sampleValues = sampleValues,
            conditionFields = conditionFields.map { field -> field.toResponse() },
            defaults = defaults.toResponse(),
        )

    private fun NotificationPackEditorDescriptor.Variable.toResponse(): NotificationPackEditorDescriptorResponse.Variable =
        NotificationPackEditorDescriptorResponse.Variable(
            key = key,
            sampleValue = sampleValue,
            forms = forms.map { form -> form.toResponse() },
        )

    private fun NotificationPackEditorDescriptor.VariableForm.toResponse(): NotificationPackEditorDescriptorResponse.VariableForm =
        NotificationPackEditorDescriptorResponse.VariableForm(
            key = key,
            placeholder = placeholder,
            sampleValue = sampleValue,
        )

    private fun NotificationPackEditorDescriptor.ConditionField.toResponse(): NotificationPackEditorDescriptorResponse.ConditionField =
        NotificationPackEditorDescriptorResponse.ConditionField(
            field = field,
            placeholder = placeholder,
            values = values.map { value -> value.toResponse() },
        )

    private fun NotificationPackEditorDescriptor.ConditionValue.toResponse(): NotificationPackEditorDescriptorResponse.ConditionValue =
        NotificationPackEditorDescriptorResponse.ConditionValue(
            value = value,
            sampleValue = sampleValue,
        )

    private fun NotificationValueForms.toResponse(): NotificationPackEditorDescriptorResponse.ValueForms =
        NotificationPackEditorDescriptorResponse.ValueForms(
            default = default,
            forms = forms,
        )

    private fun NotificationPackEditorDescriptor.Defaults.toResponse(): NotificationPackEditorDescriptorResponse.Defaults =
        NotificationPackEditorDescriptorResponse.Defaults(
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )
}
