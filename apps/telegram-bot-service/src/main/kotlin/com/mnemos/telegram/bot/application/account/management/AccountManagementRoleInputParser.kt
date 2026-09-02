package com.mnemos.telegram.bot.application.account.management

object AccountManagementRoleInputParser {
    private val inputPattern = Regex("^(.+?)\\s+(#[0-9A-Fa-f]{6})$")

    fun parse(raw: String): AccountManagementRoleInput? {
        val match = inputPattern.matchEntire(raw.trim()) ?: return null
        val name = match.groupValues[1].trim()
        val color = match.groupValues[2].uppercase()

        if (name.isBlank()) {
            return null
        }

        return AccountManagementRoleInput(
            name = name,
            color = color,
        )
    }
}

data class AccountManagementRoleInput(
    val name: String,
    val color: String,
)
