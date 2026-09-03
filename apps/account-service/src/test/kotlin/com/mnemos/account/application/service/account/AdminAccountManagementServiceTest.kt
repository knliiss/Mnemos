package com.mnemos.account.application.service.account

import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.repository.account.AccountMemberRepository
import com.mnemos.account.domain.repository.account.AccountRepository
import com.mnemos.account.domain.repository.role.RoleAssignmentRepository
import com.mnemos.account.domain.repository.user.UserRepository
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.id.UserId
import com.mnemos.contracts.account.v1.AccountStatusChangedV1
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class AdminAccountManagementServiceTest {
    @Test
    fun `permanent deletion persists tombstone and clears active account references`() {
        val account = createAccount()
        val recipientUserId = UserId.generate()
        val accountRepository = mock<AccountRepository>()
        val accountMemberRepository = mock<AccountMemberRepository>()
        val roleAssignmentRepository = mock<RoleAssignmentRepository>()
        val userRepository = mock<UserRepository>()
        val integrationEventPublisher = mock<IntegrationEventPublisher>()

        whenever(accountRepository.findByIdForUpdate(account.id)).thenReturn(account)
        whenever(accountMemberRepository.findUserIdsByAccountId(account.id))
            .thenReturn(listOf(recipientUserId))
        whenever(accountRepository.update(account)).thenReturn(account)

        val service =
            AdminAccountManagementService(
                accountRepository = accountRepository,
                accountMemberRepository = accountMemberRepository,
                roleAssignmentRepository = roleAssignmentRepository,
                userRepository = userRepository,
                integrationEventPublisher = integrationEventPublisher,
            )

        service.deleteAccount(account.id)

        assertEquals(AccountStatus.DELETED, account.status)
        assertNotNull(account.deletedAt)

        verify(userRepository).clearActiveAccountByAccountId(
            accountId = account.id,
            now = account.deletedAt!!,
        )
        verify(accountRepository).update(account)
        verify(integrationEventPublisher).publish<AccountStatusChangedV1>(any(), any())
    }

    private fun createAccount(): Account =
        Account.create(
            id = AccountId.generate(),
            ownerMemberId = AccountMemberId.generate(),
            cristalixUuid = CristalixUuid(UUID.fromString("22222222-2222-2222-2222-222222222222")),
            cristalixNickname = CristalixNickname("mnemos"),
        )
}
