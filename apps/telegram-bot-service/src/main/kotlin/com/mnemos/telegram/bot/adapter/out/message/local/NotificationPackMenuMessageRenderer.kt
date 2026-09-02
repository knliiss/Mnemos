package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.pack.NotificationPackCallbackData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuFeedback
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuMode
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuScreenData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackStoreResult
import com.mnemos.telegram.bot.application.pack.model.OwnedNotificationPacksResult
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackStoreItem

object NotificationPackMenuMessageRenderer {
    fun render(data: TelegramMenuScreenData): OutboundTelegramMessage {
        val packsData =
            data as? TelegramMenuScreenData.Packs
                ?: return unavailableMessage()

        return when (packsData.data.mode) {
            NotificationPackMenuMode.STORE -> renderStore(packsData.data)
            NotificationPackMenuMode.OWNED -> renderOwned(packsData.data)
            NotificationPackMenuMode.DETAIL -> renderDetail(packsData.data)
        }
    }

    private fun renderStore(data: NotificationPackMenuScreenData): OutboundTelegramMessage =
        when (val result = data.store) {
            is NotificationPackStoreResult.Available -> {
                val page = result.page
                val pageLabel =
                    if (page.totalPages > 0) {
                        "Страница <b>${page.page + 1}/${page.totalPages}</b> · ${page.totalItems} паков"
                    } else {
                        "Опубликованных паков пока нет"
                    }
                val rows = mutableListOf<List<OutboundTelegramMessageAction>>()

                rows += modeRow(NotificationPackMenuMode.STORE)

                page.packs.forEach { pack ->
                    rows +=
                        listOf(
                            callback(
                                label = storeItemLabel(pack),
                                data = NotificationPackCallbackData.item(pack.personalizationId),
                                style = if (pack.active) OutboundTelegramButtonStyle.SUCCESS else OutboundTelegramButtonStyle.DEFAULT,
                            ),
                        )
                }

                paginationRow(page.page, page.totalPages)?.let(rows::add)
                rows += listOf(refreshAction)
                rows += listOf(backAction)

                message(
                    text =
                        listOfNotNull(
                            "🎨 <b>Паки уведомлений</b>",
                            DIVIDER,
                            feedbackText(data.feedback),
                            "",
                            "🛍 <b>Магазин</b>",
                            pageLabel,
                            "",
                            "Выберите пак, чтобы посмотреть примеры и цену.",
                        ).joinToString("\n"),
                    rows = rows,
                )
            }

            NotificationPackStoreResult.RegistrationRequired -> registrationRequiredMessage(data.feedback)
            NotificationPackStoreResult.Unavailable,
            null,
            -> unavailableMessage(data.feedback)
        }

    private fun renderOwned(data: NotificationPackMenuScreenData): OutboundTelegramMessage =
        when (val result = data.owned) {
            is OwnedNotificationPacksResult.Available -> {
                val rows = mutableListOf<List<OutboundTelegramMessageAction>>()

                rows += modeRow(NotificationPackMenuMode.OWNED)

                result.packs.forEach { pack ->
                    rows +=
                        listOf(
                            callback(
                                label = if (pack.active) "✅ ${pack.name}" else "🎨 ${pack.name}",
                                data = NotificationPackCallbackData.apply(pack.personalizationId),
                                style = if (pack.active) OutboundTelegramButtonStyle.SUCCESS else OutboundTelegramButtonStyle.PRIMARY,
                            ),
                        )
                }

                rows += listOf(refreshAction)
                rows += listOf(backAction)

                message(
                    text =
                        listOfNotNull(
                            "🎨 <b>Паки уведомлений</b>",
                            DIVIDER,
                            feedbackText(data.feedback),
                            "",
                            "🎒 <b>Мои паки</b>",
                            if (result.packs.isEmpty()) {
                                "У вас пока нет купленных паков."
                            } else {
                                "Нажмите на пак, чтобы сделать его активным."
                            },
                        ).joinToString("\n"),
                    rows = rows,
                )
            }

            OwnedNotificationPacksResult.RegistrationRequired -> registrationRequiredMessage(data.feedback)
            OwnedNotificationPacksResult.Unavailable,
            null,
            -> unavailableMessage(data.feedback)
        }

