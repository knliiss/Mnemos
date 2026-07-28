package com.mnemos.accountservice.domain.model

import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.HexColor
import com.mnemos.accountservice.domain.model.account.role.Role
import com.mnemos.accountservice.support.TEST_TIME
import com.mnemos.accountservice.support.role
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class RoleTest {
    @Test
    fun `create preserves supplied permissions and rank`() {
        val permissions = mutableSetOf(RolePermission.VIEW_BETS, RolePermission.PLACE_BETS)
        val role =
            Role.create(
                AccountId(UUID.randomUUID()),
                HexColor("#AABBCC"),
                AccountRoleName("Bettor"),
                2 * Role.RANK_STEP,
                permissions,
            )

        assertEquals(2 * Role.RANK_STEP, role.hierarchyRank)
        assertEquals(permissions, role.permissions)
        assertEquals(role.createdAt, role.updatedAt)
    }

    @Test
    fun `update permissions applies only requested toggles`() {
        val role = role(permissions = setOf(RolePermission.VIEW_BETS, RolePermission.PLACE_BETS))

        assertTrue(
            role.updatePermissions(
                mapOf(
                    RolePermission.VIEW_BETS to false,
                    RolePermission.MANAGE_BETS to true,
                ),
            ),
        )

        assertEquals(setOf(RolePermission.PLACE_BETS, RolePermission.MANAGE_BETS), role.permissions)
    }

    @Test
    fun `update permissions is idempotent when desired state already matches`() {
        val role = role(permissions = setOf(RolePermission.VIEW_BETS))
        val updatedAt = role.updatedAt

        assertFalse(
            role.updatePermissions(
                mapOf(
                    RolePermission.VIEW_BETS to true,
                    RolePermission.PLACE_BETS to false,
                ),
            ),
        )
        assertEquals(updatedAt, role.updatedAt)
    }

    @Test
    fun `empty permission patch does nothing`() {
        val role = role(permissions = setOf(RolePermission.VIEW_BETS))
        val updatedAt = role.updatedAt

        assertFalse(role.updatePermissions(emptyMap()))
        assertEquals(updatedAt, role.updatedAt)
    }

    @Test
    fun `update name changes only when value differs`() {
        val role = role(name = "Moderator")
        val initialUpdatedAt = role.updatedAt

        assertFalse(role.updateRoleName(AccountRoleName("Moderator")))
        assertEquals(initialUpdatedAt, role.updatedAt)
        assertTrue(role.updateRoleName(AccountRoleName("Admin")))
        assertEquals("Admin", role.roleName.value)
        assertTrue(role.updatedAt >= initialUpdatedAt)
    }

    @Test
    fun `update color changes only when value differs`() {
        val role = role(color = "#AABBCC")
        val initialUpdatedAt = role.updatedAt

        assertFalse(role.updateRoleColor(HexColor("#AABBCC")))
        assertEquals(initialUpdatedAt, role.updatedAt)
        assertTrue(role.updateRoleColor(HexColor("#112233")))
        assertEquals("#112233", role.roleColor.value)
    }

    @Test
    fun `update hierarchy rank changes only when value differs`() {
        val role = role(rank = Role.RANK_STEP)
        val initialUpdatedAt = role.updatedAt

        assertFalse(role.updateHierarchyRank(Role.RANK_STEP))
        assertEquals(initialUpdatedAt, role.updatedAt)
        assertTrue(role.updateHierarchyRank(2 * Role.RANK_STEP))
        assertEquals(2 * Role.RANK_STEP, role.hierarchyRank)
    }

    @Test
    fun `restore makes defensive permission copy`() {
        val source = mutableSetOf(RolePermission.VIEW_BETS)
        val role =
            Role.restore(
                id =
                    com.mnemos.accountservice.domain.model.account.role
                        .RoleId(UUID.randomUUID()),
                accountId = AccountId(UUID.randomUUID()),
                roleColor = HexColor("#AABBCC"),
                roleName = AccountRoleName("Role"),
                hierarchyRank = Role.RANK_STEP,
                permissions = source,
                createdAt = TEST_TIME,
                updatedAt = TEST_TIME,
            )

        source += RolePermission.MANAGE_BETS

        assertEquals(setOf(RolePermission.VIEW_BETS), role.permissions)
    }
}
