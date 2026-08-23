package com.mnemos.personalization.application.badge.usecase.listall

import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ListBadgesService(
    private val repository: BadgeRepository,
) : ListBadgesUseCase {
    @Transactional(readOnly = true)
    override fun execute(): List<Badge> = repository.findAll()
}
