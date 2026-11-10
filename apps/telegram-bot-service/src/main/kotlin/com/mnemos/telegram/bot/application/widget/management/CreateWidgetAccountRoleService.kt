package com.mnemos.telegram.bot.application.widget.management

import com.mnemos.telegram.bot.application.account.management.AccountManagementRoleInputParser
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMutationResult
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.management.port.out.AccountManagementWidgetPublisher
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class CreateWidgetAccountRoleService(
    private val gateway: AccountManagementGateway,
    private val snapshots: AccountManagementWidgetSnapshotQuery,
    private val publisher: AccountManagementWidgetPublisher,
    private val prompts: WidgetTextInputSessionRepository,
) : CreateWidgetAccountRoleUseCase {
    override fun create(
        prompt: WidgetTextInputSession,
        widget: TelegramWidgetSession,
        text: String,
    ): TelegramDeliveryResult {
        if (widget.screen != TelegramWidgetScreen.ACCOUNT_MANAGEMENT ||
            prompt.purpose != WidgetTextInputPurpose.ACCOUNT_ROLE_CREATE ||
            prompt.telegramId != widget.telegramId ||
            prompt.widgetMessageId != widget.messageId ||
            prompt.accountId != widget.accountId
        ) return TelegramDeliveryResult.PermanentFailure("invalid-role-input-context")

        val current = (snapshots.load(widget.telegramId, widget.page)
            as? AccountManagementWidgetSnapshotResult.Available)?.view
            ?: return TelegramDeliveryResult.Retryable
        if (current.accountId != widget.accountId) {
            return TelegramDeliveryResult.PermanentFailure("role-account-context-changed")
        }
        if (!current.canManageRoles) {
            return TelegramDeliveryResult.PermanentFailure("role-permission-denied")
        }

        val name = AccountManagementRoleInputParser.parseName(text)
            ?.takeIf { it.length in 1..64 && it.toByteArray(Charsets.UTF_8).size <= 128 && it.none(Char::isISOControl) }
        val message = when {
            name == null -> "Назва ролі недопустима"
            !prompts.claimMutation(prompt.telegramId, prompt.promptMessageId) ->
                "Запит уже оброблено. Оновіть список ролей"
            else -> when (gateway.createRole(prompt.telegramId, name)) {
                TelegramAccountManagementMutationResult.UPDATED -> "Роль створено"
                TelegramAccountManagementMutationResult.INVALID -> "Назву ролі відхилено"
                TelegramAccountManagementMutationResult.FORBIDDEN -> "Недостатньо дозволів"
                TelegramAccountManagementMutationResult.ACTIVE_ACCOUNT_REQUIRED -> "Активний акаунт не знайдено"
                TelegramAccountManagementMutationResult.REFRESH -> "Дані змінилися. Оновіть список"
                TelegramAccountManagementMutationResult.UNAVAILABLE -> "Стан операції невідомий. Перевірте список ролей"
            }
        }

        val refreshed = (snapshots.load(widget.telegramId, widget.page)
            as? AccountManagementWidgetSnapshotResult.Available)?.view
            ?: return TelegramDeliveryResult.Retryable
        if (refreshed.accountId != widget.accountId) {
            return TelegramDeliveryResult.PermanentFailure("role-account-context-changed")
        }
        return publisher.update(widget.telegramId, widget.messageId, refreshed.copy(feedback = message))
    }
}
