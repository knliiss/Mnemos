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
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class AccountMenuMessageRendererTest {
    @Test
    fun `account dashboard separates account coin from user prime and keeps actions readable`() {
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
        val dailyAction = message.actionRows[0].single()
        val primeAction = message.actionRows[1].single()
        val switchAction =
            callbacks.single { action ->
                action.data == AccountCallbackData.switch(switchableAccount.accountId)
            }

        assertTrue(message.text.contains("<b>Активный Cristalix-аккаунт</b>"))
        assertTrue(message.text.contains("└ <code>knaliz</code>"))
        assertTrue(message.text.contains("<b>COIN активного аккаунта</b>"))
        assertTrue(message.text.contains("└ <b>125</b> COIN"))
        assertTrue(message.text.contains("<b>PRIME пользователя Mnemos</b>"))
        assertTrue(message.text.contains("└ <b>3</b> PRIME"))
        assertTrue(message.text.contains("PRIME общий для вас и не меняется"))
        assertTrue(message.text.contains("<code>old</code> · владелец · 1/5 · деактивирован"))
        assertTrue(message.text.contains("Активный аккаунт переключён"))
        assertTrue(message.actionRows.all { row -> row.size == 1 })
        assertEquals(TelegramMenuScreen.DAILY.callbackData, (dailyAction as OutboundTelegramMessageAction.Callback).data)
        assertEquals(TelegramMenuScreen.PRIME.callbackData, (primeAction as OutboundTelegramMessageAction.Callback).data)
        assertTrue(dailyAction.label.contains("Daily аккаунта"))
        assertTrue(primeAction.label.contains("PRIME пользователя"))
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, switchAction.style)
        assertTrue(callbacks.none { action -> action.data == AccountCallbackData.switch(deactivatedAccount.accountId) })
        assertTrue(callbacks.any { action -> action.data == AccountCallbackData.CREATE })
        assertTrue(callbacks.any { action -> action.data == AccountCallbackData.JOIN })
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

        assertTrue(message.text.contains("Отправьте <b>ник Cristalix</b> обычным сообщением."))
        assertTrue(message.text.contains("Ник Cristalix имеет неверный формат"))
        assertTrue(message.text.contains("4–20 букв, цифр или <code>_</code>"))
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
        assertTrue(message.text.contains("<b>COIN активного аккаунта</b>"))
        assertTrue(message.text.contains("<b>PRIME пользователя Mnemos</b>"))
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
