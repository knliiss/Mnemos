package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMember
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementResult
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementRole
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementSnapshot
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class OpenAccountManagementWidgetServiceTest {
    private val gateway = mock<AccountManagementGateway>()
    private val publisher = mock<AccountManagementWidgetPublisher>()
    private val service = OpenAccountManagementWidgetService(TelegramWidgetProperties(enabled = true), gateway, publisher)
    private val accountId = UUID.randomUUID()
    private val actorId = UUID.randomUUID()

    @Test
    fun permissionDeniedDoesNotExposeMembersOrRoles() {
        whenever(gateway.getManagement(11L)).thenReturn(TelegramAccountManagementResult.Forbidden)

        assertEquals(
            TelegramDeliveryResult.PermanentFailure("management-forbidden"),
            service.execute(11L),
        )
        verifyNoInteractions(publisher)
    }

    @Test
    fun pagesAuthorizedMemberAndRoleListsWithoutClientSideIds() {
        val members = List(4) { index ->
            TelegramAccountManagementMember(
                id = UUID.randomUUID(),
                name = "Player$index",
                owner = index == 0,
                highestRoleName = "Role$index",
                highestRoleColor = null,
                roleIds = emptySet(),
                joinedAt = Instant.EPOCH,
            )
        }
        val roles = List(4) { index ->
            TelegramAccountManagementRole(
                id = UUID.randomUUID(),
                name = "Role$index",
                color = "#ffffff",
                hierarchyRank = index.toLong(),
                permissions = emptySet(),
            )
        }
        whenever(gateway.getManagement(11L)).thenReturn(
            TelegramAccountManagementResult.Available(
                TelegramAccountManagementSnapshot(accountId, "Kirito", actorId, emptySet(), members, roles),
            ),
        )

        val result = service.load(11L, 1) as AccountManagementWidgetSnapshotResult.Available

        assertEquals(accountId, result.view.accountId)
        assertEquals(2, result.view.pages)
        assertEquals(4, result.view.totalMembers)
        assertEquals("Player3", result.view.members.single().name)
        assertEquals("Role0", result.view.roles.single().name)
    }

    @Test
    fun disabledWidgetDoesNotCallDomainService() {
        val disabled = OpenAccountManagementWidgetService(
            TelegramWidgetProperties(enabled = false), gateway, publisher,
        )
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("widgets-disabled"),
            disabled.execute(11L),
        )
        verifyNoInteractions(gateway, publisher)
    }
}
