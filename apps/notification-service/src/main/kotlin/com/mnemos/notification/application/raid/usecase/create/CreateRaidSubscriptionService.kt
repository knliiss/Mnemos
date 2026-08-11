package com.mnemos.notification.application.raid.usecase.create

import com.mnemos.notification.domain.raid.model.RaidSubscription
import com.mnemos.notification.domain.raid.repository.RaidSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateRaidSubscriptionService(
    private val repository: RaidSubscriptionRepository,
) : CreateRaidSubscriptionUseCase {
    override fun execute(command: CreateRaidSubscriptionCommand): CreateRaidSubscriptionResult {
        val subscription =
            RaidSubscription(
                userId = command.userId,
                locations = command.locations,
            )

        return CreateRaidSubscriptionResult(repository.createOrGetId(subscription))
    }
}
