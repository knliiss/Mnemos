package com.mnemos.wallet.adapter.inbound.messaging.moderation

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageDefinition
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.moderation.contracts.ModerationEventContract
import com.mnemos.moderation.contracts.ModerationEventType
import com.mnemos.moderation.contracts.event.LeaderboardExclusionChangedV1
import com.mnemos.wallet.adapter.inbound.messaging.moderation.LeaderboardExclusionContractMapper.toChange
import com.mnemos.wallet.application.leaderboard.exclusion.usecase.ApplyLeaderboardExclusionChangeUseCase
import org.springframework.stereotype.Component

@Component
class LeaderboardExclusionChangedHandler(
    private val useCase: ApplyLeaderboardExclusionChangeUseCase,
) : KafkaMessageHandler<LeaderboardExclusionChangedV1> {
    override val definition: KafkaMessageDefinition<LeaderboardExclusionChangedV1> =
        kafkaMessageDefinition(
            topic = ModerationEventContract.TOPIC,
            type = ModerationEventType.LEADERBOARD_EXCLUSION_CHANGED,
            version = ModerationEventContract.VERSION,
            aggregateType = ModerationEventContract.LEADERBOARD_EXCLUSION_AGGREGATE_TYPE,
        )

    override val consumer = "wallet-service.leaderboard-exclusion-changed"

    override fun handle(message: ConsumedMessage<LeaderboardExclusionChangedV1>) {
        useCase.execute(message.payload.toChange())
    }
}
