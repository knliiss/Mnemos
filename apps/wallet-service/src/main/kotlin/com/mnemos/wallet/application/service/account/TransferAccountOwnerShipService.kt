package com.mnemos.accountservice.application.service.account

import com.mnemos.accountservice.application.context.account.ActiveAccountContextProvider
import com.mnemos.accountservice.domain.exception.account.AccountOwnerRequiredException
import com.mnemos.accountservice.domain.exception.account.member.AccountMemberNotFoundException
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.repository.account.AccountMemberRepository
import com.mnemos.accountservice.domain.repository.account.AccountRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class TransferAccountOwnerShipService(
    val contextProvider: ActiveAccountContextProvider,
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
) {
    fun transfer(
        newOwnerMemberId: AccountMemberId,
        accountId: AccountId,
    ) {
        val context = contextProvider.requireActiveContextForUpdate(accountId)
        val account = context.account

        if (!account.isOwner(context.member.id)) {
            throw AccountOwnerRequiredException(accountId)
        }

        if (account.ownerMemberId == newOwnerMemberId) {
            return
        }

        val newOwnerMember =
            accountMemberRepository.findByIdAndAccountId(
                memberId = newOwnerMemberId,
                accountId = accountId,
            ) ?: throw AccountMemberNotFoundException(
                accountId = accountId,
                memberId = newOwnerMemberId,
            )

        if (account.transferOwnership(newOwnerMember.id, Instant.now())) {
            accountRepository.update(account)
        }
    }
}
