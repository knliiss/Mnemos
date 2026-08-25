package com.mnemos.event.application.wipe

import com.mnemos.event.domain.wipe.model.Wipe
import com.mnemos.event.domain.wipe.repository.WipeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListWipesService(
    private val repository: WipeRepository,
) {
    fun execute(): List<Wipe> = repository.findAll()
}
