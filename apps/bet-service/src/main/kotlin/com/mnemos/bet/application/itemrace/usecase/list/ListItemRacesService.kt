package com.mnemos.bet.application.itemrace.usecase.list

import com.mnemos.bet.application.itemrace.model.ItemRaceView
import com.mnemos.bet.application.itemrace.port.out.ItemRaceReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListItemRacesService(
    private val reader: ItemRaceReader,
) : ListItemRacesUseCase {
    override fun execute(): List<ItemRaceView> = reader.findCurrent()
}
