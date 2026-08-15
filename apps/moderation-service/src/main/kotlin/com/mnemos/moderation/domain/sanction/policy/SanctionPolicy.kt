package com.mnemos.moderation.domain.sanction.policy

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.moderation.domain.sanction.exception.SanctionDurationExceededException
import com.mnemos.moderation.domain.sanction.exception.SanctionTargetAccessDeniedException
import com.mnemos.moderation.domain.sanction.exception.SelfSanctionException
import java.time.Duration
import java.time.Instant

object SanctionPolicy {
    private val moderatorMaximumDuration: Duration = Duration.ofDays(30)

    fun validateCreation(
        actorUserId: UserId,
        actorRole: UserRole,
        targetUserId: UserId,
        targetRole: UserRole,
        createdAt: Instant,
        expiresAt: Instant?,
    ) {
        validateTarget(
            actorUserId = actorUserId,
            actorRole = actorRole,
            targetUserId = targetUserId,
            targetRole = targetRole,
        )

        if (actorRole == UserRole.MODERATOR) {
            val expiration = expiresAt ?: throw SanctionDurationExceededException()
            val maximumExpiration = createdAt.plus(moderatorMaximumDuration)

            if (expiration.isAfter(maximumExpiration)) {
                throw SanctionDurationExceededException()
            }
        }

        if (expiresAt == null && actorRole.rank < UserRole.ADMIN.rank) {
            throw SanctionDurationExceededException()
        }
    }

    fun validateTarget(
        actorUserId: UserId,
        actorRole: UserRole,
        targetUserId: UserId,
        targetRole: UserRole,
    ) {
        if (actorUserId == targetUserId) {
            throw SelfSanctionException()
        }

        if (actorRole.rank <= targetRole.rank) {
            throw SanctionTargetAccessDeniedException()
        }
    }

    private val UserRole.rank: Int
        get() =
            when (this) {
                UserRole.USER,
                UserRole.PERSONALIZATION_CREATOR,
                UserRole.PERSONALIZATION_MODERATOR,
                -> 0
                UserRole.MODERATOR -> 1
                UserRole.ADMIN -> 2
                UserRole.SUPER_ADMIN -> 3
            }
}
