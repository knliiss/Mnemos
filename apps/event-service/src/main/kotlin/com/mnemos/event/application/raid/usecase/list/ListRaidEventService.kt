package com.mnemos.event.application.raid.usecase.list

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.raid.model.RaidEventView
import com.mnemos.event.application.raid.port.out.RaidEventReader
import org.springframework.stereotype.Service

@Service
class ListRaidEventService(
    private val eventReader: RaidEventReader,
) : ListRaidEventUseCase {
    override fun execute(query: ListRaidEventQuery): PageResult<RaidEventView> = eventReader.find(query)
}
