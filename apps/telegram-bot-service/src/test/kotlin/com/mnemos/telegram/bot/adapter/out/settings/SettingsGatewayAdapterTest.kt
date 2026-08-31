package com.mnemos.telegram.bot.adapter.out.settings

import com.mnemos.contracts.account.internal.v1.InternalTelegramLocalizationLanguage
import com.mnemos.contracts.account.internal.v1.InternalTelegramUserSettingsResponse
import com.mnemos.personalization.contracts.event.NotificationPackSnapshotV1
import com.mnemos.personalization.contracts.event.NotificationPackTemplateV1
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackListResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackSelectionResponse
import com.mnemos.personalization.contracts.internal.v1.InternalTelegramNotificationPackSummaryResponse
import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.telegram.bot.adapter.out.account.AccountServiceClient
import com.mnemos.telegram.bot.application.settings.model.TelegramSettingsLanguage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

class SettingsGatewayAdapterTest {
    private val accountClient = mock<AccountServiceClient>()
    private val personalizationClient = mock<PersonalizationServiceClient>()
    private val gateway = SettingsGatewayAdapter(accountClient, personalizationClient)

    @Test
    fun `gateway maps language and active notification pack`() {
        val packId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")

        whenever(accountClient.getUserSettings(TELEGRAM_ID)).thenReturn(
            InternalTelegramUserSettingsResponse(
                localizationLanguage = InternalTelegramLocalizationLanguage.UK,
            ),
        )
        whenever(personalizationClient.getNotificationPackSelection(TELEGRAM_ID)).thenReturn(selection(packId, "Night <Ops>"))
        whenever(personalizationClient.getNotificationPacks(TELEGRAM_ID)).thenReturn(
            packList(
                InternalTelegramNotificationPackSummaryResponse(
                    personalizationId = packId,
                    name = "Night <Ops>",
                    active = true,
                ),
            ),
        )

        val result = requireNotNull(gateway.getSettings(TELEGRAM_ID))

        assertEquals(TelegramSettingsLanguage.UK, result.localizationLanguage)
        assertEquals(packId, result.activeNotificationPackId)
        assertEquals("Night <Ops>", result.activeNotificationPackTitle)
        assertEquals(1, result.notificationPacks.size)
        assertEquals(true, result.notificationPacks.single().active)
    }

    @Test
    fun `gateway falls back to active owned pack when explicit selection is absent`() {
        val packId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")

        whenever(personalizationClient.getNotificationPackSelection(TELEGRAM_ID)).thenReturn(null)
        whenever(personalizationClient.getNotificationPacks(TELEGRAM_ID)).thenReturn(
            packList(
                InternalTelegramNotificationPackSummaryResponse(
                    personalizationId = packId,
                    name = "Classic",
                    active = true,
                ),
            ),
        )

        val result = requireNotNull(gateway.getSettings(TELEGRAM_ID))

        assertEquals(packId, result.activeNotificationPackId)
        assertEquals("Classic", result.activeNotificationPackTitle)
    }

    @Test
    fun `gateway reports unavailable when every settings source fails`() {
        whenever(accountClient.getUserSettings(TELEGRAM_ID)).thenThrow(IllegalStateException("account unavailable"))
        whenever(personalizationClient.getNotificationPackSelection(TELEGRAM_ID))
            .thenThrow(IllegalStateException("personalization unavailable"))
        whenever(personalizationClient.getNotificationPacks(TELEGRAM_ID))
            .thenThrow(IllegalStateException("personalization unavailable"))

        assertNull(gateway.getSettings(TELEGRAM_ID))
    }

    private fun packList(vararg packs: InternalTelegramNotificationPackSummaryResponse): InternalTelegramNotificationPackListResponse =
        InternalTelegramNotificationPackListResponse(packs = packs.toList())

    private fun selection(
        personalizationId: UUID,
        name: String,
    ): InternalTelegramNotificationPackSelectionResponse =
        InternalTelegramNotificationPackSelectionResponse(
            personalizationId = personalizationId,
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

    private companion object {
        const val TELEGRAM_ID = 931458258L
    }
}
