package com.mnemos.personalization.application.ownership.usecase.listmine

import com.mnemos.personalization.application.common.pagination.PageResult
import com.mnemos.personalization.domain.ownership.model.OwnedPersonalization

interface ListOwnedPersonalizationsUseCase {
    fun execute(query: ListOwnedPersonalizationsQuery): PageResult<OwnedPersonalization>
}
