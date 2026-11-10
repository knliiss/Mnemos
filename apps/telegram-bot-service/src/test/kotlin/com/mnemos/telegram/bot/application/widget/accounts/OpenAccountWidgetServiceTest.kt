package com.mnemos.telegram.bot.application.widget.accounts

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalance
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountStatus
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSummary
import com.mnemos.telegram.bot.application.account.port.out.AccountBalanceGateway
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.accounts.port.out.AccountWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class OpenAccountWidgetServiceTest {
    private val accounts = mock<AccountGateway>()
    private val balances = mock<AccountBalanceGateway>()
    private val publisher = mock<AccountWidgetPublisher>()

    @Test
    fun disabledWidgetDoesNotCallDomainGateways() {
        val service = service(false)

        assertEquals(
            TelegramDeliveryResult.PermanentFailure("widgets-disabled"),
            service.execute(12L),
        )
        verifyNoInteractions(accounts, balances, publisher)
    }

    @Test
    fun registrationRequiredRendersEmptyAccountView() {
        whenever(accounts.getContext(12L)).thenReturn(TelegramAccountContextResult.RegistrationRequired)
        whenever(publisher.publish(eq(12L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service().execute(12L))
        val captured = argumentCaptor<AccountWidgetView>()
        verify(publisher).publish(eq(12L), captured.capture())
        assertEquals(0, captured.firstValue.totalAccounts)
        assertEquals(null, captured.firstValue.activeAccountId)
        verifyNoInteractions(balances)
    }

    @Test
    fun activeAccountHasBalanceAndOnlyFourVisibleItemsPerPage() {
        val ids = List(6) { UUID.randomUUID() }
        whenever(accounts.getContext(12L)).thenReturn(
            TelegramAccountContextResult.Available(
                TelegramAccountContext(
                    activeAccountId = ids[0],
                    accounts = ids.mapIndexed { index, id ->
                        TelegramAccountSummary(
                            accountId = id,
                            cristalixUuid = UUID.randomUUID(),
                            cristalixNickname = "Account-$index",
                            memberCount = index,
                            maxMembers = 10,
                            owner = index == 0,
                            active = index == 0,
                            status = if (index == 5) TelegramAccountStatus.DEACTIVATED else TelegramAccountStatus.ACTIVE,
                        )
                    },
                ),
            ),
        )
        whenever(balances.getBalances(12L)).thenReturn(
            TelegramAccountBalanceResult.Available(TelegramAccountBalance(coin = 333, prime = 11)),
        )
        whenever(publisher.publish(eq(12L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service().execute(12L, 1))

        val captured = argumentCaptor<AccountWidgetView>()
        verify(publisher).publish(eq(12L), captured.capture())
        assertEquals(6, captured.firstValue.totalAccounts)
        assertEquals(1, captured.firstValue.page)
        assertEquals(2, captured.firstValue.accounts.size)
        assertEquals(ids[0], captured.firstValue.activeAccountId)
        assertEquals(333L, captured.firstValue.coin)
        assertEquals(false, captured.firstValue.accounts[1].enabled)
    }

    private fun service(enabled: Boolean = true) =
        OpenAccountWidgetService(TelegramWidgetProperties(enabled = enabled), accounts, balances, publisher)
}
