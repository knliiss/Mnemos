package com.mnemos.moderation.adapter.out.security

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.user.UserRole
import com.mnemos.moderation.application.security.model.ModeratorActor
import com.mnemos.moderation.application.security.port.out.CurrentModeratorProvider
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Component

@Component
class CurrentModeratorProviderAdapter(
    private val currentMnemosUser: CurrentMnemosUser,
) : CurrentModeratorProvider {
    override fun current(): ModeratorActor {
        val principal = currentMnemosUser.get()
        val userId = requireNotNull(principal.userId)
        val role =
            when {
                UserRole.SUPER_ADMIN in principal.roles -> UserRole.SUPER_ADMIN
                UserRole.ADMIN in principal.roles -> UserRole.ADMIN
                UserRole.MODERATOR in principal.roles -> UserRole.MODERATOR
                else -> UserRole.USER
            }

        return ModeratorActor(
            userId = UserId(userId),
            role = role,
        )
    }
}
