package com.mnemos.notification.application.subscription.usecase.delete

import com.mnemos.notification.domain.subscription.exception.SubscriptionNotFoundException
import com.mnemos.notification.domain.subscription.repository.SubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DeleteSubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
) : DeleteSubscriptionUseCase {
    @Transactional
    override fun execute(command: DeleteSubscriptionCommand) {
        val deleted =
            subscriptionRepository.deleteByIdAndUserId(
                subscriptionId = command.subscriptionId,
                userId = command.userId,
            )

        if (!deleted) {
            throw SubscriptionNotFoundException(command.subscriptionId)
        }
    }
}
