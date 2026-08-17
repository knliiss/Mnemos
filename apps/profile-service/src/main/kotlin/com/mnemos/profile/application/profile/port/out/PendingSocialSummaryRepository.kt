package com.mnemos.profile.application.profile.port.out

import com.mnemos.profile.domain.profile.model.PendingSocialSummary
import java.util.UUID

interface PendingSocialSummaryRepository {
    fun save(summary: PendingSocialSummary): PendingSocialSummary

    fun findByAccountId(accountId: UUID): PendingSocialSummary?

    fun findReady(limit: Int): List<PendingSocialSummary>

    fun deleteIfRevision(
        accountId: UUID,
        revision: Long,
    )
}
