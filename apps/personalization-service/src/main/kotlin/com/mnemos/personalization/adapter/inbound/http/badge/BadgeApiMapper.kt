package com.mnemos.personalization.adapter.inbound.http.badge

import com.mnemos.personalization.application.badge.model.UserBadgeView
import com.mnemos.personalization.domain.badge.model.Badge

object BadgeApiMapper {
    fun Badge.toResponse(): BadgeResponse =
        BadgeResponse(
            id = id.id,
            code = code,
            name = name,
            imageUrl = "/api/v1/personalizations/badges/${id.id}/image",
            color = color.value,
        )

    fun UserBadgeView.toResponse(): UserBadgeResponse =
        UserBadgeResponse(
            badge = badge.toResponse(),
            grantedAt = grantedAt,
            active = active,
        )
}
