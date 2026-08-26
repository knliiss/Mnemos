package com.mnemos.notification.application.playerpresence.usecase.project

import com.mnemos.notification.application.playerpresence.port.out.PlayerPresenceSubscriptionRepository
import com.mnemos.notification.domain.playerpresence.model.PlayerPresenceSubscription
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProjectPlayerPresenceSubscriptionService(
    private val repository: PlayerPresenceSubscriptionRepository,
) : ProjectPlayerPresenceSubscriptionUseCase {
    @Transactional
    override fun execute(subscription: PlayerPresenceSubscription) {
        repository.ensure(subscription)
    }
}
