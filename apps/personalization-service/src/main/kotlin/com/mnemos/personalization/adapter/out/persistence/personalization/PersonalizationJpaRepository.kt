package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface PersonalizationJpaRepository : JpaRepository<PersonalizationEntity, UUID> {
    fun findAllByCreatorUserIdOrderByCreatedAtDesc(creatorUserId: UUID): List<PersonalizationEntity>

    fun findAllByStatusOrderByCreatedAtAsc(status: PersonalizationStatus): List<PersonalizationEntity>
}
