package com.mnemos.notification.application.raid.usecase.update

import com.mnemos.notification.domain.raid.model.RaidSubscription
import com.mnemos.notification.domain.raid.repository.RaidSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UpdateRaidSubscriptionService(
    private val repository: RaidSubscriptionRepository,
) : UpdateRaidSubscriptionUseCase {
    override fun execute(command: UpdateRaidSubscriptionCommand): UpdateRaidSubscriptionResult {
        val subscription =
            RaidSubscription(
                userId = command.userId,
                locations = command.locations,
            )

        return UpdateRaidSubscriptionResult(
            subscriptionId = repository.replaceLocations(subscription),
        )
    }
}
