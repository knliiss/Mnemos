package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateAvatarFrameRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateNickStyleRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateNotificationPackRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateProfileEffectRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.CreateProfileThemeRequest
import com.mnemos.personalization.adapter.inbound.http.creator.request.VisualLayerRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.GlowStyleRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.GradientStopRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.NotificationTemplateConditionRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.NotificationTemplateRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.NotificationTemplateVariantRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.NotificationValueFormsRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.PersonalizationPriceRequest
import com.mnemos.personalization.application.personalization.usecase.createcreator.CreateCreatorPersonalizationCommand
import com.mnemos.personalization.application.personalization.usecase.updatecreator.UpdatePersonalizationCommand
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.domain.asset.model.PersonalizationAssetId
import com.mnemos.personalization.domain.personalization.model.GlowStyle
import com.mnemos.personalization.domain.personalization.model.GradientStop
import com.mnemos.personalization.domain.personalization.model.HexColor
import com.mnemos.personalization.domain.personalization.model.NotificationTemplate
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateCondition
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateVariant
import com.mnemos.personalization.domain.personalization.model.NotificationValueForms
import com.mnemos.personalization.domain.personalization.model.PersonalizationContent
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationPrice
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.model.VisualLayer

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
            content =
                templates.toContent(
                    mixedAggregateTitle = mixedAggregateTitle,
                    valueMappings = valueMappings,
                ),
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
            content =
                templates.toContent(
                    mixedAggregateTitle = mixedAggregateTitle,
                    valueMappings = valueMappings,
                ),
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
            backgroundSecondary = HexColor(backgroundSecondary),
            surfaceSecondary = HexColor(surfaceSecondary),
            gradientAngleDegrees = gradientAngleDegrees,
            textureStrength = textureStrength,
            layers = layers.map { layer -> layer.toDomain() },
        )

    fun CreateAvatarFrameRequest.toContent(): PersonalizationContent.AvatarFrame =
        PersonalizationContent.AvatarFrame(
            borderColor = HexColor(borderColor),
            borderWidthPx = borderWidthPx,
            radiusPx = radiusPx,
            glow = glow?.toDomain(),
            secondaryBorderColor = HexColor(secondaryBorderColor),
            gapPx = gapPx,
            style = style,
            layers = layers.map { layer -> layer.toDomain() },
        )

    fun CreateProfileEffectRequest.toContent(): PersonalizationContent.ProfileEffect =
        PersonalizationContent.ProfileEffect(
            effect = effect,
            intensity = intensity,
            primaryColor = HexColor(primaryColor),
            secondaryColor = secondaryColor?.let(::HexColor),
            speed = speed,
            density = density,
            scale = scale,
            layers = layers.map { layer -> layer.toDomain() },
        )

    fun CreateNotificationPackRequest.toContent(): PersonalizationContent.NotificationPack =
        templates.toContent(
            mixedAggregateTitle = mixedAggregateTitle,
            valueMappings = valueMappings,
        )

    fun Map<NotificationTemplateKey, NotificationTemplateRequest>.toContent(
        mixedAggregateTitle: String,
        valueMappings: Map<NotificationPackValueKey, Map<String, NotificationValueFormsRequest>> = emptyMap(),
    ): PersonalizationContent.NotificationPack =
        PersonalizationContent.NotificationPack(
            mixedAggregateTitle = mixedAggregateTitle,
            templates = mapValues { (_, template) -> template.toDomain() },
            valueMappings =
                valueMappings.mapValues { (_, mappings) ->
                    mappings.mapValues { (_, forms) -> forms.toDomain() }
                },
        )

    fun PersonalizationPriceRequest.toDomain(): PersonalizationPrice =
        PersonalizationPrice(
            currency = currency,
            amount = amount,
        )

    private fun VisualLayerRequest.toDomain(): VisualLayer =
        VisualLayer(
            id = id,
            assetId = PersonalizationAssetId(assetId),
            xPercent = xPercent,
            yPercent = yPercent,
            widthPercent = widthPercent,
            opacity = opacity,
            rotationDegrees = rotationDegrees,
            blendMode = blendMode,
            animation = animation,
            zIndex = zIndex,
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
            variants = variants.map { variant -> variant.toDomain() },
        )

    private fun NotificationTemplateVariantRequest.toDomain(): NotificationTemplateVariant =
        NotificationTemplateVariant(
            id = id,
            conditions = conditions.map { condition -> condition.toDomain() },
            emoji = emoji,
            soloTitle = soloTitle,
            soloBody = soloBody,
            compact = compact,
            aggregateTitle = aggregateTitle,
        )

    private fun NotificationTemplateConditionRequest.toDomain(): NotificationTemplateCondition =
        NotificationTemplateCondition(
            field = field,
            value = value,
        )

    private fun NotificationValueFormsRequest.toDomain(): NotificationValueForms =
        NotificationValueForms(
            default = default,
            forms = forms,
        )
}
