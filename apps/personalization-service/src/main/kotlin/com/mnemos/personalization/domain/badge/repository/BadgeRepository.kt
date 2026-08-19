package com.mnemos.personalization.domain.badge.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.model.UserBadge

interface BadgeRepository {
    fun save(badge: Badge): Badge

    fun findById(id: BadgeId): Badge?

    fun findByIdForUpdate(id: BadgeId): Badge? = findById(id)

    fun findByCode(code: String): Badge?

    fun findAll(): List<Badge>

    fun saveUserBadge(userBadge: UserBadge): UserBadge

    fun findUserBadge(
        userId: UserId,
        badgeId: BadgeId,
    ): UserBadge?

    fun findUserBadgeForUpdate(
        userId: UserId,
        badgeId: BadgeId,
    ): UserBadge? = findUserBadge(userId, badgeId)

    fun findActiveUserBadge(userId: UserId): UserBadge?

    fun findUserBadges(userId: UserId): List<UserBadge>

    fun findUserBadgesForUpdate(userId: UserId): List<UserBadge> = findUserBadges(userId)

    fun deactivateActiveUserBadge(userId: UserId)

    fun deleteUserBadge(
        userId: UserId,
        badgeId: BadgeId,
    )
}
