package com.mnemos.accountservice.domain.model

import com.mnemos.accountservice.domain.model.account.CristalixNickname
import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.HexColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ValueObjectsTest {
    @Test
    fun `cristalix nickname accepts boundary lengths`() {
        assertEquals("abcd", CristalixNickname("abcd").value)
        assertEquals(20, CristalixNickname("a".repeat(20)).value.length)
    }

    @Test
    fun `cristalix nickname rejects blank and invalid lengths`() {
        listOf("", "   ", "abc", "a".repeat(21)).forEach {
            assertThrows<IllegalArgumentException> { CristalixNickname(it) }
        }
    }

    @Test
    fun `role name accepts boundary lengths`() {
        assertEquals("A", AccountRoleName("A").value)
        assertEquals(32, AccountRoleName("A".repeat(32)).value.length)
    }

    @Test
    fun `role name rejects empty and oversized values`() {
        assertThrows<IllegalArgumentException> { AccountRoleName("") }
        assertThrows<IllegalArgumentException> { AccountRoleName("A".repeat(33)) }
    }

    @Test
    fun `hex color accepts RGB and RGBA forms`() {
        assertEquals("#aBcDeF", HexColor("#aBcDeF").value)
        assertEquals("#AABBCCDD", HexColor("#AABBCCDD").value)
    }

    @Test
    fun `hex color rejects malformed values`() {
        listOf("AABBCC", "#ABC", "#GGHHII", "#AABBCCD", "#AABBCCDDEE").forEach {
            assertThrows<IllegalArgumentException> { HexColor(it) }
        }
    }
}
