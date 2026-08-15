package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationPersistenceMapper.toDomain
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.personalization.catalog.port.out.PersonalizationCatalogReader
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class PersonalizationCatalogReaderAdapter(
    private val repository: PersonalizationJpaRepository,
) : PersonalizationCatalogReader {
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
