package com.mnemos.event.domain.raid.repository

import com.mnemos.event.domain.raid.model.RaidEvent

interface RaidEventRepository {
    fun save(event: RaidEvent): RaidEvent
}
