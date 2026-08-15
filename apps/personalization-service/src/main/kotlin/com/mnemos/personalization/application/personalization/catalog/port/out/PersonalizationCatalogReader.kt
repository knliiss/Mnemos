package com.mnemos.personalization.application.personalization.catalog.port.out

import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.domain.personalization.model.Personalization
import com.mnemos.personalization.domain.personalization.model.PersonalizationType
import com.mnemos.wallet.contracts.wallet.shared.Currency

interface PersonalizationCatalogReader {
    fun findPublished(
        type: PersonalizationType?,
        currency: Currency?,
        page: PageQuery,
    ): PageResult<Personalization>
}
