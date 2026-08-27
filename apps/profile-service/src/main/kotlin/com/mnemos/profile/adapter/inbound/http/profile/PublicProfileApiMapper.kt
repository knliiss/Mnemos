package com.mnemos.profile.adapter.inbound.http.profile

import com.mnemos.profile.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.profile.adapter.inbound.http.common.pagination.PageResponseMapper.toPageResponse
import com.mnemos.profile.application.common.pagination.PageQuery
import com.mnemos.profile.application.common.pagination.PageResult
import com.mnemos.profile.application.profile.usecase.search.SearchPublicProfilesQuery
import com.mnemos.profile.domain.profile.model.ProfileAppearance
import com.mnemos.profile.domain.profile.model.ProfilePlayerState
import com.mnemos.profile.domain.profile.model.PublicProfile

object PublicProfileApiMapper {
    fun SearchPublicProfilesRequest.toQuery(): SearchPublicProfilesQuery =
        SearchPublicProfilesQuery(
            query = q,
            page =
                PageQuery(
                    page = page,
                    size = size,
                ),
        )

    fun PageResult<PublicProfile>.toResponse(): PageResponse<PublicProfileResponse> =
        toPageResponse { profile ->
            profile.toResponse()
        }

    fun PublicProfile.toResponse(): PublicProfileResponse =
        PublicProfileResponse(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = playerState?.nickname ?: cristalixNickname,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            appearance = appearance.toResponse(),
            presence = playerState.toPresenceResponse(),
            createdAt = createdAt,
        )

    private fun ProfilePlayerState?.toPresenceResponse(): PublicProfilePresenceResponse {
        if (this == null) {
            return PublicProfilePresenceResponse(
                status = PublicProfilePresenceResponse.Status.UNKNOWN,
                online = null,
                realm = null,
                lastSeenOnline = null,
                observedAt = null,
            )
        }

        val hidden = presenceStatus == ProfilePlayerState.PresenceStatus.HIDDEN || privacyHidden
        val status =
            if (hidden) {
                PublicProfilePresenceResponse.Status.HIDDEN
            } else {
                PublicProfilePresenceResponse.Status.valueOf(presenceStatus.name)
            }
        val online =
            if (hidden) {
                null
            } else {
                when (presenceStatus) {
                    ProfilePlayerState.PresenceStatus.ONLINE -> true
                    ProfilePlayerState.PresenceStatus.OFFLINE -> false
                    ProfilePlayerState.PresenceStatus.UNKNOWN,
                    ProfilePlayerState.PresenceStatus.HIDDEN,
                    -> null
                }
            }

        return PublicProfilePresenceResponse(
            status = status,
            online = online,
            realm = realm.takeUnless { hidden },
            lastSeenOnline = lastSeenOnline.takeUnless { hidden },
            observedAt = presenceObservedAt,
        )
    }

    private fun ProfileAppearance.toResponse(): ProfileAppearanceResponse =
        ProfileAppearanceResponse(
            nickStyle = nickStyle?.toResponse(),
            profileTheme = profileTheme?.toResponse(),
            profileBackground = profileBackground?.toResponse(),
            avatarFrame = avatarFrame?.toResponse(),
            profileEffect = profileEffect?.toResponse(),
        )

    private fun ProfileAppearance.NickStyle.toResponse(): ProfileAppearanceResponse.NickStyle =
        ProfileAppearanceResponse.NickStyle(
            personalizationId = personalizationId,
            colors = colors.map { stop -> stop.toResponse() },
            angleDegrees = angleDegrees,
            glow = glow?.toResponse(),
        )

    private fun ProfileAppearance.ProfileTheme.toResponse(): ProfileAppearanceResponse.ProfileTheme =
        ProfileAppearanceResponse.ProfileTheme(
            personalizationId = personalizationId,
            background = background,
            surface = surface,
            accent = accent,
            text = text,
            border = border,
            glow = glow?.toResponse(),
            backgroundSecondary = backgroundSecondary,
            surfaceSecondary = surfaceSecondary,
            gradientAngleDegrees = gradientAngleDegrees,
            textureStrength = textureStrength,
            layers = layers.map { layer -> layer.toResponse() },
        )

    private fun ProfileAppearance.ProfileBackground.toResponse(): ProfileAppearanceResponse.ProfileBackground =
        ProfileAppearanceResponse.ProfileBackground(
            personalizationId = personalizationId,
            effect = effect,
            intensity = intensity,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            speed = speed,
            density = density,
            scale = scale,
            layers = layers.map { layer -> layer.toResponse() },
        )

    private fun ProfileAppearance.AvatarFrame.toResponse(): ProfileAppearanceResponse.AvatarFrame =
        ProfileAppearanceResponse.AvatarFrame(
            personalizationId = personalizationId,
            borderColor = borderColor,
            borderWidthPx = borderWidthPx,
            radiusPx = radiusPx,
            glow = glow?.toResponse(),
            secondaryBorderColor = secondaryBorderColor,
            gapPx = gapPx,
            style = style,
            layers = layers.map { layer -> layer.toResponse() },
        )

    private fun ProfileAppearance.ProfileEffect.toResponse(): ProfileAppearanceResponse.ProfileEffect =
        ProfileAppearanceResponse.ProfileEffect(
            personalizationId = personalizationId,
            effect = effect,
            intensity = intensity,
            primaryColor = primaryColor,
            secondaryColor = secondaryColor,
            speed = speed,
            density = density,
            scale = scale,
            layers = layers.map { layer -> layer.toResponse() },
        )

    private fun ProfileAppearance.VisualLayer.toResponse(): ProfileAppearanceResponse.VisualLayer =
        ProfileAppearanceResponse.VisualLayer(
            id = id,
            assetId = assetId,
            assetUrl = "/api/v1/personalizations/assets/$assetId/content",
            xPercent = xPercent,
            yPercent = yPercent,
            widthPercent = widthPercent,
            opacity = opacity,
            rotationDegrees = rotationDegrees,
            blendMode = blendMode,
            animation = animation,
            zIndex = zIndex,
        )

    private fun ProfileAppearance.GradientStop.toResponse(): ProfileAppearanceResponse.GradientStop =
        ProfileAppearanceResponse.GradientStop(
            color = color,
            position = position,
        )

    private fun ProfileAppearance.Glow.toResponse(): ProfileAppearanceResponse.Glow =
        ProfileAppearanceResponse.Glow(
            color = color,
            strength = strength,
        )
}
