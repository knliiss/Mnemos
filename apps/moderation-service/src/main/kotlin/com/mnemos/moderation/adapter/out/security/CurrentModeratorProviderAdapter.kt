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

        return ModeratorActor(
            userId = UserId(userId),
            role = principal.roles.maxByOrNull { role -> role.rank() } ?: UserRole.USER,
        )
    }

    private fun UserRole.rank(): Int =
        when (this) {
            UserRole.USER -> 0
            UserRole.MODERATOR -> 1
            UserRole.ADMIN -> 2
            UserRole.SUPER_ADMIN -> 3
        }
}
