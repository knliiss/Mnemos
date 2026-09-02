package com.mnemos.telegram.bot.application.account.management

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AccountManagementRoleInputParserTest {
    @Test
    fun `create role accepts a plain role name`() {
        val result = AccountManagementRoleInputParser.parseName("  Модератор  ")

        assertEquals("Модератор", result)
    }

    @Test
    fun `create role rejects a blank role name`() {
        val result = AccountManagementRoleInputParser.parseName("   ")

        assertNull(result)
    }

    @Test
    fun `role details still accept name and color`() {
        val result = AccountManagementRoleInputParser.parseDetails("Модератор #7c5cfc")

        assertEquals(
            AccountManagementRoleInput(
                name = "Модератор",
                color = "#7C5CFC",
            ),
            result,
        )
    }
}
