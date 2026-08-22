package com.mnemos.personalization.adapter.inbound.http.personalization

import com.mnemos.personalization.adapter.inbound.http.common.PageResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.AvatarFrameContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.GlowStyleResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.GradientStopResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.NickStyleContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.NotificationPackContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.NotificationTemplateResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.PersonalizationContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.ProfileEffectContentResponse
import com.mnemos.personalization.adapter.inbound.http.personalization.content.ProfileThemeContentResponse
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent

object PersonalizationApiMapper {
    fun PageResult<Personalization>.toResponse(): PageResponse<PersonalizationResponse> =
        PageResponse(
            items = items.map { personalization -> personalization.toResponse() },
            page = page,
            size = size,
            totalItems = totalItems,
            totalPages = totalPages,
            hasNextPage = hasNextPage,
        )

    fun Personalization.toResponse(): PersonalizationResponse =
        PersonalizationResponse(
            id = id.id,
            creatorUserId = creatorUserId.id,
            type = type,
            origin = origin,
            visibility = visibility,
            name = name,
            content = content.toResponse(),
            price =
                price?.let { value ->
                    PersonalizationPriceResponse(
                        currency = value.currency,
                        amount = value.amount,
                    )
                },
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
                    surface = surface.value,
                    accent = accent.value,
                    text = text.value,
                    border = border.value,
                    glow = glow?.toResponse(),
                    backgroundSecondary = backgroundSecondary.value,
                    surfaceSecondary = surfaceSecondary.value,
                    gradientAngleDegrees = gradientAngleDegrees,
                    textureStrength = textureStrength,
                )

            is PersonalizationContent.AvatarFrame ->
                AvatarFrameContentResponse(
                    borderColor = borderColor.value,
                    borderWidthPx = borderWidthPx,
                    radiusPx = radiusPx,
                    glow = glow?.toResponse(),
                    secondaryBorderColor = secondaryBorderColor.value,
                    gapPx = gapPx,
                    style = style,
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
                )

            is PersonalizationContent.NotificationPack ->
                NotificationPackContentResponse(
                    mixedAggregateTitle = mixedAggregateTitle,
                    templates =
                        templates.mapValues { (_, template) ->
                            template.toResponse()
                        },
                )
        }

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

    private fun NotificationTemplate.toResponse(): NotificationTemplateResponse =
        NotificationTemplateResponse(
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )
}
