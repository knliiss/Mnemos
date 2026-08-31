package com.mnemos.telegram.bot.application.prime

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseCreationResult
import com.mnemos.telegram.bot.application.prime.model.PrimePurchaseNotice
import com.mnemos.telegram.bot.application.prime.port.out.PrimePurchaseGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class PrimePurchaseCallbackHandler(
    private val gateway: PrimePurchaseGateway,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = PrimePurchaseCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        if (!isCurrentPrimeMenu(context)) {
            return TelegramDeliveryResult.Delivered
        }

        if (context.data == PrimePurchaseCallbackData.REFRESH) {
            return update(context, PrimeMenuContext.clear())
        }

        val packageId =
            PrimePurchaseCallbackData.packageId(context.data)
                ?: return TelegramDeliveryResult.Delivered

        return when (
            val result =
                gateway.createPurchase(
                    telegramId = context.telegramId,
                    packageId = packageId,
                    requestId = context.callbackQueryId,
                )
        ) {
            is PrimePurchaseCreationResult.Ready -> update(context, PrimeMenuContext.invoice(result.invoice))
            PrimePurchaseCreationResult.RegistrationRequired ->
                update(context, PrimeMenuContext.notice(PrimePurchaseNotice.REGISTRATION_REQUIRED))

            PrimePurchaseCreationResult.Refresh -> update(context, PrimeMenuContext.clear())
            PrimePurchaseCreationResult.Unavailable ->
                update(context, PrimeMenuContext.notice(PrimePurchaseNotice.UNAVAILABLE))
        }
    }

    private fun update(
        context: TelegramCallbackContext,
        screenContext: Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.PRIME,
            update = { screenContext },
        )

    private fun isCurrentPrimeMenu(context: TelegramCallbackContext): Boolean {
        val session = sessionRepository.find(context.telegramId) ?: return false

        return session.currentScreen == TelegramMenuScreen.PRIME && session.menuMessageId == context.messageId
    }
}
