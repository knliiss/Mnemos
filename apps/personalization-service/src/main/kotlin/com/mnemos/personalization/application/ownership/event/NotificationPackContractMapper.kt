package com.mnemos.personalization.application.ownership.event

import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.personalization.contracts.event.NotificationPackTemplateV1
import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
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
                    )
                },
        )
}
