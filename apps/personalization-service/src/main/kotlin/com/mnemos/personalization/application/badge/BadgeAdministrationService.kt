package com.mnemos.personalization.application.badge

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.badge.exception.BadgeAlreadyExistsException
import com.mnemos.personalization.application.badge.exception.BadgeNotFoundException
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.model.UserBadge
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import com.mnemos.personalization.domain.personalization.model.HexColor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

data class CreateBadgeCommand(
    val code: String,
    val name: String,
    val icon: String,
    val color: HexColor,
)

@Service
class BadgeAdministrationService(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: BadgeRepository,
    private val clock: Clock,
) {
    @Transactional
    fun create(command: CreateBadgeCommand): Badge {
        requireAdministrator()

        if (repository.findByCode(command.code) != null) {
            throw BadgeAlreadyExistsException()
        }

        return repository.save(
            Badge.create(
                code = command.code,
                name = command.name,
                icon = command.icon,
                color = command.color,
                now = clock.instant(),
            ),
        )
    }

    @Transactional
    fun grant(
        userId: UserId,
        badgeId: BadgeId,
    ): UserBadge {
        requireAdministrator()
        requireBadge(badgeId)

        repository.findUserBadge(userId, badgeId)?.let { existing ->
            return existing
        }

        return repository.saveUserBadge(
            UserBadge(
                userId = userId,
                badgeId = badgeId,
                grantedAt = clock.instant(),
                active = false,
            ),
        )
    }

    @Transactional
    fun revoke(
        userId: UserId,
        badgeId: BadgeId,
    ) {
        requireAdministrator()

        if (repository.findUserBadgeForUpdate(userId, badgeId) == null) {
            throw BadgeNotFoundException()
        }

        repository.deleteUserBadge(userId, badgeId)
    }

    private fun requireAdministrator() {
        if (!actorProvider.requireActor().canManageBadges()) {
            throw PersonalizationAccessDeniedException()
        }
    }

    private fun requireBadge(badgeId: BadgeId): Badge =
        repository.findById(badgeId) ?: throw BadgeNotFoundException()
}
