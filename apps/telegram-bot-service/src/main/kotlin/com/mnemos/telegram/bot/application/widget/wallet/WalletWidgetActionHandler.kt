package com.mnemos.telegram.bot.application.widget.wallet

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetActionHandler
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.application.widget.wallet.port.out.WalletWidgetPublisher
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class WalletWidgetActionHandler(
    private val snapshots: WalletWidgetSnapshotQuery,
    private val publisher: WalletWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.WALLET

    override fun handle(
        context: TelegramWidgetQueryContext,
        session: TelegramWidgetSession,
    ): TelegramDeliveryResult {
        val current = snapshots.load(context.actorId, session.page) as? WalletWidgetSnapshotResult.Available
            ?: return responder.reply(context.queryId, "UNAVAILABLE", "Гаманець недоступний")
        if (current.view.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Акаунт змінився. Відкрийте /wallet")
        }

        val page =
            when {
                context.data == "w1:wallet:refresh" -> session.page
                context.data.startsWith("w1:wallet:next:") -> {
                    val expectedPage = context.data.removePrefix("w1:wallet:next:").toIntOrNull()
                    if (expectedPage != session.page || current.view.historyStatus != WalletHistoryStatus.AVAILABLE) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть список транзакцій")
                    }
                    (session.page + 1).coerceAtMost(current.view.totalPages - 1)
                }

                context.data.startsWith("w1:wallet:prev:") -> {
                    val expectedPage = context.data.removePrefix("w1:wallet:prev:").toIntOrNull()
                    if (expectedPage != session.page || current.view.historyStatus != WalletHistoryStatus.AVAILABLE) {
                        return responder.reply(context.queryId, "CONFLICT", "Оновіть список транзакцій")
                    }
                    (session.page - 1).coerceAtLeast(0)
                }

                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }

        val fresh = snapshots.load(context.actorId, page) as? WalletWidgetSnapshotResult.Available
            ?: return responder.reply(context.queryId, "UNAVAILABLE", "Не вдалося оновити гаманець")
        if (fresh.view.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Акаунт змінився. Відкрийте /wallet")
        }

        val result = responder.reply(context.queryId, "OK", "Гаманець оновлено", publisher.state(fresh.view))
        if (result == TelegramDeliveryResult.Delivered) {
            sessions.save(session.copy(page = fresh.view.page))
            publisher.update(context.actorId, context.messageId, fresh.view)
        }
        return result
    }
}
