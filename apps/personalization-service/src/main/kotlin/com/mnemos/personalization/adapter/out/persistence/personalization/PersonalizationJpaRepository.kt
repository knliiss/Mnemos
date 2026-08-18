package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import java.util.UUID

interface PersonalizationJpaRepository : JpaRepository<PersonalizationEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT personalization FROM PersonalizationEntity personalization WHERE personalization.id = :id")
    fun findByIdForUpdate(id: UUID): PersonalizationEntity?

    fun findAllByCreatorUserIdOrderByCreatedAtDesc(creatorUserId: UUID): List<PersonalizationEntity>

    fun findAllByStatusOrderByCreatedAtAsc(status: PersonalizationStatus): List<PersonalizationEntity>

    @Query(
        value =
            """
            SELECT personalization
            FROM PersonalizationEntity personalization
            WHERE personalization.status = com.mnemos.personalization.domain.personalization.model.PersonalizationStatus.PUBLISHED
              AND personalization.visibility = com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility.PUBLIC
              AND personalization.type = :type
              AND (:currency IS NULL OR personalization.priceCurrency = :currency)
            ORDER BY personalization.createdAt DESC, personalization.id DESC
            """,
        countQuery =
            """
            SELECT COUNT(personalization)
            FROM PersonalizationEntity personalization
            WHERE personalization.status = com.mnemos.personalization.domain.personalization.model.PersonalizationStatus.PUBLISHED
              AND personalization.visibility = com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility.PUBLIC
              AND personalization.type = :type
              AND (:currency IS NULL OR personalization.priceCurrency = :currency)
            """,
    )
    fun findPublished(
        type: PersonalizationType,
        currency: Currency?,
        pageable: Pageable,
    ): Page<PersonalizationEntity>

    @Query(
        value =
            """
            SELECT personalization
            FROM PersonalizationEntity personalization
            WHERE personalization.creatorUserId = :creatorUserId
              AND personalization.origin = com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin.CUSTOM
              AND personalization.type = com.mnemos.personalization.domain.personalization.model.PersonalizationType.NOTIFICATION_PACK
            ORDER BY personalization.createdAt DESC, personalization.id DESC
            """,
        countQuery =
            """
            SELECT COUNT(personalization)
            FROM PersonalizationEntity personalization
            WHERE personalization.creatorUserId = :creatorUserId
              AND personalization.origin = com.mnemos.personalization.domain.personalization.model.PersonalizationOrigin.CUSTOM
              AND personalization.type = com.mnemos.personalization.domain.personalization.model.PersonalizationType.NOTIFICATION_PACK
            """,
    )
    fun findCustomNotificationPacks(
        creatorUserId: UUID,
        pageable: Pageable,
    ): Page<PersonalizationEntity>
}
