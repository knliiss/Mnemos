package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.daily.DailyCallbackData
import com.mnemos.telegram.bot.application.daily.model.DailyMenuFeedback
import com.mnemos.telegram.bot.application.daily.model.DailyMenuFeedbackType
import com.mnemos.telegram.bot.application.daily.model.DailyRewardState
import com.mnemos.telegram.bot.application.daily.model.DailyRewardStateResult
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
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
                "",
                "Серия: 🔥 <b>${state.currentStreak}</b>",
                "Сегодня: <b>${if (state.claimedToday) "награда получена" else "награда доступна"}</b>",
                "Следующая награда: <b>${state.nextCoinMin}–${state.nextCoinMax} COIN</b>",
                "Шанс PRIME: <b>${formatPercent(state.primeChance)}</b>",
                "Восстановлений в этом месяце: <b>${state.restoresRemaining}</b>",
            )

        if (state.recoverable) {
            lines += ""
            lines += "Можно восстановить серию <b>${state.recoverableStreak}</b>."
        }

        renderFeedback(feedback)?.let { feedbackText ->
            lines += ""
            lines += feedbackText
        }

        return OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = availableActions(state),
        )
    }

    private fun availableActions(state: DailyRewardState): List<OutboundTelegramMessageAction> {
        val actions = mutableListOf<OutboundTelegramMessageAction>()

        if (!state.claimedToday) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "🎁 Забрать награду",
                    data = DailyCallbackData.CLAIM,
                )
        }

        if (state.recoverable && state.restoresRemaining > 0) {
            actions +=
                OutboundTelegramMessageAction.Callback(
                    label = "♻️ Восстановить серию",
                    data = DailyCallbackData.RESTORE,
                )
        }

        actions += refreshAction
        actions += navigationActions

        return actions
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

    private fun accountRequiredMessage(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                listOf(
                    "🎁 <b>Daily</b>",
                    "",
                    "Для Daily нужен активный Cristalix-аккаунт.",
                    "Откройте Mini App, если аккаунт ещё не настроен.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = navigationActions,
        )

    private fun unavailableMessage(): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text =
                listOf(
                    "🎁 <b>Daily</b>",
                    "",
                    "Не удалось получить состояние Daily. Попробуйте обновить через несколько секунд.",
                ).joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = listOf(refreshAction) + navigationActions,
        )

    private fun formatPercent(value: Double): String = String.format(Locale.US, "%.2f%%", value * 100.0)

    private val refreshAction =
        OutboundTelegramMessageAction.Callback(
            label = "🔄 Обновить",
            data = DailyCallbackData.REFRESH,
        )

    private val navigationActions =
        listOf(
            OutboundTelegramMessageAction.Callback(
                "← Назад",
                TelegramMenuScreen.BACK_CALLBACK_DATA,
            ),
            OutboundTelegramMessageAction.Callback(
                "⌂ Меню",
                TelegramMenuScreen.HOME.callbackData,
            ),
            OutboundTelegramMessageAction.OpenMiniApp("↗️ Открыть Mini App"),
        )
}
