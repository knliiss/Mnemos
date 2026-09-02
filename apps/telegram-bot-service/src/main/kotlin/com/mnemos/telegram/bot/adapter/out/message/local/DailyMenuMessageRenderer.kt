package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.daily.DailyCallbackData
import com.mnemos.telegram.bot.application.daily.model.DailyMenuFeedback
import com.mnemos.telegram.bot.application.daily.model.DailyMenuFeedbackType
import com.mnemos.telegram.bot.application.daily.model.DailyRewardState
import com.mnemos.telegram.bot.application.daily.model.DailyRewardStateResult
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import java.util.Locale

object DailyMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val dailyData =
            data as? TelegramMenuScreenData.Daily
                ?: return unavailableMessage()

        return when (val result = dailyData.data.result) {
            is DailyRewardStateResult.Available -> availableMessage(result.state, dailyData.data.feedback)
            DailyRewardStateResult.AccountRequired -> accountRequiredMessage()
            DailyRewardStateResult.Unavailable -> unavailableMessage()
        }
    }

    private fun availableMessage(
        state: DailyRewardState,
        feedback: DailyMenuFeedback?,
    ): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "🎁 <b>Daily</b>",
                MESSAGE_DIVIDER,
                "",
                "🔥 <b>Серия</b>",
                "└ ${state.currentStreak} дней",
                "",
                "🎁 <b>Сегодня</b>",
                "└ ${if (state.claimedToday) "Награда получена" else "Награда доступна"}",
                "",
                "💰 <b>Следующая награда</b>",
                "├ ${state.nextCoinMin}–${state.nextCoinMax} COIN",
                "└ PRIME: ${formatPercent(state.primeChance)}",
                "",
                "♻️ <b>Восстановление</b>",
                "└ Осталось: ${state.restoresRemaining}",
            )

        if (state.recoverable) {
            lines += "   Серия для восстановления: ${state.recoverableStreak}"
        }

        renderFeedback(feedback)?.let { feedbackText ->
            lines += ""
            lines += feedbackText
        }

        val rows = availableRows(state)

        return OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

    private fun availableRows(state: DailyRewardState): List<List<OutboundTelegramMessageAction>> =
        buildList {
            if (!state.claimedToday) {
                add(
                    listOf(
                        OutboundTelegramMessageAction.Callback(
                            label = "🎁 Забрать награду",
                            data = DailyCallbackData.CLAIM,
                            style = OutboundTelegramButtonStyle.SUCCESS,
                            accented = true,
                        ),
                    ),
                )
            }

            if (state.recoverable && state.restoresRemaining > 0) {
                add(
                    listOf(
                        OutboundTelegramMessageAction.Callback(
                            label = "♻️ Восстановить серию",
                            data = DailyCallbackData.RESTORE,
                            style = OutboundTelegramButtonStyle.PRIMARY,
                        ),
                    ),
                )
            }

            add(listOf(refreshAction))
            add(listOf(backAction))
        }

    private fun renderFeedback(feedback: DailyMenuFeedback?): String? =
        when (feedback?.type) {
            DailyMenuFeedbackType.CLAIMED -> {
                val coinText = feedback.coins?.let { coins -> "$coins COIN" } ?: "награда"
                val primeText =
                    feedback.primes
                        ?.takeIf { primes -> primes > 0 }
                        ?.let { primes -> " + $primes PRIME" }
                        .orEmpty()

                "✅ Получено: <b>$coinText$primeText</b>. Серия: <b>${feedback.streak ?: 0}</b>."
            }

            DailyMenuFeedbackType.RESTORED ->
                "✅ Серия восстановлена: <b>${feedback.streak ?: 0}</b>."

            DailyMenuFeedbackType.UNAVAILABLE ->
                "⚠️ Не удалось выполнить действие. Попробуйте ещё раз."

            null -> null
        }

    private fun accountRequiredMessage(): OutboundTelegramMessage {
        val rows =
            listOf(
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "👤 Аккаунт",
                        data = TelegramMenuScreen.ACCOUNT.callbackData,
                        style = OutboundTelegramButtonStyle.PRIMARY,
                    ),
                ),
                listOf(backAction),
            )

        return OutboundTelegramMessage(
            text =
                listOf(
                    "🎁 <b>Daily</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "⚠️ Нужен активный Cristalix-аккаунт.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

    private fun unavailableMessage(): OutboundTelegramMessage {
        val rows =
            listOf(
                listOf(refreshAction),
                listOf(backAction),
            )

        return OutboundTelegramMessage(
            text =
                listOf(
                    "🎁 <b>Daily</b>",
                    MESSAGE_DIVIDER,
                    "",
                    "⚠️ Не удалось получить состояние Daily.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

    private fun formatPercent(value: Double): String = String.format(Locale.US, "%.2f%%", value * 100.0)

    private const val MESSAGE_DIVIDER = "━━━━━━━━━━━━━━━━━━━━"

    private val refreshAction =
        OutboundTelegramMessageAction.Callback(
            label = "🔄 Обновить",
            data = DailyCallbackData.REFRESH,
        )

    private val backAction =
        OutboundTelegramMessageAction.Callback(
            label = "← Назад",
            data = TelegramMenuScreen.BACK_CALLBACK_DATA,
        )
}
