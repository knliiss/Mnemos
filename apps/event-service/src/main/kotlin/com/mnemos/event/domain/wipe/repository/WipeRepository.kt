package com.mnemos.event.domain.wipe.repository

import com.mnemos.event.domain.wipe.model.Wipe
import com.mnemos.event.domain.wipe.model.WipeId
import java.time.Instant

interface WipeRepository {
    fun findCurrent(at: Instant): Wipe?

    fun findCurrentForUpdate(at: Instant): Wipe?

    fun findLatestForUpdate(): Wipe?

    fun findById(id: WipeId): Wipe?

    fun findAll(): List<Wipe>

    fun save(wipe: Wipe): Wipe
}
