package com.mnemos.personalization.application.ownership.event

import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.personalization.contracts.event.NotificationPackTemplateV1
import com.mnemos.personalization.contracts.event.NotificationPackTemplateVariantV1
import com.mnemos.personalization.contracts.event.NotificationPackValueFormsV1
import com.mnemos.personalization.contracts.event.NotificationPackVariantConditionV1
import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateVariant
import com.mnemos.personalization.domain.personalization.model.NotificationValueForms
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent

object NotificationPackContractMapper {
    fun PersonalizationContent.NotificationPack.toContract(): NotificationPackSnapshotV1 =
        NotificationPackSnapshotV1(
            mixedAggregateTitle = mixedAggregateTitle,
            templates =
                templates.map { (key, template) ->
                    NotificationPackTemplateV1(
                        type = NotificationPackTemplateType.valueOf(key.name),
                        emoji = template.emoji,
                        soloTitle = template.soloTitle,
                        soloBody = template.soloBody,
                        compact = template.compact,
                        aggregateTitle = template.aggregateTitle,
                        variants = template.variants.map { variant -> variant.toContract() },
                    )
                },
            valueMappings =
                valueMappings.mapValues { (_, mappings) ->
                    mappings.mapValues { (_, forms) -> forms.toContract() }
                },
        )

    private fun NotificationTemplateVariant.toContract(): NotificationPackTemplateVariantV1 =
        NotificationPackTemplateVariantV1(
            id = id,
            conditions =
                conditions.map { condition ->
                    NotificationPackVariantConditionV1(
                        field = condition.field,
                        value = condition.value,
                    )
                },
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )

    private fun NotificationValueForms.toContract(): NotificationPackValueFormsV1 =
        NotificationPackValueFormsV1(
            default = default,
            forms = forms,
        )
}
