package com.mnemos.moderation.adapter.out.persistence.sanction

import com.mnemos.moderation.domain.sanction.model.DomainSanctionStatus
import com.mnemos.moderation.domain.sanction.model.DomainSanctionType
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface SanctionJpaRepository : JpaRepository<SanctionEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT sanction FROM SanctionEntity sanction WHERE sanction.id = :id")
    fun findByIdForUpdate(
        @Param("id") id: UUID,
    ): SanctionEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT sanction
        FROM SanctionEntity sanction
        WHERE sanction.userId = :userId
          AND sanction.type = :type
          AND sanction.status = :status
        ORDER BY sanction.createdAt DESC, sanction.id DESC
        """,
    )
    fun findActiveByUserIdAndTypeForUpdate(
        @Param("userId")
        userId: UUID,
        @Param("type")
        type: DomainSanctionType,
        @Param("status")
        status: DomainSanctionStatus,
    ): SanctionEntity?

    @Query(
        """
        SELECT sanction
        FROM SanctionEntity sanction
        WHERE sanction.status = :status
          AND sanction.expiresAt IS NOT NULL
          AND sanction.expiresAt <= :now
        ORDER BY sanction.expiresAt ASC, sanction.id ASC
        """,
    )
    fun findExpired(
        @Param("status") status: DomainSanctionStatus,
        @Param("now") now: Instant,
        pageable: Pageable,
    ): List<SanctionEntity>

    @Query(
        """
        SELECT sanction
        FROM SanctionEntity sanction
        WHERE sanction.type = :type
          AND sanction.status = :status
          AND (sanction.expiresAt IS NULL OR sanction.expiresAt > :now)
        ORDER BY sanction.createdAt ASC, sanction.id ASC
        """,
    )
    fun findActiveBans(
        @Param("type") type: DomainSanctionType,
        @Param("status") status: DomainSanctionStatus,
        @Param("now") now: Instant,
    ): List<SanctionEntity>

    @Query(
        """
        SELECT sanction
        FROM SanctionEntity sanction
        WHERE sanction.userId = :userId
        ORDER BY sanction.createdAt DESC, sanction.id DESC
        """,
    )
    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<SanctionEntity>
}
