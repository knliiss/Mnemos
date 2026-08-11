package com.mnemos.notification.application.dungeon.usecase.create

import com.mnemos.notification.domain.dungeon.model.DungeonSubscription
import com.mnemos.notification.domain.dungeon.repository.DungeonSubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CreateDungeonSubscriptionService(
    private val repository: DungeonSubscriptionRepository,
) : CreateDungeonSubscriptionUseCase {
    override fun execute(command: CreateDungeonSubscriptionCommand): CreateDungeonSubscriptionResult {
        val subscription =
            DungeonSubscription(
                userId = command.userId,
                dungeonType = command.dungeonType,
            )

        return CreateDungeonSubscriptionResult(repository.createOrGetId(subscription))
    }
}
