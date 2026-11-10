package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.account.model.TelegramAccountMutationResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.accounts.AccountWidgetSnapshotQuery
import com.mnemos.telegram.bot.application.widget.accounts.AccountWidgetSnapshotResult
import com.mnemos.telegram.bot.application.widget.accounts.AccountWidgetView
import com.mnemos.telegram.bot.application.widget.accounts.port.out.AccountWidgetPublisher
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class ApplyWidgetAccountInputServiceTest {
    private val accounts = mock<AccountGateway>()
    private val snapshots = mock<AccountWidgetSnapshotQuery>()
    private val publisher = mock<AccountWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val prompts = mock<WidgetTextInputSessionRepository>()
    private val service = ApplyWidgetAccountInputService(accounts, snapshots, publisher, sessions, prompts)
    private val accountId = UUID.randomUUID()
    private val widget = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ACCOUNTS, accountId)
    private val prompt = WidgetTextInputSession(11L, 41, 8, accountId, WidgetTextInputPurpose.ACCOUNT_CREATE)

    @Test
    fun contextChangePreventsAccountCreation() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountWidgetSnapshotResult.Available(view(null)))

        assertEquals(
            TelegramDeliveryResult.PermanentFailure("account-context-changed"),
            service.apply(prompt, widget, "Kirito"),
        )
        verifyNoInteractions(accounts, publisher, sessions, prompts)
    }

    @Test
    fun duplicateMutationClaimCannotRecreateAccount() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountWidgetSnapshotResult.Available(view(accountId)))
        whenever(prompts.claimMutation(11L, 41)).thenReturn(false)

        assertEquals(TelegramDeliveryResult.Delivered, service.apply(prompt, widget, "Kirito"))
        verifyNoInteractions(accounts, sessions, publisher)
    }

    @Test
    fun successfulCreationRefreshesOriginalWidget() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountWidgetSnapshotResult.Available(view(accountId)))
        whenever(prompts.claimMutation(11L, 41)).thenReturn(true)
        whenever(accounts.createAccount(11L, "Kirito")).thenReturn(TelegramAccountMutationResult.Updated)

        assertEquals(TelegramDeliveryResult.Delivered, service.apply(prompt, widget, "Kirito"))
        verify(accounts).createAccount(11L, "Kirito")
        verify(sessions).save(widget)
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun invalidInputNeverCallsAccountMutationGateway() {
        whenever(snapshots.load(11L, 0)).thenReturn(AccountWidgetSnapshotResult.Available(view(accountId)))
        whenever(prompts.claimMutation(11L, 41)).thenReturn(true)

        assertEquals(TelegramDeliveryResult.Delivered, service.apply(prompt, widget, " "))
        verifyNoInteractions(accounts)
        verify(publisher).update(eq(11L), eq(8), any())
    }

    private fun view(id: UUID?) =
        AccountWidgetView(id, emptyList(), 0, 0, 1, null, null)
}
