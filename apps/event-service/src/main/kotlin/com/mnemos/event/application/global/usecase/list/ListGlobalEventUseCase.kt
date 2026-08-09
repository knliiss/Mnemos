package com.mnemos.event.application.global.usecase.list

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.global.model.GlobalEventView

interface ListGlobalEventUseCase {
    fun execute(query: ListGlobalEventQuery): PageResult<GlobalEventView>
}
