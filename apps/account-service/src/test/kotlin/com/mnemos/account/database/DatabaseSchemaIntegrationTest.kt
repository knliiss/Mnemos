package com.mnemos.account.database

import org.flywaydb.core.Flyway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.postgresql.PostgreSQLContainer
import java.sql.PreparedStatement
import java.time.ZoneOffset
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.time.Instant
import java.util.UUID

@Testcontainers
class DatabaseSchemaIntegrationTest {
    @BeforeEach
    fun resetSchema() {
        flyway.clean()
        flyway.migrate()
    }

    @Test
    fun `all migrations apply and validate on empty PostgreSQL database`() {
        flyway.validate()

        assertEquals(8, flyway.info().applied().size)
        assertEquals(
            "8",
            flyway
                .info()
                .current()!!
                .version.version,
        )
    }

    @Test
    fun `username uniqueness is case insensitive`() {
        createUser(username = "Knalis")

        val exception =
            assertThrows<SQLException> {
                createUser(username = "kNaLiS")
            }

        assertEquals(UNIQUE_VIOLATION, exception.sqlState)
    }

    @Test
    fun `telegram id must be positive`() {
        val exception =
            assertThrows<SQLException> {
                createUser(telegramId = 0)
            }

        assertEquals(CHECK_VIOLATION, exception.sqlState)
    }

    @Test
    fun `same user cannot join an account twice`() {
        val userId = createUser()
        val account = createAccount(userId)

        val exception =
            assertThrows<SQLException> {
                execute(
                    """
                    INSERT INTO account_members (id, account_id, user_id, joined_at)
                    VALUES (?, ?, ?, ?)
                    """.trimIndent(),
                    UUID.randomUUID(),
                    account.accountId,
                    userId,
                    Instant.now(),
                )
            }

        assertEquals(UNIQUE_VIOLATION, exception.sqlState)
    }

