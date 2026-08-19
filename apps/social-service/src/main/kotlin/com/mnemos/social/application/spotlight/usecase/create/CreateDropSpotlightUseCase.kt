package com.mnemos.social.application.spotlight.usecase.create

import com.mnemos.social.domain.spotlight.model.DropSpotlight

interface CreateDropSpotlightUseCase {
    fun execute(command: CreateDropSpotlightCommand): DropSpotlight
}
