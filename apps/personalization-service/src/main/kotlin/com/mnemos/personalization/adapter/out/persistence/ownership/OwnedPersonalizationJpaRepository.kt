package com.mnemos.personalization.adapter.out.persistence.ownership

import com.mnemos.personalization.domain.personalization.model.PersonalizationTarget
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface OwnedPersonalizationJpaRepository : JpaRepository<OwnedPersonalizationEntity, UUID> {
    fun findByOwnerTypeAndOwnerIdAndPersonalizationId(
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

    fun findAllByOwnerTypeAndOwnerIdOrderByAcquiredAtDesc(
        ownerType: PersonalizationTarget,
        ownerId: UUID,
    ): List<OwnedPersonalizationEntity>
}
