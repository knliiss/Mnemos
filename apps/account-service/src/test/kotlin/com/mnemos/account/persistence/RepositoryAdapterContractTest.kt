package com.mnemos.account.persistence

import com.mnemos.account.domain.exception.role.RoleAssignmentNotFoundException
import com.mnemos.account.persistence.entity.role.RoleAssignmentEntityId
import com.mnemos.account.persistence.mapper.AccountRolePersistenceMapper.toEntity
import com.mnemos.account.persistence.repository.account.JpaAccountMemberRepository
import com.mnemos.account.persistence.repository.account.JpaAccountRepository
import com.mnemos.account.persistence.repository.account.role.JpaRoleAssignmentRepository
import com.mnemos.account.persistence.repository.account.role.JpaRoleRepository
import com.mnemos.account.persistence.repository.account.role.RoleAssignmentRepositoryAdapter
import com.mnemos.account.persistence.repository.account.role.RoleRepositoryAdapter
import com.mnemos.account.persistence.repository.user.JpaUserRepository
import com.mnemos.account.persistence.repository.user.UserRepositoryAdapter
import com.mnemos.account.support.TEST_TIME
import com.mnemos.account.support.account
import com.mnemos.account.support.interfaceProxy
import com.mnemos.account.support.member
import com.mnemos.account.support.role
import com.mnemos.account.support.user
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.lang.reflect.Method
import java.util.Optional
import java.util.UUID

class RepositoryAdapterContractTest {
    @Test
    fun `role update loads entity by role id not account id`() {
        val role = role()
        val entity = role.toEntity()
        var lookedUpId: UUID? = null
        val jpa =
            interfaceProxy<JpaRoleRepository> { method, args ->
                when (method.name) {
                    "findById" -> {
                        lookedUpId = args[0] as UUID
                        Optional.of(entity)
                    }
                    "save" -> args[0]
                    else -> defaultValue(method)
                }
            }

        RoleRepositoryAdapter(jpa).update(role)

        assertEquals(role.id.value, lookedUpId)
    }

    @Test
    fun `removing existing assignment deletes it`() {
        val member = member()
        val role = role(accountId = member.accountId)
        var deletedId: RoleAssignmentEntityId? = null
        val jpa =
            interfaceProxy<JpaRoleAssignmentRepository> { method, args ->
                when (method.name) {
                    "existsById" -> true
                    "deleteById" -> {
                        deletedId = args[0] as RoleAssignmentEntityId
                        Unit
                    }
                    else -> defaultValue(method)
                }
            }

        assertTrue(RoleAssignmentRepositoryAdapter(jpa).remove(role.id, member.id))
        assertEquals(member.id.value, deletedId!!.accountMemberId)
        assertEquals(role.id.value, deletedId!!.roleId)
    }

    @Test
    fun `removing missing assignment throws domain not found`() {
        val member = member()
        val role = role(accountId = member.accountId)
        val jpa =
            interfaceProxy<JpaRoleAssignmentRepository> { method, _ ->
                when (method.name) {
                    "existsById" -> false
                    else -> defaultValue(method)
                }
            }

        assertThrows<RoleAssignmentNotFoundException> {
            RoleAssignmentRepositoryAdapter(jpa).remove(role.id, member.id)
        }
    }

    @Test
    fun `clear active account forwards requested account and timestamp`() {
        val account = account()
        var receivedAccountId: UUID? = null
        var receivedTime: java.time.Instant? = null
        val jpa =
            interfaceProxy<JpaUserRepository> { method, args ->
                when (method.name) {
                    "clearActiveAccountByAccountId" -> {
                        receivedAccountId = args[0] as UUID
                        receivedTime = args[1] as java.time.Instant
                        2
                    }
                    else -> defaultValue(method)
                }
            }

        UserRepositoryAdapter(jpa).clearActiveAccountByAccountId(account.id, TEST_TIME)

        assertEquals(account.id.value, receivedAccountId)
        assertEquals(TEST_TIME, receivedTime)
    }

    @Test
    fun `user conditional clear converts affected row count to boolean`() {
        val user = user()
        val account = account()
        var result = 0
        val jpa =
            interfaceProxy<JpaUserRepository> { method, _ ->
                when (method.name) {
                    "clearActiveAccountIfMatches" -> result
                    else -> defaultValue(method)
                }
            }
        val adapter = UserRepositoryAdapter(jpa)

        assertFalse(adapter.clearActiveAccountIfMatches(user.id, account.id, TEST_TIME))
        result = 1
        assertTrue(adapter.clearActiveAccountIfMatches(user.id, account.id, TEST_TIME))
    }

    @Test
    fun `user bulk clear query targets matching account instead of null`() {
        val query =
            JpaUserRepository::class.java
                .getMethod("clearActiveAccountByAccountId", UUID::class.java, java.time.Instant::class.java)
                .getAnnotation(Query::class.java)
                .value

        assertTrue(query.contains("user.activeAccountId = :accountId"))
    }

    @Test
    fun `account lock query selects account entity by id`() {
        val query =
            JpaAccountRepository::class.java
                .getMethod("findByIdForUpdate", UUID::class.java)
                .getAnnotation(Query::class.java)
                .value

        assertTrue(query.contains("FROM AccountEntity"))
        assertTrue(query.contains("account.id = :id") || query.contains("account.id = :accountId"))
    }

    @Test
    fun `available accounts query is correlated by user id`() {
        val method = JpaAccountRepository::class.java.getMethod("findAllAvailableForUser", UUID::class.java)
        val query = method.getAnnotation(Query::class.java).value

        assertTrue(query.contains(":userId"))
        assertFalse(query.contains(":accountId"))
    }

    @Test
    fun `member permission query has no undeclared role id placeholder`() {
        val method =
            JpaAccountMemberRepository::class.java
                .methods
                .single { it.name == "hasPermission" }
        val query = method.getAnnotation(Query::class.java).value

        assertFalse(query.contains(":roleId"))
        assertTrue(query.contains(":memberId"))
        assertTrue(query.contains(":rolePermission") || query.contains(":permission"))
    }

    @Test
    fun `member role query correlates role entity with assignment role id`() {
        val method =
            JpaRoleRepository::class.java
                .methods
                .single { it.name == "findAllByMemberIdAndAccountIdOrderByRank" }
        val query = method.getAnnotation(Query::class.java).value

        assertTrue(query.contains("assignment.id.roleId = role.id"))
        assertFalse(query.contains(":roleId"))
    }

    @Test
    fun `role assignment queries bind spring data params and filter by member id`() {
        val findByRole = JpaRoleAssignmentRepository::class.java.methods.single { it.name == "findAllByRoleId" }
        assertTrue(findByRole.parameters[0].isAnnotationPresent(Param::class.java))
        assertEquals("roleId", findByRole.parameters[0].getAnnotation(Param::class.java).value)

        val maxRankQuery =
            JpaRoleAssignmentRepository::class.java.methods
                .single { it.name == "findMaxRankByMemberId" }
                .getAnnotation(Query::class.java)
                .value
        assertTrue(maxRankQuery.contains("assignment.id.accountMemberId = :memberId"))
    }

    private fun defaultValue(method: Method): Any? =
        when (method.returnType) {
            java.lang.Boolean.TYPE -> false
            java.lang.Integer.TYPE -> 0
            java.lang.Long.TYPE -> 0L
            java.lang.Void.TYPE -> Unit
            List::class.java -> emptyList<Any>()
            Set::class.java -> emptySet<Any>()
            Optional::class.java -> Optional.empty<Any>()
            else -> null
        }
}
