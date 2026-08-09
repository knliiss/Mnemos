package com.mnemos.event.application.global.usecase.list

import com.mnemos.event.application.common.pagination.PageQuery
import com.mnemos.event.domain.global.model.DomainGlobalEventType

data class ListGlobalEventQuery(
    val types: List<DomainGlobalEventType>,
    val page: PageQuery,
)
