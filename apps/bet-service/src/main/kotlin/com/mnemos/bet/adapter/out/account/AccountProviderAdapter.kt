package com.mnemos.bet.adapter.out.account

import com.mnemos.bet.application.account.model.AccountSnapshot
import com.mnemos.bet.application.account.port.out.AccountProvider
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Component

@Component
class AccountProviderAdapter(
    private val client: AccountInternalClient,
    private val currentUser: CurrentMnemosUser,
) : AccountProvider {
    override fun requireActiveAccount(): AccountSnapshot =
        client
            .resolve(userId = currentUser.requireUserId())
            ?.let {
                AccountSnapshot(
                    id = AccountId(it.accountId),
                    nickname = it.cristalixNickname,
                )
            }
            ?: throw IllegalStateException("Active account is required")

    override fun findByNickname(nickname: String): AccountSnapshot? =
        client
            .resolve(cristalixNickname = nickname)
            ?.let {
                AccountSnapshot(
                    id = AccountId(it.accountId),
                    nickname = it.cristalixNickname,
                )
            }
}
