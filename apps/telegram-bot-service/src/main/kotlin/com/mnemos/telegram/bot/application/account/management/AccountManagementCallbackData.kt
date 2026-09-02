package com.mnemos.telegram.bot.application.account.management

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import java.util.UUID

object AccountManagementCallbackData {
    const val PREFIX = "am:"
    const val REFRESH = "am:r"
    const val OVERVIEW = "am:o"
    const val MEMBERS = "am:ms"
    const val ROLES = "am:rs"
    const val CREATE_ROLE = "am:rc"
    const val EDIT_ROLE = "am:re"
    const val CANCEL_INPUT = "am:ic"
    const val KICK_MEMBER = "am:k"
    const val KICK_MEMBER_CONFIRM = "am:k:y"
    const val DELETE_ROLE = "am:d"
    const val DELETE_ROLE_CONFIRM = "am:d:y"
    const val CANCEL_CONFIRMATION = "am:x"
    const val MOVE_ROLE_UP = "am:u"
    const val MOVE_ROLE_DOWN = "am:n"

    private const val MEMBER_PREFIX = "am:m:"
    private const val ROLE_PREFIX = "am:z:"
    private const val ASSIGN_ROLE_PREFIX = "am:a:"
    private const val REVOKE_ROLE_PREFIX = "am:v:"
    private const val ENABLE_PERMISSION_PREFIX = "am:p1:"
    private const val DISABLE_PERMISSION_PREFIX = "am:p0:"

    fun member(memberId: UUID): String = MEMBER_PREFIX + memberId

    fun role(roleId: UUID): String = ROLE_PREFIX + roleId

    fun assignRole(roleId: UUID): String = ASSIGN_ROLE_PREFIX + roleId

    fun revokeRole(roleId: UUID): String = REVOKE_ROLE_PREFIX + roleId

    fun permission(
        permission: TelegramAccountManagementPermission,
        enabled: Boolean,
    ): String = (if (enabled) ENABLE_PERMISSION_PREFIX else DISABLE_PERMISSION_PREFIX) + permission.name

    fun selectedMemberId(data: String): UUID? = uuidAfterPrefix(data, MEMBER_PREFIX)

    fun selectedRoleId(data: String): UUID? = uuidAfterPrefix(data, ROLE_PREFIX)

    fun assignedRoleId(data: String): UUID? = uuidAfterPrefix(data, ASSIGN_ROLE_PREFIX)

    fun revokedRoleId(data: String): UUID? = uuidAfterPrefix(data, REVOKE_ROLE_PREFIX)

    fun permissionMutation(data: String): PermissionMutation? {
        val enabled = data.startsWith(ENABLE_PERMISSION_PREFIX)
        val disabled = data.startsWith(DISABLE_PERMISSION_PREFIX)

        if (!enabled && !disabled) {
            return null
        }

        val prefix = if (enabled) ENABLE_PERMISSION_PREFIX else DISABLE_PERMISSION_PREFIX
        val permission =
            data
                .removePrefix(prefix)
                .let { value ->
                    TelegramAccountManagementPermission.entries.firstOrNull { candidate -> candidate.name == value }
                } ?: return null

        return PermissionMutation(
            permission = permission,
            enabled = enabled,
        )
    }

    private fun uuidAfterPrefix(
        data: String,
        prefix: String,
    ): UUID? {
        if (!data.startsWith(prefix)) {
            return null
        }

        return runCatching { UUID.fromString(data.removePrefix(prefix)) }.getOrNull()
    }

    data class PermissionMutation(
        val permission: TelegramAccountManagementPermission,
        val enabled: Boolean,
    )
}
