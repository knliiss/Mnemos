package com.mnemos.personalization.adapter.inbound.http.internal.notificationpack

import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackDetail
import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackOption
import com.mnemos.personalization.application.ownership.model.TelegramNotificationPackStoreItem
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackDetailResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackPreviewResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackStoreItemResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackSummaryResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationTemplateConditionResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationTemplateResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationTemplateVariantResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationValueFormsResponse
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateCondition
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateVariant
import com.mnemos.personalization.domain.personalization.model.NotificationValueForms
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent

object InternalTelegramNotificationPackApiMapper {
    fun TelegramNotificationPackOption.toResponse(): InternalTelegramNotificationPackSummaryResponse =
        InternalTelegramNotificationPackSummaryResponse(
            personalizationId = personalizationId.id,
            name = name,
            active = active,
        )

    fun TelegramNotificationPackStoreItem.toResponse(): InternalTelegramNotificationPackStoreItemResponse =
        InternalTelegramNotificationPackStoreItemResponse(
            personalizationId = personalizationId.id,
            name = name,
            author = author,
            priceAmount = priceAmount,
            currency = currency.name,
            owned = owned,
            active = active,
        )

    fun TelegramNotificationPackDetail.toResponse(): InternalTelegramNotificationPackDetailResponse =
        InternalTelegramNotificationPackDetailResponse(
            personalizationId = personalizationId.id,
            name = name,
            author = author,
            priceAmount = priceAmount,
            currency = currency.name,
            owned = owned,
            active = active,
            preview = content.toResponse(),
        )

    private fun PersonalizationContent.NotificationPack.toResponse(): InternalTelegramNotificationPackPreviewResponse =
        InternalTelegramNotificationPackPreviewResponse(
            mixedAggregateTitle = mixedAggregateTitle,
            templates =
                templates
                    .mapKeys { (key, _) -> key.contractType }
                    .mapValues { (_, template) -> template.toResponse() },
            valueMappings =
                valueMappings.mapValues { (_, mappings) ->
                    mappings.mapValues { (_, forms) -> forms.toResponse() }
                },
        )

    private fun NotificationTemplate.toResponse(): InternalTelegramNotificationTemplateResponse =
        InternalTelegramNotificationTemplateResponse(
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
            variants = variants.map { variant -> variant.toResponse() },
        )

    private fun NotificationTemplateVariant.toResponse(): InternalTelegramNotificationTemplateVariantResponse =
        InternalTelegramNotificationTemplateVariantResponse(
            id = id,
            conditions = conditions.map { condition -> condition.toResponse() },
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )

    private fun NotificationTemplateCondition.toResponse(): InternalTelegramNotificationTemplateConditionResponse =
        InternalTelegramNotificationTemplateConditionResponse(
            field = field,
            value = value,
        )

    private fun NotificationValueForms.toResponse(): InternalTelegramNotificationValueFormsResponse =
        InternalTelegramNotificationValueFormsResponse(
            default = default,
            forms = forms,
        )
}
