package com.mnemos.personalization.adapter.out.persistence.badge

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserBadgeJpaRepository : JpaRepository<UserBadgeEntity, UUID> {
    fun findByUserIdAndBadgeId(
        userId: UUID,
        badgeId: UUID,
    ): UserBadgeEntity?

    fun findByUserIdAndActiveTrue(userId: UUID): UserBadgeEntity?

    fun findAllByUserIdOrderByGrantedAtDesc(userId: UUID): List<UserBadgeEntity>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        UPDATE UserBadgeEntity userBadge
        SET userBadge.active = false
        WHERE userBadge.userId = :userId
          AND userBadge.active = true
        """,
    )
    fun deactivateActive(userId: UUID): Int

    fun deleteByUserIdAndBadgeId(
        userId: UUID,
        badgeId: UUID,
    )
}
