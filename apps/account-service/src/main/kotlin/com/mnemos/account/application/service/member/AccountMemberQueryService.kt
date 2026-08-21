package com.mnemos.account.application.service.member

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.query.account.member.AccountMemberListItem
import com.mnemos.account.application.query.account.member.AccountMemberQueryRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission.VIEW_MEMBERS
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AccountMemberQueryService(
    private val contextProvider: ActiveAccountContextProvider,
    private val memberQueryRepository: AccountMemberQueryRepository,
) {
    fun getMembers(accountId: AccountId): List<AccountMemberListItem> {
        contextProvider.requirePermission(
            accountId = accountId,
            permission = VIEW_MEMBERS,
        )

        return memberQueryRepository.findAllByAccountId(accountId)
    }
}
