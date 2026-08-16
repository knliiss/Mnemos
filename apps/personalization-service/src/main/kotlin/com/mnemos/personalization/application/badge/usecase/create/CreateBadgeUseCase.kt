package com.mnemos.personalization.application.badge.usecase.create

import com.mnemos.personalization.application.badge.model.CreateBadgeCommand
import com.mnemos.personalization.domain.badge.model.Badge

interface CreateBadgeUseCase {
    fun execute(command: CreateBadgeCommand): Badge
}
