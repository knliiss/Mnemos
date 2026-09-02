package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.account.model.AccountMenuScreenData
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalance
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountStatus
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSummary
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class AccountMenuManagementNavigationTest {
    @Test
    fun `active account dashboard routes through management instead of direct invites`() {
        val accountId = UUID.randomUUID()
        val data =
            TelegramMenuScreenData.Account(
                AccountMenuScreenData(
                    result =
                        TelegramAccountContextResult.Available(
                            TelegramAccountContext(
                                activeAccountId = accountId,
                                accounts =
                                    listOf(
                                        TelegramAccountSummary(
                                            accountId = accountId,
                                            cristalixUuid = UUID.randomUUID(),
                                            cristalixNickname = "knaliz",
                                            memberCount = 2,
                                            maxMembers = 5,
                                            owner = true,
                                            active = true,
                                            status = TelegramAccountStatus.ACTIVE,
                                        ),
                                    ),
                            ),
                        ),
                    balance =
                        TelegramAccountBalanceResult.Available(
                            TelegramAccountBalance(
                                coin = 10,
                                prime = 2,
                            ),
                        ),
                    feedback = null,
                ),
            )

        val message = AccountMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.ACCOUNT_MANAGEMENT.callbackData })
        assertFalse(callbacks.any { action -> action.data == TelegramMenuScreen.ACCOUNT_INVITES.callbackData })
    }
}
