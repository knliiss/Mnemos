package com.mnemos.account.application.service.account

import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.domain.exception.account.AccountNotFoundException
import com.mnemos.account.domain.exception.account.AccountUnavailableException
import com.mnemos.account.domain.exception.account.member.AccountMemberNotFoundException
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChangeActiveAccountService(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val currentUserProvider: CurrentUserProvider,
) {
    @Transactional
    fun changeActiveAccount(accountId: AccountId) {
        changeActiveAccount(
            user = currentUserProvider.get(),
            accountId = accountId,
        )
    }

    @Transactional
    fun changeActiveAccount(
        user: User,
        accountId: AccountId,
    ) {
        accountMemberRepository.findByAccountIdAndUserId(
            accountId = accountId,
            userId = user.id,
        ) ?: throw AccountMemberNotFoundException(
            accountId = accountId,
            userId = user.id,
        )

        val account =
            accountRepository.findByIdForUpdate(accountId)
                ?: throw AccountNotFoundException(accountId)

        if (!account.isActive()) {
            throw AccountUnavailableException(
                accountId = account.id,
                status = account.status,
            )
        }

        user.changeActiveAccount(accountId)

        userRepository.update(user)
    }
}
