package com.mnemos.account.domain.model

import com.mnemos.account.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignmentActor
import com.mnemos.account.domain.model.account.role.assignment.RoleAssignmentReason
import com.mnemos.account.support.TEST_TIME
import com.mnemos.account.support.member
import com.mnemos.account.support.role
import com.mnemos.account.support.user
import com.mnemos.common.core.model.id.AccountId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class RoleAssignmentTest {
    @Test
    fun `manual assignment records user actor and reason`() {
        val accountId = AccountId(UUID.randomUUID())
        val member = member(accountId = accountId)
        val role = role(accountId = accountId)
        val actor = user()

        val assignment = RoleAssignment.createManual(accountId, member.id, role.id, actor.id, TEST_TIME)

        assertEquals(RoleAssignmentActor.User(actor.id), assignment.assignedBy)
        assertEquals(RoleAssignmentReason.MANUAL, assignment.reason)
        assertEquals(TEST_TIME, assignment.assignedAt)
    }

    @Test
    fun `base role assignment records system actor and reason`() {
        val accountId = AccountId(UUID.randomUUID())
        val member = member(accountId = accountId)
        val role = role(accountId = accountId)

        val assignment = RoleAssignment.createBaseRole(accountId, member.id, role.id, TEST_TIME)

        assertEquals(RoleAssignmentActor.System, assignment.assignedBy)
        assertEquals(RoleAssignmentReason.BASE_ROLE, assignment.reason)
    }
}
