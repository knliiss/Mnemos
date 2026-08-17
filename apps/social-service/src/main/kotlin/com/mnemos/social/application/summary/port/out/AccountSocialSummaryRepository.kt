package com.mnemos.social.application.summary.port.out

import com.mnemos.social.domain.summary.model.AccountSocialSummary
import java.time.Instant
import java.util.UUID

interface AccountSocialSummaryRepository {
    fun ensureExists(
        accountId: UUID,
        now: Instant,
    )

    fun findForUpdate(accountId: UUID): AccountSocialSummary

    fun save(summary: AccountSocialSummary): AccountSocialSummary
}
