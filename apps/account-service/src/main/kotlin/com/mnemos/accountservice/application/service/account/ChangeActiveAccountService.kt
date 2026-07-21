package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.api.dto.account.AccountResponse
import com.mnemos.accountservice.api.mapper.account.AccountMapper.toResponse
import com.mnemos.accountservice.application.security.CurrentUserProvider
import com.mnemos.accountservice.domain.exception.account.AccountAccessDeniedException
import com.mnemos.accountservice.domain.exception.account.AccountNotFoundException
import com.mnemos.accountservice.domain.repository.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.AccountRepository
import com.mnemos.accountservice.domain.repository.UserRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChangeActiveAccountService(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val currentUserProvider: CurrentUserProvider
) {

    @Transactional
    fun changeActiveAccount(
        accountId: AccountId
    ): AccountResponse {
        val user = currentUserProvider.get()

        val account = accountRepository.findById(accountId = accountId)
            ?: throw AccountNotFoundException(accountId)

        if (!accountMemberRepository.existsByAccountIdAndUserId(
                accountId = account.id,
                userId = user.id
            )
        ) {
            throw AccountAccessDeniedException(accountId)
        }

        if (user.activateAccount(accountId = account.id)) userRepository.update(user)
        return account.toResponse()
    }

}