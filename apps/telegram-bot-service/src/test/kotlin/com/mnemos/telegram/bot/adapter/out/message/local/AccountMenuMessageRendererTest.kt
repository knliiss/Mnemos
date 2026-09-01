package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.account.AccountCallbackData
import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedback
import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedbackType
import com.mnemos.telegram.bot.application.account.model.AccountMenuScreenData
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalance
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountInputMode
import com.mnemos.telegram.bot.application.account.model.TelegramAccountStatus
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSummary
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class AccountMenuMessageRendererTest {
    @Test
    fun `account dashboard groups daily prime and keeps account controls together`() {
        val activeAccount = account("knaliz", active = true, status = TelegramAccountStatus.ACTIVE)
        val switchableAccount = account("alt", active = false, status = TelegramAccountStatus.ACTIVE)
        val deactivatedAccount = account("old", active = false, status = TelegramAccountStatus.DEACTIVATED)
        val data =
            TelegramMenuScreenData.Account(
                AccountMenuScreenData(
                    result =
                        TelegramAccountContextResult.Available(
                            TelegramAccountContext(
                                activeAccountId = activeAccount.accountId,
                                accounts = listOf(activeAccount, switchableAccount, deactivatedAccount),
                            ),
                        ),
                    balance =
                        TelegramAccountBalanceResult.Available(
                            TelegramAccountBalance(
                                coin = 125,
                                prime = 3,
                            ),
                        ),
                    feedback = AccountMenuFeedback(AccountMenuFeedbackType.SWITCHED),
                ),
            )

        val message = AccountMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val quickActions = message.actionRows[0]
        val dailyLabel = quickActions[0].label
        val primeLabel = quickActions[1].label

        assertTrue(message.text.contains("<b>Активный аккаунт</b>"))
        assertTrue(message.text.contains("└ <code>knaliz</code>"))
        assertTrue(message.text.contains("├ COIN: <b>125</b>"))
        assertTrue(message.text.contains("└ PRIME: <b>3</b> ⭐"))
        assertTrue(message.text.contains("<code>old</code> · владелец · 1/5 · деактивирован"))
        assertTrue(message.text.contains("Активный аккаунт переключён"))
        assertEquals(2, quickActions.size)
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.DAILY.callbackData })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.PRIME.callbackData })
        assertTrue(callbacks.any { action -> action.data == AccountCallbackData.switch(switchableAccount.accountId) })
        assertTrue(callbacks.none { action -> action.data == AccountCallbackData.switch(deactivatedAccount.accountId) })
        assertTrue(callbacks.any { action -> action.data == AccountCallbackData.CREATE })
        assertTrue(callbacks.any { action -> action.data == AccountCallbackData.JOIN })
        assertTrue(dailyLabel.contains("Daily"))
        assertTrue(primeLabel.contains("PRIME"))
    }

    @Test
    fun `registration required screen offers chat native account setup`() {
        val data =
            TelegramMenuScreenData.Account(
                AccountMenuScreenData(
                    result = TelegramAccountContextResult.RegistrationRequired,
                    balance = TelegramAccountBalanceResult.NotRequired,
                    feedback = null,
                ),
            )

        val message = AccountMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val miniAppActions = message.actions.filterIsInstance<OutboundTelegramMessageAction.OpenMiniApp>()

        assertTrue(message.text.contains("Не подключён"))
        assertTrue(callbacks.any { action -> action.data == AccountCallbackData.CREATE })
        assertTrue(callbacks.any { action -> action.data == AccountCallbackData.JOIN })
        assertTrue(miniAppActions.isEmpty())
    }

    @Test
    fun `account input mode keeps user inside chat flow`() {
        val data =
            TelegramMenuScreenData.Account(
                AccountMenuScreenData(
                    result = TelegramAccountContextResult.RegistrationRequired,
                    balance = TelegramAccountBalanceResult.NotRequired,
                    feedback = AccountMenuFeedback(AccountMenuFeedbackType.CREATE_INVALID),
                    inputMode = TelegramAccountInputMode.CREATE,
                ),
            )

        val message = AccountMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("ник Cristalix"))
        assertTrue(message.text.contains("отправьте его ещё раз"))
        assertTrue(callbacks.any { action -> action.data == AccountCallbackData.CANCEL_INPUT })
        assertFalse(callbacks.any { action -> action.data == AccountCallbackData.JOIN })
    }

    @Test
    fun `wallet outage keeps account dashboard structure`() {
        val activeAccount = account("knaliz", active = true, status = TelegramAccountStatus.ACTIVE)
        val data =
            TelegramMenuScreenData.Account(
                AccountMenuScreenData(
                    result =
                        TelegramAccountContextResult.Available(
                            TelegramAccountContext(
                                activeAccountId = activeAccount.accountId,
                                accounts = listOf(activeAccount),
                            ),
                        ),
                    balance = TelegramAccountBalanceResult.Unavailable,
                    feedback = null,
                ),
            )

        val message = AccountMenuMessageRenderer.render(data)

        assertTrue(message.text.contains("└ <code>knaliz</code>"))
        assertTrue(message.text.contains("Временно недоступен"))
    }

    @Test
    fun `unavailable account screen keeps refresh action`() {
        val data =
            TelegramMenuScreenData.Account(
                AccountMenuScreenData(
                    result = TelegramAccountContextResult.Unavailable,
                    balance = TelegramAccountBalanceResult.NotRequired,
                    feedback = null,
                ),
            )

        val message = AccountMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Не удалось получить данные аккаунта"))
        assertTrue(callbacks.any { action -> action.data == AccountCallbackData.REFRESH })
    }

    private fun account(
        nickname: String,
        active: Boolean,
        status: TelegramAccountStatus,
    ): TelegramAccountSummary =
        TelegramAccountSummary(
            accountId = UUID.randomUUID(),
            cristalixUuid = UUID.randomUUID(),
            cristalixNickname = nickname,
            memberCount = 1,
            maxMembers = 5,
            owner = true,
            active = active,
            status = status,
        )
}
