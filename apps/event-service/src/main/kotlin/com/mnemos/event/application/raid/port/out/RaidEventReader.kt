package com.mnemos.event.application.raid.port.out

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.raid.model.RaidEventView
import com.mnemos.event.application.raid.usecase.list.ListRaidEventQuery

interface RaidEventReader {
    fun find(query: ListRaidEventQuery): PageResult<RaidEventView>
}
