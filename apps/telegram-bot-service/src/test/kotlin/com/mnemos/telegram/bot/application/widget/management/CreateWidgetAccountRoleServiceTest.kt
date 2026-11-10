package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMutationResult
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetPublisher
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class CreateWidgetAccountRoleServiceTest {
    private val gateway = mock<AccountManagementGateway>()
    private val snapshots = mock<AccountManagementWidgetSnapshotQuery>()
    private val publisher = mock<AccountManagementWidgetPublisher>()
    private val pending = mock<WidgetTextInputSessionRepository>()
    private val service = CreateWidgetAccountRoleService(gateway, snapshots, publisher, pending)
    private val accountId = UUID.randomUUID()
    private val widget = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ACCOUNT_MANAGEMENT, accountId)
    private val prompt = WidgetTextInputSession(11L, 41, 8, accountId, WidgetTextInputPurpose.ACCOUNT_ROLE_CREATE)

    @Test
    fun validRoleCreationUsesExistingGatewayAndRefreshesOriginatingWidget() {
        whenever(snapshots.load(11L, 0)).thenReturn(available())
        whenever(pending.claimMutation(11L, 41)).thenReturn(true)
        whenever(gateway.createRole(11L, "Officer")).thenReturn(TelegramAccountManagementMutationResult.UPDATED)
        whenever(publisher.update(eq(11L), eq(8), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.create(prompt, widget, " Officer "))
        verify(gateway).createRole(11L, "Officer")
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun invalidNameCannotClaimOrModifyTheDomain() {
        whenever(snapshots.load(11L, 0)).thenReturn(available())
        whenever(publisher.update(eq(11L), eq(8), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.create(prompt, widget, " "))
        verifyNoInteractions(gateway, pending)
    }

    @Test
    fun replayedReplyCannotCreateSecondRole() {
        whenever(snapshots.load(11L, 0)).thenReturn(available())
        whenever(pending.claimMutation(11L, 41)).thenReturn(false)
        whenever(publisher.update(eq(11L), eq(8), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.create(prompt, widget, "Officer"))
        verifyNoInteractions(gateway)
    }

    @Test
    fun forbidsCreationWhenPermissionWasRevoked() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            AccountManagementWidgetSnapshotResult.Available(view(canManage = false)),
        )
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("role-permission-denied"),
            service.create(prompt, widget, "Officer"),
        )
        verifyNoInteractions(gateway, pending)
    }

    @Test
    fun activeAccountSwitchBeforeReplyRejectsCreation() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            AccountManagementWidgetSnapshotResult.Available(view(id = UUID.randomUUID())),
        )
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("role-account-context-changed"),
            service.create(prompt, widget, "Officer"),
        )
        verifyNoInteractions(gateway, pending)
    }

    @Test
    fun mismatchedPromptCannotCreateAnyRole() {
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("invalid-role-input-context"),
            service.create(prompt.copy(widgetMessageId = 9), widget, "Officer"),
        )
        verifyNoInteractions(snapshots, gateway, pending)
    }

    private fun available() = AccountManagementWidgetSnapshotResult.Available(view())

    private fun view(id: UUID = accountId, canManage: Boolean = true) =
        AccountManagementWidgetView(
            accountId = id,
            nickname = "Kirito",
            page = 0,
            pages = 1,
            totalMembers = 0,
            totalRoles = 0,
            members = emptyList(),
            roles = emptyList(),
            canRemoveMembers = false,
            canAssignRoles = false,
            canManageRoles = canManage,
        )
}
