package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateAvatarFrameRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateNickStyleRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateNotificationPackRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateProfileEffectRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateProfileThemeRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.GlowStyleRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.GradientStopRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.NotificationTemplateRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.PersonalizationPriceRequest
import com.mnemos.personalization.application.personalization.usecase.createcreator.CreateCreatorPersonalizationCommand
import com.mnemos.personalization.application.personalization.usecase.updatecreator.UpdatePersonalizationCommand
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationType

object CreatorPersonalizationApiMapper {
    fun CreateNickStyleRequest.toCreateCommand(): CreateCreatorPersonalizationCommand =
        CreateCreatorPersonalizationCommand(
            type = PersonalizationType.NICK_STYLE,
            name = name,
            content = toContent(),
            price = price.toDomain(),
        )

    fun CreateProfileThemeRequest.toCreateCommand(): CreateCreatorPersonalizationCommand =
        CreateCreatorPersonalizationCommand(
            type = PersonalizationType.PROFILE_THEME,
            name = name,
            content = toContent(),
            price = price.toDomain(),
        )

    fun CreateAvatarFrameRequest.toCreateCommand(): CreateCreatorPersonalizationCommand =
        CreateCreatorPersonalizationCommand(
            type = PersonalizationType.AVATAR_FRAME,
            name = name,
            content = toContent(),
            price = price.toDomain(),
        )

    fun CreateProfileEffectRequest.toCreateCommand(): CreateCreatorPersonalizationCommand =
        CreateCreatorPersonalizationCommand(
            type = PersonalizationType.PROFILE_EFFECT,
            name = name,
            content = toContent(),
            price = price.toDomain(),
        )

    fun CreateNotificationPackRequest.toCreateCommand(): CreateCreatorPersonalizationCommand =
        CreateCreatorPersonalizationCommand(
            type = PersonalizationType.NOTIFICATION_PACK,
            name = name,
            content = toContent(),
            price = price.toDomain(),
        )

    fun CreateNickStyleRequest.toUpdateCommand(id: PersonalizationId): UpdatePersonalizationCommand =
        UpdatePersonalizationCommand(
            personalizationId = id,
            name = name,
            content = toContent(),
            price = price.toDomain(),
        )

    fun CreateProfileThemeRequest.toUpdateCommand(id: PersonalizationId): UpdatePersonalizationCommand =
        UpdatePersonalizationCommand(
            personalizationId = id,
            name = name,
            content = toContent(),
            price = price.toDomain(),
        )

    fun CreateAvatarFrameRequest.toUpdateCommand(id: PersonalizationId): UpdatePersonalizationCommand =
        UpdatePersonalizationCommand(
            personalizationId = id,
            name = name,
            content = toContent(),
            price = price.toDomain(),
        )

    fun CreateProfileEffectRequest.toUpdateCommand(id: PersonalizationId): UpdatePersonalizationCommand =
        UpdatePersonalizationCommand(
            personalizationId = id,
            name = name,
            content = toContent(),
            price = price.toDomain(),
        )

    fun CreateNotificationPackRequest.toUpdateCommand(id: PersonalizationId): UpdatePersonalizationCommand =
        UpdatePersonalizationCommand(
            personalizationId = id,
            name = name,
            content = toContent(),
            price = price.toDomain(),
        )

    fun CreateNickStyleRequest.toContent(): PersonalizationContent.NickStyle =
        PersonalizationContent.NickStyle(
            colors = colors.map { stop -> stop.toDomain() },
            angleDegrees = angleDegrees,
            glow = glow?.toDomain(),
        )

    fun CreateProfileThemeRequest.toContent(): PersonalizationContent.ProfileTheme =
        PersonalizationContent.ProfileTheme(
            background = HexColor(background),
            surface = HexColor(surface),
            accent = HexColor(accent),
            text = HexColor(text),
            border = HexColor(border),
            glow = glow?.toDomain(),
        )

    fun CreateAvatarFrameRequest.toContent(): PersonalizationContent.AvatarFrame =
        PersonalizationContent.AvatarFrame(
            borderColor = HexColor(borderColor),
            borderWidthPx = borderWidthPx,
            radiusPx = radiusPx,
            glow = glow?.toDomain(),
        )

    fun CreateProfileEffectRequest.toContent(): PersonalizationContent.ProfileEffect =
        PersonalizationContent.ProfileEffect(
            effect = effect,
            intensity = intensity,
            primaryColor = HexColor(primaryColor),
            secondaryColor = secondaryColor?.let(::HexColor),
        )

    fun CreateNotificationPackRequest.toContent(): PersonalizationContent.NotificationPack =
        templates.toContent(mixedAggregateTitle)

    fun Map<NotificationTemplateKey, NotificationTemplateRequest>.toContent(
        mixedAggregateTitle: String,
    ): PersonalizationContent.NotificationPack =
        PersonalizationContent.NotificationPack(
            mixedAggregateTitle = mixedAggregateTitle,
            templates = mapValues { (_, template) -> template.toDomain() },
        )

    fun PersonalizationPriceRequest.toDomain(): PersonalizationPrice =
        PersonalizationPrice(
            currency = currency,
            amount = amount,
        )

    private fun GradientStopRequest.toDomain(): GradientStop =
        GradientStop(
            color = HexColor(color),
            position = position,
        )

    private fun GlowStyleRequest.toDomain(): GlowStyle =
        GlowStyle(
            color = HexColor(color),
            strength = strength,
        )

    private fun NotificationTemplateRequest.toDomain(): NotificationTemplate =
        NotificationTemplate(
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )
}
