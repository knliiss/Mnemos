package com.mnemos.telegram.bot.application.account.model

import java.util.UUID

data class TelegramAccountContext(
    val activeAccountId: UUID?,
    val accounts: List<TelegramAccountSummary>,
)

data class TelegramAccountSummary(
    val accountId: UUID,
    val cristalixUuid: UUID,
    val cristalixNickname: String,
    val memberCount: Int,
    val maxMembers: Int,
    val owner: Boolean,
    val active: Boolean,
    val status: TelegramAccountStatus,
)

enum class TelegramAccountStatus {
    ACTIVE,
    DEACTIVATED,
}

sealed interface TelegramAccountContextResult {
    data class Available(
        val context: TelegramAccountContext,
    ) : TelegramAccountContextResult

    data object RegistrationRequired : TelegramAccountContextResult

    data object Unavailable : TelegramAccountContextResult
}

data class TelegramAccountBalance(
    val coin: Long,
    val prime: Long,
)

sealed interface TelegramAccountBalanceResult {
    data class Available(
        val balance: TelegramAccountBalance,
    ) : TelegramAccountBalanceResult

    data object NotRequired : TelegramAccountBalanceResult

    data object Unavailable : TelegramAccountBalanceResult
}

sealed interface TelegramAccountSwitchResult {
    data object Switched : TelegramAccountSwitchResult

    data object RegistrationRequired : TelegramAccountSwitchResult

    data object Refresh : TelegramAccountSwitchResult

    data object Unavailable : TelegramAccountSwitchResult
}

data class AccountMenuScreenData(
    val result: TelegramAccountContextResult,
    val balance: TelegramAccountBalanceResult,
    val feedback: AccountMenuFeedback?,
)

data class AccountMenuFeedback(
    val type: AccountMenuFeedbackType,
)

enum class AccountMenuFeedbackType {
    SWITCHED,
    UNAVAILABLE,
}
