package com.mnemos.reward.adapter.out.persistence.daily

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.reward.adapter.out.persistence.daily.DailyStreakPersistenceMapper.toDomain
import com.mnemos.reward.adapter.out.persistence.daily.DailyStreakPersistenceMapper.toEntity
import com.mnemos.reward.domain.daily.model.DailyStreak
import com.mnemos.reward.domain.daily.repository.DailyStreakRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class DailyStreakRepositoryAdapter(
    private val jpaRepository: DailyStreakJpaRepository,
) : DailyStreakRepository {
    override fun findByAccountId(accountId: AccountId): DailyStreak? =
        jpaRepository
            .findById(accountId.id)
            .orElse(null)
            ?.toDomain()

    override fun findByAccountIdForUpdate(accountId: AccountId): DailyStreak? = jpaRepository.findForUpdate(accountId.id)?.toDomain()

    override fun save(streak: DailyStreak): DailyStreak = jpaRepository.save(streak.toEntity()).toDomain()

    override fun findMissedForUpdate(cutoff: LocalDate): List<DailyStreak> = jpaRepository.findMissedForUpdate(cutoff).map { it.toDomain() }
}
