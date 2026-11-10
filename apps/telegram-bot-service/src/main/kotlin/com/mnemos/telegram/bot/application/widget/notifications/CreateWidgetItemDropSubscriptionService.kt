package com.mnemos.telegram.bot.application.widget.notifications

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.NotificationItemDropInputValidator
import com.mnemos.telegram.bot.application.notification.model.NotificationItemDropSubscriptionDraft
import com.mnemos.telegram.bot.application.notification.model.NotificationSubscriptionCreationResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionCreationGateway
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.notifications.port.out.NotificationWidgetPublisher
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class CreateWidgetItemDropSubscriptionService(
    private val creation: NotificationSubscriptionCreationGateway,
    private val snapshots: NotificationWidgetSnapshotQuery,
    private val publisher: NotificationWidgetPublisher,
    private val prompts: WidgetTextInputSessionRepository,
) : CreateWidgetItemDropSubscriptionUseCase {
    override fun create(
        prompt: WidgetTextInputSession,
        widget: TelegramWidgetSession,
        text: String,
    ): TelegramDeliveryResult {
        if (widget.screen != TelegramWidgetScreen.NOTIFICATIONS ||
            prompt.purpose !in DROP_PURPOSES ||
            prompt.telegramId != widget.telegramId || prompt.widgetMessageId != widget.messageId ||
            prompt.accountId != widget.accountId
        ) return TelegramDeliveryResult.PermanentFailure("invalid-notification-input-context")

        val current = snapshots.load(widget.telegramId, widget.page) as? NotificationWidgetSnapshotResult.Available
            ?: return TelegramDeliveryResult.Retryable
        if (current.view.accountId != widget.accountId) {
            return TelegramDeliveryResult.PermanentFailure("notification-account-context-changed")
        }

        val draft = parse(prompt.purpose, text)
        val claimed = draft != null && prompts.claimMutation(prompt.telegramId, prompt.promptMessageId)
        val message =
            when {
                draft == null -> "Невірні дані. Перевірте нікнейм або назву предмета"
                !claimed -> "Цей запит уже оброблено. Оновіть список підписок"
                else -> when (creation.createItemDrop(prompt.telegramId, draft)) {
                    NotificationSubscriptionCreationResult.APPLIED -> "Підписку на дроп додано"
                    NotificationSubscriptionCreationResult.USER_NOT_FOUND -> "Гравця не знайдено"
                    NotificationSubscriptionCreationResult.UNAVAILABLE -> "Стан невідомий. Перевірте список перед повторною спробою"
                }
            }

        val refreshed = snapshots.load(widget.telegramId, widget.page) as? NotificationWidgetSnapshotResult.Available
            ?: return TelegramDeliveryResult.Retryable
        if (refreshed.view.accountId != widget.accountId) {
            return TelegramDeliveryResult.PermanentFailure("notification-account-context-changed")
        }
        return publisher.update(widget.telegramId, widget.messageId, refreshed.view.copy(feedback = message))
    }

    private fun parse(purpose: WidgetTextInputPurpose, raw: String): NotificationItemDropSubscriptionDraft? =
        when (purpose) {
            WidgetTextInputPurpose.ITEM_DROP_PLAYER ->
                NotificationItemDropInputValidator.player(raw)?.let { NotificationItemDropSubscriptionDraft(it, null) }
            WidgetTextInputPurpose.ITEM_DROP_ITEM ->
                NotificationItemDropInputValidator.item(raw)?.let { NotificationItemDropSubscriptionDraft(null, it) }
            WidgetTextInputPurpose.ITEM_DROP_BOTH -> {
                val parts = raw.split(';', limit = 3)
                if (parts.size != 2) null
                else {
                    val player = NotificationItemDropInputValidator.player(parts[0])
                    val item = NotificationItemDropInputValidator.item(parts[1])
                    if (player == null || item == null) null else NotificationItemDropSubscriptionDraft(player, item)
                }
            }
            else -> null
        }

    private companion object {
        val DROP_PURPOSES = setOf(
            WidgetTextInputPurpose.ITEM_DROP_PLAYER,
            WidgetTextInputPurpose.ITEM_DROP_ITEM,
            WidgetTextInputPurpose.ITEM_DROP_BOTH,
        )
    }
}
