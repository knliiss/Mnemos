package com.mnemos.personalization.adapter.out.persistence.ownership

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.adapter.out.persistence.ownership.OwnedPersonalizationPersistenceMapper.toDomain
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.ownership.port.out.OwnedPersonalizationReader
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Component
class OwnedPersonalizationReaderAdapter(
    private val repository: OwnedPersonalizationJpaRepository,
) : OwnedPersonalizationReader {
    override fun findOwned(
        userId: UserId,
        accountId: AccountId?,
        type: PersonalizationType?,
        page: PageQuery,
    ): PageResult<OwnedPersonalization> {
        val pageable = PageRequest.of(page.page, page.size)
        val result =
            if (accountId == null) {
                repository.findOwnedByUser(
                    userId = userId.id,
                    personalizationType = type,
                    pageable = pageable,
                )
            } else {
                repository.findOwnedByUserAndAccount(
                    userId = userId.id,
                    accountId = accountId.id,
                    personalizationType = type,
                    pageable = pageable,
                )
            }

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
