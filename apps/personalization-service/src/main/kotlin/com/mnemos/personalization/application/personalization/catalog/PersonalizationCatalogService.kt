package com.mnemos.personalization.application.personalization.catalog

import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.personalization.catalog.port.out.PersonalizationCatalogReader
import com.mnemos.personalization.application.personalization.exception.PersonalizationNotFoundException
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationId
import com.mnemos.personalization.domain.personalization.model.PersonalizationStatus
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.personalization.domain.personalization.model.PersonalizationVisibility
import com.mnemos.personalization.domain.personalization.repository.PersonalizationRepository
import com.mnemos.wallet.contracts.wallet.shared.Currency
import org.springframework.stereotype.Service

data class ListPersonalizationsQuery(
    val type: PersonalizationType?,
    val currency: Currency?,
    val page: PageQuery,
)

@Service
class PersonalizationCatalogService(
    private val repository: PersonalizationRepository,
    private val catalogReader: PersonalizationCatalogReader,
) {
    fun list(query: ListPersonalizationsQuery): PageResult<Personalization> =
        catalogReader.findPublished(
            type = query.type,
            currency = query.currency,
            page = query.page,
        )

    fun get(personalizationId: PersonalizationId): Personalization {
        val personalization =
            repository.findById(personalizationId)
                ?: throw PersonalizationNotFoundException()

        val visible =
            personalization.status == PersonalizationStatus.PUBLISHED &&
                personalization.visibility == PersonalizationVisibility.PUBLIC

        if (!visible) {
            throw PersonalizationNotFoundException()
        }

        return personalization
    }
}
