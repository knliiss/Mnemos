package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetMutationGuard
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetPublisher
import com.mnemos.telegram.bot.application.widget.input.StartWidgetTextInputUseCase
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class AccountManagementWidgetActionHandlerTest {
    private val snapshots = mock<AccountManagementWidgetSnapshotQuery>()
    private val publisher = mock<AccountManagementWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val guard = mock<AccountManagementWidgetMutationGuard>()
    private val mutations = mock<ChangeAccountManagementWidgetUseCase>()
    private val textInput = mock<StartWidgetTextInputUseCase>()
    private val handler = AccountManagementWidgetActionHandler(snapshots, publisher, sessions, responder, guard, mutations, textInput)
    private val accountId = UUID.randomUUID()
    private val memberId = UUID.randomUUID()
    private val roleId = UUID.randomUUID()
    private val session = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ACCOUNT_MANAGEMENT, accountId)

    @Test
    fun createRoleRequestOpensChatPromptOnlyWithManageRolesPermission() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            AccountManagementWidgetSnapshotResult.Available(view().copy(canManageRoles = true)),
        )
        whenever(responder.reply(eq("q"), eq("INPUT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:create"), session))
        verify(textInput).start(session, WidgetTextInputPurpose.ACCOUNT_ROLE_CREATE)
        verifyNoInteractions(mutations)
    }

    @Test
    fun forbiddenRoleCreationDoesNotPromptUser() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountManagementWidgetSnapshotResult.Available(view()))
        whenever(responder.reply(eq("q"), eq("FORBIDDEN"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:create"), session))
        verifyNoInteractions(textInput, mutations)
    }

    @Test
    fun changingActiveAccountInvalidatesAllManagementActions() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountManagementWidgetSnapshotResult.Available(view(UUID.randomUUID())))
        whenever(responder.reply(eq("q"), eq("CONTEXT_CHANGED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:kick:0:0"), session))
        verifyNoInteractions(guard, mutations)
    }

    @Test
    fun deletingRoleRequiresMatchingAccountRoleAndConfirmation() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            AccountManagementWidgetSnapshotResult.Available(view().copy(canManageRoles = true)),
        )
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("CONFIRM"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)
        whenever(responder.reply(eq("q"), eq("CONFIRM_REQUIRED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:manage:role-delete:0:0"), session),
        )
        verify(guard).requestRoleDeletion(11L, 8, accountId, roleId)
        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:manage:role-delete-confirm:0:0"), session),
        )
        verifyNoInteractions(mutations)
    }

    @Test
    fun confirmedRoleDeletionCallsTypedUseCaseAndRefreshes() {
        whenever(snapshots.load(11L, 0)).thenReturn(
            AccountManagementWidgetSnapshotResult.Available(view().copy(canManageRoles = true)),
        )
        whenever(guard.consumeRoleDeletion(11L, 8, accountId, roleId)).thenReturn(true)
        whenever(mutations.deleteRole(11L, "q", view().copy(canManageRoles = true), 0))
            .thenReturn(AccountManagementWidgetMutationResult.APPLIED)
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:manage:role-delete-confirm:0:0"), session),
        )
        verify(mutations).deleteRole(11L, "q", view().copy(canManageRoles = true), 0)
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun requestingKickStoresAccountAndMemberBoundConfirmation() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountManagementWidgetSnapshotResult.Available(view()))
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("CONFIRM"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:kick:0:0"), session))
        verify(guard).requestKick(11L, 8, accountId, memberId)
        verifyNoInteractions(mutations)
    }

    @Test
    fun cannotKickWithoutMatchingUnexpiredConfirmation() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountManagementWidgetSnapshotResult.Available(view()))
        whenever(responder.reply(eq("q"), eq("CONFIRM_REQUIRED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:kick-confirm:0:0"), session))
        verifyNoInteractions(mutations)
    }

    @Test
    fun confirmedKickUsesMutationUseCaseAndRefreshesWidget() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountManagementWidgetSnapshotResult.Available(view()))
        whenever(guard.consumeKick(11L, 8, accountId, memberId)).thenReturn(true)
        whenever(mutations.kickMember(11L, "q", view(), 0)).thenReturn(AccountManagementWidgetMutationResult.APPLIED)
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:kick-confirm:0:0"), session))
        verify(mutations).kickMember(11L, "q", view(), 0)
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun rejectsRoleChangesOnOldPage() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountManagementWidgetSnapshotResult.Available(view()))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:role:1:0:0:on"), session))
        verifyNoInteractions(mutations)
    }

    @Test
    fun roleActionDelegatesServerVisibleIndicesToMutationUseCase() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountManagementWidgetSnapshotResult.Available(view()))
        whenever(mutations.assignRole(11L, "q", view(), 0, 0, true)).thenReturn(AccountManagementWidgetMutationResult.APPLIED)
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:manage:role:0:0:0:on"), session))
        verify(mutations).assignRole(11L, "q", view(), 0, 0, true)
    }

    private fun query(action: String) =
        TelegramWidgetQueryContext("q", 11L, 11L, 8, action, "data", true, false)

    private fun view(id: UUID = accountId) =
        AccountManagementWidgetView(
            accountId = id,
            nickname = "Kirito",
            page = 0,
            pages = 1,
            totalMembers = 1,
            totalRoles = 1,
            members = listOf(AccountManagementWidgetMember(memberId, "Asuna", "Member", false, false, emptySet())),
            roles = listOf(AccountManagementWidgetRole(roleId, "Officer", 1, 3)),
            canRemoveMembers = true,
            canAssignRoles = true,
        )
}
