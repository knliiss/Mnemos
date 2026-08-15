package com.mnemos.social.application.summary.port.out

import com.mnemos.social.domain.summary.model.AccountSocialSummary
import java.util.UUID

interface AccountSocialSummaryEventPublisher {
    fun publish(
        summary: AccountSocialSummary,
        actorUserId: UUID,
    )
}
