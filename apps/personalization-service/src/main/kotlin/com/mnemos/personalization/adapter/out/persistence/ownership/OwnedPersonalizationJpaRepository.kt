package com.mnemos.personalization.adapter.out.persistence.ownership

import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    @Query(
        """
        SELECT ownership
        FROM OwnedPersonalizationEntity ownership
        WHERE ownership.personalizationId = :personalizationId
          AND ownership.active = true
        ORDER BY ownership.ownerType, ownership.ownerId
        """,
    )
    fun findAllActiveByPersonalizationId(personalizationId: UUID): List<OwnedPersonalizationEntity>

    @Query(
        value =
            """
            SELECT ownership
            FROM OwnedPersonalizationEntity ownership
            WHERE ownership.ownerType = com.mnemos.personalization.domain.personalization.model.PersonalizationTarget.USER
              AND ownership.ownerId = :userId
              AND (:personalizationType IS NULL OR ownership.personalizationType = :personalizationType)
            ORDER BY ownership.acquiredAt DESC, ownership.id DESC
            """,
        countQuery =
            """
            SELECT COUNT(ownership)
            FROM OwnedPersonalizationEntity ownership
            WHERE ownership.ownerType = com.mnemos.personalization.domain.personalization.model.PersonalizationTarget.USER
              AND ownership.ownerId = :userId
              AND (:personalizationType IS NULL OR ownership.personalizationType = :personalizationType)
            """,
    )
    fun findOwnedByUser(
        userId: UUID,
        personalizationType: PersonalizationType?,
        pageable: Pageable,
    ): Page<OwnedPersonalizationEntity>

    @Query(
        value =
            """
            SELECT ownership
            FROM OwnedPersonalizationEntity ownership
            WHERE (
                    (
                        ownership.ownerType = com.mnemos.personalization.domain.personalization.model.PersonalizationTarget.USER
                        AND ownership.ownerId = :userId
                    )
                    OR (
                        ownership.ownerType = com.mnemos.personalization.domain.personalization.model.PersonalizationTarget.ACCOUNT
                        AND ownership.ownerId = :accountId
                    )
                )
              AND (:personalizationType IS NULL OR ownership.personalizationType = :personalizationType)
            ORDER BY ownership.acquiredAt DESC, ownership.id DESC
            """,
        countQuery =
            """
            SELECT COUNT(ownership)
            FROM OwnedPersonalizationEntity ownership
            WHERE (
                    (
                        ownership.ownerType = com.mnemos.personalization.domain.personalization.model.PersonalizationTarget.USER
                        AND ownership.ownerId = :userId
                    )
                    OR (
                        ownership.ownerType = com.mnemos.personalization.domain.personalization.model.PersonalizationTarget.ACCOUNT
                        AND ownership.ownerId = :accountId
                    )
                )
              AND (:personalizationType IS NULL OR ownership.personalizationType = :personalizationType)
            """,
    )
    fun findOwnedByUserAndAccount(
        userId: UUID,
        accountId: UUID,
        personalizationType: PersonalizationType?,
        pageable: Pageable,
    ): Page<OwnedPersonalizationEntity>

    @Query(
        """
        SELECT ownership
        FROM OwnedPersonalizationEntity ownership
        WHERE ownership.ownerType = :ownerType
          AND ownership.ownerId = :ownerId
        ORDER BY ownership.acquiredAt DESC, ownership.id DESC
        """,
    )
    fun findAllByOwnerTypeAndOwnerIdOrderByAcquiredAtDesc(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
    ): List<OwnedPersonalizationEntity>

    @Query(
        """
        SELECT DISTINCT ownership.ownerId
        FROM OwnedPersonalizationEntity ownership
        WHERE ownership.ownerType = com.mnemos.personalization.domain.personalization.model.PersonalizationTarget.ACCOUNT
          AND ownership.active = true
          AND ownership.personalizationType IN :types
        ORDER BY ownership.ownerId
        """,
    )
    fun findActiveAccountIds(types: Set<PersonalizationType>): List<UUID>

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

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    fun deleteByOwnerTypeAndOwnerIdAndPersonalizationId(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
        personalizationId: UUID,
    ): Int
}
