package com.mnemos.personalization.adapter.out.persistence.badge

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.model.UserBadge
import com.mnemos.personalization.domain.personalization.model.HexColor

object BadgePersistenceMapper {
    fun Badge.toEntity(): BadgeEntity =
        BadgeEntity(
            id = id.id,
            code = code,
            name = name,
            icon = icon,
            color = color.value,
            createdAt = createdAt,
        )

    fun BadgeEntity.toDomain(): Badge =
        Badge(
            id = BadgeId(id),
            code = code,
            name = name,
            icon = icon,
            color = HexColor(color),
            createdAt = createdAt,
        )

    fun UserBadgeEntity.toDomain(): UserBadge =
        UserBadge(
            userId = UserId(userId),
            badgeId = BadgeId(badgeId),
            grantedAt = grantedAt,
            active = active,
        )
}
