package com.mnemos.reward.adapter.out.persistence.daily

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.reward.domain.daily.model.DailyStreak
import java.time.YearMonth

object DailyStreakPersistenceMapper {
    fun DailyStreakEntity.toDomain(): DailyStreak =
        DailyStreak(
            accountId = AccountId(accountId),
            currentStreak = currentStreak,
            lastClaimedDate = lastClaimedDate,
            recoverableStreak = recoverableStreak,
            restoresUsed = restoresUsed,
            restoresMonth = YearMonth.parse(restoresMonth),
        )

    fun DailyStreak.toEntity(): DailyStreakEntity =
        DailyStreakEntity(
            accountId = accountId.id,
            currentStreak = currentStreak,
            lastClaimedDate = lastClaimedDate,
            recoverableStreak = recoverableStreak,
            restoresUsed = restoresUsed,
            restoresMonth = restoresMonth.toString(),
        )
}
