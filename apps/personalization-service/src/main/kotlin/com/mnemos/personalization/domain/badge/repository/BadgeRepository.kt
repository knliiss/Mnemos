package com.mnemos.personalization.domain.badge.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.model.UserBadge

interface BadgeRepository {
    fun save(badge: Badge): Badge

    fun findById(id: BadgeId): Badge?

    fun findByCode(code: String): Badge?

    fun saveUserBadge(userBadge: UserBadge): UserBadge

    fun findUserBadge(
        userId: UserId,
        badgeId: BadgeId,
    ): UserBadge?

    fun findActiveUserBadge(userId: UserId): UserBadge?

    fun findUserBadges(userId: UserId): List<UserBadge>

    fun deleteUserBadge(
        userId: UserId,
        badgeId: BadgeId,
    )
}
