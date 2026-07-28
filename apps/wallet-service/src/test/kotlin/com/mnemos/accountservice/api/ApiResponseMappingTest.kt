package com.mnemos.accountservice.api

import com.mnemos.accountservice.api.dto.account.invite.AccountInviteStatusResponse
import com.mnemos.accountservice.api.dto.account.invite.toResponse
import com.mnemos.accountservice.api.dto.account.member.toResponse
import com.mnemos.accountservice.api.dto.account.role.toResponse
import com.mnemos.accountservice.api.dto.account.toResponse
import com.mnemos.accountservice.api.dto.user.toResponse
import com.mnemos.accountservice.application.query.account.AccountDetails
import com.mnemos.accountservice.application.query.account.AccountSummary
import com.mnemos.accountservice.application.query.account.invite.AccountInviteListItem
import com.mnemos.accountservice.application.query.account.invite.AccountInviteStatus
import com.mnemos.accountservice.application.query.account.member.AccountMemberListItem
import com.mnemos.accountservice.application.query.account.member.AccountMemberRoleSummary
import com.mnemos.accountservice.application.query.account.role.RoleView
import com.mnemos.accountservice.application.query.user.CurrentUserView
import com.mnemos.accountservice.application.result.account.CreateAccountResult
import com.mnemos.accountservice.application.service.invite.CreateAccountInviteService.CreateAccountInviteResult
import com.mnemos.accountservice.application.service.member.JoinAccountResult
import com.mnemos.accountservice.domain.model.account.AccountMemberId
import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import com.mnemos.accountservice.domain.model.account.invite.InviteId
import com.mnemos.accountservice.domain.model.account.role.AccountRoleName
import com.mnemos.accountservice.domain.model.account.role.HexColor
import com.mnemos.accountservice.domain.model.account.role.RoleId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.core.model.permission.RolePermission
import com.mnemos.common.core.model.user.LocalizationLanguage
import com.mnemos.common.core.model.user.UserId
import com.mnemos.common.core.model.user.Username
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ApiResponseMappingTest {
    private val now = Instant.parse("2026-07-27T10:00:00Z")

    @Test
    fun `role response includes every permission with explicit false defaults`() {
        val granted = setOf(RolePermission.MANAGE_BETS, RolePermission.PLACE_BETS)
        val response =
            RoleView(
                RoleId(UUID.randomUUID()),
                "Manager",
                "#AABBCC",
                1_000,
                granted,
                now,
                now,
            ).toResponse()

        assertEquals(RolePermission.entries.size, response.permissions.size)
        granted.forEach { assertTrue(response.permissions.getValue(it)) }
        (RolePermission.entries - granted).forEach { assertFalse(response.permissions.getValue(it)) }
    }

    @Test
    fun `invite status mapping preserves every enum state`() {
        AccountInviteStatus.entries.forEach { status ->
            val response =
                AccountInviteListItem(
                    InviteId(UUID.randomUUID()),
                    InviteCode.of("ABCD-EFGH-JKLM-NPQR"),
                    UserId(UUID.randomUUID()),
                    now,
                    null,
                    null,
                    null,
                    status,
                ).toResponse()

            assertEquals(AccountInviteStatusResponse.valueOf(status.name), response.status)
        }
    }

    @Test
    fun `account result and projections preserve identifiers and metadata`() {
        val accountId = AccountId(UUID.randomUUID())
        val memberId = AccountMemberId(UUID.randomUUID())
        val create = CreateAccountResult(accountId, memberId).toResponse()
        assertEquals(accountId.value, create.accountId)
        assertEquals(memberId.value, create.memberId)

        val summary = AccountSummary(accountId, "Knalis", memberId, 3, 10, true, true).toResponse()
        assertEquals("Knalis", summary.cristalixNickname)
        assertTrue(summary.owner)
        assertTrue(summary.active)

        val cristalixUuid = UUID.randomUUID()
        val details =
            AccountDetails(
                accountId,
                cristalixUuid,
                "Knalis",
                memberId,
                3,
                10,
                true,
                true,
                now,
                now.plusSeconds(1),
            ).toResponse()
        assertEquals(cristalixUuid, details.cristalixUuid)
        assertEquals(now.plusSeconds(1), details.updatedAt)
    }

    @Test
    fun `member and join responses preserve nested role details`() {
        val memberId = AccountMemberId(UUID.randomUUID())
        val roleId = RoleId(UUID.randomUUID())
        val item =
            AccountMemberListItem(
                memberId,
                "Member",
                false,
                AccountMemberRoleSummary(roleId, AccountRoleName("Admin"), HexColor("#AABBCC")),
                now,
            ).toResponse()

        assertEquals(roleId.value, item.highestRole!!.id)
        assertEquals("Admin", item.highestRole!!.name)
        assertEquals("#AABBCC", item.highestRole!!.color)

        val accountId = AccountId(UUID.randomUUID())
        val join = JoinAccountResult(accountId, memberId).toResponse()
        assertEquals(accountId.value, join.accountId)
        assertEquals(memberId.value, join.memberId)
    }

    @Test
    fun `user response preserves nullable active account`() {
        val userId = UserId(UUID.randomUUID())
        val activeAccountId = AccountId(UUID.randomUUID())
        val response =
            CurrentUserView(
                userId,
                Username("knalis"),
                LocalizationLanguage.EN,
                activeAccountId,
                now,
                now,
            ).toResponse()

        assertEquals(userId.value, response.id)
        assertEquals(activeAccountId.value, response.activeAccountId)
    }

    @Test
    fun `created invite response exposes canonical code and expiration`() {
        val expiresAt = now.plusSeconds(3_600)
        val result =
            CreateAccountInviteResult(
                InviteId(UUID.randomUUID()),
                InviteCode.of("abcdefghjklmnpqr"),
                now,
                expiresAt,
            ).toResponse()

        assertEquals("ABCD-EFGH-JKLM-NPQR", result.code)
        assertEquals(expiresAt, result.expiresAt)
    }
}
