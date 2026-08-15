package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationPersistenceMapper.toDomain
import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationPersistenceMapper.toEntity
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.springframework.data.domain.PageRequest
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

    override fun findPublished(
        type: PersonalizationType?,
        currency: Currency?,
        page: PageQuery,
    ): PageResult<Personalization> {
        val result =
            repository.findPublished(
                type = type,
                currency = currency,
                pageable = PageRequest.of(page.page, page.size),
            )

        return PageResult(
            items = result.content.map { entity -> entity.toDomain() },
            page = result.number,
            size = result.size,
            totalItems = result.totalElements,
            totalPages = result.totalPages,
            hasNextPage = result.hasNext(),
        )
    }
}
