package com.mnemos.personalization.adapter.out.persistence.badge

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface UserBadgeJpaRepository : JpaRepository<UserBadgeEntity, UUID> {
    fun findByUserIdAndBadgeId(
        userId: UUID,
        badgeId: UUID,
    ): UserBadgeEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT userBadge
        FROM UserBadgeEntity userBadge
        WHERE userBadge.userId = :userId
          AND userBadge.badgeId = :badgeId
        """,
    )
    fun findByUserIdAndBadgeIdForUpdate(
        userId: UUID,
        badgeId: UUID,
    ): UserBadgeEntity?

    fun findByUserIdAndActiveTrue(userId: UUID): UserBadgeEntity?

    @Query(
        """
        SELECT userBadge
        FROM UserBadgeEntity userBadge
        WHERE userBadge.userId = :userId
        ORDER BY userBadge.grantedAt DESC, userBadge.id ASC
        """,
    )
    fun findAllByUserIdOrderByGrantedAtDesc(userId: UUID): List<UserBadgeEntity>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT userBadge
        FROM UserBadgeEntity userBadge
        WHERE userBadge.userId = :userId
        ORDER BY userBadge.grantedAt DESC, userBadge.id ASC
        """,
    )
    fun findAllByUserIdForUpdate(userId: UUID): List<UserBadgeEntity>

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
