package com.mnemos.personalization.application.badge.usecase.listall

import com.mnemos.personalization.domain.badge.model.Badge

interface ListBadgesUseCase {
    fun execute(): List<Badge>
}
