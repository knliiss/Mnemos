package com.mnemos.accountservice.persistence

import com.mnemos.accountservice.domain.model.account.AccountStatus
import com.mnemos.accountservice.domain.model.account.role.assignment.RoleAssignment
import com.mnemos.accountservice.persistence.entity.role.RoleAssignmentActorType
import com.mnemos.accountservice.persistence.entity.role.RoleAssignmentEntity
import com.mnemos.accountservice.persistence.entity.role.RoleAssignmentEntityId
import com.mnemos.accountservice.persistence.mapper.AccountInvitePersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountInvitePersistenceMapper.toEntity
import com.mnemos.accountservice.persistence.mapper.AccountMemberPersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountMemberPersistenceMapper.toEntity
import com.mnemos.accountservice.persistence.mapper.AccountPersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountPersistenceMapper.toEntity
import com.mnemos.accountservice.persistence.mapper.AccountPersistenceMapper.updateFrom
import com.mnemos.accountservice.persistence.mapper.AccountRoleAssignmentPersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountRoleAssignmentPersistenceMapper.toEntity
import com.mnemos.accountservice.persistence.mapper.AccountRolePersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.AccountRolePersistenceMapper.toEntity
import com.mnemos.accountservice.persistence.mapper.UserPersistenceMapper.toDomain
import com.mnemos.accountservice.persistence.mapper.UserPersistenceMapper.toEntity
import com.mnemos.accountservice.support.TEST_TIME
import com.mnemos.accountservice.support.account
import com.mnemos.accountservice.support.invite
import com.mnemos.accountservice.support.member
import com.mnemos.accountservice.support.role
import com.mnemos.accountservice.support.user
import com.mnemos.common.core.model.permission.RolePermission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class PersistenceMapperTest {
    @Test
    fun `user round trip preserves all fields`() {
        val source =
            user(
                activeAccountId =
                    com.mnemos.common.core.model.id
                        .AccountId(UUID.randomUUID()),
            )
        val restored = source.toEntity().toDomain()

        assertEquals(source.id, restored.id)
        assertEquals(source.telegramId, restored.telegramId)
        assertEquals(source.keycloakId, restored.keycloakId)
        assertEquals(source.username, restored.username)
        assertEquals(source.localizationLanguage, restored.localizationLanguage)
        assertEquals(source.activeAccountId, restored.activeAccountId)
        assertEquals(source.createdAt, restored.createdAt)
        assertEquals(source.updatedAt, restored.updatedAt)
    }

    @Test
    fun `account round trip preserves lifecycle and configuration`() {
        val source =
            account(
                status = AccountStatus.DELETED,
                deletionRequestedAt = TEST_TIME.plusSeconds(10),
                deletedAt = TEST_TIME.plusSeconds(20),
                baseRoleId =
                    com.mnemos.accountservice.domain.model.account.role
                        .RoleId(UUID.randomUUID()),
                maxMembers = 25,
            )
        val restored = source.toEntity().toDomain()

        assertEquals(source.id, restored.id)
        assertEquals(source.ownerMemberId, restored.ownerMemberId)
        assertEquals(source.status, restored.status)
        assertEquals(source.deletionRequestedAt, restored.deletionRequestedAt)
        assertEquals(source.deletedAt, restored.deletedAt)
        assertEquals(source.baseRoleId, restored.baseRoleId)
        assertEquals(source.maxMembers, restored.maxMembers)
    }

    @Test
    fun `account entity update copies actual deleted timestamp`() {
        val entity = account().toEntity()
        val source =
            account(
                id =
                    com.mnemos.common.core.model.id
                        .AccountId(entity.id),
                status = AccountStatus.DELETED,
                deletionRequestedAt = TEST_TIME.plusSeconds(10),
                deletedAt = TEST_TIME.plusSeconds(20),
            )

        entity.updateFrom(source)

        assertEquals(source.deletionRequestedAt, entity.deletionRequestedAt)
        assertEquals(source.deletedAt, entity.deletedAt)
    }

    @Test
    fun `member role invite mappings round trip`() {
        val member = member()
        val role = role(accountId = member.accountId, permissions = setOf(RolePermission.VIEW_BETS))
        val invite = invite(accountId = member.accountId, consumedAt = TEST_TIME.plusSeconds(5))

        assertEquals(member.id, member.toEntity().toDomain().id)
        assertEquals(role.permissions, role.toEntity().toDomain().permissions)
        assertEquals(invite.consumedAt, invite.toEntity().toDomain().consumedAt)
    }

    @Test
    fun `manual assignment round trip preserves user actor`() {
        val member = member()
        val role = role(accountId = member.accountId)
        val actor = user()
        val source = RoleAssignment.createManual(member.accountId, member.id, role.id, actor.id, TEST_TIME)

        val restored = source.toEntity().toDomain()

        assertEquals(source.assignedBy, restored.assignedBy)
        assertEquals(source.reason, restored.reason)
        assertEquals(source.assignedAt, restored.assignedAt)
    }

    @Test
    fun `system assignment round trip preserves null actor id`() {
        val member = member()
        val role = role(accountId = member.accountId)
        val source = RoleAssignment.createBaseRole(member.accountId, member.id, role.id, TEST_TIME)

        val entity = source.toEntity()
        val restored = entity.toDomain()

        assertEquals(RoleAssignmentActorType.SYSTEM, entity.assignedByType)
        assertEquals(null, entity.assignedByUserId)
        assertEquals(source.assignedBy, restored.assignedBy)
    }

    @Test
    fun `invalid persisted assignment actor combinations are rejected`() {
        val member = member()
        val role = role(accountId = member.accountId)
        val systemWithUser =
            RoleAssignmentEntity(
                RoleAssignmentEntityId(member.id.value, role.id.value),
                member.accountId.value,
                RoleAssignmentActorType.SYSTEM,
                UUID.randomUUID(),
                com.mnemos.accountservice.domain.model.account.role.assignment.RoleAssignmentReason.BASE_ROLE,
                TEST_TIME,
            )
        val userWithoutId =
            RoleAssignmentEntity(
                RoleAssignmentEntityId(member.id.value, role.id.value),
                member.accountId.value,
                RoleAssignmentActorType.USER,
                null,
                com.mnemos.accountservice.domain.model.account.role.assignment.RoleAssignmentReason.MANUAL,
                TEST_TIME,
            )

        assertThrows<IllegalArgumentException> { systemWithUser.toDomain() }
        assertThrows<IllegalArgumentException> { userWithoutId.toDomain() }
    }
}
