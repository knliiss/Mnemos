package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.account.model.TelegramAccountMutationResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.accounts.AccountWidgetSnapshotQuery
import com.mnemos.telegram.bot.application.widget.accounts.AccountWidgetSnapshotResult
import com.mnemos.telegram.bot.application.widget.accounts.port.out.AccountWidgetPublisher
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class ApplyWidgetAccountInputService(
    private val accounts: AccountGateway,
    private val snapshots: AccountWidgetSnapshotQuery,
    private val publisher: AccountWidgetPublisher,
    private val widgetSessions: TelegramWidgetSessionRepository,
    private val prompts: WidgetTextInputSessionRepository,
) : ApplyWidgetAccountInputUseCase {
    override fun apply(
        prompt: WidgetTextInputSession,
        widget: TelegramWidgetSession,
        text: String,
    ): TelegramDeliveryResult {
        if (widget.screen != TelegramWidgetScreen.ACCOUNTS ||
            prompt.purpose !in setOf(WidgetTextInputPurpose.ACCOUNT_CREATE, WidgetTextInputPurpose.ACCOUNT_JOIN) ||
            prompt.telegramId != widget.telegramId || prompt.widgetMessageId != widget.messageId ||
            prompt.accountId != widget.accountId
        ) return TelegramDeliveryResult.PermanentFailure("invalid-account-input-context")

        val current = snapshots.load(widget.telegramId, 0) as? AccountWidgetSnapshotResult.Available
            ?: return TelegramDeliveryResult.Retryable
        if (current.view.activeAccountId != widget.accountId) {
            return TelegramDeliveryResult.PermanentFailure("account-context-changed")
        }

        if (!prompts.claimMutation(prompt.telegramId, prompt.promptMessageId)) {
            return TelegramDeliveryResult.Delivered
        }

        val result =
            if (text.isBlank() || text.length > 64 || text.toByteArray(Charsets.UTF_8).size > 256) {
                TelegramAccountMutationResult.Invalid()
            } else {
                try {
                    when (prompt.purpose) {
                        WidgetTextInputPurpose.ACCOUNT_CREATE -> accounts.createAccount(prompt.telegramId, text)
                        WidgetTextInputPurpose.ACCOUNT_JOIN -> accounts.joinAccount(prompt.telegramId, text)
                        else -> TelegramAccountMutationResult.Invalid()
                    }
                } catch (_: Exception) {
                    TelegramAccountMutationResult.Unavailable
                }
            }

        val message =
            when (result) {
                TelegramAccountMutationResult.Updated ->
                    if (prompt.purpose == WidgetTextInputPurpose.ACCOUNT_CREATE) "Акаунт створено" else "Приєднання виконано"
                is TelegramAccountMutationResult.Invalid ->
                    when (result.code) {
                        "ACCOUNT_IDENTITY_CONFLICT" -> "Цей гравець уже прив'язаний до акаунта"
                        "CRISTALIX_PLAYER_NOT_FOUND" -> "Гравця Cristalix не знайдено"
                        else -> "Недійсний нікнейм або код запрошення"
                    }
                TelegramAccountMutationResult.Unavailable -> "Не вдалося підтвердити операцію. Перевірте список акаунтів"
            }

        val refreshed = snapshots.load(widget.telegramId, 0) as? AccountWidgetSnapshotResult.Available
        if (refreshed != null) {
            widgetSessions.save(widget.copy(accountId = refreshed.view.activeAccountId, page = refreshed.view.page))
            publisher.update(widget.telegramId, widget.messageId, refreshed.view.copy(feedback = message))
        }
        return TelegramDeliveryResult.Delivered
    }
}
