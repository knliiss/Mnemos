package com.mnemos.account.application.service.account

import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.domain.exception.account.member.AccountMemberNotFoundException
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
        val user = currentUserProvider.get()

        accountMemberRepository.findByAccountIdAndUserId(
            accountId = accountId,
            userId = user.id,
        ) ?: throw AccountMemberNotFoundException(
            accountId = accountId,
            userId = user.id,
        )

        user.changeActiveAccount(accountId)

        userRepository.update(user)
    }
}
