package com.mnemos.account.application.service.role

import com.mnemos.account.application.query.account.role.RoleView
import com.mnemos.account.application.query.account.role.RoleViewMapper.toView
import com.mnemos.account.domain.exception.role.RoleNotFoundException
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.common.core.model.id.AccountId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AdminRoleQueryService(
    private val roleRepository: RoleRepository,
) {
    fun getRoles(accountId: AccountId): List<RoleView> =
        roleRepository
            .findAllByAccountIdOrderByRank(accountId)
            .map { it.toView() }

    fun getRole(
        accountId: AccountId,
        roleId: RoleId,
    ): RoleView =
        roleRepository
            .findByIdAndAccountId(
                roleId = roleId,
                accountId = accountId,
            )?.toView() ?: throw RoleNotFoundException(
            roleId = roleId,
            accountId = accountId,
        )

    fun getMemberRoles(
        accountId: AccountId,
        memberId: AccountMemberId,
    ): List<RoleView> =
        roleRepository
            .findAllByMemberId(
                accountId = accountId,
                memberId = memberId,
            ).map { it.toView() }
}
