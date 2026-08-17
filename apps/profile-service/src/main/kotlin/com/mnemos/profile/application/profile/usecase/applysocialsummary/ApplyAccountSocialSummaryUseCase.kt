package com.mnemos.profile.application.profile.usecase.applysocialsummary

import java.time.Instant
import java.util.UUID

interface ApplyAccountSocialSummaryUseCase {
    fun execute(
        accountId: UUID,
        likes: Long,
        dislikes: Long,
        comments: Long,
        revision: Long,
        occurredAt: Instant,
    )
}
