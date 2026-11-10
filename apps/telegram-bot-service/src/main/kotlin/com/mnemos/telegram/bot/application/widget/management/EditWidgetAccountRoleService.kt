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
class EditWidgetAccountRoleService(
    private val gateway: AccountManagementGateway,
    private val snapshots: AccountManagementWidgetSnapshotQuery,
    private val publisher: AccountManagementWidgetPublisher,
    private val prompts: WidgetTextInputSessionRepository,
) : EditWidgetAccountRoleUseCase {
    override fun edit(
        prompt: WidgetTextInputSession,
        widget: TelegramWidgetSession,
        text: String,
    ): TelegramDeliveryResult {
        if (widget.screen != TelegramWidgetScreen.ACCOUNT_MANAGEMENT ||
            prompt.purpose != WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT ||
            prompt.telegramId != widget.telegramId ||
            prompt.widgetMessageId != widget.messageId ||
            prompt.accountId != widget.accountId ||
            prompt.targetRoleId == null
        ) return TelegramDeliveryResult.PermanentFailure("invalid-role-edit-context")

        val current = (snapshots.load(widget.telegramId, widget.page)
            as? AccountManagementWidgetSnapshotResult.Available)?.view
            ?: return TelegramDeliveryResult.Retryable
        if (current.accountId != widget.accountId) {
            return TelegramDeliveryResult.PermanentFailure("role-account-context-changed")
        }
        if (!current.canManageRoles) return TelegramDeliveryResult.PermanentFailure("role-permission-denied")
        if (current.roles.none { it.id == prompt.targetRoleId }) {
            return TelegramDeliveryResult.PermanentFailure("role-no-longer-on-original-page")
        }

        val details = AccountManagementRoleInputParser.parseDetails(text)?.takeIf {
            it.name.length in 1..64 &&
                it.name.toByteArray(Charsets.UTF_8).size <= 128 &&
                it.name.none { char -> Character.isISOControl(char) }
        }
        val message = when {
            details == null -> "Введіть назву й колір у форматі «Назва #RRGGBB»"
            !prompts.claimMutation(prompt.telegramId, prompt.promptMessageId) ->
                "Цей запит уже оброблено. Оновіть ролі"
            else -> when (gateway.updateRoleDetails(
                prompt.telegramId, prompt.targetRoleId, details.name, details.color,
            )) {
                TelegramAccountManagementMutationResult.UPDATED -> "Назву й колір ролі оновлено"
                TelegramAccountManagementMutationResult.INVALID -> "Зміни ролі відхилено"
                TelegramAccountManagementMutationResult.FORBIDDEN -> "Недостатньо дозволів"
                TelegramAccountManagementMutationResult.ACTIVE_ACCOUNT_REQUIRED -> "Активний акаунт не знайдено"
                TelegramAccountManagementMutationResult.REFRESH -> "Дані змінилися. Оновіть список"
                TelegramAccountManagementMutationResult.UNAVAILABLE -> "Стан операції невідомий. Перевірте список"
            }
        }

        val fresh = (snapshots.load(widget.telegramId, widget.page)
            as? AccountManagementWidgetSnapshotResult.Available)?.view
            ?: return TelegramDeliveryResult.Retryable
        if (fresh.accountId != widget.accountId) {
            return TelegramDeliveryResult.PermanentFailure("role-account-context-changed")
        }
        return publisher.update(widget.telegramId, widget.messageId, fresh.copy(feedback = message))
    }
}
