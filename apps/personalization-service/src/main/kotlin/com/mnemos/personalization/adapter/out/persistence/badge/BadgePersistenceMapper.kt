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
            imageObjectKey = imageObjectKey,
            color = color.value,
            createdAt = createdAt,
        )

    fun BadgeEntity.toDomain(): Badge =
        try {
            Badge(
                id = BadgeId(id),
                code = code,
                name = name,
                imageObjectKey = imageObjectKey,
                color = HexColor(color),
                createdAt = createdAt,
            )
        } catch (exception: IllegalArgumentException) {
            throw IllegalStateException(
                "Persisted badge $id contains invalid domain state",
                exception,
            )
        }

    fun UserBadgeEntity.toDomain(): UserBadge =
        UserBadge(
            userId = UserId(userId),
            badgeId = BadgeId(badgeId),
            grantedAt = grantedAt,
            active = active,
        )
}
