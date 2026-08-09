package com.mnemos.event.application.global.usecase.list

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.global.model.GlobalEventView
import com.mnemos.event.application.global.port.out.GlobalEventReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListGlobalEventService(
    private val eventReader: GlobalEventReader,
) : ListGlobalEventUseCase {
    override fun execute(query: ListGlobalEventQuery): PageResult<GlobalEventView> = eventReader.find(query)
}
