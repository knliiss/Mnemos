package com.mnemos.realtime.botagent

import com.mnemos.realtime.application.collector.port.out.CollectorIngressPublisher
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import tools.jackson.databind.json.JsonMapper

class BotAgentTelemetryTest {
    @Test
    fun reportsAreValidated() {
        val mapper = JsonMapper.builder().build()
        val socket =
            BotAgentSocket(
                mock(BotAgentIdentityClient::class.java),
                mock(BotAgentSessions::class.java),
                mapper,
                mock(CollectorIngressPublisher::class.java),
            )
        val event =
            mapper.readTree(
                """{"itemName":"Серафим","itemType":"PET","itemRarity":"MYTHICAL","droppedFor":"ishfis1"}""",
            )
        assertTrue(socket.validTelemetryEvent(event, "ITEM_DROP"))
    }
}
