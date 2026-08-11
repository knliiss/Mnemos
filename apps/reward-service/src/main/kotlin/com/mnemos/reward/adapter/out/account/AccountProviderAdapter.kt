package com.mnemos.reward.adapter.out.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.reward.application.daily.port.out.AccountByNicknameProvider
import com.mnemos.reward.application.daily.port.out.ActiveAccountProvider
import com.mnemos.reward.domain.daily.exception.ActiveAccountRequiredException
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Component

@Component
class AccountProviderAdapter(
    private val client: AccountInternalClient,
    private val currentUser: CurrentMnemosUser,
) : ActiveAccountProvider,
    AccountByNicknameProvider {
    override fun getActiveAccountId(): AccountId =
        client
            .resolve(userId = currentUser.requireUserId())
            ?.accountId
            ?.let(::AccountId)
            ?: throw ActiveAccountRequiredException()

    override fun findAccountId(nickname: String): AccountId? =
        client
            .resolve(cristalixNickname = nickname)
            ?.accountId
            ?.let(::AccountId)
}
