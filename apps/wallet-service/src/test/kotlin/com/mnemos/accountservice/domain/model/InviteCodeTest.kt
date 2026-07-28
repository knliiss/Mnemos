package com.mnemos.accountservice.domain.model

import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class InviteCodeTest {
    @Test
    fun `normalizes lowercase unformatted value`() {
        assertEquals("ABCD-EFGH-JKLM-NPQR", InviteCode.of("abcdefghjklmnpqr").value)
    }

    @Test
    fun `removes whitespace and hyphens`() {
        assertEquals("ABCD-EFGH-JKLM-NPQR", InviteCode.of(" abcd efgh - jklm npqr ").value)
    }

    @Test
    fun `rejects too few symbols`() {
        assertThrows<IllegalArgumentException> { InviteCode.of("ABCD-EFGH") }
    }

    @Test
    fun `rejects too many symbols`() {
        assertThrows<IllegalArgumentException> { InviteCode.of("ABCD-EFGH-JKLM-NPQR-STUV") }
    }

    @Test
    fun `rejects ambiguous letters`() {
        listOf('I', 'O').forEach { unsupported ->
            assertThrows<IllegalArgumentException> {
                InviteCode.of("ABCD-EFGH-JKLM-NPQ$unsupported")
            }
        }
    }

    @Test
    fun `rejects unsupported digits`() {
        listOf('0', '1').forEach { unsupported ->
            assertThrows<IllegalArgumentException> {
                InviteCode.of("ABCD-EFGH-JKLM-NPQ$unsupported")
            }
        }
    }
}
