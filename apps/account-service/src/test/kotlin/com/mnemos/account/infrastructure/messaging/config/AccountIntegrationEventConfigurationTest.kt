package com.mnemos.account.infrastructure.messaging.config

import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.messaging.core.event.IntegrationEvent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class AccountIntegrationEventConfigurationTest {
    private val resolver =
        AccountIntegrationEventConfiguration()
            .accountIntegrationEventRouteResolver()

    @Test
    fun `all account aggregate events resolve to the account route`() {
        val accountEventTypes =
            listOf(
                AccountEventType.ACCOUNT_CREATED,
                AccountEventType.ACCOUNT_ACTIVATED,
                AccountEventType.ACCOUNT_DEACTIVATED,
                AccountEventType.ACCOUNT_DELETED,
                AccountEventType.ACCOUNT_OWNER_TRANSFERRED,
                AccountEventType.ACCOUNT_MEMBER_JOINED,
                AccountEventType.ACCOUNT_MEMBER_LEFT,
                AccountEventType.ACCOUNT_MEMBER_REMOVED,
            )

        accountEventTypes.forEach { eventType ->
            val route = resolver.resolve(event(eventType))

            assertEquals(AccountEventContract.TOPIC, route.topic)
            assertEquals(AccountEventContract.VERSION, route.version)
            assertEquals(AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE, route.aggregateType)
        }
    }

    @Test
    fun `user created resolves to the user route`() {
        val route = resolver.resolve(event(AccountEventType.USER_CREATED))

        assertEquals(AccountEventContract.TOPIC, route.topic)
        assertEquals(AccountEventContract.VERSION, route.version)
        assertEquals(AccountEventAggregateType.USER_AGGREGATE_TYPE, route.aggregateType)
    }

    @Test
    fun `unknown account service event remains fail closed`() {
        assertThrows(IllegalStateException::class.java) {
            resolver.resolve(event("account.unknown"))
        }
    }

    private fun event(type: String): IntegrationEvent<Unit> =
        IntegrationEvent(
            type = type,
            aggregateId = UUID.randomUUID(),
            occurredAt = Instant.EPOCH,
            payload = Unit,
        )
}
