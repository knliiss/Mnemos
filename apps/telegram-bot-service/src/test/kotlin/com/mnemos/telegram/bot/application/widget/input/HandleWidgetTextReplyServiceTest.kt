package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.online.SaoOnlineMenuContext
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.notifications.CreateWidgetItemDropSubscriptionUseCase
import com.mnemos.telegram.bot.application.widget.management.CreateWidgetAccountRoleUseCase
import com.mnemos.telegram.bot.application.widget.management.EditWidgetAccountRoleUseCase
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

class HandleWidgetTextReplyServiceTest {
    private val pending = mock<WidgetTextInputSessionRepository>()
    private val widgets = mock<TelegramWidgetSessionRepository>()
    private val menu = mock<TelegramMenuService>()
    private val search = mock<ApplyWidgetTextSearchUseCase>()
    private val accountInput = mock<ApplyWidgetAccountInputUseCase>()
    private val itemDrop = mock<CreateWidgetItemDropSubscriptionUseCase>()
    private val roleInput = mock<CreateWidgetAccountRoleUseCase>()
    private val editRole = mock<EditWidgetAccountRoleUseCase>()
    private val handler = HandleWidgetTextReplyService(
        TelegramWidgetProperties(enabled = true), pending, widgets, menu, search, accountInput, itemDrop, roleInput, editRole,
    )

    @Test
    fun unrelatedReplyDoesNotInterceptLegacyInput() {
        assertNull(handler.handle(11L, 500, "Some message"))
        verifyNoInteractions(widgets, menu)
    }

    @Test
    fun eventReplyUpdatesItsOwnWidgetWithoutOpeningTextMenu() {
        givenPending(WidgetTextInputPurpose.EVENT_SEARCH, TelegramWidgetScreen.EVENTS)
        whenever(search.apply(TelegramWidgetSession(11L, 8, TelegramWidgetScreen.EVENTS, null), "Kirito"))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, "  Kirito  "))
        verify(search).apply(TelegramWidgetSession(11L, 8, TelegramWidgetScreen.EVENTS, null), "Kirito")
        verifyNoInteractions(menu)
        verify(pending).delete(11L, 41)
    }

    @Test
    fun onlineReplyUpdatesTheOnlineWidget() {
        givenPending(WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH, TelegramWidgetScreen.ONLINE)
        whenever(search.apply(TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ONLINE, null), "Kirito"))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, "Kirito"))
        verify(search).apply(TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ONLINE, null), "Kirito")
        verifyNoInteractions(menu)
        verify(pending).delete(11L, 41)
    }

    @Test
    fun profileSearchReplyUpdatesItsOriginalWidget() {
        givenPending(WidgetTextInputPurpose.PUBLIC_PROFILE_SEARCH, TelegramWidgetScreen.PROFILES)
        val widget = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.PROFILES, null)
        whenever(search.apply(widget, "Kirito")).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, " Kirito "))
        verify(search).apply(widget, "Kirito")
        verifyNoInteractions(menu)
    }

    @Test
    fun roleEditReplyDelegatesToRoleIdBoundUseCase() {
        val roleId = java.util.UUID.randomUUID()
        val prompt = WidgetTextInputSession(11L, 41, 8, null, WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT, roleId)
        val widget = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ACCOUNT_MANAGEMENT, null)
        whenever(pending.find(11L, 41)).thenReturn(prompt)
        whenever(widgets.find(11L, 8)).thenReturn(widget)
        whenever(editRole.edit(prompt, widget, "Officer #ABCDEF")).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, "Officer #ABCDEF"))
        verify(editRole).edit(prompt, widget, "Officer #ABCDEF")
        verifyNoInteractions(roleInput, accountInput, itemDrop)
        verify(pending).delete(11L, 41)
    }

    @Test
    fun roleCreationReplyUsesItsOriginalManagementWidget() {
        givenPending(WidgetTextInputPurpose.ACCOUNT_ROLE_CREATE, TelegramWidgetScreen.ACCOUNT_MANAGEMENT)
        val prompt = WidgetTextInputSession(11L, 41, 8, null, WidgetTextInputPurpose.ACCOUNT_ROLE_CREATE)
        val widget = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ACCOUNT_MANAGEMENT, null)
        whenever(roleInput.create(prompt, widget, "Officer")).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, " Officer "))
        verify(roleInput).create(prompt, widget, "Officer")
        verifyNoInteractions(menu, accountInput, itemDrop)
        verify(pending).delete(11L, 41)
    }

    @Test
    fun itemDropReplyIsBoundToNotificationWidget() {
        givenPending(WidgetTextInputPurpose.ITEM_DROP_BOTH, TelegramWidgetScreen.NOTIFICATIONS)
        val prompt = WidgetTextInputSession(11L, 41, 8, null, WidgetTextInputPurpose.ITEM_DROP_BOTH)
        val widget = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.NOTIFICATIONS, null)
        whenever(itemDrop.create(prompt, widget, "Kirito;Клинок"))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, "Kirito;Клинок"))
        verify(itemDrop).create(prompt, widget, "Kirito;Клинок")
        verifyNoInteractions(menu, search, accountInput)
        verify(pending).delete(11L, 41)
    }

    @Test
    fun accountJoinReplyIsRoutedToTypedApplicationUseCase() {
        givenPending(WidgetTextInputPurpose.ACCOUNT_JOIN, TelegramWidgetScreen.ACCOUNTS)
        val prompt = WidgetTextInputSession(11L, 41, 8, null, WidgetTextInputPurpose.ACCOUNT_JOIN)
        val widget = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ACCOUNTS, null)
        whenever(accountInput.apply(prompt, widget, "invite42")).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, "invite42"))
        verify(accountInput).apply(prompt, widget, "invite42")
        verifyNoInteractions(menu, search)
        verify(pending).delete(11L, 41)
    }

    @Test
    fun cancellingInputDoesNotOpenLegacyMenu() {
        givenPending(WidgetTextInputPurpose.ACCOUNT_CREATE, TelegramWidgetScreen.ACCOUNTS)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, "скасувати"))
        verifyNoInteractions(menu, search, accountInput)
        verify(pending).delete(11L, 41)
    }

    @Test
    fun retryableDeliveryPreservesPromptForTheNextUpdateAttempt() {
        givenPending(WidgetTextInputPurpose.EVENT_SEARCH, TelegramWidgetScreen.EVENTS)
        whenever(search.apply(TelegramWidgetSession(11L, 8, TelegramWidgetScreen.EVENTS, null), "test"))
            .thenReturn(TelegramDeliveryResult.Retryable)

        assertEquals(TelegramDeliveryResult.Retryable, handler.handle(11L, 41, "test"))
        verify(pending, org.mockito.kotlin.never()).delete(any(), any())
    }

    @Test
    fun missingWidgetSessionDoesNotExecuteTheOldSearch() {
        whenever(pending.find(11L, 41)).thenReturn(
            WidgetTextInputSession(11L, 41, 8, null, WidgetTextInputPurpose.EVENT_SEARCH),
        )
        whenever(menu.open(eq(11L), eq(TelegramMenuScreen.EVENTS), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(11L, 41, "private query"))
        verify(menu).open(11L, TelegramMenuScreen.EVENTS)
        verify(pending).delete(11L, 41)
    }

    private fun givenPending(purpose: WidgetTextInputPurpose, screen: TelegramWidgetScreen) {
        whenever(pending.find(11L, 41)).thenReturn(WidgetTextInputSession(11L, 41, 8, null, purpose))
        whenever(widgets.find(11L, 8)).thenReturn(TelegramWidgetSession(11L, 8, screen, null))
    }
}
