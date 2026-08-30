package com.mnemos.telegram.bot.adapter.out.settings

import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.personalization.contracts.event.NotificationPackTemplateV1
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackSelectionResponse
import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.UUID

class SettingsGatewayAdapterTest {
    @Test
    fun `gateway maps active notification pack name`() {
        val gateway = SettingsGatewayAdapter(FakePersonalizationServiceClient(selection("Night <Ops>")))

        val result = requireNotNull(gateway.getSettings(931458258))

        assertEquals("Night <Ops>", result.activeNotificationPackTitle)
    }

    @Test
    fun `gateway treats missing selection as empty settings`() {
        val gateway = SettingsGatewayAdapter(FakePersonalizationServiceClient(null))

        val result = requireNotNull(gateway.getSettings(931458258))

        assertNull(result.activeNotificationPackTitle)
    }

    @Test
    fun `gateway reports unavailable service failures`() {
        val gateway = SettingsGatewayAdapter(FailingPersonalizationServiceClient())

        assertNull(gateway.getSettings(931458258))
    }

    private fun selection(name: String): InternalTelegramNotificationPackSelectionResponse =
        InternalTelegramNotificationPackSelectionResponse(
            personalizationId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"),
            name = name,
            selectionRevision = 7,
            pack =
                NotificationPackSnapshotV1(
                    mixedAggregateTitle = "aggregate title",
                    templates =
                        NotificationPackTemplateType.entries.map { type ->
                            NotificationPackTemplateV1(
                                type = type,
                                emoji = null,
                                soloTitle = "title",
                                soloBody = "body",
                                compact = "compact",
                                aggregateTitle = "aggregate",
                            )
                        },
                ),
        )

    private class FakePersonalizationServiceClient(
        private val selection: InternalTelegramNotificationPackSelectionResponse?,
    ) : PersonalizationServiceClient {
        override fun getNotificationPackSelection(telegramId: Long): InternalTelegramNotificationPackSelectionResponse? {
            return selection
        }
    }

    private class FailingPersonalizationServiceClient : PersonalizationServiceClient {
        override fun getNotificationPackSelection(telegramId: Long): InternalTelegramNotificationPackSelectionResponse {
            error("personalization unavailable")
        }
    }
}
