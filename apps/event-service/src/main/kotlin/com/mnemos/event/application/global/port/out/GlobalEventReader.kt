package com.mnemos.event.application.global.port.out

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.global.model.GlobalEventView
import com.mnemos.event.application.global.usecase.list.ListGlobalEventQuery

interface GlobalEventReader {
    fun find(query: ListGlobalEventQuery): PageResult<GlobalEventView>
}
