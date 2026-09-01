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
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRace
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRaceHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacePosition
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacesResult
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
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

        if (bets.data.operationTracked && !bets.data.waitingForAmount) {
            return trackedOperationMessage(bets.data)
        }

        return when (bets.data.mode) {
            BetMenuMode.ROUNDS -> renderRounds(bets.data)
            BetMenuMode.RACES -> renderRaces(bets.data)
            BetMenuMode.HISTORY -> renderHistory(bets.data)
        }
    }

    private fun trackedOperationMessage(data: BetMenuScreenData): OutboundTelegramMessage {
        val status =
            if (data.feedback == BetMenuFeedback.UNAVAILABLE) {
                feedbackText(data.feedback)
            } else {
                "⏳ Операция ещё обрабатывается. Нажмите «Обновить», чтобы проверить статус."
            }

        return message(
            text =
                listOfNotNull(
                    "🎲 <b>Ставки</b>",
                    MESSAGE_DIVIDER,
                    "",
                    status,
                ).joinToString("\n"),
            rows = trackedOperationRows,
        )
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

        val lines =
            mutableListOf(
                "🎲 <b>Ставки</b>",
                MESSAGE_DIVIDER,
                "",
                "🎯 <b>События</b>",
            )
        addFeedback(lines, data.feedback)

        if (rounds.isEmpty()) {
            lines += "└ Нет доступных раундов."
        } else {
            rounds.forEachIndexed { index, round ->
                lines += ""
                lines += renderRound(index + 1, round)
            }
        }

        val rows = mutableListOf<List<OutboundTelegramMessageAction>>()

        rounds.forEachIndexed { index, round ->
            if (round.status != OPEN_STATUS) {
                return@forEachIndexed
            }

            if (round.myBet == null) {
                rows +=
                    listOf(
                        callback(
                            label = "${index + 1} · 👍 Да",
                            data = BetCallbackData.place(round.id, TelegramBetSide.YES),
                            style = OutboundTelegramButtonStyle.SUCCESS,
                        ),
                        callback(
                            label = "${index + 1} · 👎 Нет",
                            data = BetCallbackData.place(round.id, TelegramBetSide.NO),
                            style = OutboundTelegramButtonStyle.DANGER,
                        ),
                    )
            } else {
                rows +=
                    listOf(
                        callback(
                            label = "${index + 1} · ➕ Увеличить ставку",
                            data = BetCallbackData.increase(round.id),
                            style = OutboundTelegramButtonStyle.PRIMARY,
                        ),
                    )
            }
        }

        rows += commonRows(BetMenuMode.ROUNDS)

        return message(lines.joinToString("\n"), rows)
    }

    private fun renderRaces(data: BetMenuScreenData): OutboundTelegramMessage =
        when (val result = data.races) {
            is TelegramItemRacesResult.Available -> renderAvailableRaces(data, result.races)
            TelegramItemRacesResult.AccountRequired -> accountRequiredMessage(data.feedback)
            TelegramItemRacesResult.Unavailable -> unavailableMessage(data.feedback)
        }

    private fun renderAvailableRaces(
        data: BetMenuScreenData,
        races: List<TelegramItemRace>,
    ): OutboundTelegramMessage {
        if (data.waitingForAmount) {
            return amountPrompt(data)
        }

        val lines =
            mutableListOf(
                "🎲 <b>Ставки</b>",
                MESSAGE_DIVIDER,
                "",
                "🏁 <b>Item Race</b>",
            )
        addFeedback(lines, data.feedback)

        if (races.isEmpty()) {
            lines += "└ Нет активных гонок."
        } else {
            races.forEachIndexed { index, race ->
                lines += ""
                lines += renderRace(index + 1, race)
            }
        }

        val rows = mutableListOf<List<OutboundTelegramMessageAction>>()

        races.forEachIndexed { index, race ->
            if (race.status != REGISTRATION_STATUS) {
                return@forEachIndexed
            }

            if (race.myPosition == null) {
                rows +=
                    listOf(
                        callback(
                            label = "${index + 1} · 🏁 Вступить · ${race.entryAmount} COIN",
                            data = BetCallbackData.joinRace(race.id),
                            style = OutboundTelegramButtonStyle.PRIMARY,
                        ),
                    )
            } else {
                rows +=
                    listOf(
                        callback(
                            label = "${index + 1} · ➕ Увеличить позицию",
                            data = BetCallbackData.increaseRace(race.id),
                            style = OutboundTelegramButtonStyle.PRIMARY,
                        ),
                    )
            }
        }

        rows += commonRows(BetMenuMode.RACES)

        return message(lines.joinToString("\n"), rows)
    }

    private fun renderHistory(data: BetMenuScreenData): OutboundTelegramMessage {
        if (data.waitingForAmount) {
            return amountPrompt(data)
        }

        if (data.eventHistory is TelegramBetHistoryResult.AccountRequired ||
            data.raceHistory is TelegramItemRaceHistoryResult.AccountRequired
        ) {
            return accountRequiredMessage(data.feedback)
        }

        if (data.eventHistory !is TelegramBetHistoryResult.Available ||
            data.raceHistory !is TelegramItemRaceHistoryResult.Available
        ) {
            return unavailableMessage(data.feedback)
        }

        val eventBets = data.eventHistory.bets
        val racePositions = data.raceHistory.positions
        val lines =
            mutableListOf(
                "🎲 <b>Ставки</b>",
                MESSAGE_DIVIDER,
                "",
                "📜 <b>История</b>",
            )
        addFeedback(lines, data.feedback)

        if (eventBets.isEmpty() && racePositions.isEmpty()) {
            lines += "└ История пока пустая."
        } else {
            if (eventBets.isNotEmpty()) {
                lines += ""
                lines += "🎯 <b>События</b>"
                eventBets.take(HISTORY_LIMIT).forEachIndexed { index, bet ->
                    lines += ""
                    lines += renderHistoryBet(index + 1, bet)
                }
            }

            if (racePositions.isNotEmpty()) {
                lines += ""
                lines += "🏁 <b>Item Race</b>"
                racePositions.take(HISTORY_LIMIT).forEachIndexed { index, position ->
                    lines += ""
                    lines += renderHistoryRacePosition(index + 1, position)
                }
            }
        }

        return message(
            text = lines.joinToString("\n"),
            rows = commonRows(BetMenuMode.HISTORY),
        )
    }

    private fun amountPrompt(data: BetMenuScreenData): OutboundTelegramMessage {
        val selectedRace =
            (data.races as? TelegramItemRacesResult.Available)
                ?.races
                ?.firstOrNull { race -> race.id == data.selectedRaceId }

        if (selectedRace != null) {
            return message(
                text =
                    listOfNotNull(
                        "🏁 <b>Item Race</b>",
                        MESSAGE_DIVIDER,
                        "",
                        "📦 Предмет: <b>${escapeHtml(selectedRace.itemName)}</b>",
                        "💰 Отправьте сумму COIN для увеличения позиции.",
                        feedbackText(data.feedback),
                    ).joinToString("\n"),
                rows = amountPromptRows,
            )
        }

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

        return message(
            text =
                listOfNotNull(
                    "🎲 <b>Ставки</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "🎯 ${escapeHtml(eventName)}",
                    "└ Вы хотите $action.",
                    "",
                    "💰 Отправьте сумму COIN одним сообщением.",
                    feedbackText(data.feedback),
                ).joinToString("\n"),
            rows = amountPromptRows,
        )
    }

    private fun renderRound(
        number: Int,
        round: TelegramEventBetRound,
    ): String {
        val lines =
            mutableListOf(
                "<b>$number. ${escapeHtml(eventLabel(round.eventType))}</b>",
                "├ 🕒 ${timeFormatter.format(round.scheduledAt)}",
                "├ 🔒 ${timeFormatter.format(round.closesAt)}",
                "└ Да ×${odds(round.yesOdds)} · Нет ×${odds(round.noOdds)} · ${statusLabel(round.status)}",
            )

        round.myBet?.let { bet ->
            lines += "   Моя ставка: <b>${sideLabel(bet.side)}</b> · ${bet.confirmedAmount} COIN${pendingAmount(bet)}"
        }

        round.outcome?.let { outcome ->
            lines += "   Результат: <b>${escapeHtml(outcome.lowercase())}</b>"
        }

        return lines.joinToString("\n")
    }

    private fun renderRace(
        number: Int,
        race: TelegramItemRace,
    ): String {
        val lines =
            mutableListOf(
                "<b>$number. 📦 ${escapeHtml(race.itemName)}</b> · ${escapeHtml(race.itemType.lowercase())}",
                "├ 💰 Вход: <b>${race.entryAmount} COIN</b> · комиссия ${race.feePercent}%",
                "├ 🔒 ${timeFormatter.format(race.registrationClosesAt)}",
                "└ 🏁 ${timeFormatter.format(race.startsAt)} · ${statusLabel(race.status)}",
            )

        race.myPosition?.let { position ->
            lines += "   Моя позиция: <b>${position.confirmedAmount} COIN</b>${pendingAmount(position)}"
        }

        return lines.joinToString("\n")
    }

    private fun renderHistoryBet(
        number: Int,
        bet: TelegramEventBet,
    ): String =
        listOf(
            "<b>$number. ${sideLabel(bet.side)}</b> · ${bet.confirmedAmount} COIN${pendingAmount(bet)}",
            "├ ${escapeHtml(bet.status.lowercase())}",
            "└ <code>${bet.roundId.toString().take(8)}</code>",
        ).joinToString("\n")

    private fun renderHistoryRacePosition(
        number: Int,
        position: TelegramItemRacePosition,
    ): String =
        listOf(
            "<b>$number. ${escapeHtml(position.accountNickname)}</b> · ${position.confirmedAmount} COIN${pendingAmount(position)}",
            "├ ${escapeHtml(position.status.lowercase())}",
            "└ <code>${position.raceId.toString().take(8)}</code>",
        ).joinToString("\n")

    private fun pendingAmount(bet: TelegramEventBet): String = pendingAmount(bet.pendingAmount)

    private fun pendingAmount(position: TelegramItemRacePosition): String = pendingAmount(position.pendingAmount)

    private fun pendingAmount(amount: Long): String =
        if (amount > 0) {
            " · ожидает $amount"
        } else {
            ""
        }

    private fun commonRows(current: BetMenuMode): List<List<OutboundTelegramMessageAction>> =
        listOf(
            modeRow(current),
            listOf(refreshAction),
            listOf(backAction),
        )

    private fun modeRow(current: BetMenuMode): List<OutboundTelegramMessageAction> =
        listOf(
            modeCallback("🎯 События", BetCallbackData.ROUNDS, current == BetMenuMode.ROUNDS),
            modeCallback("🏁 Race", BetCallbackData.RACES, current == BetMenuMode.RACES),
            modeCallback("📜 История", BetCallbackData.HISTORY, current == BetMenuMode.HISTORY),
        )

    private fun modeCallback(
        label: String,
        data: String,
        selected: Boolean,
    ): OutboundTelegramMessageAction.Callback =
        callback(
            label = label,
            data = data,
            style =
                if (selected) {
                    OutboundTelegramButtonStyle.SUCCESS
                } else {
                    OutboundTelegramButtonStyle.DEFAULT
                },
        )

    private fun accountRequiredMessage(feedback: BetMenuFeedback?): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "🎲 <b>Ставки</b>",
                MESSAGE_DIVIDER,
                "",
                "⚠️ Нужен активный Cristalix-аккаунт.",
            )
        addFeedback(lines, feedback)

        return message(
            text = lines.joinToString("\n"),
            rows =
                listOf(
                    listOf(
                        callback(
                            label = "👤 Аккаунт",
                            data = TelegramMenuScreen.ACCOUNT.callbackData,
                            style = OutboundTelegramButtonStyle.PRIMARY,
                        ),
                    ),
                    listOf(backAction),
                ),
        )
    }

    private fun unavailableMessage(feedback: BetMenuFeedback? = null): OutboundTelegramMessage =
        message(
            text =
                listOfNotNull(
                    "🎲 <b>Ставки</b>",
                    MESSAGE_DIVIDER,
                    feedbackText(feedback),
                    "",
                    "⚠️ Сервис ставок временно недоступен.",
                ).joinToString("\n"),
            rows =
                listOf(
                    listOf(refreshAction),
                    listOf(backAction),
                ),
        )

    private fun addFeedback(
        lines: MutableList<String>,
        feedback: BetMenuFeedback?,
    ) {
        feedbackText(feedback)?.let { value ->
            lines += ""
            lines += value
        }
    }

    private fun feedbackText(feedback: BetMenuFeedback?): String? =
        when (feedback) {
            BetMenuFeedback.COMPLETED -> "✅ Операция подтверждена. Баланс и позиция обновлены."
            BetMenuFeedback.FAILED -> "❌ Транзакция не прошла. Ставка не была подтверждена."
            BetMenuFeedback.PENDING -> "⏳ Операция ещё обрабатывается."
            BetMenuFeedback.INVALID_AMOUNT -> "⚠️ Проверьте сумму, баланс, права и состояние рынка."
            BetMenuFeedback.ACCOUNT_REQUIRED -> "⚠️ Активный аккаунт не найден."
            BetMenuFeedback.UNAVAILABLE -> "⚠️ Не удалось проверить или выполнить операцию."
            null -> null
        }

    private fun eventLabel(value: String): String =
        when (value.uppercase()) {
            "MOON" -> "🌙 Луна"
            "ECLIPSE" -> "🌘 Затмение"
            "EXPLOSION" -> "💥 Взрыв"
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
            REGISTRATION_STATUS -> "регистрация"
            "CLOSED" -> "закрыта"
            "ACTIVE" -> "активна"
            "RESOLVED", "SETTLED" -> "завершена"
            "REFUNDED" -> "возврат"
            "EXPIRED" -> "истекла"
            else -> value.lowercase()
        }

    private fun odds(value: BigDecimal): String = value.stripTrailingZeros().toPlainString()

    private fun message(
        text: String,
        rows: List<List<OutboundTelegramMessageAction>>,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = text,
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )

    private fun callback(
        label: String,
        data: String,
        style: OutboundTelegramButtonStyle = OutboundTelegramButtonStyle.DEFAULT,
    ): OutboundTelegramMessageAction.Callback =
        OutboundTelegramMessageAction.Callback(
            label = label,
            data = data,
            style = style,
        )

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private const val MESSAGE_DIVIDER = "━━━━━━━━━━━━━━━━━━━━"
    private const val OPEN_STATUS = "OPEN"
    private const val REGISTRATION_STATUS = "REGISTRATION"
    private const val HISTORY_LIMIT = 20

    private val amountPromptRows =
        listOf(
            listOf(
                callback(
                    label = "✖ Отмена",
                    data = BetCallbackData.CANCEL_INPUT,
                    style = OutboundTelegramButtonStyle.DANGER,
                ),
            ),
        )

    private val trackedOperationRows =
        listOf(
            listOf(refreshAction),
            listOf(backAction),
        )

    private val refreshAction = callback("🔄 Обновить", BetCallbackData.REFRESH)

    private val backAction = callback("← Назад", TelegramMenuScreen.BACK_CALLBACK_DATA)

    private val timeFormatter: DateTimeFormatter =
        DateTimeFormatter
            .ofPattern("dd.MM HH:mm")
            .withZone(ZoneId.of("Europe/Moscow"))
}
