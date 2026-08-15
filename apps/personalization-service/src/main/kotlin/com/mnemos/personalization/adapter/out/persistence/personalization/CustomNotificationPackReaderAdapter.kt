package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.out.persistence.personalization.PersonalizationPersistenceMapper.toDomain
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.personalization.custom.port.out.CustomNotificationPackReader
import com.mnemos.personalization.domain.personalization.model.Personalization
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class CustomNotificationPackReaderAdapter(
    private val repository: PersonalizationJpaRepository,
) : CustomNotificationPackReader {
    override fun findByCreatorUserId(
        creatorUserId: UserId,
        page: PageQuery,
    ): PageResult<Personalization> {
        val result =
            repository.findCustomNotificationPacks(
                creatorUserId = creatorUserId.id,
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
