package com.mnemos.moderation.domain.sanction.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.moderation.domain.sanction.model.DomainSanctionType
import com.mnemos.moderation.domain.sanction.model.Sanction
import com.mnemos.moderation.domain.sanction.model.SanctionId
import java.time.Instant

interface SanctionRepository {
    fun save(sanction: Sanction): Sanction

    fun findByIdForUpdate(id: SanctionId): Sanction?

    fun findActiveByUserIdAndTypeForUpdate(
        userId: UserId,
        type: DomainSanctionType,
    ): Sanction?

    fun findExpired(
        now: Instant,
        limit: Int,
    ): List<Sanction>

    fun findActiveBans(now: Instant): List<Sanction>

    fun findByUserId(userId: UserId): List<Sanction>
}
