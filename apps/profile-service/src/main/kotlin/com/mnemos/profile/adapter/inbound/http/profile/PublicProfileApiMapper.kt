package com.mnemos.profile.adapter.inbound.http.profile

import com.mnemos.profile.adapter.inbound.http.common.pagination.PageResponse
import com.mnemos.profile.adapter.inbound.http.common.pagination.PageResponseMapper.toPageResponse
import com.mnemos.profile.application.common.pagination.PageQuery
import com.mnemos.profile.application.common.pagination.PageResult
import com.mnemos.profile.application.profile.usecase.search.SearchPublicProfilesQuery
import com.mnemos.profile.domain.profile.model.ProfileAppearance
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

    fun PageResult<PublicProfile>.toResponse(): PageResponse<PublicProfileResponse> = toPageResponse { profile -> profile.toResponse() }

    fun PublicProfile.toResponse(): PublicProfileResponse =
        PublicProfileResponse(
            accountId = accountId,
            cristalixUuid = cristalixUuid,
            cristalixNickname = cristalixNickname,
            likes = likes,
            dislikes = dislikes,
            comments = comments,
            appearance = appearance.toResponse(),
            createdAt = createdAt,
        )

    private fun ProfileAppearance.toResponse(): ProfileAppearanceResponse =
        ProfileAppearanceResponse(
            nickStyle = nickStyle?.toResponse(),
            profileTheme = profileTheme?.toResponse(),
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
