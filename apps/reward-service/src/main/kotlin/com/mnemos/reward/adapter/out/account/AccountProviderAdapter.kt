package com.mnemos.reward.adapter.out.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.reward.application.daily.port.out.AccountByNicknameProvider
import com.mnemos.reward.application.daily.port.out.ActiveAccountContext
import com.mnemos.reward.application.daily.port.out.ActiveAccountProvider
import com.mnemos.reward.application.daily.port.out.TelegramActiveAccountProvider
import com.mnemos.reward.domain.daily.exception.ActiveAccountRequiredException
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Component

@Component
class AccountProviderAdapter(
    private val client: AccountInternalClient,
    private val currentUser: CurrentMnemosUser,
) : ActiveAccountProvider,
    AccountByNicknameProvider,
    TelegramActiveAccountProvider {
    override fun getActiveAccountContext(): ActiveAccountContext {
        val userId = UserId(currentUser.requireUserId())

        return resolveActiveAccount(userId)
    }

    override fun findAccountId(nickname: String): AccountId? =
        client
            .resolve(cristalixNickname = nickname)
            ?.accountId
            ?.let(::AccountId)

    override fun findByTelegramId(telegramId: TelegramId): ActiveAccountContext? {
        val context = client.resolveTelegramUserContext(telegramId.id) ?: return null
        val accountId = context.activeAccountId ?: return null

        return ActiveAccountContext(
            accountId = AccountId(accountId),
            userId = UserId(context.userId),
        )
    }

    private fun resolveActiveAccount(userId: UserId): ActiveAccountContext {
        val accountId =
            client
                .resolve(userId = userId.id)
                ?.accountId
                ?.let(::AccountId)
                ?: throw ActiveAccountRequiredException()

        return ActiveAccountContext(
            accountId = accountId,
            userId = userId,
        )
    }
}
