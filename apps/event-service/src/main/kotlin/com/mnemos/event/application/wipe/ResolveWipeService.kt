package com.mnemos.event.application.wipe

import com.mnemos.event.domain.wipe.exception.WipeNotFoundException
import com.mnemos.event.domain.wipe.model.Wipe
import com.mnemos.event.domain.wipe.model.WipeId
import com.mnemos.event.domain.wipe.repository.WipeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ResolveWipeService(
    private val repository: WipeRepository,
) {
    fun execute(wipeId: WipeId?): Wipe {
        if (wipeId == null) {
            return repository.findCurrent()
                ?: throw WipeNotFoundException("Active wipe was not found")
        }

        return repository.findById(wipeId)
            ?: throw WipeNotFoundException("Wipe ${wipeId.id} was not found")
    }
}
