package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMember
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementResult
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementRole
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementSnapshot
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetPublisher
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

class OpenAccountManagementWidgetServiceTest {
    private val gateway = mock<AccountManagementGateway>()
    private val publisher = mock<AccountManagementWidgetPublisher>()
    private val service = OpenAccountManagementWidgetService(TelegramWidgetProperties(enabled = true), gateway, publisher)
    private val accountId = UUID.randomUUID()
    private val actorId = UUID.randomUUID()

    @Test
    fun paginatesAuthorizedMembersAndRolesAndPreservesServerIdentifiers() {
        val roles = List(5) { i ->
            TelegramAccountManagementRole(UUID.randomUUID(), "Role $i", "#ffffff", i.toLong(), emptySet())
        }
        val members = List(5) { i ->
            TelegramAccountManagementMember(
                id = if (i == 0) actorId else UUID.randomUUID(),
                name = "Member $i",
                owner = false,
                highestRoleName = null,
                highestRoleColor = null,
                roleIds = setOf(roles[0].id),
                joinedAt = Instant.EPOCH,
            )
        }
        whenever(gateway.getManagement(11L)).thenReturn(
            TelegramAccountManagementResult.Available(
                TelegramAccountManagementSnapshot(
                    accountId, "Kirito", actorId,
                    setOf(
                        TelegramAccountManagementPermission.MANAGE_MEMBERS,
                        TelegramAccountManagementPermission.ASSIGN_MEMBER_ROLES,
                        TelegramAccountManagementPermission.MANAGE_ROLES,
                        TelegramAccountManagementPermission.MANAGE_ROLE_HIERARCHY,
                    ),
                    members, roles,
                ),
            ),
        )

        val result = service.load(11L, 1) as AccountManagementWidgetSnapshotResult.Available
        assertEquals(2, result.view.pages)
        assertEquals(1, result.view.page)
        assertEquals(members.drop(3).map { it.id }, result.view.members.map { it.id })
        assertEquals(roles.sortedBy { it.hierarchyRank }.drop(3).map { it.id }, result.view.roles.map { it.id })
        assertEquals(true, result.view.canRemoveMembers)
        assertEquals(true, result.view.canAssignRoles)
        assertEquals(true, result.view.canManageHierarchy)
        assertEquals(true, result.view.canManageRoles)
    }

    @Test
    fun rejectsForbiddenContextWithoutPublishingAnyData() {
        whenever(gateway.getManagement(11L)).thenReturn(TelegramAccountManagementResult.Forbidden)
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("management-forbidden"),
            service.execute(11L),
        )
        verifyNoInteractions(publisher)
    }

    @Test
    fun publishesAuthorizedCurrentAccount() {
        whenever(gateway.getManagement(11L)).thenReturn(
            TelegramAccountManagementResult.Available(
                TelegramAccountManagementSnapshot(accountId, "Kirito", actorId, emptySet(), emptyList(), emptyList()),
            ),
        )
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)
        assertEquals(TelegramDeliveryResult.Delivered, service.execute(11L))
        verify(publisher).publish(eq(11L), any())
    }

    @Test
    fun disabledWidgetDoesNotQueryAccountService() {
        val disabled = OpenAccountManagementWidgetService(TelegramWidgetProperties(enabled = false), gateway, publisher)
        assertEquals(TelegramDeliveryResult.PermanentFailure("widgets-disabled"), disabled.execute(11L))
        verifyNoInteractions(gateway, publisher)
    }
}
