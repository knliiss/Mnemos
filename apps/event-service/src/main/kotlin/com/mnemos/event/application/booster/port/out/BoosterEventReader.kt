package com.mnemos.event.application.booster.port.out

import com.mnemos.event.application.booster.model.BoosterEventView
import com.mnemos.event.application.booster.usecase.list.ListBoosterEventQuery
import com.mnemos.event.application.common.pagination.PageResult

interface BoosterEventReader {
    fun find(query: ListBoosterEventQuery): PageResult<BoosterEventView>
}
