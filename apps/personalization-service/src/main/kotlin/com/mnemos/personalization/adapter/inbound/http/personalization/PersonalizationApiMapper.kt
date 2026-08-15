package com.mnemos.personalization.adapter.inbound.http.personalization

import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent

object PersonalizationApiMapper {
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
                )

            is PersonalizationContent.AvatarFrame ->
                AvatarFrameContentResponse(
                    borderColor = borderColor.value,
                    borderWidthPx = borderWidthPx,
                    radiusPx = radiusPx,
                    glow = glow?.toResponse(),
                )

            is PersonalizationContent.ProfileEffect ->
                ProfileEffectContentResponse(
                    effect = effect,
                    intensity = intensity,
                    primaryColor = primaryColor.value,
                    secondaryColor = secondaryColor?.value,
                )

            is PersonalizationContent.NotificationPack ->
                NotificationPackContentResponse(
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
        )
}
