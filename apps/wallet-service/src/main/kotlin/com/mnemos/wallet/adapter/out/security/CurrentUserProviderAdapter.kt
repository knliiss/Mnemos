package com.mnemos.wallet.adapter.out.security

import com.mnemos.common.core.model.id.UserId
import com.mnemos.security.CurrentMnemosUser
import com.mnemos.wallet.application.security.port.out.CurrentUserProvider
import org.springframework.stereotype.Component

@Component
class CurrentUserProviderAdapter(
    private val currentMnemosUser: CurrentMnemosUser,
) : CurrentUserProvider {
    override fun requireUserId(): UserId = UserId(currentMnemosUser.requireUserId())
}
