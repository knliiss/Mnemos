package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.account.invite.AccountInviteCallbackData
import com.mnemos.telegram.bot.application.account.invite.model.AccountInviteMenuScreenData
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInvite
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteExpiry
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteListResult
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteStatus
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInvites
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class AccountInviteMenuMessageRendererTest {
    @Test
    fun `invite dashboard keeps expiry creation and active revoke controls separate`() {
        val activeInvite = invite("MNEMOS-ACTIVE", TelegramAccountInviteStatus.ACTIVE)
        val consumedInvite = invite("MNEMOS-USED", TelegramAccountInviteStatus.CONSUMED)
        val data =
            TelegramMenuScreenData.AccountInvites(
                AccountInviteMenuScreenData(
                    result =
                        TelegramAccountInviteListResult.Available(
                            TelegramAccountInvites(
                                accountId = UUID.randomUUID(),
                                cristalixNickname = "knaliz",
                                invites = listOf(activeInvite, consumedInvite),
                            ),
                        ),
                    expiry = TelegramAccountInviteExpiry.ONE_DAY,
                    feedback = null,
                    revokeConfirmationId = null,
                ),
            )

        val message = AccountInviteMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val expiryRow = message.actionRows.first()
        val oneDay = callbacks.single { action -> action.data == AccountInviteCallbackData.expiry(TelegramAccountInviteExpiry.ONE_DAY) }
        val revoke = callbacks.single { action -> action.data == AccountInviteCallbackData.requestRevoke(activeInvite.id) }

        assertTrue(message.text.contains("<code>knaliz</code>"))
        assertTrue(message.text.contains("<code>MNEMOS-ACTIVE</code>"))
        assertFalse(message.text.contains("MNEMOS-USED"))
        assertEquals(4, expiryRow.size)
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, oneDay.style)
        assertEquals(OutboundTelegramButtonStyle.DANGER, revoke.style)
        assertTrue(callbacks.any { action -> action.data == AccountInviteCallbackData.CREATE })
        assertTrue(callbacks.any { action -> action.data == AccountInviteCallbackData.REFRESH })
    }

    @Test
    fun `invite revoke requires explicit confirmation for the selected code`() {
        val activeInvite = invite("MNEMOS-ACTIVE", TelegramAccountInviteStatus.ACTIVE)
        val data =
            TelegramMenuScreenData.AccountInvites(
                AccountInviteMenuScreenData(
                    result =
                        TelegramAccountInviteListResult.Available(
                            TelegramAccountInvites(
                                accountId = UUID.randomUUID(),
                                cristalixNickname = "knaliz",
                                invites = listOf(activeInvite),
                            ),
                        ),
                    expiry = TelegramAccountInviteExpiry.ONE_DAY,
                    feedback = null,
                    revokeConfirmationId = activeInvite.id,
                ),
            )

        val message = AccountInviteMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val confirm = callbacks.single { action -> action.data == AccountInviteCallbackData.confirmRevoke(activeInvite.id) }

        assertTrue(message.text.contains("Отозвать приглашение?"))
        assertTrue(message.text.contains("<code>MNEMOS-ACTIVE</code>"))
        assertEquals(OutboundTelegramButtonStyle.DANGER, confirm.style)
        assertTrue(callbacks.any { action -> action.data == AccountInviteCallbackData.REVOKE_CANCEL })
        assertFalse(callbacks.any { action -> action.data == AccountInviteCallbackData.CREATE })
    }

    private fun invite(
        code: String,
        status: TelegramAccountInviteStatus,
    ): TelegramAccountInvite =
        TelegramAccountInvite(
            id = UUID.randomUUID(),
            code = code,
            createdAt = Instant.parse("2026-09-01T12:00:00Z"),
            expiresAt = Instant.parse("2026-09-02T12:00:00Z"),
            consumedAt = null,
            revokedAt = null,
            status = status,
        )
}
