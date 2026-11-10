package com.mnemos.telegram.bot.presentation.widget

import com.mnemos.telegram.bot.application.widget.management.AccountManagementWidgetMember
import com.mnemos.telegram.bot.application.widget.management.AccountManagementWidgetRole
import com.mnemos.telegram.bot.application.widget.management.AccountManagementWidgetView
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementPermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class AccountManagementWidgetRendererTest {
    private val json = ObjectMapper()
    private val renderer = AccountManagementWidgetRenderer(json)

    @Test
    fun rolePermissionStateIsEncodedAsEnumOrdinalBitMask() {
        val role = AccountManagementWidgetRole(
            id = UUID.randomUUID(),
            name = "Officer",
            rank = 10,
            permissionCount = 2,
            color = "#AABBCC",
            permissions = setOf(
                TelegramAccountManagementPermission.MANAGE_ROLES,
                TelegramAccountManagementPermission.MODERATE_COMMENTS,
            ),
        )
        val view = AccountManagementWidgetView(
            accountId = UUID.randomUUID(),
            nickname = "Kirito",
            page = 0, pages = 1,
            totalMembers = 0, totalRoles = 1,
            members = emptyList(),
            roles = listOf(role),
            canRemoveMembers = false,
            canAssignRoles = false,
            canManageRoles = true,
            canManageHierarchy = true,
        )
        val state = json.readTree(renderer.render(view).chatData)

        assertEquals((1 shl 2) or (1 shl 9), state.path("roles")[0].path("m").asInt())
        assertEquals("#AABBCC", state.path("roles")[0].path("c").asText())
        assertTrue(state.path("canHierarchy").asBoolean())
    }

    @Test
    fun maximumManagementPageFitsTelegramLimitAndExposesOnlyVisibleRoleIndices() {
        val roles = List(3) {
            AccountManagementWidgetRole(UUID.randomUUID(), "В".repeat(17), 100 - it.toLong(), 10)
        }
        val members = List(3) {
            AccountManagementWidgetMember(
                id = UUID.randomUUID(),
                name = "а".repeat(17),
                role = "М".repeat(13),
                owner = false,
                self = false,
                roleIds = roles.map { role -> role.id }.toSet(),
            )
        }
        val view = AccountManagementWidgetView(
            accountId = UUID.randomUUID(),
            nickname = "В".repeat(18),
            page = 3,
            pages = 20,
            totalMembers = 99,
            totalRoles = 99,
            members = members,
            roles = roles,
            canRemoveMembers = true,
            canAssignRoles = true,
            canManageRoles = true,
            feedback = "Роль створено",
        )

        val rendered = renderer.render(view)
        val data = json.readTree(rendered.chatData)

        assertTrue(rendered.allowQueries)
        assertTrue(rendered.chatData!!.toByteArray(Charsets.UTF_8).size <= 1_024)
        assertEquals(3, data.path("members").size())
        assertEquals(3, data.path("roles").size())
        assertEquals(3, data.path("members")[0].path("a").size())
        assertTrue(data.path("canCreate").asBoolean())
        assertFalse(rendered.html.contains(members.first().name))
    }

    @Test
    fun untrustedMemberNameIsDataNotInjectedIntoJavascript() {
        val payload = "<script>alert('x')</script>"
        val view = AccountManagementWidgetView(
            accountId = UUID.randomUUID(),
            nickname = "Kirito",
            page = 0,
            pages = 1,
            totalMembers = 1,
            totalRoles = 0,
            members = listOf(AccountManagementWidgetMember(
                UUID.randomUUID(), payload, null, false, false, emptySet(),
            )),
            roles = emptyList(),
            canRemoveMembers = false,
            canAssignRoles = false,
        )

        val rendered = renderer.render(view)

        assertEquals(payload, json.readTree(rendered.chatData).path("members")[0].path("n").asText())
        assertFalse(rendered.html.contains(payload))
        assertTrue(rendered.html.contains("textContent"))
        assertTrue(rendered.html.contains("kick-confirm"))
        assertTrue(rendered.html.contains("role-delete-confirm"))
        assertTrue(rendered.html.contains("w1:manage:permission:"))
        assertTrue(rendered.html.contains("w1:manage:move:"))
        assertTrue(rendered.html.contains("w1:manage:edit:"))
        assertTrue(rendered.html.contains("w1:manage:"))
    }
}