    private fun renderDetail(data: NotificationPackMenuScreenData): OutboundTelegramMessage {
        val result = data.store as? NotificationPackStoreResult.Available
            ?: return unavailableMessage(data.feedback)
        val selectedId = data.selectedPersonalizationId
        val pack = result.page.packs.firstOrNull { item -> item.personalizationId == selectedId }
            ?: return missingPackMessage(data.feedback)
        val rows = mutableListOf<List<OutboundTelegramMessageAction>>()

        when {
            pack.active ->
                rows +=
                    listOf(
                        callback(
                            label = "✅ Активен",
                            data = NotificationPackCallbackData.REFRESH,
                            style = OutboundTelegramButtonStyle.SUCCESS,
                        ),
                    )

            pack.owned ->
                rows +=
                    listOf(
                        callback(
                            label = "✅ Применить",
                            data = NotificationPackCallbackData.apply(pack.personalizationId),
                            style = OutboundTelegramButtonStyle.SUCCESS,
                        ),
                    )

            else ->
                rows +=
                    listOf(
                        callback(
                            label = "💎 Купить · ${pack.priceAmount} ${pack.currency}",
                            data = NotificationPackCallbackData.purchase(pack.personalizationId),
                            style = OutboundTelegramButtonStyle.PRIMARY,
                        ),
                    )
        }

        rows +=
            listOf(
                callback(
                    label = "🛍 К магазину",
                    data = NotificationPackCallbackData.STORE,
                ),
                callback(
                    label = "🎒 Мои",
                    data = NotificationPackCallbackData.OWNED,
                ),
            )
        rows += listOf(backAction)

        val sampleLines =
            pack.samples
                .mapIndexed { index, sample ->
                    "${index + 1}. ${escapeHtml(sample)}"
                }
                .joinToString("\n")

        return message(
            text =
                listOfNotNull(
                    "🎨 <b>${escapeHtml(pack.name)}</b>",
                    DIVIDER,
                    feedbackText(data.feedback),
                    "",
                    "Автор: <b>${escapeHtml(pack.author)}</b>",
                    "Цена: <b>${pack.priceAmount} ${escapeHtml(pack.currency)}</b>",
                    if (pack.active) "Статус: <b>активен</b>" else if (pack.owned) "Статус: <b>куплен</b>" else null,
                    "",
                    "<b>Примеры</b>",
                    sampleLines.ifBlank { "Примеры для этого пака не указаны." },
                ).joinToString("\n"),
            rows = rows,
        )
    }

    private fun modeRow(activeMode: NotificationPackMenuMode): List<OutboundTelegramMessageAction> =
        listOf(
            callback(
                label = "🛍 Магазин",
                data = NotificationPackCallbackData.STORE,
                style = if (activeMode == NotificationPackMenuMode.STORE) OutboundTelegramButtonStyle.PRIMARY else OutboundTelegramButtonStyle.DEFAULT,
            ),
            callback(
                label = "🎒 Мои",
                data = NotificationPackCallbackData.OWNED,
                style = if (activeMode == NotificationPackMenuMode.OWNED) OutboundTelegramButtonStyle.PRIMARY else OutboundTelegramButtonStyle.DEFAULT,
            ),
        )

    private fun paginationRow(
        page: Int,
        totalPages: Int,
    ): List<OutboundTelegramMessageAction>? {
        if (totalPages <= 1) {
            return null
        }

        return listOfNotNull(
            if (page > 0) {
                callback(
                    label = "← ${page}",
                    data = NotificationPackCallbackData.page(page - 1),
                )
            } else {
                null
            },
            if (page + 1 < totalPages) {
                callback(
                    label = "${page + 2} →",
                    data = NotificationPackCallbackData.page(page + 1),
                )
            } else {
                null
            },
        ).takeIf(List<OutboundTelegramMessageAction>::isNotEmpty)
    }

