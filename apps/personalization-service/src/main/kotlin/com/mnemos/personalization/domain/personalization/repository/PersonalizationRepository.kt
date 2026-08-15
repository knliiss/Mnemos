package com.mnemos.personalization.domain.personalization.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency

interface PersonalizationRepository {
    fun save(personalization: Personalization): Personalization

    fun findById(id: PersonalizationId): Personalization?

    fun findByIdForUpdate(id: PersonalizationId): Personalization? = findById(id)

    fun findByCreatorUserId(creatorUserId: UserId): List<Personalization>

    fun findByStatus(status: PersonalizationStatus): List<Personalization>

    fun findPublished(
        type: PersonalizationType?,
        currency: Currency?,
        page: PageQuery,
    ): PageResult<Personalization>
}
