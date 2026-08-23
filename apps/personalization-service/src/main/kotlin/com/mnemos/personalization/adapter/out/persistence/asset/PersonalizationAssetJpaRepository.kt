package com.mnemos.personalization.adapter.out.persistence.asset

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PersonalizationAssetJpaRepository : JpaRepository<PersonalizationAssetEntity, UUID> {
    fun findAllByOwnerUserIdOrderByCreatedAtDesc(ownerUserId: UUID): List<PersonalizationAssetEntity>

    fun existsByIdAndOwnerUserId(
        id: UUID,
        ownerUserId: UUID,
    ): Boolean
}
