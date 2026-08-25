package com.mnemos.event.application.global.port.out

import com.mnemos.event.application.common.pagination.PageResult
import com.mnemos.event.application.global.model.GlobalEventView
import com.mnemos.event.application.global.usecase.list.ListGlobalEventQuery
import java.time.Instant

interface GlobalEventReader {
    fun find(query: ListGlobalEventQuery): PageResult<GlobalEventView>

    fun findStartedAfter(startedAfter: Instant): List<GlobalEventView>
}
