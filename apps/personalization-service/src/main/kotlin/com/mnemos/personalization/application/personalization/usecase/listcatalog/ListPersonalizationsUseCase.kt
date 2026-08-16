package com.mnemos.personalization.application.personalization.usecase.listcatalog

import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.domain.personalization.model.Personalization

interface ListPersonalizationsUseCase {
    fun execute(query: ListPersonalizationsQuery): PageResult<Personalization>
}
