package com.mnemos.accountservice.domain.model

import com.mnemos.accountservice.domain.exception.invite.AccountInviteUnavailableException
import com.mnemos.accountservice.domain.model.account.invite.AccountInvite
import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import com.mnemos.accountservice.support.TEST_TIME
import com.mnemos.accountservice.support.invite
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.user.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class AccountInviteTest {
    @Test
    fun `create accepts invite without expiration`() {
        val invite =
            AccountInvite.create(
                accountId = AccountId(UUID.randomUUID()),
                code = InviteCode.of("ABCD-EFGH-JKLM-NPQR"),
                createdByUserId = UserId(UUID.randomUUID()),
                expiresAt = null,
                now = TEST_TIME,
            )

        assertEquals(TEST_TIME, invite.createdAt)
        assertNull(invite.expiresAt)
        assertTrue(invite.isAvailableAt(TEST_TIME.plusSeconds(100)))
    }

    @Test
    fun `create rejects expiration equal to creation time`() {
        assertThrows<IllegalArgumentException> {
            AccountInvite.create(
                AccountId(UUID.randomUUID()),
                InviteCode.of("ABCD-EFGH-JKLM-NPQR"),
                UserId(UUID.randomUUID()),
                TEST_TIME,
                TEST_TIME,
            )
        }
    }

    @Test
    fun `create rejects expiration in the past`() {
        assertThrows<IllegalArgumentException> {
            AccountInvite.create(
                AccountId(UUID.randomUUID()),
                InviteCode.of("ABCD-EFGH-JKLM-NPQR"),
                UserId(UUID.randomUUID()),
                TEST_TIME.minusSeconds(1),
                TEST_TIME,
            )
        }
    }

    @Test
    fun `invite is available strictly before expiration`() {
        val invite = invite(expiresAt = TEST_TIME.plusSeconds(10))

        assertTrue(invite.isAvailableAt(TEST_TIME.plusSeconds(9)))
        assertFalse(invite.isAvailableAt(TEST_TIME.plusSeconds(10)))
        assertFalse(invite.isAvailableAt(TEST_TIME.plusSeconds(11)))
    }

    @Test
    fun `consume marks available invite`() {
        val invite = invite()
        val consumedAt = TEST_TIME.plusSeconds(5)

        invite.consume(consumedAt)

        assertEquals(consumedAt, invite.consumedAt)
        assertFalse(invite.isAvailableAt(consumedAt))
    }

    @Test
    fun `consume rejects consumed invite`() {
        val invite = invite(consumedAt = TEST_TIME.plusSeconds(1))

        assertThrows<AccountInviteUnavailableException> {
            invite.consume(TEST_TIME.plusSeconds(2))
        }
    }

    @Test
    fun `consume rejects revoked invite`() {
        val invite = invite(revokedAt = TEST_TIME.plusSeconds(1))

        assertThrows<AccountInviteUnavailableException> {
            invite.consume(TEST_TIME.plusSeconds(2))
        }
    }

    @Test
    fun `consume rejects expired invite`() {
        val invite = invite(expiresAt = TEST_TIME.plusSeconds(1))

        assertThrows<AccountInviteUnavailableException> {
            invite.consume(TEST_TIME.plusSeconds(1))
        }
    }

    @Test
    fun `revoke marks available invite and is idempotent`() {
        val invite = invite()
        val revokedAt = TEST_TIME.plusSeconds(5)

        assertTrue(invite.revoke(revokedAt))
        assertEquals(revokedAt, invite.revokedAt)
        assertFalse(invite.revoke(TEST_TIME.plusSeconds(10)))
        assertEquals(revokedAt, invite.revokedAt)
    }

    @Test
    fun `revoke does not modify consumed invite`() {
        val consumedAt = TEST_TIME.plusSeconds(1)
        val invite = invite(consumedAt = consumedAt)

        assertFalse(invite.revoke(TEST_TIME.plusSeconds(2)))
        assertEquals(consumedAt, invite.consumedAt)
        assertNull(invite.revokedAt)
    }
}
