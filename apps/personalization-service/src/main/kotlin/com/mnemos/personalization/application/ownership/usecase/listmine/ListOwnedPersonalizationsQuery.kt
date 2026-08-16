package com.mnemos.personalization.application.ownership.usecase.listmine

import com.mnemos.personalization.application.common.pagination.PageQuery
import com.mnemos.personalization.domain.personalization.model.PersonalizationType

data class ListOwnedPersonalizationsQuery(
    val type: PersonalizationType?,
    val page: PageQuery,
)
