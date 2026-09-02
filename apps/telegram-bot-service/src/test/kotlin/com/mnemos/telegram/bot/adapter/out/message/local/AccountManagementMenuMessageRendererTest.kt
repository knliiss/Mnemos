package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.account.management.AccountManagementCallbackData
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementConfirmation
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuScreenData
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuView
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMember
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementResult
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementRole
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementSnapshot
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class AccountManagementMenuMessageRendererTest {
    @Test
    fun `overview exposes one coherent management surface`() {
        val data = screenData(view = AccountManagementMenuView.OVERVIEW)

        val message = AccountManagementMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("<b>Управление аккаунтом</b>"))
        assertTrue(message.text.contains("<code>knaliz</code>"))
        assertTrue(callbacks.any { action -> action.data == AccountManagementCallbackData.MEMBERS })
        assertTrue(callbacks.any { action -> action.data == AccountManagementCallbackData.ROLES })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.ACCOUNT_INVITES.callbackData })
        assertTrue(message.actionRows.all { row -> row.size == 1 })
    }

    @Test
    fun `member detail gates destructive and role actions by permissions`() {
        val snapshot = snapshot()
        val targetMember = snapshot.members.last()
        val assignedRole = snapshot.roles.first()
        val data =
            screenData(
                snapshot = snapshot,
                view = AccountManagementMenuView.MEMBER,
                selectedMemberId = targetMember.id,
            )

        val message = AccountManagementMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val revoke = callbacks.single { action -> action.data == AccountManagementCallbackData.revokeRole(assignedRole.id) }

        assertTrue(message.text.contains(targetMember.name))
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, revoke.style)
        assertTrue(callbacks.any { action -> action.data == AccountManagementCallbackData.KICK_MEMBER })
        assertTrue(callbacks.any { action -> action.data.startsWith("am:a:") })
    }

    @Test
    fun `role detail exposes permission hierarchy edit and delete controls`() {
        val snapshot = snapshot()
        val role = snapshot.roles.last()
        val data =
            screenData(
                snapshot = snapshot,
                view = AccountManagementMenuView.ROLE,
                selectedRoleId = role.id,
            )

        val message = AccountManagementMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(callbacks.any { action -> action.data == AccountManagementCallbackData.MOVE_ROLE_UP })
        assertTrue(callbacks.any { action -> action.data == AccountManagementCallbackData.EDIT_ROLE })
        assertTrue(callbacks.any { action -> action.data == AccountManagementCallbackData.DELETE_ROLE })
        assertTrue(
            callbacks.any { action ->
                action.data ==
                    AccountManagementCallbackData.permission(
                        TelegramAccountManagementPermission.MANAGE_MEMBERS,
                        enabled = true,
                    )
            },
        )
        assertTrue(callbacks.all { action -> action.data.length <= 64 })
    }

    @Test
    fun `confirmation replaces mutation action with explicit destructive confirmation`() {
        val snapshot = snapshot()
        val targetMember = snapshot.members.last()
        val data =
            screenData(
                snapshot = snapshot,
                view = AccountManagementMenuView.MEMBER,
                selectedMemberId = targetMember.id,
                confirmation = AccountManagementConfirmation.KICK_MEMBER,
            )

        val message = AccountManagementMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Исключить участника?"))
        assertTrue(callbacks.any { action -> action.data == AccountManagementCallbackData.KICK_MEMBER_CONFIRM })
        assertTrue(callbacks.any { action -> action.data == AccountManagementCallbackData.CANCEL_CONFIRMATION })
        assertFalse(callbacks.any { action -> action.data == AccountManagementCallbackData.KICK_MEMBER })
    }

    private fun screenData(
        snapshot: TelegramAccountManagementSnapshot = snapshot(),
        view: AccountManagementMenuView,
        selectedMemberId: UUID? = null,
        selectedRoleId: UUID? = null,
        confirmation: AccountManagementConfirmation? = null,
    ): TelegramMenuScreenData.AccountManagement =
        TelegramMenuScreenData.AccountManagement(
            AccountManagementMenuScreenData(
                result = TelegramAccountManagementResult.Available(snapshot),
                view = view,
                selectedMemberId = selectedMemberId,
                selectedRoleId = selectedRoleId,
                feedback = null,
                confirmation = confirmation,
                inputMode = null,
            ),
        )

    private fun snapshot(): TelegramAccountManagementSnapshot {
        val actorId = UUID.randomUUID()
        val targetId = UUID.randomUUID()
        val moderatorRoleId = UUID.randomUUID()
        val helperRoleId = UUID.randomUUID()

        return TelegramAccountManagementSnapshot(
            accountId = UUID.randomUUID(),
            cristalixNickname = "knaliz",
            actorMemberId = actorId,
            actorPermissions =
                setOf(
                    TelegramAccountManagementPermission.MANAGE_MEMBERS,
                    TelegramAccountManagementPermission.ASSIGN_MEMBER_ROLES,
                    TelegramAccountManagementPermission.MANAGE_ROLES,
                    TelegramAccountManagementPermission.MANAGE_ROLE_HIERARCHY,
                ),
            members =
                listOf(
                    TelegramAccountManagementMember(
                        id = actorId,
                        name = "knaliz",
                        owner = true,
                        highestRoleName = "Owner",
                        highestRoleColor = "#FFFFFF",
                        roleIds = emptySet(),
                        joinedAt = Instant.EPOCH,
                    ),
                    TelegramAccountManagementMember(
                        id = targetId,
                        name = "member",
                        owner = false,
                        highestRoleName = "Moderator",
                        highestRoleColor = "#7C5CFC",
                        roleIds = setOf(moderatorRoleId),
                        joinedAt = Instant.EPOCH,
                    ),
                ),
            roles =
                listOf(
                    TelegramAccountManagementRole(
                        id = moderatorRoleId,
                        name = "Moderator",
                        color = "#7C5CFC",
                        hierarchyRank = 0,
                        permissions = setOf(TelegramAccountManagementPermission.MANAGE_MEMBERS),
                    ),
                    TelegramAccountManagementRole(
                        id = helperRoleId,
                        name = "Helper",
                        color = "#3AA675",
                        hierarchyRank = 1,
                        permissions = emptySet(),
                    ),
                ),
        )
    }
}
