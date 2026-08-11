package com.mnemos.notification.application.itemdrop.usecase.create

import com.mnemos.notification.domain.itemdrop.model.ItemDropSubscription
import com.mnemos.notification.domain.itemdrop.repository.ItemDropSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateItemDropSubscriptionService(
    private val repository: ItemDropSubscriptionRepository,
) : CreateItemDropSubscriptionUseCase {
    override fun execute(command: CreateItemDropSubscriptionCommand): CreateItemDropSubscriptionResult {
        val subscription =
            ItemDropSubscription(
                userId = command.userId,
                droppedFor = command.droppedFor,
                itemName = command.itemName,
            )

        return CreateItemDropSubscriptionResult(
            subscriptionId = repository.createOrGetId(subscription),
        )
    }
}
