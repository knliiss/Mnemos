package com.mnemos.profile.adapter.inbound.messaging.social

import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.messaging.kafka.consumer.KafkaMessageHandler
import com.mnemos.messaging.kafka.consumer.KafkaMessageProtocolException
import com.mnemos.messaging.kafka.consumer.kafkaMessageDefinition
import com.mnemos.profile.application.profile.PublicProfileService
import com.mnemos.social.contracts.SocialEventContract
import com.mnemos.social.contracts.SocialEventType
import com.mnemos.social.contracts.event.AccountSocialSummaryChangedV1
import org.springframework.stereotype.Component

@Component
class AccountSocialSummaryChangedHandler(
    private val service: PublicProfileService,
) : KafkaMessageHandler<AccountSocialSummaryChangedV1> {
    override val definition =
        kafkaMessageDefinition<AccountSocialSummaryChangedV1>(
            SocialEventContract.TOPIC,
            SocialEventType.ACCOUNT_SOCIAL_SUMMARY_CHANGED,
            SocialEventContract.VERSION,
            SocialEventContract.AGGREGATE_TYPE,
        ) { envelope ->
            if (envelope.producer != SOCIAL_SERVICE) {
                throw KafkaMessageProtocolException("Social summary must be produced by social-service")
            }

            if (envelope.aggregateId != envelope.payload.accountId) {
                throw KafkaMessageProtocolException("Social summary aggregateId must match accountId")
            }
        }

    override val consumer = "profile-service.account-social-summary-changed"

    override fun handle(message: ConsumedMessage<AccountSocialSummaryChangedV1>) {
        val event = message.payload

        service.socialSummaryChanged(
            accountId = event.accountId,
            likes = event.likes,
            dislikes = event.dislikes,
            comments = event.comments,
            revision = event.revision,
            occurredAt = event.occurredAt,
        )
    }

    private companion object {
        const val SOCIAL_SERVICE = "social-service"
    }
}
