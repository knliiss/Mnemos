package com.mnemos.event.application.booster.usecase.list

import com.mnemos.event.application.booster.model.BoosterEventView
import com.mnemos.event.application.booster.port.out.BoosterEventReader
import com.mnemos.event.application.common.pagination.PageResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListBoosterEventService(
    private val reader: BoosterEventReader,
) : ListBoosterEventUseCase {
    override fun execute(query: ListBoosterEventQuery): PageResult<BoosterEventView> = reader.find(query)
}
