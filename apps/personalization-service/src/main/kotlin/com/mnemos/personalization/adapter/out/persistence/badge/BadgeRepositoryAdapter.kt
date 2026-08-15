package com.mnemos.personalization.adapter.out.persistence.badge

import com.github.f4b6a3.uuid.UuidCreator
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.out.persistence.badge.BadgePersistenceMapper.toDomain
import com.mnemos.personalization.adapter.out.persistence.badge.BadgePersistenceMapper.toEntity
import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.model.UserBadge
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Component

@Component
class BadgeRepositoryAdapter(
    private val badgeRepository: BadgeJpaRepository,
    private val userBadgeRepository: UserBadgeJpaRepository,
) : BadgeRepository {
    override fun save(badge: Badge): Badge = badgeRepository.saveAndFlush(badge.toEntity()).toDomain()

    override fun findById(id: BadgeId): Badge? = badgeRepository.findById(id.id).orElse(null)?.toDomain()

    override fun findByIdForUpdate(id: BadgeId): Badge? = badgeRepository.findByIdForUpdate(id.id)?.toDomain()

    override fun findByCode(code: String): Badge? = badgeRepository.findByCode(code)?.toDomain()

    override fun saveUserBadge(userBadge: UserBadge): UserBadge {
        val existing = userBadgeRepository.findByUserIdAndBadgeId(userBadge.userId.id, userBadge.badgeId.id)
        val entity =
            UserBadgeEntity(
                id = existing?.id ?: UuidCreator.getTimeOrderedEpoch(),
                userId = userBadge.userId.id,
                badgeId = userBadge.badgeId.id,
                grantedAt = userBadge.grantedAt,
                active = userBadge.active,
            )

        return userBadgeRepository.save(entity).toDomain()
    }

    override fun findUserBadge(
        userId: UserId,
        badgeId: BadgeId,
    ): UserBadge? = userBadgeRepository.findByUserIdAndBadgeId(userId.id, badgeId.id)?.toDomain()

    override fun findUserBadgeForUpdate(
        userId: UserId,
        badgeId: BadgeId,
    ): UserBadge? = userBadgeRepository.findByUserIdAndBadgeIdForUpdate(userId.id, badgeId.id)?.toDomain()

    override fun findActiveUserBadge(userId: UserId): UserBadge? = userBadgeRepository.findByUserIdAndActiveTrue(userId.id)?.toDomain()

    override fun findUserBadges(userId: UserId): List<UserBadge> =
        userBadgeRepository.findAllByUserIdOrderByGrantedAtDesc(userId.id).map { it.toDomain() }

    override fun findUserBadgesForUpdate(userId: UserId): List<UserBadge> =
        userBadgeRepository.findAllByUserIdForUpdate(userId.id).map { it.toDomain() }

    override fun deactivateActiveUserBadge(userId: UserId) {
        userBadgeRepository.deactivateActive(userId.id)
    }

    override fun deleteUserBadge(
        userId: UserId,
        badgeId: BadgeId,
    ) {
        userBadgeRepository.deleteByUserIdAndBadgeId(userId.id, badgeId.id)
    }
}
