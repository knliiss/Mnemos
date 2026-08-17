package com.mnemos.event.application.itemdrop.usecase.get

import com.mnemos.event.application.itemdrop.model.ItemDropView
import com.mnemos.event.application.itemdrop.port.out.ItemDropReader
import com.mnemos.event.domain.EventId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetItemDropService(
    private val itemDropReader: ItemDropReader,
) : GetItemDropUseCase {
    override fun execute(id: EventId): ItemDropView? = itemDropReader.findById(id)
}
