package com.mnemos.event.application.booster.usecase.list

import com.mnemos.event.application.common.pagination.PageQuery
import com.mnemos.event.domain.booster.model.DomainBoosterType

data class ListBoosterEventQuery(
    val type: DomainBoosterType?,
    val activatedByName: String?,
    val page: PageQuery,
)
