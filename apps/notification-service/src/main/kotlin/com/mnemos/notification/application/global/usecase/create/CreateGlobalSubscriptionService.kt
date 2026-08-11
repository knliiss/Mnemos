package com.mnemos.notification.application.global.usecase.create

import com.mnemos.notification.domain.global.model.GlobalSubscription
import com.mnemos.notification.domain.global.repository.GlobalSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateGlobalSubscriptionService(
    private val repository: GlobalSubscriptionRepository,
) : CreateGlobalSubscriptionUseCase {
    override fun execute(command: CreateGlobalSubscriptionCommand): CreateGlobalSubscriptionResult {
        val subscription =
            GlobalSubscription(
                userId = command.userId,
                eventType = command.eventType,
            )

        return CreateGlobalSubscriptionResult(repository.createOrGetId(subscription))
    }
}
