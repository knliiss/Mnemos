package com.mnemos.telegram.bot.application.account.invite.model

import java.time.Instant
import java.util.UUID

data class TelegramAccountInvites(
    val accountId: UUID,
    val cristalixNickname: String,
    val invites: List<TelegramAccountInvite>,
)

data class TelegramAccountInvite(
    val id: UUID,
    val code: String,
    val createdAt: Instant,
    val expiresAt: Instant?,
    val consumedAt: Instant?,
    val revokedAt: Instant?,
    val status: TelegramAccountInviteStatus,
)

enum class TelegramAccountInviteStatus {
    ACTIVE,
    CONSUMED,
    REVOKED,
    EXPIRED,
}

enum class TelegramAccountInviteExpiry(
    val expiresInMinutes: Long?,
) {
    NONE(null),
    ONE_HOUR(60),
    ONE_DAY(1_440),
    SEVEN_DAYS(10_080),
}

sealed interface TelegramAccountInviteListResult {
    data class Available(
        val snapshot: TelegramAccountInvites,
    ) : TelegramAccountInviteListResult

    data object ActiveAccountRequired : TelegramAccountInviteListResult

    data object Forbidden : TelegramAccountInviteListResult

    data object Unavailable : TelegramAccountInviteListResult
}

enum class TelegramAccountInviteMutationResult {
    UPDATED,
    ACTIVE_ACCOUNT_REQUIRED,
    FORBIDDEN,
    REFRESH,
    UNAVAILABLE,
}

data class AccountInviteMenuScreenData(
    val result: TelegramAccountInviteListResult,
    val expiry: TelegramAccountInviteExpiry,
    val feedback: AccountInviteMenuFeedback?,
    val revokeConfirmationId: UUID?,
)

enum class AccountInviteMenuFeedback {
    CREATED,
    REVOKED,
    REFRESHED,
    UNAVAILABLE,
}
