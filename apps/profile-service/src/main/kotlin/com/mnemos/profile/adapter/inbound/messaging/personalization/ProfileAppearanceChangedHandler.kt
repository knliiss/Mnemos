package com.mnemos.profile.adapter.inbound.messaging.personalization

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.personalization.contracts.PersonalizationAggregateType
import com.mnemos.personalization.contracts.PersonalizationEventContract
import com.mnemos.personalization.contracts.PersonalizationEventType
import com.mnemos.personalization.contracts.event.ProfileAppearanceChangedV1
import com.mnemos.profile.adapter.personalization.ProfileAppearanceSnapshotMapper.toDomain
import com.mnemos.profile.application.profile.usecase.applyappearance.ApplyProfileAppearanceUseCase
import org.springframework.stereotype.Component

@Component
class ProfileAppearanceChangedHandler(
    private val applyProfileAppearanceUseCase: ApplyProfileAppearanceUseCase,
) : KafkaMessageHandler<ProfileAppearanceChangedV1> {
    override val definition =
        kafkaMessageDefinition<ProfileAppearanceChangedV1>(
            PersonalizationEventContract.TOPIC,
            PersonalizationEventType.PROFILE_APPEARANCE_CHANGED,
            PersonalizationEventContract.VERSION,
            PersonalizationAggregateType.ACCOUNT_PROFILE_APPEARANCE,
        ) { envelope ->
            if (envelope.producer != PERSONALIZATION_SERVICE) {
                throw KafkaMessageProtocolException("Profile appearance must be produced by personalization-service")
            }

            if (envelope.aggregateId != envelope.payload.accountId) {
                throw KafkaMessageProtocolException("Profile appearance aggregateId must match accountId")
            }
        }

    override val consumer = "profile-service.profile-appearance-changed"

    override fun handle(message: ConsumedMessage<ProfileAppearanceChangedV1>) {
        val payload = message.payload
        val appearance =
            payload.appearance.toDomain(
                accountId = payload.accountId,
                revision = payload.revision,
                updatedAt = payload.occurredAt,
            )

        applyProfileAppearanceUseCase.execute(appearance)
    }

    private companion object {
        const val PERSONALIZATION_SERVICE = "personalization-service"
    }
}