    private fun storeItemLabel(pack: TelegramNotificationPackStoreItem): String {
        val prefix =
            when {
                pack.active -> "✅"
                pack.owned -> "🎒"
                else -> "🎨"
            }

        return "$prefix ${pack.name} · ${pack.priceAmount} ${pack.currency}"
    }

    private fun feedbackText(feedback: NotificationPackMenuFeedback?): String? =
        when (feedback) {
            NotificationPackMenuFeedback.PURCHASE_ACCEPTED -> "✅ Покупка принята. После завершения пак появится в «Мои»."
            NotificationPackMenuFeedback.PURCHASE_REFRESH -> "ℹ️ Состояние пака изменилось. Обновите список."
            NotificationPackMenuFeedback.PURCHASE_UNAVAILABLE -> "⚠️ Не удалось отправить покупку. Попробуйте ещё раз."
            NotificationPackMenuFeedback.APPLIED -> "✅ Пак уведомлений активирован."
            NotificationPackMenuFeedback.APPLY_REFRESH -> "ℹ️ Пак уже изменился или больше недоступен. Обновите список."
            NotificationPackMenuFeedback.APPLY_UNAVAILABLE -> "⚠️ Не удалось применить пак. Попробуйте ещё раз."
            null -> null
        }

    private fun registrationRequiredMessage(feedback: NotificationPackMenuFeedback?): OutboundTelegramMessage =
        message(
            text =
                listOfNotNull(
                    "🎨 <b>Паки уведомлений</b>",
                    DIVIDER,
                    feedbackText(feedback),
                    "",
                    "Сначала зарегистрируйте аккаунт Mnemos.",
                ).joinToString("\n"),
            rows =
                listOf(
                    listOf(OutboundTelegramMessageAction.OpenMiniApp("✨ Открыть Mnemos")),
                    listOf(backAction),
                ),
        )

    private fun unavailableMessage(feedback: NotificationPackMenuFeedback? = null): OutboundTelegramMessage =
        message(
            text =
                listOfNotNull(
                    "🎨 <b>Паки уведомлений</b>",
                    DIVIDER,
                    feedbackText(feedback),
                    "",
                    "Магазин паков временно недоступен.",
                ).joinToString("\n"),
            rows =
                listOf(
                    listOf(refreshAction),
                    listOf(backAction),
                ),
        )

    private fun missingPackMessage(feedback: NotificationPackMenuFeedback?): OutboundTelegramMessage =
        message(
            text =
                listOfNotNull(
                    "🎨 <b>Паки уведомлений</b>",
                    DIVIDER,
                    feedbackText(feedback),
                    "",
                    "Этот пак больше не находится на текущей странице магазина.",
                ).joinToString("\n"),
            rows =
                listOf(
                    listOf(
                        callback(
                            label = "🛍 К магазину",
                            data = NotificationPackCallbackData.STORE,
                            style = OutboundTelegramButtonStyle.PRIMARY,
                        ),
                    ),
                    listOf(backAction),
                ),
        )

    private fun message(
        text: String,
        rows: List<List<OutboundTelegramMessageAction>>,
    ): OutboundTelegramMessage =
        OutboundTelegramMessage(
            text = text,
            parseMode = OutboundTelegramMessageParseMode.HTML,
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

    private val refreshAction =
        callback(
            label = "↻ Обновить",
            data = NotificationPackCallbackData.REFRESH,
        )

    private val backAction =
        callback(
            label = "← Назад",
            data = TelegramMenuScreen.BACK_CALLBACK_DATA,
        )

    private const val DIVIDER = "━━━━━━━━━━━━━━━━━━━━"
}
