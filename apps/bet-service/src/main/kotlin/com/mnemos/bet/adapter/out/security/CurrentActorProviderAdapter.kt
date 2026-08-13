package com.mnemos.bet.adapter.out.security

import com.mnemos.bet.application.security.port.out.CurrentActorProvider
import com.mnemos.common.core.model.id.UserId
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Component

@Component
class CurrentActorProviderAdapter(
    private val currentMnemosUser: CurrentMnemosUser,
) : CurrentActorProvider {
    override fun requireUserId(): UserId = UserId(currentMnemosUser.requireUserId())
}
