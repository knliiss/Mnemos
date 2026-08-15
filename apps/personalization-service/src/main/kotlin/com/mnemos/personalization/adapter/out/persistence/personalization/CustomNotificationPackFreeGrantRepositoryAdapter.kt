package com.mnemos.personalization.adapter.out.persistence.personalization

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.domain.personalization.repository.CustomNotificationPackFreeGrantRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class CustomNotificationPackFreeGrantRepositoryAdapter(
    private val repository: CustomNotificationPackFreeGrantJpaRepository,
) : CustomNotificationPackFreeGrantRepository {
    override fun claim(
        userId: UserId,
        claimedAt: Instant,
    ): Boolean =
        repository.claim(
            userId = userId.id,
            claimedAt = claimedAt,
        ) == 1
}
