package com.mnemos.event.application.raid.usecase.list

import com.mnemos.event.application.common.pagination.PageQuery

data class ListRaidEventQuery(
    val minLocation: Int?,
    val maxLocation: Int?,
    val page: PageQuery,
)
