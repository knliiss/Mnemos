package com.mnemos.telegram.bot.adapter.out.personalization

import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackPreviewResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackStoreItemResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationTemplateConditionResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationTemplateResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationTemplateVariantResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationValueFormsResponse
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackPreview
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackStoreItem
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplate
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplateCondition
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplateVariant
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationValueForms

object TelegramNotificationPackContractMapper {
    fun InternalTelegramNotificationPackStoreItemResponse.toModel(): TelegramNotificationPackStoreItem =
        TelegramNotificationPackStoreItem(
            personalizationId = personalizationId,
            name = name,
            author = author,
            priceAmount = priceAmount,
            currency = currency,
            owned = owned,
            active = active,
            preview = preview.toModel(),
        )

    private fun InternalTelegramNotificationPackPreviewResponse.toModel(): TelegramNotificationPackPreview =
        TelegramNotificationPackPreview(
            mixedAggregateTitle = mixedAggregateTitle,
            templates = templates.mapValues { (_, template) -> template.toModel() },
            valueMappings =
                valueMappings.mapValues { (_, mappings) ->
                    mappings.mapValues { (_, forms) -> forms.toModel() }
                },
        )

    private fun InternalTelegramNotificationTemplateResponse.toModel(): TelegramNotificationTemplate =
        TelegramNotificationTemplate(
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
            variants = variants.map { variant -> variant.toModel() },
        )

    private fun InternalTelegramNotificationTemplateVariantResponse.toModel(): TelegramNotificationTemplateVariant =
        TelegramNotificationTemplateVariant(
            id = id,
            conditions = conditions.map { condition -> condition.toModel() },
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )

    private fun InternalTelegramNotificationTemplateConditionResponse.toModel(): TelegramNotificationTemplateCondition =
        TelegramNotificationTemplateCondition(
            field = field,
            value = value,
        )

    private fun InternalTelegramNotificationValueFormsResponse.toModel(): TelegramNotificationValueForms =
        TelegramNotificationValueForms(
            default = default,
            forms = forms,
        )
}
