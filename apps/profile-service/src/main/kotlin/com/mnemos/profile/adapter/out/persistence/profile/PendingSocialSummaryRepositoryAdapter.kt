package com.mnemos.profile.adapter.out.persistence.profile

import com.mnemos.profile.adapter.out.persistence.profile.PendingSocialSummaryPersistenceMapper.toDomain
import com.mnemos.profile.adapter.out.persistence.profile.PendingSocialSummaryPersistenceMapper.toEntity
import com.mnemos.profile.application.profile.port.out.PendingSocialSummaryRepository
import com.mnemos.profile.domain.profile.model.PendingSocialSummary
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PendingSocialSummaryRepositoryAdapter(
    private val repository: PendingSocialSummaryJpaRepository,
) : PendingSocialSummaryRepository {
    override fun save(summary: PendingSocialSummary): PendingSocialSummary =
        repository.save(summary.toEntity()).toDomain()

    override fun findByAccountId(accountId: UUID): PendingSocialSummary? =
        repository.findById(accountId).orElse(null)?.toDomain()

    override fun findReady(limit: Int): List<PendingSocialSummary> =
        repository.findReady(limit).map { it.toDomain() }

    override fun deleteIfRevision(
        accountId: UUID,
        revision: Long,
    ) {
        repository.deleteIfRevision(accountId, revision)
    }
}
