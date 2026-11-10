package com.mnemos.telegram.bot.application.widget.invites

import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInvite
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteListResult
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteStatus
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInvites
import com.mnemos.telegram.bot.application.account.invite.port.out.AccountInviteGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.invites.port.out.InviteWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class OpenInviteWidgetServiceTest {
    private val gateway = mock<AccountInviteGateway>()
    private val publisher = mock<InviteWidgetPublisher>()
    private val service = OpenInviteWidgetService(TelegramWidgetProperties(enabled = true), gateway, publisher)
    private val accountId = UUID.randomUUID()

    @Test
    fun exposesOnlyAuthorizedInvitesAndClampsPagination() {
        val start = Instant.parse("2026-11-10T12:00:00Z")
        val invites = List(5) { index ->
            TelegramAccountInvite(
                UUID.randomUUID(),
                "CODE$index",
                start.plusSeconds(index.toLong()),
                null,
                null,
                null,
                TelegramAccountInviteStatus.ACTIVE,
            )
        }
        whenever(gateway.getInvites(11L)).thenReturn(
            TelegramAccountInviteListResult.Available(TelegramAccountInvites(accountId, "Kirito", invites)),
        )

        val result = service.load(11L, 99) as InviteWidgetSnapshotResult.Available

        assertEquals(accountId, result.view.accountId)
        assertEquals(2, result.view.totalPages)
        assertEquals(1, result.view.page)
        assertEquals(listOf("CODE1", "CODE0"), result.view.invites.map { it.code })
    }

    @Test
    fun doesNotPublishForbiddenInvites() {
        whenever(gateway.getInvites(11L)).thenReturn(TelegramAccountInviteListResult.Forbidden)
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("invites-forbidden"),
            service.execute(11L),
        )
        verifyNoInteractions(publisher)
    }

    @Test
    fun delegatesWidgetPublishingOnPermittedAccount() {
        whenever(gateway.getInvites(11L)).thenReturn(
            TelegramAccountInviteListResult.Available(TelegramAccountInvites(accountId, "Kirito", emptyList())),
        )
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.execute(11L))
        verify(publisher).publish(eq(11L), any())
    }

    @Test
    fun disabledFeatureNeverQueriesAccountService() {
        val disabled = OpenInviteWidgetService(TelegramWidgetProperties(enabled = false), gateway, publisher)

        assertEquals(TelegramDeliveryResult.PermanentFailure("widgets-disabled"), disabled.execute(11L))
        verifyNoInteractions(gateway, publisher)
    }
}
