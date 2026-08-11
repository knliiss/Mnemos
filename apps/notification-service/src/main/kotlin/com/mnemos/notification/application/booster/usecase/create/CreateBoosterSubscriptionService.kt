package com.mnemos.notification.application.booster.usecase.create

import com.mnemos.notification.domain.booster.model.BoosterSubscription
import com.mnemos.notification.domain.booster.repository.BoosterSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateBoosterSubscriptionService(
    private val repository: BoosterSubscriptionRepository,
) : CreateBoosterSubscriptionUseCase {
    override fun execute(command: CreateBoosterSubscriptionCommand): CreateBoosterSubscriptionResult {
        val subscription =
            BoosterSubscription(
                userId = command.userId,
                boosterType = command.boosterType,
            )

        return CreateBoosterSubscriptionResult(repository.createOrGetId(subscription))
    }
}
