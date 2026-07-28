package com.mnemos.account.api

import com.mnemos.account.api.dto.account.CreateAccountRequest
import com.mnemos.account.api.dto.account.invite.CreateAccountInviteRequest
import com.mnemos.account.api.dto.account.member.JoinAccountRequest
import com.mnemos.account.api.dto.account.role.CreateRoleRequest
import com.mnemos.account.api.dto.account.role.UpdateRoleHierarchyRequest
import com.mnemos.account.api.dto.account.role.UpdateRolePermissionsRequest
import com.mnemos.account.api.dto.user.UpdateUsernameRequest
import com.mnemos.common.core.model.permission.RolePermission
import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class ApiDtoValidationTest {
    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `username request accepts boundary lengths`() {
        assertTrue(validator.validate(UpdateUsernameRequest("abc")).isEmpty())
        assertTrue(validator.validate(UpdateUsernameRequest("a".repeat(20))).isEmpty())
    }

    @Test
    fun `username request rejects blank short and oversized values`() {
        assertTrue(validator.validate(UpdateUsernameRequest("   ")).isNotEmpty())
        assertTrue(validator.validate(UpdateUsernameRequest("ab")).isNotEmpty())
        assertTrue(validator.validate(UpdateUsernameRequest("a".repeat(21))).isNotEmpty())
    }

    @Test
    fun `account request accepts nickname boundaries`() {
        assertTrue(validator.validate(CreateAccountRequest("Abcd")).isEmpty())
        assertTrue(validator.validate(CreateAccountRequest("A".repeat(20))).isEmpty())
    }

    @Test
    fun `account request rejects blank nickname before domain construction`() {
        assertTrue(validator.validate(CreateAccountRequest("    ")).isNotEmpty())
    }

    @Test
    fun `role request validates name and canonical color`() {
        assertTrue(validator.validate(CreateRoleRequest("Member", "#AABBCC")).isEmpty())
        assertTrue(validator.validate(CreateRoleRequest("Member", "#AABBCCDD")).isEmpty())
        assertTrue(validator.validate(CreateRoleRequest("", "#AABBCC")).isNotEmpty())
        assertTrue(validator.validate(CreateRoleRequest("Member", "AABBCC")).isNotEmpty())
    }

    @Test
    fun `role permissions patch cannot be empty`() {
        assertTrue(validator.validate(UpdateRolePermissionsRequest(emptyMap())).isNotEmpty())
        assertTrue(
            validator
                .validate(
                    UpdateRolePermissionsRequest(mapOf(RolePermission.MANAGE_BETS to true)),
                ).isEmpty(),
        )
    }

    @Test
    fun `role hierarchy request cannot be empty`() {
        assertTrue(validator.validate(UpdateRoleHierarchyRequest(emptyList())).isNotEmpty())
        assertTrue(validator.validate(UpdateRoleHierarchyRequest(listOf(UUID.randomUUID()))).isEmpty())
    }

    @Test
    fun `invite expiration validates minute boundaries and nullable value`() {
        assertTrue(validator.validate(CreateAccountInviteRequest(null)).isEmpty())
        assertTrue(validator.validate(CreateAccountInviteRequest(1)).isEmpty())
        assertTrue(validator.validate(CreateAccountInviteRequest(10_080)).isEmpty())
        assertTrue(validator.validate(CreateAccountInviteRequest(0)).isNotEmpty())
        assertTrue(validator.validate(CreateAccountInviteRequest(10_081)).isNotEmpty())
    }

    @Test
    fun `join request rejects blank code`() {
        assertTrue(validator.validate(JoinAccountRequest(" ")).isNotEmpty())
        assertEquals(0, validator.validate(JoinAccountRequest("ABCD-EFGH-JKLM-NPQR")).size)
    }
}
