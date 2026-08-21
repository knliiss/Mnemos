package com.mnemos.account.application.service.account

import com.mnemos.account.api.dto.account.CreateAccountRequest
import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.application.event.account.AccountIntegrationEvents
import com.mnemos.account.application.result.account.CreateAccountResult
import com.mnemos.account.domain.exception.account.AccountIdentityConflictException
import com.mnemos.account.domain.exception.account.cristalix.CristalixPlayerNotFoundException
import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.role.AccountRoleName
import com.mnemos.account.domain.model.account.role.HexColor
import com.mnemos.account.domain.model.account.role.Role
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.account.integration.cristalix.CristalixIdentityProvider
import com.mnemos.account.integration.cristalix.model.CristalixPlayer
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate

@Service
class CreateAccountService(
    private val accountRepository: AccountRepository,
    private val accountMemberRepository: AccountMemberRepository,
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val cristalixIdentityProvider: CristalixIdentityProvider,
    private val currentUserProvider: CurrentUserProvider,
    private val integrationEventPublisher: IntegrationEventPublisher,
    private val transactionTemplate: TransactionTemplate,
) {
    fun createAccount(request: CreateAccountRequest): CreateAccountResult {
        val nickname = CristalixNickname(request.cristalixNickname)

        val player =
            cristalixIdentityProvider.findByNickname(nickname)
                ?: throw CristalixPlayerNotFoundException(nickname)

        return requireNotNull(
            transactionTemplate.execute {
                createFromPlayer(player)
            },
        ) {
            "Create account transaction returned no result"
        }
    }

    private fun createFromPlayer(player: CristalixPlayer): CreateAccountResult {
        val owner = currentUserProvider.get()

        if (accountRepository.existsByCristalixUuid(player.uuid)) {
            throw AccountIdentityConflictException(player.uuid)
        }

        val accountId = AccountId.generate()
        val ownerMemberId = AccountMemberId.generate()

        val account =
            Account.create(
                id = accountId,
                ownerMemberId = ownerMemberId,
                cristalixUuid = player.uuid,
                cristalixNickname = player.nickname,
            )

        val ownerMember =
            AccountMember.create(
                id = ownerMemberId,
                accountId = accountId,
                userId = owner.id,
            )

        val createdAccount = accountRepository.create(account)
        val createdOwnerMember = accountMemberRepository.create(ownerMember)

        val baseRole =
            roleRepository.create(
                Role.create(
                    accountId = createdAccount.id,
                    roleColor = HexColor(DEFAULT_BASE_ROLE_COLOR),
                    roleName = AccountRoleName(DEFAULT_BASE_ROLE_NAME),
                    hierarchyRank = Role.RANK_STEP,
                    permissions = DEFAULT_BASE_ROLE_PERMISSIONS.toMutableSet(),
                ),
            )

        createdAccount.changeBaseRole(baseRole.id)
        val accountWithBaseRole = accountRepository.update(createdAccount)

        owner.changeActiveAccount(accountWithBaseRole.id)
        userRepository.update(owner)

        integrationEventPublisher.publish(
            event =
                AccountIntegrationEvents.created(
                    account = accountWithBaseRole,
                    ownerMember = createdOwnerMember,
                ),
            metadata = EventMetadata(actorUserId = owner.id.id),
        )

        return CreateAccountResult(
            accountId = accountWithBaseRole.id,
            memberId = createdOwnerMember.id,
        )
    }

    private companion object {
        const val DEFAULT_BASE_ROLE_NAME = "Member"
        const val DEFAULT_BASE_ROLE_COLOR = "#A9B1A4"

        val DEFAULT_BASE_ROLE_PERMISSIONS =
            setOf(
                RolePermission.VIEW_MEMBERS,
                RolePermission.VIEW_ROLES,
                RolePermission.VIEW_WALLET,
                RolePermission.CLAIM_DAILY_REWARD,
                RolePermission.PUBLISH_COMMENTS,
                RolePermission.PUBLISH_REACTIONS,
                RolePermission.VIEW_BETS,
                RolePermission.VIEW_BET_HISTORY,
                RolePermission.VIEW_PERSONALIZATIONS,
                RolePermission.APPLY_PERSONALIZATIONS,
            )
    }
}
