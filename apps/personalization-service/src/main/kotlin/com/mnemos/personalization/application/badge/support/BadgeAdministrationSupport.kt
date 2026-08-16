package com.mnemos.personalization.application.badge.support

import com.mnemos.personalization.application.badge.exception.BadgeNotFoundException
import com.mnemos.personalization.application.personalization.exception.PersonalizationAccessDeniedException
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.badge.repository.BadgeRepository
import org.springframework.stereotype.Component

@Component
class BadgeAdministrationSupport(
    private val actorProvider: CurrentPersonalizationActorProvider,
    private val repository: BadgeRepository,
) {
    fun requireAdministrator() {
        if (!actorProvider.requireActor().canManageBadges()) {
            throw PersonalizationAccessDeniedException()
        }
    }

    fun requireBadgeForUpdate(badgeId: BadgeId): Badge =
        repository.findByIdForUpdate(badgeId) ?: throw BadgeNotFoundException()
}
