package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.application.context.CurrentUserProvider
import com.mnemos.accountservice.domain.exception.account.AccountNotActiveException
import com.mnemos.accountservice.domain.exception.account.AccountNotFoundException
import com.mnemos.accountservice.domain.exception.account.AccountOwnerRequiredException
import com.mnemos.accountservice.domain.exception.account.member.ActiveAccountMembershipNotFoundException
import com.mnemos.accountservice.domain.repository.account.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.account.AccountRepository
import com.mnemos.accountservice.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class DeleteAccountService(
    private val currentUserProvider: CurrentUserProvider,
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val userRepository: UserRepository,
) {
    @Transactional
    fun deleteAccount(accountId: AccountId) {
        val user = currentUserProvider.get()

        if (user.activeAccountId != accountId) {
            throw AccountNotActiveException(accountId)
        }

        val account =
            accountRepository.findByIdForUpdate(accountId)
                ?: throw AccountNotFoundException(accountId)

        val member =
            accountMemberRepository.findByAccountIdAndUserId(
                accountId = accountId,
                userId = user.id,
            ) ?: throw ActiveAccountMembershipNotFoundException(
                accountId = accountId,
                userId = user.id,
            )

        if (account.ownerMemberId != member.id) {
            throw AccountOwnerRequiredException(accountId)
        }

        val now = Instant.now()

        account.requestDeletion(now)

        userRepository.clearActiveAccountByAccountId(
            accountId = accountId,
            now = now,
        )

        accountRepository.update(account)
    }
}
