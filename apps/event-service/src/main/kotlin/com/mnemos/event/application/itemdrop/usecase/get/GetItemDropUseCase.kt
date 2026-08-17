package com.mnemos.event.application.itemdrop.usecase.get

import com.mnemos.event.application.itemdrop.model.ItemDropView
import com.mnemos.event.domain.EventId

interface GetItemDropUseCase {
    fun execute(id: EventId): ItemDropView?
}
