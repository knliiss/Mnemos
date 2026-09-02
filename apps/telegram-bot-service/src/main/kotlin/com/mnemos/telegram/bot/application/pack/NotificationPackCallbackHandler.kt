package com.mnemos.telegram.bot.application.pack

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.pack.model.NotificationPackApplyResult
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuFeedback
import com.mnemos.telegram.bot.application.pack.model.NotificationPackPurchaseResult
import com.mnemos.telegram.bot.application.pack.port.out.NotificationPackGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class NotificationPackCallbackHandler(
    private val menuService: TelegramMenuService,
    private val gateway: NotificationPackGateway,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = NotificationPackCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        currentSession(context) ?: return TelegramDeliveryResult.Delivered

        if (context.data == NotificationPackCallbackData.STORE) {
            return updateContext(context) { current ->
                NotificationPackMenuContext.store(current)
            }
        }

        if (context.data == NotificationPackCallbackData.OWNED) {
            return updateContext(context, NotificationPackMenuContext::owned)
        }

        if (context.data == NotificationPackCallbackData.REFRESH) {
            return updateContext(context, NotificationPackMenuContext::clearFeedback)
        }

        NotificationPackCallbackData.selectedPage(context.data)?.let { page ->
            return updateContext(context) { current ->
                NotificationPackMenuContext.store(current, page)
            }
        }

        NotificationPackCallbackData.selectedItem(context.data)?.let { personalizationId ->
            return updateContext(context) { current ->
                NotificationPackMenuContext.detail(current, personalizationId)
            }
        }

        NotificationPackCallbackData.purchaseTarget(context.data)?.let { personalizationId ->
            return purchase(
                context = context,
                personalizationId = personalizationId,
            )
        }

        NotificationPackCallbackData.applyTarget(context.data)?.let { personalizationId ->
            return apply(
                context = context,
                personalizationId = personalizationId,
            )
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun purchase(
        context: TelegramCallbackContext,
        personalizationId: UUID,
    ): TelegramDeliveryResult {
        val session = currentSession(context) ?: return TelegramDeliveryResult.Delivered
        val selectedId = NotificationPackMenuContext.selectedPersonalizationId(session.screenContext)

        if (selectedId != personalizationId) {
            return TelegramDeliveryResult.Delivered
        }

        val feedback =
            when (
                gateway.purchase(
                    telegramId = context.telegramId,
                    personalizationId = personalizationId,
                    idempotencyKey = UUID.randomUUID(),
                )
            ) {
                is NotificationPackPurchaseResult.Accepted -> NotificationPackMenuFeedback.PURCHASE_ACCEPTED
                NotificationPackPurchaseResult.Refresh -> NotificationPackMenuFeedback.PURCHASE_REFRESH
                NotificationPackPurchaseResult.Unavailable -> NotificationPackMenuFeedback.PURCHASE_UNAVAILABLE
            }

        return updateContext(context) { current ->
            NotificationPackMenuContext.withFeedback(current, feedback)
        }
    }

    private fun apply(
        context: TelegramCallbackContext,
        personalizationId: UUID,
    ): TelegramDeliveryResult {
        val feedback =
            when (
                gateway.apply(
                    telegramId = context.telegramId,
                    personalizationId = personalizationId,
                )
            ) {
                NotificationPackApplyResult.Applied -> NotificationPackMenuFeedback.APPLIED
                NotificationPackApplyResult.Refresh -> NotificationPackMenuFeedback.APPLY_REFRESH
                NotificationPackApplyResult.Unavailable -> NotificationPackMenuFeedback.APPLY_UNAVAILABLE
            }

        return updateContext(context) { current ->
            NotificationPackMenuContext.withFeedback(current, feedback)
        }
    }

    private fun updateContext(
        context: TelegramCallbackContext,
        update: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.PACKS,
            update = update,
        )

    private fun currentSession(context: TelegramCallbackContext): TelegramMenuSession? {
        val session = sessionRepository.find(context.telegramId) ?: return null

        if (session.currentScreen != TelegramMenuScreen.PACKS) {
            return null
        }

        if (session.menuMessageId != context.messageId) {
            return null
        }

        return session
    }
}
