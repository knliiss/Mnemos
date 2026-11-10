package com.mnemos.telegram.bot.application.widget.notifications

import com.mnemos.telegram.bot.application.notification.model.NotificationBoosterType
import com.mnemos.telegram.bot.application.notification.model.NotificationRaidSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscription
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionCreationResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionPage
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionResult
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionType
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionCreationGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class UpdateWidgetSubscriptionServiceTest {
    private val current = mock<NotificationSubscriptionGateway>()
    private val create = mock<NotificationSubscriptionCreationGateway>()
    private val service = UpdateWidgetSubscriptionService(current, create)

    @Test
    fun enablingAlreadySubscribedBoosterDoesNotRepeatCreation() {
        val id = UUID.randomUUID()
        whenever(current.list(11, 0, 1)).thenReturn(state(mapOf(NotificationBoosterType.LUCK to id)))

        assertEquals(
            WidgetSubscriptionUpdateResult.APPLIED,
            service.set(11, WidgetSubscriptionKind.BOOSTER, "LUCK", true),
        )
        verifyNoInteractions(create)
    }

    @Test
    fun enablingMissingBoosterCallsExistingCreationGateway() {
        whenever(current.list(11, 0, 1)).thenReturn(state())
        whenever(create.createBooster(11, NotificationBoosterType.POWER))
            .thenReturn(NotificationSubscriptionCreationResult.APPLIED)

        assertEquals(
            WidgetSubscriptionUpdateResult.APPLIED,
            service.set(11, WidgetSubscriptionKind.BOOSTER, "POWER", true),
        )
        verify(create).createBooster(11, NotificationBoosterType.POWER)
    }

    @Test
    fun disablingSubscribedBoosterUsesServerReturnedId() {
        val id = UUID.randomUUID()
        whenever(current.list(11, 0, 1)).thenReturn(state(mapOf(NotificationBoosterType.LUCK to id)))
        whenever(current.delete(11, id)).thenReturn(true)

        assertEquals(
            WidgetSubscriptionUpdateResult.APPLIED,
            service.set(11, WidgetSubscriptionKind.BOOSTER, "LUCK", false),
        )
        verify(current).delete(11, id)
        verifyNoInteractions(create)
    }

    @Test
    fun allRaidLocationsUseExistingBackendAllConvention() {
        whenever(current.list(11, 0, 1)).thenReturn(state())
        whenever(create.updateRaid(11, NotificationRaidSubscriptionDraft(emptySet())))
            .thenReturn(NotificationSubscriptionCreationResult.APPLIED)

        assertEquals(
            WidgetSubscriptionUpdateResult.APPLIED,
            service.setRaidLocations(11, (1..28).toSet()),
        )
        verify(create).updateRaid(11, NotificationRaidSubscriptionDraft(emptySet()))
    }

    @Test
    fun emptyRaidSelectionDisablesExistingRaid() {
        val id = UUID.randomUUID()
        val raid = NotificationSubscription(
            id, NotificationSubscriptionType.RAID, Instant.EPOCH,
            null, null, null, null, null, setOf(1, 7),
        )
        whenever(current.list(11, 0, 1)).thenReturn(state(raid = raid))
        whenever(current.delete(11, id)).thenReturn(true)

        assertEquals(WidgetSubscriptionUpdateResult.APPLIED, service.setRaidLocations(11, emptySet()))
        verify(current).delete(11, id)
    }

    @Test
    fun invalidRaidLocationsNeverCallMutationGateway() {
        assertEquals(WidgetSubscriptionUpdateResult.INVALID, service.setRaidLocations(11, setOf(29)))
        verifyNoInteractions(current, create)
    }

    private fun state(
        boosters: Map<NotificationBoosterType, UUID> = emptyMap(),
        raid: NotificationSubscription? = null,
    ) = NotificationSubscriptionResult.Available(
        page = NotificationSubscriptionPage(emptyList(), 0, 0, 0, false),
        boosterSubscriptions = boosters,
        raidSubscription = raid,
    )
}
