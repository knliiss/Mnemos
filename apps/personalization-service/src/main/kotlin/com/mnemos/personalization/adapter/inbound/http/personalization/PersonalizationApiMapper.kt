package com.mnemos.personalization.adapter.inbound.http.personalization

import com.mnemos.personalization.adapter.inbound.http.personalization.content.AvatarFrameContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.GlowStyleResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.GradientStopResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.NickStyleContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.NotificationPackContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.NotificationTemplateConditionResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.NotificationTemplateResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.NotificationTemplateVariantResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.NotificationValueFormsResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.PersonalizationContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.PersonalizationPriceResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.ProfileBackgroundContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.ProfileEffectContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.ProfileThemeContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.VisualLayerResponse
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateCondition
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateVariant
import com.mnemos.personalization.domain.personalization.model.NotificationValueForms
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.ProfileBackgroundContent
import com.mnemos.personalization.domain.personalization.model.VisualLayer

object PersonalizationApiMapper {
    fun Personalization.toResponse(): PersonalizationResponse =
        PersonalizationResponse(
            id = id.id,
            creatorUserId = creatorUserId?.id,
            catalogKey = catalogKey?.value,
            type = type,
            origin = origin,
            visibility = visibility,
            name = name,
            content = content.toResponse(),
            price = price?.toResponse(),
            status = status,
            rejectionReason = rejectionReason,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )

    private fun PersonalizationContent.toResponse(): PersonalizationContentResponse =
        when (this) {
            is PersonalizationContent.NickStyle ->
                NickStyleContentResponse(
                    colors = colors.map { stop -> stop.toResponse() },
                    angleDegrees = angleDegrees,
                    glow = glow?.toResponse(),
                )

            is PersonalizationContent.ProfileTheme ->
                ProfileThemeContentResponse(
                    background = background.value,
                    backgroundSecondary = backgroundSecondary.value,
                    surface = surface.value,
                    surfaceSecondary = surfaceSecondary.value,
                    accent = accent.value,
                    text = text.value,
                    border = border.value,
                    gradientAngleDegrees = gradientAngleDegrees,
                    textureStrength = textureStrength,
                    glow = glow?.toResponse(),
                    layers = layers.map { layer -> layer.toResponse() },
                )

            is ProfileBackgroundContent ->
                ProfileBackgroundContentResponse(
                    effect = effect,
                    intensity = intensity,
                    primaryColor = primaryColor.value,
                    secondaryColor = secondaryColor?.value,
                    speed = speed,
                    density = density,
                    scale = scale,
                    layers = layers.map { layer -> layer.toResponse() },
                )

            is PersonalizationContent.AvatarFrame ->
                AvatarFrameContentResponse(
                    borderColor = borderColor.value,
                    secondaryBorderColor = secondaryBorderColor.value,
                    borderWidthPx = borderWidthPx,
                    gapPx = gapPx,
                    radiusPx = radiusPx,
                    style = style,
                    glow = glow?.toResponse(),
                    layers = layers.map { layer -> layer.toResponse() },
                )

            is PersonalizationContent.ProfileEffect ->
                ProfileEffectContentResponse(
                    effect = effect,
                    intensity = intensity,
                    primaryColor = primaryColor.value,
                    secondaryColor = secondaryColor?.value,
                    speed = speed,
                    density = density,
                    scale = scale,
                    layers = layers.map { layer -> layer.toResponse() },
                )

            is PersonalizationContent.NotificationPack ->
                NotificationPackContentResponse(
                    mixedAggregateTitle = mixedAggregateTitle,
                    templates = templates.mapValues { (_, template) -> template.toResponse() },
                    valueMappings =
                        valueMappings.mapValues { (_, mappings) ->
                            mappings.mapValues { (_, forms) -> forms.toResponse() }
                        },
                )
        }

    private fun NotificationTemplate.toResponse(): NotificationTemplateResponse =
        NotificationTemplateResponse(
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
            variants = variants.map { variant -> variant.toResponse() },
        )

    private fun NotificationTemplateVariant.toResponse(): NotificationTemplateVariantResponse =
        NotificationTemplateVariantResponse(
            id = id,
            conditions = conditions.map { condition -> condition.toResponse() },
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )

    private fun NotificationTemplateCondition.toResponse(): NotificationTemplateConditionResponse =
        NotificationTemplateConditionResponse(
            field = field,
            value = value,
        )

    private fun NotificationValueForms.toResponse(): NotificationValueFormsResponse =
        NotificationValueFormsResponse(
            default = default,
            forms = forms,
        )

    private fun PersonalizationPrice.toResponse(): PersonalizationPriceResponse =
        PersonalizationPriceResponse(
            currency = currency,
            amount = amount,
        )

    private fun VisualLayer.toResponse(): VisualLayerResponse =
        VisualLayerResponse(
            id = id,
            assetId = assetId.id,
            assetUrl = "/api/v1/personalizations/assets/${assetId.id}/content",
            xPercent = xPercent,
            yPercent = yPercent,
            widthPercent = widthPercent,
            opacity = opacity,
            rotationDegrees = rotationDegrees,
            blendMode = blendMode,
            animation = animation,
            zIndex = zIndex,
        )

    private fun GradientStop.toResponse(): GradientStopResponse =
        GradientStopResponse(
            color = color.value,
            position = position,
        )

    private fun GlowStyle.toResponse(): GlowStyleResponse =
        GlowStyleResponse(
            color = color.value,
            strength = strength,
        )
}