    @Test
    fun `account owner must be a member of the same account`() {
        val ownerUserId = createUser()
        val foreignUserId = createUser()
        val first = createAccount(ownerUserId)
        val foreignMemberId = UUID.randomUUID()
        execute(
            """
            INSERT INTO account_members (id, account_id, user_id, joined_at)
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
            foreignMemberId,
            first.accountId,
            foreignUserId,
            Instant.now(),
        )

        val exception =
            assertThrows<SQLException> {
                transaction { connection ->
                    val anotherAccountId = UUID.randomUUID()
                    execute(
                        connection,
                        """
                        INSERT INTO accounts (
                            id, owner_member_id, cristalix_uuid, cristalix_nickname,
                            created_at, updated_at, max_members
                        ) VALUES (?, ?, ?, ?, ?, ?, ?)
                        """.trimIndent(),
                        anotherAccountId,
                        foreignMemberId,
                        UUID.randomUUID(),
                        "Another",
                        Instant.now(),
                        Instant.now(),
                        10,
                    )
                }
            }

        assertEquals(FOREIGN_KEY_VIOLATION, exception.sqlState)
    }

    @Test
    fun `active account requires membership for that exact user`() {
        val memberUserId = createUser()
        val outsiderUserId = createUser()
        val account = createAccount(memberUserId)

        val exception =
            assertThrows<SQLException> {
                transaction { connection ->
                    execute(
                        connection,
                        "UPDATE users SET active_account_id = ? WHERE id = ?",
                        account.accountId,
                        outsiderUserId,
                    )
                }
            }

        assertEquals(FOREIGN_KEY_VIOLATION, exception.sqlState)
    }

    @Test
    fun `role hierarchy rank can be swapped atomically because uniqueness is deferred`() {
        val ownerUserId = createUser()
        val account = createAccount(ownerUserId)
        val firstRoleId = createRole(account.accountId, "First", 1_000)
        val secondRoleId = createRole(account.accountId, "Second", 2_000)

        transaction { connection ->
            execute(connection, "UPDATE roles SET hierarchy_rank = 2000 WHERE id = ?", firstRoleId)
            execute(connection, "UPDATE roles SET hierarchy_rank = 1000 WHERE id = ?", secondRoleId)
        }

        assertEquals(2_000L, queryLong("SELECT hierarchy_rank FROM roles WHERE id = ?", firstRoleId))
        assertEquals(1_000L, queryLong("SELECT hierarchy_rank FROM roles WHERE id = ?", secondRoleId))
    }

    @Test
    fun `role color must be canonical RGB or RGBA hex`() {
        val account = createAccount(createUser())

        val exception =
            assertThrows<SQLException> {
                createRole(account.accountId, "Broken", 1_000, "white")
            }

        assertEquals(CHECK_VIOLATION, exception.sqlState)
    }

    @Test
    fun `manual role assignment requires user actor`() {
        val ownerUserId = createUser()
        val account = createAccount(ownerUserId)
        val roleId = createRole(account.accountId, "Member", 1_000)

        val exception =
            assertThrows<SQLException> {
                execute(
                    """
                    INSERT INTO role_assignments (
                        account_id, account_member_id, role_id,
                        assigned_by_type, assigned_by_user_id, assignment_reason, assigned_at
                    ) VALUES (?, ?, ?, 'USER', NULL, 'MANUAL', ?)
                    """.trimIndent(),
                    account.accountId,
                    account.ownerMemberId,
                    roleId,
                    Instant.now(),
                )
            }

        assertEquals(CHECK_VIOLATION, exception.sqlState)
    }

    @Test
    fun `base role assignment requires system actor`() {
        val ownerUserId = createUser()
        val account = createAccount(ownerUserId)
        val roleId = createRole(account.accountId, "Member", 1_000)

        val exception =
            assertThrows<SQLException> {
                execute(
                    """
                    INSERT INTO role_assignments (
                        account_id, account_member_id, role_id,
                        assigned_by_type, assigned_by_user_id, assignment_reason, assigned_at
                    ) VALUES (?, ?, ?, 'SYSTEM', ?, 'BASE_ROLE', ?)
                    """.trimIndent(),
                    account.accountId,
                    account.ownerMemberId,
                    roleId,
                    ownerUserId,
                    Instant.now(),
                )
            }

        assertEquals(CHECK_VIOLATION, exception.sqlState)
    }

    @Test
    fun `invite cannot be consumed and revoked simultaneously`() {
        val creatorId = createUser()
        val account = createAccount(creatorId)
        val now = Instant.now()

        val exception =
            assertThrows<SQLException> {
                execute(
                    """
                    INSERT INTO account_invites (
                        id, account_id, code, created_by_user_id,
                        created_at, expires_at, consumed_at, revoked_at
                    ) VALUES (?, ?, ?, ?, ?, NULL, ?, ?)
                    """.trimIndent(),
                    UUID.randomUUID(),
                    account.accountId,
                    "ABCD-EFGH-JKLM-NPQR",
                    creatorId,
                    now,
                    now,
                    now,
                )
            }

        assertEquals(CHECK_VIOLATION, exception.sqlState)
    }

    @Test
    fun `account status and deletion timestamps must be consistent`() {
        val account = createAccount(createUser())

        val exception =
            assertThrows<SQLException> {
                execute(
                    "UPDATE accounts SET status = 'DELETED', deleted_at = ? WHERE id = ?",
                    Instant.now(),
                    account.accountId,
                )
            }

        assertEquals(CHECK_VIOLATION, exception.sqlState)
    }

    @Test
    fun `account member limit must be positive`() {
        val exception =
            assertThrows<SQLException> {
                createAccount(createUser(), maxMembers = 0)
            }

        assertEquals(CHECK_VIOLATION, exception.sqlState)
    }

    @Test
    fun `deleting account cascades aggregate data but preserves users`() {
        val ownerUserId = createUser()
        val account = createAccount(ownerUserId)
        val roleId = createRole(account.accountId, "Member", 1_000)
        execute(
            """
            INSERT INTO role_assignments (
                account_id, account_member_id, role_id,
                assigned_by_type, assigned_by_user_id, assignment_reason, assigned_at
            ) VALUES (?, ?, ?, 'SYSTEM', NULL, 'BASE_ROLE', ?)
            """.trimIndent(),
            account.accountId,
            account.ownerMemberId,
            roleId,
            Instant.now(),
        )
        execute(
            """
            INSERT INTO account_invites (
                id, account_id, code, created_by_user_id, created_at
            ) VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            UUID.randomUUID(),
            account.accountId,
            "ABCD-EFGH-JKLM-NPQR",
            ownerUserId,
            Instant.now(),
        )

        execute("DELETE FROM accounts WHERE id = ?", account.accountId)

        assertEquals(0, queryLong("SELECT COUNT(*) FROM account_members WHERE account_id = ?", account.accountId))
        assertEquals(0, queryLong("SELECT COUNT(*) FROM roles WHERE account_id = ?", account.accountId))
        assertEquals(0, queryLong("SELECT COUNT(*) FROM role_assignments WHERE account_id = ?", account.accountId))
        assertEquals(0, queryLong("SELECT COUNT(*) FROM account_invites WHERE account_id = ?", account.accountId))
        assertEquals(1, queryLong("SELECT COUNT(*) FROM users WHERE id = ?", ownerUserId))
    }

