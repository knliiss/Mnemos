package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.application.context.CurrentUserProvider
import com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider
import com.mnemos.accountservice.domain.exception.account.AccountNotFoundException
import com.mnemos.accountservice.domain.exception.account.AccountOwnerCannotLeaveException
import com.mnemos.accountservice.domain.exception.account.ActiveAccountNotSetException
import com.mnemos.accountservice.domain.repository.account.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.user.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LeaveActiveAccountService(
    private val userRepository: UserRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val currentUserProvider: CurrentUserProvider,
    private val accountContextProvider: ActiveAccountContextProvider,
) {
    @Transactional
    fun leaveActiveAccount() {
        val user = currentUserProvider.get()
        val accountId =
            user.activeAccountId
                ?: throw ActiveAccountNotSetException(user.id)

        val context =
            try {
                accountContextProvider.requireActiveContext(accountId)
            } catch (ex: AccountNotFoundException) {
                user.clearActiveAccount()
                userRepository.update(user)
                throw ex
            }

        val account = context.account
        val member = context.member

        if (account.ownerMemberId == member.id) {
            throw AccountOwnerCannotLeaveException(accountId)
        }

        accountMemberRepository.deleteById(member.id)

        user.clearActiveAccount()
        userRepository.update(user)
    }
}
