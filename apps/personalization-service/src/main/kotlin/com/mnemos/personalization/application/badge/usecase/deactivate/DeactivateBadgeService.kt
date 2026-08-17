package com.mnemos.personalization.application.badge.usecase.deactivate

import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeactivateBadgeService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: BadgeRepository,
) : DeactivateBadgeUseCase {
    @Transactional
    override fun execute() {
        val actor = actorProvider.requireActor()

        repository.deactivateActiveUserBadge(actor.userId)
    }
}
