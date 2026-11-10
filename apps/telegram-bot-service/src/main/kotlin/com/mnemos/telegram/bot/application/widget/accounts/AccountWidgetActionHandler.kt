package com.mnemos.telegram.bot.application.widget.accounts

import com.mnemos.telegram.bot.application.account.model.TelegramAccountSwitchResult
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.accounts.port.out.AccountWidgetPublisher
import com.mnemos.telegram.bot.application.widget.input.StartWidgetTextInputUseCase
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetActionHandler
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class AccountWidgetActionHandler(
    private val snapshots: AccountWidgetSnapshotQuery,
    private val accounts: AccountGateway,
    private val publisher: AccountWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
    private val input: StartWidgetTextInputUseCase,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.ACCOUNTS

    override fun handle(
        context: TelegramWidgetQueryContext,
        session: TelegramWidgetSession,
    ): TelegramDeliveryResult {
        val telegramId = context.actorId
        val loaded = snapshots.load(telegramId, session.page)
        if (loaded !is AccountWidgetSnapshotResult.Available) {
            return responder.reply(context.queryId, "UNAVAILABLE", "Акаунти зараз недоступні")
        }

        val current = loaded.view
        if (current.activeAccountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Активний акаунт змінився. Відкрийте /account")
        }

        val purpose =
            when (context.data) {
                "w1:acct:create" -> WidgetTextInputPurpose.ACCOUNT_CREATE
                "w1:acct:join" -> WidgetTextInputPurpose.ACCOUNT_JOIN
                else -> null
            }
        if (purpose != null) {
            val response = responder.reply(context.queryId, "INPUT", "Відповідайте на підказку в чаті")
            if (response == TelegramDeliveryResult.Delivered) {
                runCatching { input.start(session, purpose) }
            }
            return response
        }

        var page = session.page
        var selectedId: UUID? = null
        val message =
            when {
                context.data == "w1:acct:refresh" -> "Дані оновлено"
                context.data.startsWith("w1:acct:next:") -> {
                    val expected = context.data.removePrefix("w1:acct:next:").toIntOrNull()
                    if (expected != session.page) {
                        return responder.reply(context.queryId, "CONFLICT", "Сторінку вже змінено")
                    }
                    page = (session.page + 1).coerceAtMost(current.totalPages - 1)
                    "Наступна сторінка"
                }

                context.data.startsWith("w1:acct:prev:") -> {
                    val expected = context.data.removePrefix("w1:acct:prev:").toIntOrNull()
                    if (expected != session.page) {
                        return responder.reply(context.queryId, "CONFLICT", "Сторінку вже змінено")
                    }
                    page = (session.page - 1).coerceAtLeast(0)
                    "Попередня сторінка"
                }

                context.data.startsWith("w1:acct:select:") -> {
                    val requested =
                        runCatching { UUID.fromString(context.data.removePrefix("w1:acct:select:")) }
                            .getOrNull()
                            ?: return responder.reply(context.queryId, "INVALID", "Невірний акаунт")
                    val entry = current.accounts.firstOrNull { it.accountId == requested }
                        ?: return responder.reply(context.queryId, "CONFLICT", "Оновіть список акаунтів")
                    if (!entry.enabled) {
                        return responder.reply(context.queryId, "FORBIDDEN", "Акаунт деактивовано")
                    }
                    if (current.activeAccountId != requested) {
                        when (accounts.changeActiveAccount(telegramId, requested)) {
                            TelegramAccountSwitchResult.Switched -> selectedId = requested
                            TelegramAccountSwitchResult.RegistrationRequired ->
                                return responder.reply(context.queryId, "ACCOUNT_REQUIRED", "Спочатку зареєструйте акаунт")
                            TelegramAccountSwitchResult.Refresh ->
                                return responder.reply(context.queryId, "CONFLICT", "Перевірте список акаунтів")
                            TelegramAccountSwitchResult.Unavailable ->
                                return responder.reply(context.queryId, "UNAVAILABLE", "Не вдалося перемкнути акаунт")
                        }
                    }
                    "Активний акаунт оновлено"
                }

                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }

        if (selectedId != null) {
            sessions.save(session.copy(accountId = selectedId))
        }

        val refreshed = snapshots.load(telegramId, page) as? AccountWidgetSnapshotResult.Available
            ?: return responder.reply(context.queryId, "OK", "$message. Оновіть картку")

        if (selectedId != null && refreshed.view.activeAccountId != selectedId) {
            return responder.reply(context.queryId, "OK", "$message. Оновіть картку")
        }

        val response =
            responder.reply(
                queryId = context.queryId,
                status = "OK",
                message = message,
                state = publisher.state(refreshed.view),
            )
        if (response == TelegramDeliveryResult.Delivered) {
            val updated = session.copy(
                accountId = refreshed.view.activeAccountId,
                page = refreshed.view.page,
            )
            if (selectedId == null || updated.page != session.page) {
                sessions.save(updated)
            }
            publisher.update(telegramId, context.messageId, refreshed.view)
        }
        return response
    }
}
