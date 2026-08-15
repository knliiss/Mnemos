package com.mnemos.personalization.application.personalization.catalog

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
)

@Service
class PersonalizationCatalogService(
    private val repository: PersonalizationRepository,
) {
    fun list(query: ListPersonalizationsQuery): List<Personalization> =
        repository.findPublished(
            type = query.type,
            currency = query.currency,
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
