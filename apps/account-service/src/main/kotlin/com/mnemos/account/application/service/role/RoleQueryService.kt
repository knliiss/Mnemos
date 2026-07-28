package com.mnemos.account.application.service.role

import com.mnemos.account.application.context.account.ActiveAccountContextProvider
import com.mnemos.account.application.query.account.role.RoleView
import com.mnemos.account.application.query.account.role.toView
import com.mnemos.account.domain.exception.role.RoleNotFoundException
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class RoleQueryService(
    private val contextProvider: ActiveAccountContextProvider,
    private val roleRepository: RoleRepository,
) {
    fun getRoles(accountId: AccountId): List<RoleView> {
        contextProvider.requireActiveContext(accountId)

        return roleRepository
            .findAllByAccountId(accountId)
            .map { it.toView() }
    }

    fun getRole(
        accountId: AccountId,
        roleId: RoleId,
    ): RoleView {
        contextProvider.requireActiveContext(accountId)

        return roleRepository
            .findByIdAndAccountId(
                roleId = roleId,
                accountId = accountId,
            )?.toView() ?: throw RoleNotFoundException(
            roleId = roleId,
            accountId = accountId,
        )
    }

    fun getMemberRoles(
        accountId: AccountId,
        memberId: AccountMemberId,
    ): List<RoleView> {
        contextProvider.requireActiveContext(accountId)

        return roleRepository
            .findAllByMemberId(
                accountId = accountId,
                memberId = memberId,
            ).map { it.toView() }
    }
}
