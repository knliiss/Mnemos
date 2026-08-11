package com.mnemos.wallet.infrastructure.messaging.kafka.account

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.contracts.account.AccountEventAggregateType
import com.mnemos.contracts.account.AccountEventContract
import com.mnemos.contracts.account.AccountEventType
import com.mnemos.contracts.account.v1.AccountCreatedV1
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.message.ConsumedMessage
import com.mnemos.wallet.application.balance.InitializeAccountBalancesService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import java.time.Instant
import java.util.UUID

class AccountCreatedEventHandlerTest {
    private val initializeAccountBalancesService = mock<InitializeAccountBalancesService>()
    private val handler = AccountCreatedEventHandler(initializeAccountBalancesService)

    @Test
    fun `definition matches account created contract`() {
        assertEquals(AccountEventContract.TOPIC, handler.definition.topic)
        assertEquals(AccountEventType.ACCOUNT_CREATED, handler.definition.type)
        assertEquals(AccountEventContract.VERSION, handler.definition.version)
        assertEquals(AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE, handler.definition.aggregateType)
        assertEquals(AccountCreatedV1::class.java, handler.definition.payloadType)
    }

    @Test
    fun `handle initializes balances for aggregate account`() {
        val accountId = UUID.randomUUID()

        handler.handle(
            ConsumedMessage(
                messageId = UUID.randomUUID(),
                type = AccountEventType.ACCOUNT_CREATED,
                version = AccountEventContract.VERSION,
                producer = "account-service",
                aggregateType = AccountEventAggregateType.ACCOUNT_AGGREGATE_TYPE,
                aggregateId = accountId,
                occurredAt = Instant.parse("2026-08-09T00:00:00Z"),
                metadata = EventMetadata(),
                payload =
                    AccountCreatedV1(
                        ownerMemberId = UUID.randomUUID(),
                        ownerUserId = UUID.randomUUID(),
                        cristalixUuid = UUID.randomUUID(),
                        cristalixNickname = "Knalis",
                        maxMembers = 10,
                    ),
            ),
        )

        verify(initializeAccountBalancesService).initialize(AccountId(accountId))
    }
}
