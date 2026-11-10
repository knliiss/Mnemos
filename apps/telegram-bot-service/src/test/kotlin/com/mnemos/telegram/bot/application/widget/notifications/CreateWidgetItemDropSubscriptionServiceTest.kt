package com.mnemos.telegram.bot.application.widget.notifications

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionCreationResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionCreationGateway
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.notifications.port.out.NotificationWidgetPublisher
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class CreateWidgetItemDropSubscriptionServiceTest {
    private val creation = mock<NotificationSubscriptionCreationGateway>()
    private val snapshots = mock<NotificationWidgetSnapshotQuery>()
    private val publisher = mock<NotificationWidgetPublisher>()
    private val prompts = mock<WidgetTextInputSessionRepository>()
    private val service = CreateWidgetItemDropSubscriptionService(creation, snapshots, publisher, prompts)
    private val accountId = UUID.randomUUID()
    private val widget = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.NOTIFICATIONS, accountId)

    @Test
    fun bothFiltersReuseExistingValidationAndCreationGateway() {
        val prompt = WidgetTextInputSession(11L, 41, 8, accountId, WidgetTextInputPurpose.ITEM_DROP_BOTH)
        whenever(snapshots.load(11L, 0)).thenReturn(available())
        whenever(prompts.claimMutation(11L, 41)).thenReturn(true)
        whenever(creation.createItemDrop(11L, NotificationItemDropSubscriptionDraft("Kirito", "Клинок")))
            .thenReturn(NotificationSubscriptionCreationResult.APPLIED)
        whenever(publisher.update(eq(11L), eq(8), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.create(prompt, widget, "  Kirito ; Клинок  "))
        verify(creation).createItemDrop(11L, NotificationItemDropSubscriptionDraft("Kirito", "Клинок"))
    }

    @Test
    fun duplicateReplyCannotCallBackendAgain() {
        val prompt = WidgetTextInputSession(11L, 41, 8, accountId, WidgetTextInputPurpose.ITEM_DROP_PLAYER)
        whenever(snapshots.load(11L, 0)).thenReturn(available())
        whenever(prompts.claimMutation(11L, 41)).thenReturn(false)
        whenever(publisher.update(eq(11L), eq(8), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.create(prompt, widget, "Kirito"))
        verifyNoInteractions(creation)
    }

    @Test
    fun invalidDataDoesNotClaimInputOrCallCreationGateway() {
        val prompt = WidgetTextInputSession(11L, 41, 8, accountId, WidgetTextInputPurpose.ITEM_DROP_BOTH)
        whenever(snapshots.load(11L, 0)).thenReturn(available())
        whenever(publisher.update(eq(11L), eq(8), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.create(prompt, widget, "Missing delimiter"))
        verifyNoInteractions(creation, prompts)
    }

    @Test
    fun changedAccountContextPreventsDomainMutation() {
        val prompt = WidgetTextInputSession(11L, 41, 8, accountId, WidgetTextInputPurpose.ITEM_DROP_ITEM)
        whenever(snapshots.load(11L, 0)).thenReturn(
            NotificationWidgetSnapshotResult.Available(view(UUID.randomUUID())),
        )

        assertEquals(
            TelegramDeliveryResult.PermanentFailure("notification-account-context-changed"),
            service.create(prompt, widget, "Клинок"),
        )
        verifyNoInteractions(creation, prompts)
    }

    private fun available() = NotificationWidgetSnapshotResult.Available(view(accountId))

    private fun view(id: UUID) =
        NotificationWidgetView(id, 0, 1, 0, true, emptyList(), 0, 0, 0, false)
}
