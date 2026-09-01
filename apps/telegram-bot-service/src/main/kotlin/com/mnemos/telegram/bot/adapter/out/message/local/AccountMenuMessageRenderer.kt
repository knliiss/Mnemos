package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.account.AccountCallbackData
import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedback
import com.mnemos.telegram.bot.application.account.model.AccountMenuFeedbackType
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountInputMode
import com.mnemos.telegram.bot.application.account.model.TelegramAccountStatus
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSummary
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode

object AccountMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val accountData =
            data as? TelegramMenuScreenData.Account
                ?: return unavailableMessage()

        return when (val result = accountData.data.result) {
            is TelegramAccountContextResult.Available ->
                availableMessage(
                    context = result.context,
                    balance = accountData.data.balance,
                    feedback = accountData.data.feedback,
                    inputMode = accountData.data.inputMode,
                )

            TelegramAccountContextResult.RegistrationRequired ->
                registrationRequiredMessage(
                    feedback = accountData.data.feedback,
                    inputMode = accountData.data.inputMode,
                )

            TelegramAccountContextResult.Unavailable ->
                unavailableMessage(
                    feedback = accountData.data.feedback,
                    inputMode = accountData.data.inputMode,
                )
        }
    }

    private fun availableMessage(
        context: TelegramAccountContext,
        balance: TelegramAccountBalanceResult,
        feedback: AccountMenuFeedback?,
        inputMode: TelegramAccountInputMode?,
    ): OutboundTelegramMessage {
        val activeAccount = context.accounts.firstOrNull { account -> account.active }
        val lines =
            mutableListOf(
                "👤 <b>Аккаунты</b>",
                MESSAGE_DIVIDER,
                "",
                "🔑 <b>Активный Cristalix-аккаунт</b>",
                activeAccountSection(activeAccount),
                "",
            )

        lines += balanceSection(balance)
        lines += ""
        lines += "👥 <b>Доступные аккаунты</b>"
        lines += "└ ${context.accounts.size}"

        if (context.accounts.isNotEmpty()) {
            lines += ""
            lines += accountLines(context)
        }

        appendInputPrompt(lines, inputMode)
        appendFeedback(lines, feedback)

        val rows = availableRows(context, inputMode)

        return OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

    private fun registrationRequiredMessage(
        feedback: AccountMenuFeedback?,
        inputMode: TelegramAccountInputMode?,
    ): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "👤 <b>Аккаунты</b>",
                MESSAGE_DIVIDER,
                "",
                "🔑 <b>Активный Cristalix-аккаунт</b>",
                "└ Не подключён",
                "",
                "Создайте Cristalix-аккаунт или присоединитесь по приглашению.",
            )

        appendInputPrompt(lines, inputMode)
        appendFeedback(lines, feedback)

        val rows = setupRows(inputMode) + listOf(listOf(backAction))

        return OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

    private fun unavailableMessage(
        feedback: AccountMenuFeedback? = null,
        inputMode: TelegramAccountInputMode? = null,
    ): OutboundTelegramMessage {
        val lines =
            mutableListOf(
                "👤 <b>Аккаунты</b>",
                MESSAGE_DIVIDER,
                "",
                "⚠️ Не удалось получить данные аккаунта.",
            )

        appendInputPrompt(lines, inputMode)
        appendFeedback(lines, feedback)

        val rows =
            setupRows(inputMode) +
                listOf(
                    listOf(refreshAction),
                    listOf(backAction),
                )

        return OutboundTelegramMessage(
            text = lines.joinToString("\n"),
            parseMode = OutboundTelegramMessageParseMode.HTML,
            actions = rows.flatten(),
            actionRows = rows,
        )
    }

    private fun availableRows(
        context: TelegramAccountContext,
        inputMode: TelegramAccountInputMode?,
    ): List<List<OutboundTelegramMessageAction>> {
        if (inputMode != null) {
            return setupRows(inputMode) + listOf(listOf(backAction))
        }

        return buildList {
            add(listOf(dailyAction))
            add(listOf(primeAction))

            if (context.activeAccountId != null) {
                add(listOf(invitesAction))
            }

            accountSwitchActions(context).forEach { action ->
                add(listOf(action))
            }

            addAll(setupRows(null))
            add(listOf(refreshAction))
            add(listOf(backAction))
        }
    }

    private fun activeAccountSection(account: TelegramAccountSummary?): String =
        account
            ?.let { activeAccount ->
                "└ <code>${escapeHtml(activeAccount.cristalixNickname)}</code>"
            } ?: "└ Не выбран"

    private fun balanceSection(balance: TelegramAccountBalanceResult): List<String> =
        when (balance) {
            is TelegramAccountBalanceResult.Available ->
                listOf(
                    "💰 <b>COIN активного аккаунта</b>",
                    "└ <b>${balance.balance.coin}</b> COIN",
                    "",
                    "⭐ <b>PRIME пользователя Mnemos</b>",
                    "└ <b>${balance.balance.prime}</b> PRIME",
                    "   PRIME общий для вас и не меняется при переключении Cristalix-аккаунта.",
                )

            TelegramAccountBalanceResult.NotRequired ->
                listOf(
                    "💰 <b>COIN активного аккаунта</b>",
                    "└ Выберите Cristalix-аккаунт",
                    "",
                    "⭐ <b>PRIME пользователя Mnemos</b>",
                    "└ Откройте раздел PRIME",
                )

            TelegramAccountBalanceResult.Unavailable ->
                listOf(
                    "💰 <b>COIN активного аккаунта</b>",
                    "└ Временно недоступен",
                    "",
                    "⭐ <b>PRIME пользователя Mnemos</b>",
                    "└ Временно недоступен",
                )
        }

    private fun accountLines(context: TelegramAccountContext): String =
        context.accounts
            .joinToString("\n") { account -> renderAccountLine(account) }
            .ifBlank {
                "Cristalix-аккаунты пока не подключены."
            }

    private fun renderAccountLine(account: TelegramAccountSummary): String {
        val marker =
            when {
                account.active -> "🔑"
                account.status == TelegramAccountStatus.DEACTIVATED -> "⛔"
                else -> "•"
            }
        val relation = if (account.owner) "владелец" else "участник"
        val status =
            if (account.status == TelegramAccountStatus.DEACTIVATED) {
                " · деактивирован"
            } else {
                ""
            }
        val nickname = escapeHtml(account.cristalixNickname)
        val memberCount = "${account.memberCount}/${account.maxMembers}"

        return "$marker <code>$nickname</code> · $relation · $memberCount$status"
    }

    private fun accountSwitchActions(context: TelegramAccountContext): List<OutboundTelegramMessageAction> =
        context.accounts
            .filter { account -> !account.active && account.status == TelegramAccountStatus.ACTIVE }
            .map { account ->
                OutboundTelegramMessageAction.Callback(
                    label = "🔄 Выбрать ${account.cristalixNickname}",
                    data = AccountCallbackData.switch(account.accountId),
                    style = OutboundTelegramButtonStyle.PRIMARY,
                )
            }

    private fun setupRows(inputMode: TelegramAccountInputMode?): List<List<OutboundTelegramMessageAction>> =
        if (inputMode == null) {
            listOf(
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "➕ Создать аккаунт",
                        data = AccountCallbackData.CREATE,
                        style = OutboundTelegramButtonStyle.PRIMARY,
                    ),
                ),
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "🔗 Присоединиться по инвайту",
                        data = AccountCallbackData.JOIN,
                        style = OutboundTelegramButtonStyle.PRIMARY,
                    ),
                ),
            )
        } else {
            listOf(
                listOf(
                    OutboundTelegramMessageAction.Callback(
                        label = "✖ Отменить ввод",
                        data = AccountCallbackData.CANCEL_INPUT,
                        style = OutboundTelegramButtonStyle.DANGER,
                    ),
                ),
            )
        }

    private fun appendInputPrompt(
        lines: MutableList<String>,
        inputMode: TelegramAccountInputMode?,
    ) {
        val prompt =
            when (inputMode) {
                TelegramAccountInputMode.CREATE ->
                    "Отправьте <b>ник Cristalix</b> обычным сообщением."

                TelegramAccountInputMode.JOIN ->
                    "Отправьте <b>код приглашения</b> обычным сообщением."

                null -> null
            } ?: return

        lines += ""
        lines += "✏️ $prompt"
    }

    private fun appendFeedback(
        lines: MutableList<String>,
        feedback: AccountMenuFeedback?,
    ) {
        val feedbackText = renderFeedback(feedback) ?: return

        lines += ""
        lines += feedbackText
    }

    private fun renderFeedback(feedback: AccountMenuFeedback?): String? =
        when (feedback?.type) {
            AccountMenuFeedbackType.SWITCHED -> "✅ Активный аккаунт переключён."
            AccountMenuFeedbackType.CREATED -> "✅ Cristalix-аккаунт создан и выбран активным."
            AccountMenuFeedbackType.JOINED -> "✅ Вы присоединились к аккаунту, он выбран активным."
            AccountMenuFeedbackType.CREATE_INVALID ->
                "⚠️ Не удалось создать аккаунт. Проверьте ник Cristalix и отправьте его ещё раз."

            AccountMenuFeedbackType.JOIN_INVALID ->
                "⚠️ Код приглашения недействителен или больше недоступен. Отправьте другой код."

            AccountMenuFeedbackType.UNAVAILABLE ->
                "⚠️ Сервис временно недоступен. Ввод сохранён — можно повторить сообщение позже."

            null -> null
        }

    private fun escapeHtml(value: String): String =
        value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")

    private const val MESSAGE_DIVIDER = "━━━━━━━━━━━━━━━━━━━━"

    private val dailyAction =
        OutboundTelegramMessageAction.Callback(
            label = "🎁 Daily аккаунта",
            data = TelegramMenuScreen.DAILY.callbackData,
            style = OutboundTelegramButtonStyle.SUCCESS,
        )

    private val primeAction =
        OutboundTelegramMessageAction.Callback(
            label = "⭐ PRIME пользователя",
            data = TelegramMenuScreen.PRIME.callbackData,
            style = OutboundTelegramButtonStyle.PRIMARY,
        )

    private val invitesAction =
        OutboundTelegramMessageAction.Callback(
            label = "🔗 Приглашения аккаунта",
            data = TelegramMenuScreen.ACCOUNT_INVITES.callbackData,
            style = OutboundTelegramButtonStyle.PRIMARY,
        )

    private val refreshAction =
        OutboundTelegramMessageAction.Callback(
            label = "🔄 Обновить",
            data = AccountCallbackData.REFRESH,
        )

    private val backAction =
        OutboundTelegramMessageAction.Callback(
            label = "← Назад",
            data = TelegramMenuScreen.BACK_CALLBACK_DATA,
        )
}
