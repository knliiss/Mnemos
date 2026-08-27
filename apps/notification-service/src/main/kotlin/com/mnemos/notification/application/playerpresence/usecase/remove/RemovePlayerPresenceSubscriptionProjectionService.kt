package com.mnemos.notification.application.playerpresence.usecase.remove

import com.mnemos.notification.application.playerpresence.port.out.PlayerPresenceSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RemovePlayerPresenceSubscriptionProjectionService(
    private val repository: PlayerPresenceSubscriptionRepository,
) : RemovePlayerPresenceSubscriptionProjectionUseCase {
    @Transactional
    override fun execute(subscriptionId: UUID) {
        repository.delete(subscriptionId)
    }
}
