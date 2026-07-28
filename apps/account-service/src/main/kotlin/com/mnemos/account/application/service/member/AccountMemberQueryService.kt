package com.mnemos.account.application.service.member

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.query.account.member.AccountMemberListItem
import com.mnemos.account.application.query.account.member.AccountMemberQueryRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AccountMemberQueryService(
    private val contextProvider: ActiveAccountContextProvider,
    private val memberQueryRepository: AccountMemberQueryRepository,
) {
    fun getMembers(accountId: AccountId): List<AccountMemberListItem> {
        contextProvider.requireActiveContext(accountId)

        return memberQueryRepository.findAllByAccountId(accountId)
    }
}
