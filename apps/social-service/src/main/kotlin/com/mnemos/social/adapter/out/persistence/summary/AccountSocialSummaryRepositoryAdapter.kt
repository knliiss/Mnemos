package com.mnemos.social.adapter.out.persistence.summary

import com.mnemos.social.adapter.out.persistence.summary.AccountSocialSummaryPersistenceMapper.toDomain
import com.mnemos.social.adapter.out.persistence.summary.AccountSocialSummaryPersistenceMapper.toEntity
import com.mnemos.social.application.summary.port.out.AccountSocialSummaryRepository
import com.mnemos.social.domain.summary.model.AccountSocialSummary
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
class AccountSocialSummaryRepositoryAdapter(
    private val repository: AccountSocialSummaryJpaRepository,
) : AccountSocialSummaryRepository {
    override fun ensureExists(
        accountId: UUID,
        now: Instant,
    ) {
        repository.ensureExists(accountId, now)
    }

    override fun findForUpdate(accountId: UUID): AccountSocialSummary =
        repository.findForUpdate(accountId).toDomain()

    override fun save(summary: AccountSocialSummary): AccountSocialSummary =
        repository.save(summary.toEntity()).toDomain()
}
