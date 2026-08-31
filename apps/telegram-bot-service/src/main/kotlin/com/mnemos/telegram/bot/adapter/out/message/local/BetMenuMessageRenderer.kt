package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.bet.BetCallbackData
import com.mnemos.telegram.bot.application.bet.model.BetMenuFeedback
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.BetMenuScreenData
import com.mnemos.telegram.bot.application.bet.model.TelegramBetHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import com.mnemos.telegram.bot.application.bet.model.TelegramEventBet
import com.mnemos.telegram.bot.application.bet.model.TelegramEventBetRound
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import java.math.BigDecimal
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object BetMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val bets =
            data as? TelegramMenuScreenData.Bets
                ?: return unavailableMessage()

        return when (bets.data.mode) {
            BetMenuMode.ROUNDS -> renderRounds(bets.data)
            BetMenuMode.HISTORY -> renderHistory(bets.data)
        }
    }

    private fun renderRounds(data: BetMenuScreenData): OutboundTelegramMessage =
        when (val result = data.rounds) {
            is TelegramBetRoundsResult.Available -> renderAvailableRounds(data, result.rounds)
            TelegramBetRoundsResult.AccountRequired -> accountRequiredMessage(data.feedback)
            TelegramBetRoundsResult.Unavailable -> unavailableMessage(data.feedback)
        }

    private fun renderAvailableRounds(
        data: BetMenuScreenData,
        rounds: List<TelegramEventBetRound>,
    ): OutboundTelegramMessage {
        if (data.waitingForAmount) {
            return amountPrompt(data)
        }

        val lines = mutableListOf("🎲 <b>Ставки</b>")
        feedbackText(data.feedback)?.let { feedback ->
            lines += ""
            lines += feedback
        }

        if (rounds.isEmpty()) {
            lines += ""
            lines += "Сейчас нет доступных раундов событий."
        } else {
            rounds.forEachIndexed { index, round ->
                lines += ""
                lines += renderRound(index + 1, round)
            }
        }

        val actions = mutableListOf<OutboundTelegramMessageAction>()

        rounds.forEachIndexed { index, round ->
            if (round.status == OPEN_STATUS) {
                if (round.myBet == null) {
                    actions += callback("${index + 1} · ✅ Да", BetCallbackData.place(round.id, TelegramBetSide.YES))
                    actions += callback("${index + 1} · ❌ Нет", BetCallbackData.place(round.id, TelegramBetSide.NO))
                } else {
                    actions += callback("${index + 1} · ➕ Увеличить", BetCallbackData.increase(round.id))
                }
            }
        }

        actions += callback("📜 История", BetCallbackData.HISTORY)
        actions += callback("🔄 Обновить", BetCallbackData.REFRESH)
        actions += navigationActions

        return message(lines.joinToString("\n"), actions)
    }

    private fun renderHistory(data: BetMenuScreenData): OutboundTelegramMessage {
        if (data.waitingForAmount) {
            return amountPrompt(data)
        }

        return when (val history = data.history) {
            is TelegramBetHistoryResult.Available -> {
                val lines = mutableListOf("📜 <b>История ставок</b>")
                feedbackText(data.feedback)?.let { feedback ->
                    lines += ""
                    lines += feedback
                }

                if (history.bets.isEmpty()) {
                    lines += ""
                    lines += "История ставок пока пустая."
                } else {
                    history.bets.take(HISTORY_LIMIT).forEachIndexed { index, bet ->
                        lines += ""
                        lines += renderHistoryBet(index + 1, bet)
                    }

                    if (history.bets.size > HISTORY_LIMIT) {
                        lines += ""
                        lines += "Показаны последние $HISTORY_LIMIT ставок из ${history.bets.size}."
                    }
                }

                message(
                    text = lines.joinToString("\n"),
                    actions =
                        listOf(
                            callback("← Текущие ставки", BetCallbackData.ROUNDS),
                            callback("🔄 Обновить", BetCallbackData.HISTORY),
                        ) + navigationActions,
                )
            }

            TelegramBetHistoryResult.AccountRequired -> accountRequiredMessage(data.feedback)
            TelegramBetHistoryResult.Unavailable,
            null,
            -> unavailableMessage(data.feedback)
        }
    }

    private fun amountPrompt(data: BetMenuScreenData): OutboundTelegramMessage {
        val selectedRound =
            (data.rounds as? TelegramBetRoundsResult.Available)
                ?.rounds
                ?.firstOrNull { round -> round.id == data.selectedRoundId }
        val eventName = selectedRound?.eventType?.let(::eventLabel) ?: "событие"
        val action =
            if (data.increasing) {
                "увеличить ставку"
            } else {
                val side = data.selectedSide?.let(::sideLabel) ?: "—"
                "поставить на <b>$side</b>"
            }
        val lines =
            mutableListOf(
                "🎲 <b>Ставки</b>",
                "",
                "Вы выбрали <b>${escapeHtml(eventName)}</b> и хотите $action.",
                "Отправьте сумму в COIN одним сообщением.",
            )

        feedbackText(data.feedback)?.let { feedback ->
            lines += ""
            lines += feedback
        }

        return message(
            text = lines.joinToString("\n"),
            actions =
                listOf(
                    callback("✖ Отмена", BetCallbackData.CANCEL_INPUT),
                    callback("⌂ Меню", TelegramMenuScreen.HOME.callbackData),
                ),
        )
    }

    private fun renderRound(
        number: Int,
        round: TelegramEventBetRound,
    ): String {
        val myBet = round.myBet
        val lines =
            mutableListOf(
                "<b>$number. ${escapeHtml(eventLabel(round.eventType))}</b>",
                "Время: <code>${timeFormatter.format(round.scheduledAt)}</code>",
                "Закрытие: <code>${timeFormatter.format(round.closesAt)}</code>",
                "Да ×${odds(round.yesOdds)} · Нет ×${odds(round.noOdds)} · ${statusLabel(round.status)}",
            )

        if (myBet != null) {
            lines += "Моя ставка: <b>${sideLabel(myBet.side)}</b> · ${myBet.confirmedAmount} COIN${pendingAmount(myBet)}"
        }

        round.outcome?.let { outcome ->
            lines += "Результат: <b>${escapeHtml(outcome.lowercase())}</b>"
        }

        return lines.joinToString("\n")
    }

    private fun renderHistoryBet(
        number: Int,
        bet: TelegramEventBet,
    ): String =
        listOf(
            "<b>$number. ${sideLabel(bet.side)}</b> · ${bet.confirmedAmount} COIN${pendingAmount(bet)}",
            "Статус: ${escapeHtml(bet.status.lowercase())}",
            "Раунд: <code>${bet.roundId.toString().take(8)}</code>",
        ).joinToString("\n")

    private fun pendingAmount(bet: TelegramEventBet): String =
        if (bet.pendingAmount > 0) {
            " · ожидает ${bet.pendingAmount}"
        } else {
            ""
        }

    private fun accountRequiredMessage(feedback: BetMenuFeedback?): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "🎲 <b>Ставки</b>",
                "",
                "Для ставок нужен активный Cristalix-аккаунт Mnemos.",
                "Его можно создать, подключить или переключить прямо в Telegram.",
            )

        feedbackText(feedback)?.let { value ->
            lines += ""
            lines += value
        }

        return message(
            text = lines.joinToString("\n"),
            actions =
                listOf(
                    callback("👤 Настроить аккаунт", TelegramMenuScreen.ACCOUNT.callbackData),
                    callback("⌂ Меню", TelegramMenuScreen.HOME.callbackData),
                ),
        )
    }

    private fun unavailableMessage(feedback: BetMenuFeedback? = null): OutboundTelegramMessage =
        message(
            text =
                listOfNotNull(
                    "🎲 <b>Ставки</b>",
                    feedbackText(feedback),
                    "",
                    "Сервис ставок временно недоступен. Попробуйте обновить.",
                ).joinToString("\n"),
            actions =
                listOf(
                    callback("🔄 Обновить", BetCallbackData.REFRESH),
                ) + navigationActions,
        )

    private fun feedbackText(feedback: BetMenuFeedback?): String? =
        when (feedback) {
            BetMenuFeedback.ACCEPTED -> "✅ Операция принята. Баланс и ставка обновятся после подтверждения транзакции."
            BetMenuFeedback.INVALID_AMOUNT -> "⚠️ Не удалось принять сумму. Проверьте лимиты, баланс и состояние раунда."
            BetMenuFeedback.ACCOUNT_REQUIRED -> "⚠️ Активный аккаунт не найден."
            BetMenuFeedback.UNAVAILABLE -> "⚠️ Не удалось выполнить операцию. Попробуйте еще раз."
            null -> null
        }

    private fun eventLabel(value: String): String =
        when (value.uppercase()) {
            "MOON" -> "Луна"
            "ECLIPSE" -> "Затмение"
            "EXPLOSION" -> "Взрыв"
            else -> value.lowercase().replace('_', ' ').replaceFirstChar { character -> character.titlecase() }
        }

    private fun sideLabel(side: TelegramBetSide): String =
        when (side) {
            TelegramBetSide.YES -> "Да"
            TelegramBetSide.NO -> "Нет"
        }

    private fun statusLabel(value: String): String =
        when (value.uppercase()) {
            OPEN_STATUS -> "открыта"
            "CLOSED" -> "закрыта"
            "RESOLVED" -> "завершена"
            "REFUNDED" -> "возврат"
            else -> value.lowercase()
        }

    private fun odds(value: BigDecimal): String = value.stripTrailingZeros().toPlainString()

    private fun message(
        text: String,
        actions: List<OutboundTelegramMessageAction>,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = text,
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = actions,
        )

    private fun callback(
        label: String,
        data: String,
    ): OutboundTelegramMessageAction.Callback =
        OutboundTelegramMessageAction.Callback(
            label = label,
            data = data,
        )

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private const val OPEN_STATUS = "OPEN"
    private const val HISTORY_LIMIT = 20

    private val navigationActions =
        listOf(
            callback("← Назад", TelegramMenuScreen.BACK_CALLBACK_DATA),
            callback("⌂ Меню", TelegramMenuScreen.HOME.callbackData),
        )

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("dd.MM HH:mm")
            .withZone(ZoneId.of("Europe/Moscow"))
}
