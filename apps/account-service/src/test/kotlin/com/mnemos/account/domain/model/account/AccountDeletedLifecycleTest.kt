package com.mnemos.account.domain.model.account

import com.mnemos.account.domain.exception.account.DeletedAccountImmutableException
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class AccountDeletedLifecycleTest {
    @Test
    fun `permanent deletion leaves a tombstone and records lifecycle timestamps`() {
        val account = createAccount()
        val deletedAt = Instant.parse("2026-09-03T00:15:00Z")

        account.deletePermanently(deletedAt)

        assertEquals(AccountStatus.DELETED, account.status)
        assertEquals(deletedAt, account.deletedAt)
        assertEquals(deletedAt, account.deactivatedAt)
        assertEquals(deletedAt, account.updatedAt)
        assertNotNull(account.cristalixUuid)
        assertEquals("knaliz", account.cristalixNickname.value)
    }

    @Test
    fun `deleted account cannot be reactivated or mutated`() {
        val account = createAccount()

        account.deletePermanently(Instant.parse("2026-09-03T00:15:00Z"))

        assertThrows(DeletedAccountImmutableException::class.java) {
            account.reactivate(Instant.parse("2026-09-03T00:16:00Z"))
        }
        assertThrows(DeletedAccountImmutableException::class.java) {
            account.updateCristalixNickname(
                newCristalixNickname = CristalixNickname("renamed"),
                now = Instant.parse("2026-09-03T00:16:00Z"),
            )
        }
        assertThrows(DeletedAccountImmutableException::class.java) {
            account.transferOwnership(
                newOwnerMemberId = AccountMemberId.generate(),
                now = Instant.parse("2026-09-03T00:16:00Z"),
            )
        }
        assertThrows(DeletedAccountImmutableException::class.java) {
            account.requireAvailableMemberSlot(currentMemberCount = 1)
        }
    }

    @Test
    fun `deactivated account keeps original deactivation timestamp when permanently deleted`() {
        val account = createAccount()
        val deactivatedAt = Instant.parse("2026-09-03T00:10:00Z")
        val deletedAt = Instant.parse("2026-09-03T00:15:00Z")

        account.deactivate(deactivatedAt)
        account.deletePermanently(deletedAt)

        assertEquals(AccountStatus.DELETED, account.status)
        assertEquals(deactivatedAt, account.deactivatedAt)
        assertEquals(deletedAt, account.deletedAt)
        assertEquals(deletedAt, account.updatedAt)
    }

    private fun createAccount(): Account =
        Account.create(
            id = AccountId.generate(),
            ownerMemberId = AccountMemberId.generate(),
            cristalixUuid = CristalixUuid(UUID.fromString("11111111-1111-1111-1111-111111111111")),
            cristalixNickname = CristalixNickname("knaliz"),
        )
}
