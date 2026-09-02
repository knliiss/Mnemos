package com.mnemos.telegram.bot.application.account.management.model

import java.time.Instant
import java.util.UUID

data class TelegramAccountManagementSnapshot(
    val accountId: UUID,
    val cristalixNickname: String,
    val actorMemberId: UUID,
    val actorPermissions: Set<TelegramAccountManagementPermission>,
    val members: List<TelegramAccountManagementMember>,
    val roles: List<TelegramAccountManagementRole>,
)

data class TelegramAccountManagementMember(
    val id: UUID,
    val name: String,
    val owner: Boolean,
    val highestRoleName: String?,
    val highestRoleColor: String?,
    val roleIds: Set<UUID>,
    val joinedAt: Instant,
)

data class TelegramAccountManagementRole(
    val id: UUID,
    val name: String,
    val color: String,
    val hierarchyRank: Long,
    val permissions: Set<TelegramAccountManagementPermission>,
)

enum class TelegramAccountManagementPermission {
    MANAGE_MEMBERS,
    ASSIGN_MEMBER_ROLES,
    MANAGE_ROLES,
    MANAGE_ROLE_HIERARCHY,
    MANAGE_BASE_ROLE,
    VIEW_TRANSACTION_HISTORY,
    PLACE_BETS,
    BUY_ACCOUNT_PERSONALIZATIONS,
    APPLY_ACCOUNT_PERSONALIZATIONS,
    MODERATE_COMMENTS,
}

sealed interface TelegramAccountManagementResult {
    data class Available(
        val snapshot: TelegramAccountManagementSnapshot,
    ) : TelegramAccountManagementResult

    data object ActiveAccountRequired : TelegramAccountManagementResult

    data object Forbidden : TelegramAccountManagementResult

    data object Unavailable : TelegramAccountManagementResult
}

enum class TelegramAccountManagementMutationResult {
    UPDATED,
    INVALID,
    ACTIVE_ACCOUNT_REQUIRED,
    FORBIDDEN,
    REFRESH,
    UNAVAILABLE,
}

enum class AccountManagementMenuView {
    OVERVIEW,
    MEMBERS,
    MEMBER,
    ROLES,
    ROLE,
}

enum class AccountManagementMenuFeedback {
    UPDATED,
    INVALID,
    UNAVAILABLE,
}

enum class AccountManagementConfirmation {
    KICK_MEMBER,
    DELETE_ROLE,
}

enum class AccountManagementInputMode {
    CREATE_ROLE,
    EDIT_ROLE,
}

data class AccountManagementMenuScreenData(
    val result: TelegramAccountManagementResult,
    val view: AccountManagementMenuView,
    val selectedMemberId: UUID?,
    val selectedRoleId: UUID?,
    val feedback: AccountManagementMenuFeedback?,
    val confirmation: AccountManagementConfirmation?,
    val inputMode: AccountManagementInputMode?,
)