    private val flyway: Flyway
        get() =
            Flyway
                .configure()
                .dataSource(postgres.jdbcUrl, postgres.username, postgres.password)
                .cleanDisabled(false)
                .load()

    private fun createUser(
        username: String = "user_${UUID.randomUUID().toString().take(8)}",
        telegramId: Long = positiveLong(),
    ): UUID {
        val id = UUID.randomUUID()
        execute(
            """
            INSERT INTO users (
                id, keycloak_id, telegram_id, username,
                localization_language, created_at, updated_at
            ) VALUES (?, ?, ?, ?, 'EN', ?, ?)
            """.trimIndent(),
            id,
            UUID.randomUUID(),
            telegramId,
            username,
            Instant.now(),
            Instant.now(),
        )
        return id
    }

    private fun createAccount(
        ownerUserId: UUID,
        maxMembers: Int = 10,
    ): AccountFixture {
        val accountId = UUID.randomUUID()
        val ownerMemberId = UUID.randomUUID()
        transaction { connection ->
            execute(
                connection,
                """
                INSERT INTO accounts (
                    id, owner_member_id, cristalix_uuid, cristalix_nickname,
                    created_at, updated_at, max_members
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                accountId,
                ownerMemberId,
                UUID.randomUUID(),
                "Nick_${accountId.toString().take(6)}",
                Instant.now(),
                Instant.now(),
                maxMembers,
            )
            execute(
                connection,
                """
                INSERT INTO account_members (id, account_id, user_id, joined_at)
                VALUES (?, ?, ?, ?)
                """.trimIndent(),
                ownerMemberId,
                accountId,
                ownerUserId,
                Instant.now(),
            )
        }
        return AccountFixture(accountId, ownerMemberId)
    }

    private fun createRole(
        accountId: UUID,
        name: String,
        rank: Long,
        color: String = "#AABBCC",
    ): UUID {
        val roleId = UUID.randomUUID()
        execute(
            """
            INSERT INTO roles (
                id, account_id, role_name, role_color,
                hierarchy_rank, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            roleId,
            accountId,
            name,
            color,
            rank,
            Instant.now(),
            Instant.now(),
        )
        return roleId
    }

    private fun execute(
        sql: String,
        vararg args: Any?,
    ) {
        connection().use { execute(it, sql, *args) }
    }

    private fun execute(connection: Connection, sql: String, vararg args: Any?) {
        connection.prepareStatement(sql).use { statement ->
            args.forEachIndexed { index, value ->
                statement.bind(index + 1, value)
            }

            statement.executeUpdate()
        }
    }

    private fun queryLong(
        sql: String,
        vararg args: Any?,
    ): Long =
        connection().use { connection ->
            connection.prepareStatement(sql).use { statement ->
                args.forEachIndexed { index, value ->
                    statement.bind(index + 1, value)
                }
                statement.executeQuery().use { result ->
                    assertTrue(result.next())
                    result.getLong(1)
                }
            }
        }

    private fun transaction(block: (Connection) -> Unit) {
        connection().use { connection ->
            connection.autoCommit = false
            try {
                block(connection)
                connection.commit()
            } catch (error: Throwable) {
                runCatching { connection.rollback() }
                throw error
            }
        }
    }

    private fun connection(): Connection =
        DriverManager.getConnection(
            postgres.jdbcUrl,
            postgres.username,
            postgres.password,
        )

    private fun positiveLong(): Long =
        UUID
            .randomUUID()
            .mostSignificantBits
            .let { if (it == Long.MIN_VALUE) 1 else kotlin.math.abs(it) }
            .coerceAtLeast(1)

    private fun PreparedStatement.bind(index: Int, value: Any?) {
        when (value) {
            is Instant -> setObject(index, value.atOffset(ZoneOffset.UTC))
            else -> setObject(index, value)
        }
    }

    private data class AccountFixture(
        val accountId: UUID,
        val ownerMemberId: UUID,
    )

    companion object {
        private const val UNIQUE_VIOLATION = "23505"
        private const val FOREIGN_KEY_VIOLATION = "23503"
        private const val CHECK_VIOLATION = "23514"

        @Container
        @JvmStatic
        val postgres =
            PostgreSQLContainer("postgres:17-alpine")
                .withDatabaseName("mnemos_schema_test")
                .withUsername("mnemos")
                .withPassword("mnemos_test_password")
    }
}
