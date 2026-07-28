package com.mnemos.account.domain.model

import com.mnemos.account.domain.exception.account.AccountDeletionUnavailableException
import com.mnemos.account.domain.exception.account.member.AccountMemberLimitReachedException
import com.mnemos.account.domain.model.account.Account
import com.mnemos.account.domain.model.account.AccountMemberId
import com.mnemos.account.domain.model.account.AccountStatus
import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.account.domain.model.account.role.RoleId
import com.mnemos.account.support.TEST_TIME
import com.mnemos.account.support.account
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.UUID

class AccountTest {
    @Test
    fun `create initializes active account with consistent timestamps`() {
        val id = AccountId(UUID.randomUUID())
        val ownerId = AccountMemberId(UUID.randomUUID())
        val account = Account.create(id, ownerId, CristalixUuid(UUID.randomUUID()), CristalixNickname("PlayerOne"))

        assertEquals(id, account.id)
        assertEquals(ownerId, account.ownerMemberId)
        assertEquals(AccountStatus.ACTIVE, account.status)
        assertEquals(Account.DEFAULT_MAX_MEMBERS, account.maxMembers)
        assertEquals(account.createdAt, account.updatedAt)
        assertNull(account.deletionRequestedAt)
        assertNull(account.deletedAt)
        assertNull(account.baseRoleId)
    }

    @Test
    fun `create rejects non-positive member limit`() {
        assertThrows<IllegalArgumentException> {
            Account.create(
                AccountId(UUID.randomUUID()),
                AccountMemberId(UUID.randomUUID()),
                CristalixUuid(UUID.randomUUID()),
                CristalixNickname("PlayerOne"),
                maxMembers = 0,
            )
        }
    }

    @Test
    fun `transfer ownership updates owner and timestamp`() {
        val newOwnerId = AccountMemberId(UUID.randomUUID())
        val now = TEST_TIME.plusSeconds(10)
        val account = account()

        assertTrue(account.transferOwnership(newOwnerId, now))
        assertEquals(newOwnerId, account.ownerMemberId)
        assertEquals(now, account.updatedAt)
    }

    @Test
    fun `transfer ownership is idempotent for current owner`() {
        val account = account()
        val originalUpdatedAt = account.updatedAt

        assertFalse(account.transferOwnership(account.ownerMemberId, TEST_TIME.plusSeconds(10)))
        assertEquals(originalUpdatedAt, account.updatedAt)
    }

    @Test
    fun `request deletion moves active account to deleting`() {
        val account = account()
        val now = TEST_TIME.plusSeconds(20)

        account.requestDeletion(now)

        assertEquals(AccountStatus.DELETING, account.status)
        assertEquals(now, account.deletionRequestedAt)
        assertEquals(now, account.updatedAt)
        assertNull(account.deletedAt)
    }

    @Test
    fun `request deletion rejects non-active account`() {
        val account =
            account(
                status = AccountStatus.DELETING,
                deletionRequestedAt = TEST_TIME,
            )

        assertThrows<AccountDeletionUnavailableException> {
            account.requestDeletion(TEST_TIME.plusSeconds(1))
        }
    }

    @Test
    fun `complete deletion moves deleting account to deleted`() {
        val requestedAt = TEST_TIME
        val now = TEST_TIME.plusSeconds(30)
        val account =
            account(
                status = AccountStatus.DELETING,
                deletionRequestedAt = requestedAt,
            )

        account.completeDeletion(now)

        assertEquals(AccountStatus.DELETED, account.status)
        assertEquals(requestedAt, account.deletionRequestedAt)
        assertEquals(now, account.deletedAt)
        assertEquals(now, account.updatedAt)
    }

    @Test
    fun `complete deletion rejects account not in deleting state`() {
        assertThrows<AccountDeletionUnavailableException> {
            account().completeDeletion(TEST_TIME.plusSeconds(1))
        }
    }

    @Test
    fun `available slot accepts count below limit`() {
        account(maxMembers = 3).requireAvailableMemberSlot(2)
    }

    @Test
    fun `available slot rejects count equal to limit`() {
        assertThrows<AccountMemberLimitReachedException> {
            account(maxMembers = 3).requireAvailableMemberSlot(3)
        }
    }

    @Test
    fun `available slot rejects count above limit`() {
        assertThrows<AccountMemberLimitReachedException> {
            account(maxMembers = 3).requireAvailableMemberSlot(4)
        }
    }

    @Test
    fun `update member limit rejects zero and values below current count`() {
        val account = account(maxMembers = 10)

        assertThrows<IllegalArgumentException> { account.updateMaxMembers(0, 1) }
        assertThrows<IllegalArgumentException> { account.updateMaxMembers(4, 5) }
    }

    @Test
    fun `update member limit is idempotent`() {
        val account = account(maxMembers = 10)
        val updatedAt = account.updatedAt

        assertFalse(account.updateMaxMembers(10, 3, TEST_TIME.plusSeconds(5)))
        assertEquals(updatedAt, account.updatedAt)
    }

    @Test
    fun `update member limit changes value and timestamp`() {
        val account = account(maxMembers = 10)
        val now = TEST_TIME.plusSeconds(5)

        assertTrue(account.updateMaxMembers(12, 3, now))
        assertEquals(12, account.maxMembers)
        assertEquals(now, account.updatedAt)
    }

    @Test
    fun `change base role supports setting clearing and idempotency`() {
        val account = account()
        val roleId = RoleId(UUID.randomUUID())
        val firstChange = TEST_TIME.plusSeconds(1)
        val clearChange = TEST_TIME.plusSeconds(2)

        assertTrue(account.changeBaseRole(roleId, firstChange))
        assertEquals(roleId, account.baseRoleId)
        assertFalse(account.changeBaseRole(roleId, TEST_TIME.plusSeconds(10)))
        assertEquals(firstChange, account.updatedAt)
        assertTrue(account.changeBaseRole(null, clearChange))
        assertNull(account.baseRoleId)
        assertEquals(clearChange, account.updatedAt)
    }

    @Test
    fun `is owner compares member identifiers`() {
        val account = account()

        assertTrue(account.isOwner(account.ownerMemberId))
        assertFalse(account.isOwner(AccountMemberId(UUID.randomUUID())))
    }

    @Test
    fun `restore preserves deletion lifecycle state`() {
        val deletedAt = Instant.parse("2026-07-27T11:00:00Z")
        val account =
            account(
                status = AccountStatus.DELETED,
                deletionRequestedAt = TEST_TIME,
                deletedAt = deletedAt,
            )

        assertEquals(AccountStatus.DELETED, account.status)
        assertEquals(TEST_TIME, account.deletionRequestedAt)
        assertEquals(deletedAt, account.deletedAt)
        assertFalse(account.isActive())
    }
}
