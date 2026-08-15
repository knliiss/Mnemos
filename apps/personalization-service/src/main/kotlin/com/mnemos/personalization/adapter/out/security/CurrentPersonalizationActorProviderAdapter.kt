package com.mnemos.personalization.adapter.out.security

import com.mnemos.common.core.model.id.UserId
import com.mnemos.personalization.application.security.model.PersonalizationActor
import com.mnemos.personalization.application.security.port.out.CurrentPersonalizationActorProvider
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Component

@Component
class CurrentPersonalizationActorProviderAdapter(
    private val currentMnemosUser: CurrentMnemosUser,
) : CurrentPersonalizationActorProvider {
    override fun requireActor(): PersonalizationActor {
        val principal = currentMnemosUser.get()
        val userId =
            requireNotNull(principal.userId) {
                "Required JWT claim mnemos_user_id is missing"
            }

        return PersonalizationActor(
            userId = UserId(userId),
            roles = principal.roles,
        )
    }
}
