package com.mnemos.event.domain.wipe.repository

import com.mnemos.event.domain.wipe.model.Wipe
import com.mnemos.event.domain.wipe.model.WipeId

interface WipeRepository {
    fun findCurrent(): Wipe?

    fun findCurrentForUpdate(): Wipe?

    fun findById(id: WipeId): Wipe?

    fun findAll(): List<Wipe>

    fun save(wipe: Wipe): Wipe
}
