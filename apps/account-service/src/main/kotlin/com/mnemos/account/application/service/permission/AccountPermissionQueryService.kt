package com.mnemos.account.application.service.permission

import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.core.model.permission.RolePermission
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class AccountPermissionQueryService(
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val userRepository: UserRepository,
) {
    fun hasPermission(
        accountId: AccountId,
        userId: UserId,
        permission: RolePermission,
    ): Boolean {
        val account =
            accountRepository.findActiveById(accountId)
                ?: return false

        val member =
            accountMemberRepository.findByAccountIdAndUserId(
                accountId = accountId,
                userId = userId,
            ) ?: return false

        if (account.ownerMemberId == member.id) {
            return true
        }

        return accountMemberRepository.hasPermission(
            memberId = member.id,
            permission = permission,
        )
    }

    fun getEffectivePermissions(
        accountId: AccountId,
        userId: UserId,
    ): Set<RolePermission> {
        val account =
            accountRepository.findActiveById(accountId)
                ?: return emptySet()

        val member =
            accountMemberRepository.findByAccountIdAndUserId(
                accountId = accountId,
                userId = userId,
            ) ?: return emptySet()

        if (account.ownerMemberId == member.id) {
            return RolePermission.entries.toSet()
        }

        return accountMemberRepository.findPermissions(member.id)
    }

    fun hasPermissionByKeycloakId(
        accountId: AccountId,
        keycloakId: KeycloakId,
        permission: RolePermission,
    ): Boolean {
        val user =
            userRepository.findByKeycloakId(keycloakId)
                ?: return false

        return hasPermission(
            accountId = accountId,
            userId = user.id,
            permission = permission,
        )
    }
}
