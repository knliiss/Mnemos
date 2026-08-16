package com.mnemos.social.application.spotlight.usecase.create

import com.mnemos.social.domain.spotlight.model.DropSpotlight
import java.util.UUID

interface CreateDropSpotlightUseCase {
    fun execute(itemDropEventId: UUID): DropSpotlight
}
