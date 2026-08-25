package com.mnemos.account.application.service.account

import com.mnemos.account.api.dto.account.CreateAccountRequest
import com.mnemos.account.application.context.CurrentUserProvider
import com.mnemos.account.domain.exception.account.AccountIdentityConflictException
import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.role.RoleRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.account.integration.cristalix.CristalixIdentityProvider
import com.mnemos.account.integration.cristalix.model.CristalixPlayer
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.Username
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID
import kotlin.test.assertEquals

class CreateAccountServiceTest {
    @Test
    fun `owner re-registering deactivated account reactivates existing account`() {
        val owner = createUser()
        val ownerMemberId = AccountMemberId.generate()
        val player = createPlayer()
        val account =
            Account.create(
                id = AccountId.generate(),
                ownerMemberId = ownerMemberId,
                cristalixUuid = player.uuid,
                cristalixNickname = CristalixNickname("OldNick"),
            )
        val ownerMember =
            AccountMember.create(
                id = ownerMemberId,
                accountId = account.id,
                userId = owner.id,
            )

        account.deactivate()

        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleRepository = mock<RoleRepository>()
        val userRepository = mock<UserRepository>()
        val cristalixIdentityProvider = mock<CristalixIdentityProvider>()
        val currentUserProvider = mock<CurrentUserProvider>()
        val integrationEventPublisher = mock<IntegrationEventPublisher>()

        whenever(cristalixIdentityProvider.findByNickname(player.nickname)).thenReturn(player)
        whenever(currentUserProvider.get()).thenReturn(owner)
        whenever(accountRepository.findByCristalixUuidForUpdate(player.uuid)).thenReturn(account)
        whenever(
            accountMemberRepository.findByIdAndAccountId(
                memberId = ownerMemberId,
                accountId = account.id,
            ),
        ).thenReturn(ownerMember)
        whenever(accountRepository.update(account)).thenReturn(account)
        whenever(userRepository.update(owner)).thenReturn(owner)

        val service =
            CreateAccountService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleRepository = roleRepository,
                userRepository = userRepository,
                cristalixIdentityProvider = cristalixIdentityProvider,
                currentUserProvider = currentUserProvider,
                integrationEventPublisher = integrationEventPublisher,
                transactionTemplate = transactionTemplate(),
            )

        val result =
            service.createAccount(
                CreateAccountRequest(cristalixNickname = player.nickname.value),
            )

        assertEquals(account.id, result.accountId)
        assertEquals(ownerMember.id, result.memberId)
        assertEquals(AccountStatus.ACTIVE, account.status)
        assertEquals(player.nickname, account.cristalixNickname)
        assertEquals(account.id, owner.activeAccountId)

        verify(accountRepository).update(account)
        verify(userRepository).update(owner)
        verify(accountRepository, never()).create(any())
        verifyNoInteractions(roleRepository, integrationEventPublisher)
    }

    @Test
    fun `non-owner cannot reactivate account by registering its Cristalix identity`() {
        val currentUser = createUser()
        val actualOwner =
            User.create(
                telegramId = TelegramId(931458259),
                username = Username("other-owner"),
                keycloakId = KeycloakId(UUID.fromString("49f20154-d86d-41c5-9659-6986437905bd")),
            )
        val ownerMemberId = AccountMemberId.generate()
        val player = createPlayer()
        val account =
            Account.create(
                id = AccountId.generate(),
                ownerMemberId = ownerMemberId,
                cristalixUuid = player.uuid,
                cristalixNickname = player.nickname,
            )
        val ownerMember =
            AccountMember.create(
                id = ownerMemberId,
                accountId = account.id,
                userId = actualOwner.id,
            )

        account.deactivate()

        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleRepository = mock<RoleRepository>()
        val userRepository = mock<UserRepository>()
        val cristalixIdentityProvider = mock<CristalixIdentityProvider>()
        val currentUserProvider = mock<CurrentUserProvider>()
        val integrationEventPublisher = mock<IntegrationEventPublisher>()

        whenever(cristalixIdentityProvider.findByNickname(player.nickname)).thenReturn(player)
        whenever(currentUserProvider.get()).thenReturn(currentUser)
        whenever(accountRepository.findByCristalixUuidForUpdate(player.uuid)).thenReturn(account)
        whenever(
            accountMemberRepository.findByIdAndAccountId(
                memberId = ownerMemberId,
                accountId = account.id,
            ),
        ).thenReturn(ownerMember)

        val service =
            CreateAccountService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleRepository = roleRepository,
                userRepository = userRepository,
                cristalixIdentityProvider = cristalixIdentityProvider,
                currentUserProvider = currentUserProvider,
                integrationEventPublisher = integrationEventPublisher,
                transactionTemplate = transactionTemplate(),
            )

        assertThrows<AccountIdentityConflictException> {
            service.createAccount(
                CreateAccountRequest(cristalixNickname = player.nickname.value),
            )
        }

        assertEquals(AccountStatus.DEACTIVATED, account.status)
        verify(accountRepository, never()).update(any())
        verifyNoInteractions(roleRepository, userRepository, integrationEventPublisher)
    }

    private fun createPlayer(): CristalixPlayer =
        CristalixPlayer(
            uuid = CristalixUuid(UUID.fromString("11111111-1111-1111-1111-111111111111")),
            nickname = CristalixNickname("knaliz"),
        )

    private fun createUser(): User =
        User.create(
            telegramId = TelegramId(931458258),
            username = Username("knaliz"),
            keycloakId = KeycloakId(UUID.fromString("39f20154-d86d-41c5-9659-6986437905bd")),
        )

    private fun transactionTemplate(): TransactionTemplate {
        val transactionManager = mock<PlatformTransactionManager>()
        val transactionStatus = mock<TransactionStatus>()

        whenever(
            transactionManager.getTransaction(any<TransactionDefinition>()),
        ).thenReturn(transactionStatus)

        return TransactionTemplate(transactionManager)
    }
}
