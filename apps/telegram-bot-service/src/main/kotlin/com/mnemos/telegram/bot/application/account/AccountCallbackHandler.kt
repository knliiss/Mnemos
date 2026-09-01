package com.mnemos.telegram.bot.application.account

import com.mnemos.telegram.bot.application.account.model.TelegramAccountSwitchResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
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
        val session = currentAccountSession(context) ?: return TelegramDeliveryResult.Delivered

        if (isWaitingForAccountInput(session.pendingInput)) {
            return if (context.data == AccountCallbackData.CANCEL_INPUT) {
                cancelInput(context)
            } else {
                TelegramDeliveryResult.Delivered
            }
        }

        return when (context.data) {
            AccountCallbackData.REFRESH -> update(context, AccountMenuContext.clear())
            AccountCallbackData.CREATE -> requestInput(context, TelegramPendingInput.ACCOUNT_CREATE)
            AccountCallbackData.JOIN -> requestInput(context, TelegramPendingInput.ACCOUNT_JOIN)
            AccountCallbackData.CANCEL_INPUT -> TelegramDeliveryResult.Delivered
            else -> switchAccount(context)
        }
    }

    private fun requestInput(
        context: TelegramCallbackContext,
        pendingInput: TelegramPendingInput,
    ): TelegramDeliveryResult =
        menuService.requestTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.ACCOUNT,
            pendingInput = pendingInput,
            updateContext = { AccountMenuContext.clear() },
        )

    private fun cancelInput(context: TelegramCallbackContext): TelegramDeliveryResult =
        menuService.cancelTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.ACCOUNT,
            updateContext = { AccountMenuContext.clear() },
        )

    private fun switchAccount(context: TelegramCallbackContext): TelegramDeliveryResult {
        val accountId =
            AccountCallbackData.switchedAccountId(context.data)
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

    private fun currentAccountSession(context: TelegramCallbackContext): TelegramMenuSession? {
        val session = sessionRepository.find(context.telegramId) ?: return null

        if (session.currentScreen != TelegramMenuScreen.ACCOUNT || session.menuMessageId != context.messageId) {
            return null
        }

        return session
    }

    private fun isWaitingForAccountInput(pendingInput: TelegramPendingInput?): Boolean =
        pendingInput == TelegramPendingInput.ACCOUNT_CREATE ||
            pendingInput == TelegramPendingInput.ACCOUNT_JOIN
}
