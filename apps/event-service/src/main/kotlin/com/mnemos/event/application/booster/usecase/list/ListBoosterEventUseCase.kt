package com.mnemos.event.application.booster.usecase.list

import com.mnemos.event.application.booster.model.BoosterEventView
import com.mnemos.event.application.common.pagination.PageResult

interface ListBoosterEventUseCase {
    fun execute(query: ListBoosterEventQuery): PageResult<BoosterEventView>
}
