package com.mnemos.personalization.application.personalization.usecase.listcatalog

import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.application.personalization.catalog.port.out.PersonalizationCatalogReader
import com.mnemos.personalization.domain.personalization.model.Personalization
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListPersonalizationsService(
    private val catalogReader: PersonalizationCatalogReader,
) : ListPersonalizationsUseCase {
    @Transactional(readOnly = true)
    override fun execute(query: ListPersonalizationsQuery): PageResult<Personalization> =
        catalogReader.findPublished(
            type = query.type,
            currency = query.currency,
            page = query.page,
        )
}
