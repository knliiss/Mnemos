package com.mnemos.account.application.service.account

import com.mnemos.account.api.dto.account.CreateAccountRequest
import com.mnemos.account.application.query.account.AccountQueryRepository
import com.mnemos.account.application.query.account.InternalTelegramAccountContext
import com.mnemos.account.application.service.member.JoinAccountService
import com.mnemos.account.application.service.user.InternalTelegramUserBootstrapService
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.TelegramId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class InternalTelegramAccountService(
    private val userRepository: UserRepository,
    private val accountQueryRepository: AccountQueryRepository,
    private val changeActiveAccountService: ChangeActiveAccountService,
    private val createAccountService: CreateAccountService,
    private val joinAccountService: JoinAccountService,
    private val userBootstrapService: InternalTelegramUserBootstrapService,
) {
    @Transactional(readOnly = true)
    fun getContext(telegramId: TelegramId): InternalTelegramAccountContext? {
        val user = userRepository.findByTelegramId(telegramId) ?: return null

        return getContext(user)
    }

    @Transactional
    fun createAccount(
        telegramId: Long,
        cristalixNickname: String,
    ): InternalTelegramAccountContext {
        val user = userBootstrapService.getOrCreate(telegramId)

        createAccountService.createAccountFor(
            owner = user,
            request = CreateAccountRequest(cristalixNickname = cristalixNickname),
        )

        return getContext(user)
    }

    @Transactional
    fun joinAccount(
        telegramId: Long,
        inviteCode: String,
    ): InternalTelegramAccountContext {
        val user = userBootstrapService.getOrCreate(telegramId)

        joinAccountService.joinAccountFor(
            user = user,
            rawInviteCode = inviteCode,
        )

        return getContext(user)
    }

    @Transactional
    fun changeActiveAccount(
        telegramId: TelegramId,
        accountId: AccountId,
    ): InternalTelegramAccountContext? {
        val user = userRepository.findByTelegramId(telegramId) ?: return null

        changeActiveAccountService.changeActiveAccount(
            user = user,
            accountId = accountId,
        )

        return getContext(user)
    }

    private fun getContext(user: User): InternalTelegramAccountContext =
        InternalTelegramAccountContext(
            activeAccountId = user.activeAccountId,
            accounts =
                accountQueryRepository.findSummariesByUserId(
                    userId = user.id,
                    activeAccountId = user.activeAccountId,
                ),
        )
}
