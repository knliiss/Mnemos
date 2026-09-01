package com.mnemos.telegram.bot.application.account.invite

import com.mnemos.telegram.bot.application.account.invite.model.AccountInviteMenuFeedback
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteExpiry
import java.util.UUID

object AccountInviteMenuContext {
    private const val EXPIRY_KEY = "account.invite.expiry"
    private const val FEEDBACK_KEY = "account.invite.feedback"
    private const val REVOKE_ID_KEY = "account.invite.revoke-id"

    fun expiry(context: Map<String, String>): TelegramAccountInviteExpiry =
        context[EXPIRY_KEY]
            ?.let { value -> runCatching { TelegramAccountInviteExpiry.valueOf(value) }.getOrNull() }
            ?: TelegramAccountInviteExpiry.ONE_DAY

    fun selectExpiry(
        context: Map<String, String>,
        expiry: TelegramAccountInviteExpiry,
    ): Map<String, String> =
        context
            .withoutTransientState() +
            (EXPIRY_KEY to expiry.name)

    fun feedback(context: Map<String, String>): AccountInviteMenuFeedback? =
        context[FEEDBACK_KEY]
            ?.let { value -> runCatching { AccountInviteMenuFeedback.valueOf(value) }.getOrNull() }

    fun created(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState() +
            (FEEDBACK_KEY to AccountInviteMenuFeedback.CREATED.name)

    fun revoked(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState() +
            (FEEDBACK_KEY to AccountInviteMenuFeedback.REVOKED.name)

    fun unavailable(context: Map<String, String>): Map<String, String> =
        context
            .withoutTransientState() +
            (FEEDBACK_KEY to AccountInviteMenuFeedback.UNAVAILABLE.name)

    fun clearFeedback(context: Map<String, String>): Map<String, String> = context - FEEDBACK_KEY

    fun requestRevoke(
        context: Map<String, String>,
        inviteId: UUID,
    ): Map<String, String> =
        context
            .withoutTransientState() +
            (REVOKE_ID_KEY to inviteId.toString())

    fun cancelRevoke(context: Map<String, String>): Map<String, String> = context - REVOKE_ID_KEY

    fun revokeConfirmationId(context: Map<String, String>): UUID? =
        context[REVOKE_ID_KEY]
            ?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }

    private fun Map<String, String>.withoutTransientState(): Map<String, String> =
        this - FEEDBACK_KEY - REVOKE_ID_KEY
}
