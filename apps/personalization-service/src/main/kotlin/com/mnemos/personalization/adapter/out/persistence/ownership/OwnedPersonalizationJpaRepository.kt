package com.mnemos.personalization.adapter.out.persistence.ownership

import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface OwnedPersonalizationJpaRepository : JpaRepository<OwnedPersonalizationEntity, UUID> {
    fun findByOwnerTypeAndOwnerIdAndPersonalizationId(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
        personalizationId: UUID,
    ): OwnedPersonalizationEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT ownership
        FROM OwnedPersonalizationEntity ownership
        WHERE ownership.ownerType = :ownerType
          AND ownership.ownerId = :ownerId
          AND ownership.personalizationId = :personalizationId
        """,
    )
    fun findByOwnerTypeAndOwnerIdAndPersonalizationIdForUpdate(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
        personalizationId: UUID,
    ): OwnedPersonalizationEntity?

    fun existsByOwnerTypeAndOwnerIdAndPersonalizationId(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
        personalizationId: UUID,
    ): Boolean

    fun findByOwnerTypeAndOwnerIdAndPersonalizationTypeAndActiveTrue(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
        personalizationType: PersonalizationType,
    ): OwnedPersonalizationEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT ownership
        FROM OwnedPersonalizationEntity ownership
        WHERE ownership.ownerType = :ownerType
          AND ownership.ownerId = :ownerId
          AND ownership.personalizationType = :personalizationType
          AND ownership.active = true
        """,
    )
    fun findActiveForUpdate(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
        personalizationType: PersonalizationType,
    ): OwnedPersonalizationEntity?

    fun findAllByOwnerTypeAndOwnerIdOrderByAcquiredAtDesc(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
    ): List<OwnedPersonalizationEntity>

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(
        """
        UPDATE OwnedPersonalizationEntity ownership
        SET ownership.active = false
        WHERE ownership.ownerType = :ownerType
          AND ownership.ownerId = :ownerId
          AND ownership.personalizationType = :personalizationType
          AND ownership.active = true
        """,
    )
    fun deactivateActive(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
        personalizationType: PersonalizationType,
    ): Int
}
