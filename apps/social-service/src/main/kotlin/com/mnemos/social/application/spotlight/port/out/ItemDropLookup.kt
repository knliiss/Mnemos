package com.mnemos.social.application.spotlight.port.out

import com.mnemos.social.domain.spotlight.model.ItemDropSnapshot
import java.util.UUID

interface ItemDropLookup {
    fun findById(itemDropEventId: UUID): ItemDropSnapshot?
}
