package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationPersistenceMapper.toDomain
import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationPersistenceMapper.toEntity
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import org.springframework.stereotype.Component

@Component
class PersonalizationRepositoryAdapter(
    private val repository: PersonalizationJpaRepository,
) : PersonalizationRepository {
    override fun save(personalization: Personalization): Personalization =
        repository.save(personalization.toEntity()).toDomain()

    override fun findById(id: PersonalizationId): Personalization? =
        repository.findById(id.id).orElse(null)?.toDomain()

    override fun findByIdForUpdate(id: PersonalizationId): Personalization? =
        repository.findByIdForUpdate(id.id)?.toDomain()

    override fun findByCreatorUserId(creatorUserId: UserId): List<Personalization> =
        repository.findAllByCreatorUserIdOrderByCreatedAtDesc(creatorUserId.id).map { it.toDomain() }

    override fun findByStatus(status: PersonalizationStatus): List<Personalization> =
        repository.findAllByStatusOrderByCreatedAtAsc(status).map { it.toDomain() }
}
