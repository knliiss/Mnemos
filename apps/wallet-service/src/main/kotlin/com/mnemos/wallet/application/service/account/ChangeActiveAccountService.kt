package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.application.context.CurrentUserProvider
import com.mnemos.accountservice.domain.exception.account.member.AccountMemberNotFoundException
import com.mnemos.accountservice.domain.repository.account.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.account.AccountRepository
import com.mnemos.accountservice.domain.repository.user.UserRepository
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
