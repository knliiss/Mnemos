package com.mnemos.personalization.application.security.model

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole

data class PersonalizationActor(
    val userId: UserId,
    val roles: Set<UserRole>,
) {
    fun canCreateMarketplacePersonalization(): Boolean =
        UserRole.PERSONALIZATION_CREATOR in roles ||
            UserRole.ADMIN in roles ||
            UserRole.SUPER_ADMIN in roles

    fun canModeratePersonalization(): Boolean =
        UserRole.PERSONALIZATION_MODERATOR in roles ||
            UserRole.ADMIN in roles ||
            UserRole.SUPER_ADMIN in roles

    fun canModerateOwnPersonalization(): Boolean =
        UserRole.ADMIN in roles || UserRole.SUPER_ADMIN in roles

    fun canManageBadges(): Boolean =
        UserRole.ADMIN in roles || UserRole.SUPER_ADMIN in roles
}
