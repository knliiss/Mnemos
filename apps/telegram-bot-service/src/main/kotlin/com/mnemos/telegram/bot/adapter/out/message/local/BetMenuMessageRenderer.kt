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
            BetMenuMode.RACES -> renderRaces(bets.data)
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

        val lines = mutableListOf("🎲 <b>Ставки · События</b>")
        addFeedback(lines, data.feedback)

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

        actions += modeActions(BetMenuMode.ROUNDS)
        actions += callback("🔄 Обновить", BetCallbackData.REFRESH)
        actions += navigationActions

        return message(lines.joinToString("\n"), actions)
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

        val lines = mutableListOf("🏁 <b>Ставки · Item Race</b>")
        addFeedback(lines, data.feedback)

        if (races.isEmpty()) {
            lines += ""
            lines += "Сейчас нет активных item race."
        } else {
            races.forEachIndexed { index, race ->
                lines += ""
                lines += renderRace(index + 1, race)
            }
        }

        val actions = mutableListOf<OutboundTelegramMessageAction>()

        races.forEachIndexed { index, race ->
            if (race.status == REGISTRATION_STATUS) {
                if (race.myPosition == null) {
                    actions += callback(
                        "${index + 1} · Вступить за ${race.entryAmount} COIN",
                        BetCallbackData.joinRace(race.id),
                    )
                } else {
                    actions +=
                        callback(
                            "${index + 1} · ➕ Увеличить",
                            BetCallbackData.increaseRace(race.id),
                        )
                }
            }
        }

        actions += modeActions(BetMenuMode.RACES)
        actions += callback("🔄 Обновить", BetCallbackData.REFRESH)
        actions += navigationActions

        return message(lines.joinToString("\n"), actions)
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
        val lines = mutableListOf("📜 <b>История ставок</b>")
        addFeedback(lines, data.feedback)

        if (eventBets.isEmpty() && racePositions.isEmpty()) {
            lines += ""
            lines += "История ставок пока пустая."
        } else {
            if (eventBets.isNotEmpty()) {
                lines += ""
                lines += "<b>События</b>"
                eventBets.take(HISTORY_LIMIT).forEachIndexed { index, bet ->
                    lines += ""
                    lines += renderHistoryBet(index + 1, bet)
                }
            }

            if (racePositions.isNotEmpty()) {
                lines += ""
                lines += "<b>Item Race</b>"
                racePositions.take(HISTORY_LIMIT).forEachIndexed { index, position ->
                    lines += ""
                    lines += renderHistoryRacePosition(index + 1, position)
                }
            }
        }

        return message(
            text = lines.joinToString("\n"),
            actions = modeActions(BetMenuMode.HISTORY) + callback("🔄 Обновить", BetCallbackData.REFRESH) + navigationActions,
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
                        "",
                        "Предмет: <b>${escapeHtml(selectedRace.itemName)}</b>",
                        "Отправьте сумму COIN, на которую хотите увеличить свою позицию.",
                        feedbackText(data.feedback),
                    ).joinToString("\n"),
                actions = amountPromptActions,
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
                    "",
                    "Вы выбрали <b>${escapeHtml(eventName)}</b> и хотите $action.",
                    "Отправьте сумму в COIN одним сообщением.",
                    feedbackText(data.feedback),
                ).joinToString("\n"),
            actions = amountPromptActions,
        )
    }

    private fun renderRound(
        number: Int,
        round: TelegramEventBetRound,
    ): String {
        val lines =
            mutableListOf(
                "<b>$number. ${escapeHtml(eventLabel(round.eventType))}</b>",
                "Время: <code>${timeFormatter.format(round.scheduledAt)}</code>",
                "Закрытие: <code>${timeFormatter.format(round.closesAt)}</code>",
                "Да ×${odds(round.yesOdds)} · Нет ×${odds(round.noOdds)} · ${statusLabel(round.status)}",
            )

        round.myBet?.let { bet ->
            lines += "Моя ставка: <b>${sideLabel(bet.side)}</b> · ${bet.confirmedAmount} COIN${pendingAmount(bet)}"
        }

        round.outcome?.let { outcome ->
            lines += "Результат: <b>${escapeHtml(outcome.lowercase())}</b>"
        }

        return lines.joinToString("\n")
    }

    private fun renderRace(
        number: Int,
        race: TelegramItemRace,
    ): String {
        val lines =
            mutableListOf(
                "<b>$number. ${escapeHtml(race.itemName)}</b> · ${escapeHtml(race.itemType.lowercase())}",
                "Вход: <b>${race.entryAmount} COIN</b> · комиссия ${race.feePercent}%",
                "Регистрация до: <code>${timeFormatter.format(race.registrationClosesAt)}</code>",
                "Старт: <code>${timeFormatter.format(race.startsAt)}</code> · ${statusLabel(race.status)}",
            )

        race.myPosition?.let { position ->
            lines += "Моя позиция: <b>${position.confirmedAmount} COIN</b>${pendingAmount(position)}"
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

    private fun renderHistoryRacePosition(
        number: Int,
        position: TelegramItemRacePosition,
    ): String =
        listOf(
            "<b>$number. ${escapeHtml(position.accountNickname)}</b> · ${position.confirmedAmount} COIN${pendingAmount(position)}",
            "Статус: ${escapeHtml(position.status.lowercase())}",
            "Race: <code>${position.raceId.toString().take(8)}</code>",
        ).joinToString("\n")

    private fun pendingAmount(bet: TelegramEventBet): String = pendingAmount(bet.pendingAmount)

    private fun pendingAmount(position: TelegramItemRacePosition): String = pendingAmount(position.pendingAmount)

    private fun pendingAmount(amount: Long): String =
        if (amount > 0) {
            " · ожидает $amount"
        } else {
            ""
        }

    private fun modeActions(current: BetMenuMode): List<OutboundTelegramMessageAction> =
        buildList {
            if (current != BetMenuMode.ROUNDS) {
                add(callback("🎲 События", BetCallbackData.ROUNDS))
            }
            if (current != BetMenuMode.RACES) {
                add(callback("🏁 Item Race", BetCallbackData.RACES))
            }
            if (current != BetMenuMode.HISTORY) {
                add(callback("📜 История", BetCallbackData.HISTORY))
            }
        }

    private fun accountRequiredMessage(feedback: BetMenuFeedback?): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "🎲 <b>Ставки</b>",
                "",
                "Для ставок нужен активный Cristalix-аккаунт Mnemos.",
                "Его можно создать, подключить или переключить прямо в Telegram.",
            )
        addFeedback(lines, feedback)

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
            actions = listOf(callback("🔄 Обновить", BetCallbackData.REFRESH)) + navigationActions,
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
            BetMenuFeedback.PENDING -> "⏳ Операция ещё обрабатывается. Нажмите «Обновить», чтобы проверить статус."
            BetMenuFeedback.INVALID_AMOUNT -> "⚠️ Операция отклонена. Проверьте сумму, баланс, права и состояние рынка."
            BetMenuFeedback.ACCOUNT_REQUIRED -> "⚠️ Активный аккаунт не найден."
            BetMenuFeedback.UNAVAILABLE -> "⚠️ Не удалось проверить или выполнить операцию. Попробуйте ещё раз."
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
    private const val REGISTRATION_STATUS = "REGISTRATION"
    private const val HISTORY_LIMIT = 20

    private val amountPromptActions =
        listOf(
            callback("✖ Отмена", BetCallbackData.CANCEL_INPUT),
            callback("⌂ Меню", TelegramMenuScreen.HOME.callbackData),
        )

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
