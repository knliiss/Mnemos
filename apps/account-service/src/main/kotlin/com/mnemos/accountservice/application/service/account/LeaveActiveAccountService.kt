package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.api.dto.account.AvailableAccountResponse
import com.mnemos.accountservice.api.mapper.account.AccountMapper.toAvailableAccountResponse
import com.mnemos.accountservice.application.security.CurrentUserProvider
import com.mnemos.accountservice.domain.exception.account.AccountOwnerCannotLeaveException
import com.mnemos.accountservice.domain.repository.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.AccountRepository
import com.mnemos.accountservice.domain.repository.UserRepository
import com.mnemos.common.core.model.user.UserId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LeaveActiveAccountService(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val currentUserProvider: CurrentUserProvider
) {

    @Transactional
    fun leaveActiveAccount(): List<AvailableAccountResponse> {
        val user = currentUserProvider.get()
        val accountId = user.activeAccountId
            ?: return getAccessibleAccounts(user.id)

        val account = accountRepository.findById(accountId)

        if (account == null) {
            user.clearActiveAccount()
            userRepository.update(user)
            return getAccessibleAccounts(user.id)
        }

        if (account.ownerUserId == user.id) {
            throw AccountOwnerCannotLeaveException(accountId)
        }

        accountMemberRepository.deleteByAccountIdAndUserId(
            userId = user.id,
            accountId = accountId
        )

        user.clearActiveAccount()
        userRepository.update(user)

        return getAccessibleAccounts(user.id)
    }

    private fun getAccessibleAccounts(userId: UserId): List<AvailableAccountResponse> {
        return accountRepository.findAllAvailableForUser(userId)
            .map { it.toAvailableAccountResponse() }
    }

}