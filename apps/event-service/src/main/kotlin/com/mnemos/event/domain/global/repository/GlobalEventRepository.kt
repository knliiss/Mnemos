package com.mnemos.event.domain.global.repository

import com.mnemos.event.domain.global.model.GlobalEvent

interface GlobalEventRepository {
    fun save(globalEvent: GlobalEvent): GlobalEvent
}
