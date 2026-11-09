package com.mnemos.telegram.bot.application.widget.bets

import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.bets.port.out.BetWidgetPublisher
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetActionHandler
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class BetWidgetActionHandler(
    private val snapshots: BetWidgetSnapshotQuery,
    private val publisher: BetWidgetPublisher,
    private val sessions: TelegramWidgetSessionRepository,
    private val responder: WidgetQueryResponder,
) : TelegramWidgetActionHandler {
    override val screen = TelegramWidgetScreen.BETS

    override fun handle(
        context: TelegramWidgetQueryContext,
        session: TelegramWidgetSession,
    ): TelegramDeliveryResult {
        val mode = BetMenuMode.entries.getOrNull(session.page)
            ?: return responder.reply(context.queryId, "EXPIRED", "Відкрийте /bets")
        val loaded = snapshots.load(context.actorId, mode)
        if (loaded !is BetWidgetSnapshotResult.Available) {
            return responder.reply(context.queryId, "UNAVAILABLE", "Ставки зараз недоступні")
        }
        if (loaded.view.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Акаунт змінився. Відкрийте /bets")
        }

        val target =
            when {
                context.data == "w1:bet:refresh" -> mode
                context.data.startsWith("w1:bet:mode:") ->
                    BetMenuMode.entries.firstOrNull { it.name == context.data.removePrefix("w1:bet:mode:") }
                        ?: return responder.reply(context.queryId, "INVALID", "Невідома категорія ставок")
                else -> return responder.reply(context.queryId, "UNSUPPORTED", "Невідома дія")
            }
        val refreshed = (snapshots.load(context.actorId, target) as? BetWidgetSnapshotResult.Available)?.view
            ?: return responder.reply(context.queryId, "UNAVAILABLE", "Не вдалося оновити ставки")

        if (refreshed.accountId != session.accountId) {
            return responder.reply(context.queryId, "CONTEXT_CHANGED", "Акаунт змінився. Відкрийте /bets")
        }

        val result = responder.reply(context.queryId, "OK", "Список ставок оновлено", publisher.state(refreshed))
        if (result == TelegramDeliveryResult.Delivered) {
            if (target != mode) sessions.save(session.copy(page = target.ordinal))
            publisher.update(context.actorId, context.messageId, refreshed)
        }
        return result
    }
}
