package com.mnemos.telegram.bot.application.account

import com.mnemos.telegram.bot.application.account.model.TelegramAccountSwitchResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class AccountCallbackHandler(
    private val gateway: AccountGateway,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = AccountCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        if (!isCurrentAccountMenu(context)) {
            return TelegramDeliveryResult.Delivered
        }

        if (context.data == AccountCallbackData.REFRESH) {
            return update(context, AccountMenuContext.clear())
        }

        val accountId = AccountCallbackData.switchedAccountId(context.data)
            ?: return TelegramDeliveryResult.Delivered

        return when (gateway.changeActiveAccount(context.telegramId, accountId)) {
            TelegramAccountSwitchResult.Switched -> update(context, AccountMenuContext.switched())
            TelegramAccountSwitchResult.RegistrationRequired,
            TelegramAccountSwitchResult.Refresh,
            -> update(context, AccountMenuContext.clear())

            TelegramAccountSwitchResult.Unavailable -> update(context, AccountMenuContext.unavailable())
        }
    }

    private fun update(
        context: TelegramCallbackContext,
        screenContext: Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.ACCOUNT,
            update = { screenContext },
        )

    private fun isCurrentAccountMenu(context: TelegramCallbackContext): Boolean {
        val session = sessionRepository.find(context.telegramId) ?: return false

        return session.currentScreen == TelegramMenuScreen.ACCOUNT && session.menuMessageId == context.messageId
    }
}
