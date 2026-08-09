package com.mnemos.event.domain.drop.repository

import com.mnemos.event.domain.drop.model.ItemDropEvent

interface ItemDropEventRepository {
    fun save(itemDropEvent: ItemDropEvent): ItemDropEvent
}
