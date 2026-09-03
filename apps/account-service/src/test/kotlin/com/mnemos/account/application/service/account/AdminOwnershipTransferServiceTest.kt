package com.mnemos.account.application.service.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMember
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.model.user.User
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.role.RoleAssignmentRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.KeycloakId
import com.mnemos.common.core.model.id.TelegramId
import com.mnemos.common.core.model.user.Username
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class AdminOwnershipTransferServiceTest {
    @Test
    fun `admin transfer joins external user and transfers ownership atomically`() {
        val previousOwnerUser = createUser(931458258, "previous-owner")
        val newOwnerUser = createUser(931458259, "new-owner")
        val previousOwnerMemberId = AccountMemberId.generate()
        val account = createAccount(previousOwnerMemberId, maxMembers = 1)
        val previousOwnerMember =
            AccountMember.create(
                id = previousOwnerMemberId,
                accountId = account.id,
                userId = previousOwnerUser.id,
            )
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleAssignmentRepository = mock<RoleAssignmentRepository>()
        val userRepository = mock<UserRepository>()
        val eventPublisher = mock<IntegrationEventPublisher>()

        whenever(accountRepository.findByIdForUpdate(account.id)).thenReturn(account)
        whenever(userRepository.findById(newOwnerUser.id)).thenReturn(newOwnerUser)
        whenever(
            accountMemberRepository.findByAccountIdAndUserId(
                accountId = account.id,
                userId = newOwnerUser.id,
            ),
        ).thenReturn(null)
        whenever(
            accountMemberRepository.findByIdAndAccountId(
                memberId = previousOwnerMemberId,
                accountId = account.id,
            ),
        ).thenReturn(previousOwnerMember)
        whenever(accountMemberRepository.countByAccountId(account.id)).thenReturn(1)
        whenever(accountMemberRepository.create(any())).thenAnswer { invocation ->
            invocation.getArgument<AccountMember>(0)
        }
        whenever(accountRepository.update(account)).thenReturn(account)
        whenever(accountMemberRepository.findUserIdsByAccountId(account.id))
            .thenReturn(listOf(previousOwnerUser.id, newOwnerUser.id))

        val service =
            AdminAccountManagementService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleAssignmentRepository = roleAssignmentRepository,
                userRepository = userRepository,
                integrationEventPublisher = eventPublisher,
            )

        service.transferOwnership(
            accountId = account.id,
            newOwnerUserId = newOwnerUser.id,
        )

        val memberCaptor = argumentCaptor<AccountMember>()
        verify(accountMemberRepository).create(memberCaptor.capture())

        val createdMember = memberCaptor.firstValue

        assertEquals(newOwnerUser.id, createdMember.userId)
        assertEquals(account.id, createdMember.accountId)
        assertEquals(createdMember.id, account.ownerMemberId)
        assertNotEquals(previousOwnerMemberId, account.ownerMemberId)
        assertEquals(2, account.maxMembers)

        val eventCaptor = argumentCaptor<IntegrationEvent<Any>>()
        verify(eventPublisher, times(2)).publish(eventCaptor.capture(), any())

        assertEquals(
            listOf(
                AccountEventType.ACCOUNT_MEMBER_JOINED,
                AccountEventType.ACCOUNT_OWNER_TRANSFERRED,
            ),
            eventCaptor.allValues.map { event -> event.type },
        )
    }

    @Test
    fun `admin transfer to existing member does not create duplicate membership`() {
        val previousOwnerUser = createUser(931458260, "previous-owner")
        val newOwnerUser = createUser(931458261, "existing-member")
        val previousOwnerMemberId = AccountMemberId.generate()
        val newOwnerMemberId = AccountMemberId.generate()
        val account = createAccount(previousOwnerMemberId)
        val previousOwnerMember =
            AccountMember.create(
                id = previousOwnerMemberId,
                accountId = account.id,
                userId = previousOwnerUser.id,
            )
        val newOwnerMember =
            AccountMember.create(
                id = newOwnerMemberId,
                accountId = account.id,
                userId = newOwnerUser.id,
            )
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleAssignmentRepository = mock<RoleAssignmentRepository>()
        val userRepository = mock<UserRepository>()
        val eventPublisher = mock<IntegrationEventPublisher>()

        whenever(accountRepository.findByIdForUpdate(account.id)).thenReturn(account)
        whenever(userRepository.findById(newOwnerUser.id)).thenReturn(newOwnerUser)
        whenever(
            accountMemberRepository.findByAccountIdAndUserId(
                accountId = account.id,
                userId = newOwnerUser.id,
            ),
        ).thenReturn(newOwnerMember)
        whenever(
            accountMemberRepository.findByIdAndAccountId(
                memberId = previousOwnerMemberId,
                accountId = account.id,
            ),
        ).thenReturn(previousOwnerMember)
        whenever(accountRepository.update(account)).thenReturn(account)
        whenever(accountMemberRepository.findUserIdsByAccountId(account.id))
            .thenReturn(listOf(previousOwnerUser.id, newOwnerUser.id))

        val service =
            AdminAccountManagementService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleAssignmentRepository = roleAssignmentRepository,
                userRepository = userRepository,
                integrationEventPublisher = eventPublisher,
            )

        service.transferOwnership(
            accountId = account.id,
            newOwnerUserId = newOwnerUser.id,
        )

        assertEquals(newOwnerMemberId, account.ownerMemberId)
        verify(accountMemberRepository, never()).create(any())

        val eventCaptor = argumentCaptor<IntegrationEvent<Any>>()
        verify(eventPublisher).publish(eventCaptor.capture(), any())

        assertEquals(
            AccountEventType.ACCOUNT_OWNER_TRANSFERRED,
            eventCaptor.firstValue.type,
        )
    }

    private fun createAccount(
        ownerMemberId: AccountMemberId,
        maxMembers: Int = Account.DEFAULT_MAX_MEMBERS,
    ): Account =
        Account.create(
            id = AccountId.generate(),
            ownerMemberId = ownerMemberId,
            cristalixUuid = CristalixUuid(UUID.randomUUID()),
            cristalixNickname = CristalixNickname("admin-test"),
            maxMembers = maxMembers,
        )

    private fun createUser(
        telegramId: Long,
        username: String,
    ): User =
        User.create(
            telegramId = TelegramId(telegramId),
            username = Username(username),
            keycloakId = KeycloakId(UUID.randomUUID()),
        )
}
