package com.mnemos.social.adapter.out.messaging.summary

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.social.application.summary.port.out.AccountSocialSummaryEventPublisher
import com.mnemos.social.contracts.SocialEventType
import com.mnemos.social.contracts.event.AccountSocialSummaryChangedV1
import com.mnemos.social.domain.summary.model.AccountSocialSummary
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class AccountSocialSummaryEventPublisherAdapter(
    private val publisher: IntegrationEventPublisher,
) : AccountSocialSummaryEventPublisher {
    override fun publish(
        summary: AccountSocialSummary,
        actorUserId: UUID,
    ) {
        publisher.publish(
            event =
                IntegrationEvent(
                    type = SocialEventType.ACCOUNT_SOCIAL_SUMMARY_CHANGED,
                    aggregateId = summary.accountId,
                    occurredAt = summary.updatedAt,
                    payload =
                        AccountSocialSummaryChangedV1(
                            accountId = summary.accountId,
                            likes = summary.likes,
                            dislikes = summary.dislikes,
                            comments = summary.comments,
                            revision = summary.revision,
                            occurredAt = summary.updatedAt,
                        ),
                ),
            metadata =
                EventMetadata(
                    actorUserId = actorUserId,
                ),
        )
    }
}
