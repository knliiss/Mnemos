package com.mnemos.event.application.itemdrop.usecase.first

import com.mnemos.event.application.itemdrop.model.FirstItemDropView
import com.mnemos.event.application.itemdrop.port.out.FirstItemDropReader
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class FindFirstItemDropService(
    private val firstItemDropReader: FirstItemDropReader,
) : FindFirstItemDropUseCase {
    override fun execute(query: FindFirstItemDropQuery): FirstItemDropView? = firstItemDropReader.findFirst(query)
}
