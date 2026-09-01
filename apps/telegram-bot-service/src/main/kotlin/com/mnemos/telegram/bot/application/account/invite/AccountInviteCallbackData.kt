package com.mnemos.telegram.bot.application.account.invite

import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteExpiry
import java.util.UUID

object AccountInviteCallbackData {
    const val PREFIX = "ai:"
    const val REFRESH = "ai:r"
    const val CREATE = "ai:c"
    const val REVOKE_CANCEL = "ai:rx"

    private const val EXPIRY_PREFIX = "ai:e:"
    private const val REVOKE_REQUEST_PREFIX = "ai:rq:"
    private const val REVOKE_CONFIRM_PREFIX = "ai:rc:"

    fun expiry(expiry: TelegramAccountInviteExpiry): String = "$EXPIRY_PREFIX${expiry.name}"

    fun selectedExpiry(callbackData: String): TelegramAccountInviteExpiry? {
        if (!callbackData.startsWith(EXPIRY_PREFIX)) {
            return null
        }

        return runCatching {
            TelegramAccountInviteExpiry.valueOf(callbackData.removePrefix(EXPIRY_PREFIX))
        }.getOrNull()
    }

    fun requestRevoke(inviteId: UUID): String = "$REVOKE_REQUEST_PREFIX$inviteId"

    fun requestedRevokeId(callbackData: String): UUID? = parseUuid(callbackData, REVOKE_REQUEST_PREFIX)

    fun confirmRevoke(inviteId: UUID): String = "$REVOKE_CONFIRM_PREFIX$inviteId"

    fun confirmedRevokeId(callbackData: String): UUID? = parseUuid(callbackData, REVOKE_CONFIRM_PREFIX)

    private fun parseUuid(
        callbackData: String,
        prefix: String,
    ): UUID? {
        if (!callbackData.startsWith(prefix)) {
            return null
        }

        return runCatching {
            UUID.fromString(callbackData.removePrefix(prefix))
        }.getOrNull()
    }
}
