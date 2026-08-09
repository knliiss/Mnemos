package com.mnemos.event.application.raid.usecase.list

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.raid.model.RaidEventView

interface ListRaidEventUseCase {
    fun execute(query: ListRaidEventQuery): PageResult<RaidEventView>
}
