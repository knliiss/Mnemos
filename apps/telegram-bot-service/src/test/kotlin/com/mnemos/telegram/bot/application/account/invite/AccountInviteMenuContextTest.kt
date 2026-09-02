package com.mnemos.telegram.bot.application.account.invite

import com.mnemos.telegram.bot.application.account.invite.model.AccountInviteMenuFeedback
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteExpiry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class AccountInviteMenuContextTest {
    @Test
    fun `invite expiry defaults to one day and survives transient state changes`() {
        val inviteId = UUID.randomUUID()
        var context = AccountInviteMenuContext.requestRevoke(emptyMap(), inviteId)

        context = AccountInviteMenuContext.selectExpiry(context, TelegramAccountInviteExpiry.SEVEN_DAYS)
        context = AccountInviteMenuContext.created(context)

        assertEquals(TelegramAccountInviteExpiry.SEVEN_DAYS, AccountInviteMenuContext.expiry(context))
        assertEquals(AccountInviteMenuFeedback.CREATED, AccountInviteMenuContext.feedback(context))
        assertNull(AccountInviteMenuContext.revokeConfirmationId(context))
    }

    @Test
    fun `revoke callback ids are replay safe and stay below telegram limit`() {
        val inviteId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val request = AccountInviteCallbackData.requestRevoke(inviteId)
        val confirm = AccountInviteCallbackData.confirmRevoke(inviteId)

        assertEquals(inviteId, AccountInviteCallbackData.requestedRevokeId(request))
        assertEquals(inviteId, AccountInviteCallbackData.confirmedRevokeId(confirm))
        assertNull(AccountInviteCallbackData.confirmedRevokeId(request))
        assertTrue(request.length <= 64)
        assertTrue(confirm.length <= 64)
    }
}
