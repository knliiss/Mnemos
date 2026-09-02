package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageParseMode
import com.mnemos.telegram.bot.application.pack.NotificationPackCallbackData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackAggregatePreviewMode
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuFeedback
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuMode
import com.mnemos.telegram.bot.application.pack.model.NotificationPackMenuScreenData
import com.mnemos.telegram.bot.application.pack.model.NotificationPackStoreResult
import com.mnemos.telegram.bot.application.pack.model.OwnedNotificationPacksResult
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationPackStoreItem
import com.mnemos.telegram.bot.application.pack.model.TelegramNotificationTemplateVariant

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
                            "Выберите пак, чтобы открыть полный интерактивный предпросмотр.",
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
        val preview =
            NotificationPackPreviewFormatter.render(
                preview = pack.preview,
                selectedType = data.selectedTemplateType,
                selectedVariantId = data.selectedVariantId,
                aggregateMode = data.aggregatePreviewMode,
            )
        val rows = mutableListOf<List<OutboundTelegramMessageAction>>()

        rows += templateRows(preview.selectedType)
        rows += variantRows(pack, preview.selectedType, preview.selectedVariant)
        rows += aggregateModeRow(data.aggregatePreviewMode)
        rows += purchaseOrApplyRow(pack)
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

        return message(
            text = renderDetailText(data, pack, preview),
            rows = rows,
        )
    }

    private fun renderDetailText(
        data: NotificationPackMenuScreenData,
        pack: TelegramNotificationPackStoreItem,
        preview: NotificationPackRenderedPreview,
    ): String {
        val variantLabel =
            preview.selectedVariant
                ?.let(NotificationPackPreviewFormatter::variantLabel)
                ?: "Базовый"
        val aggregateLabel =
            when (data.aggregatePreviewMode) {
                NotificationPackAggregatePreviewMode.SAME_TYPE -> "один тип"
                NotificationPackAggregatePreviewMode.MIXED -> "смешанный"
            }
        val aggregateBody =
            preview.aggregateLines
                .joinToString("\n\n") { line -> escapeHtml(line) }
        val status =
            when {
                pack.active -> "Статус: <b>активен</b>"
                pack.owned -> "Статус: <b>куплен</b>"
                else -> null
            }

        return listOfNotNull(
            "🎨 <b>${escapeHtml(pack.name)}</b>",
            DIVIDER,
            feedbackText(data.feedback),
            "",
            "Автор: <b>${escapeHtml(pack.author)}</b>",
            "Цена: <b>${pack.priceAmount} ${escapeHtml(pack.currency)}</b>",
            status,
            "",
            "Событие: <b>${escapeHtml(templateLabel(preview.selectedType))}</b>",
            "Вариант: <b>${escapeHtml(variantLabel)}</b>",
            "",
            "📨 <b>Обычное сообщение</b>",
            "<b>${escapeHtml(preview.standardTitle)}</b>",
            escapeHtml(preview.standardBody),
            "",
            "🧩 <b>Агрегированное · $aggregateLabel</b>",
            "<b>${escapeHtml(preview.aggregateTitle)}</b>",
            aggregateBody,
        ).joinToString("\n")
    }

    private fun templateRows(selectedType: NotificationPackTemplateType): List<List<OutboundTelegramMessageAction>> =
        NotificationPackPreviewFormatter.templateTypes
            .map { type ->
                callback(
                    label = templateButtonLabel(type),
                    data = NotificationPackCallbackData.template(type),
                    style = selectedStyle(type == selectedType),
                )
            }.chunked(TEMPLATE_BUTTONS_PER_ROW)

    private fun variantRows(
        pack: TelegramNotificationPackStoreItem,
        selectedType: NotificationPackTemplateType,
        selectedVariant: TelegramNotificationTemplateVariant?,
    ): List<List<OutboundTelegramMessageAction>> {
        val template = pack.preview.templates[selectedType] ?: return emptyList()

        if (template.variants.isEmpty()) {
            return emptyList()
        }

        val actions =
            buildList {
                add(
                    callback(
                        label = "Базовый",
                        data = NotificationPackCallbackData.BASE_VARIANT,
                        style = selectedStyle(selectedVariant == null),
                    ),
                )

                template.variants.forEach { variant ->
                    add(
                        callback(
                            label = NotificationPackPreviewFormatter.variantLabel(variant),
                            data = NotificationPackCallbackData.variant(variant.id),
                            style = selectedStyle(selectedVariant?.id == variant.id),
                        ),
                    )
                }
            }
        val hasLongLabel = actions.maxOf { action -> action.label.length } > LONG_VARIANT_LABEL_LENGTH

        return if (hasLongLabel) {
            actions.map(::listOf)
        } else {
            actions.chunked(VARIANT_BUTTONS_PER_ROW)
        }
    }

    private fun aggregateModeRow(activeMode: NotificationPackAggregatePreviewMode): List<OutboundTelegramMessageAction> =
        listOf(
            callback(
                label = "🧩 Один тип",
                data = NotificationPackCallbackData.aggregateMode(NotificationPackAggregatePreviewMode.SAME_TYPE),
                style = selectedStyle(activeMode == NotificationPackAggregatePreviewMode.SAME_TYPE),
            ),
            callback(
                label = "🌐 Смешанный",
                data = NotificationPackCallbackData.aggregateMode(NotificationPackAggregatePreviewMode.MIXED),
                style = selectedStyle(activeMode == NotificationPackAggregatePreviewMode.MIXED),
            ),
        )

    private fun purchaseOrApplyRow(pack: TelegramNotificationPackStoreItem): List<OutboundTelegramMessageAction> =
        when {
            pack.active ->
                listOf(
                    callback(
                        label = "✅ Активен",
                        data = NotificationPackCallbackData.REFRESH,
                        style = OutboundTelegramButtonStyle.SUCCESS,
                    ),
                )

            pack.owned ->
                listOf(
                    callback(
                        label = "✅ Применить",
                        data = NotificationPackCallbackData.apply(pack.personalizationId),
                        style = OutboundTelegramButtonStyle.SUCCESS,
                    ),
                )

            else ->
                listOf(
                    callback(
                        label = "💎 Купить · ${pack.priceAmount} ${pack.currency}",
                        data = NotificationPackCallbackData.purchase(pack.personalizationId),
                        style = OutboundTelegramButtonStyle.PRIMARY,
                    ),
                )
        }

    private fun modeRow(activeMode: NotificationPackMenuMode): List<OutboundTelegramMessageAction> =
        listOf(
            callback(
                label = "🛍 Магазин",
                data = NotificationPackCallbackData.STORE,
                style = selectedStyle(activeMode == NotificationPackMenuMode.STORE),
            ),
            callback(
                label = "🎒 Мои",
                data = NotificationPackCallbackData.OWNED,
                style = selectedStyle(activeMode == NotificationPackMenuMode.OWNED),
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

    private fun templateButtonLabel(type: NotificationPackTemplateType): String =
        when (type) {
            NotificationPackTemplateType.ITEM_DROP -> "📦 Дроп"
            NotificationPackTemplateType.BOOSTER -> "⚡ Бустер"
            NotificationPackTemplateType.DUNGEON -> "🏰 Dungeon"
            NotificationPackTemplateType.RAID -> "🛡 RAID"
            NotificationPackTemplateType.GLOBAL -> "🌌 Глобал"
            NotificationPackTemplateType.PLAYER_OFFLINE -> "👤 Offline"
        }

    private fun templateLabel(type: NotificationPackTemplateType): String =
        when (type) {
            NotificationPackTemplateType.ITEM_DROP -> "Дроп предмета"
            NotificationPackTemplateType.BOOSTER -> "Бустер"
            NotificationPackTemplateType.DUNGEON -> "Dungeon"
            NotificationPackTemplateType.RAID -> "RAID"
            NotificationPackTemplateType.GLOBAL -> "Глобальное событие"
            NotificationPackTemplateType.PLAYER_OFFLINE -> "Игрок вышел"
        }

    private fun selectedStyle(selected: Boolean): OutboundTelegramButtonStyle =
        if (selected) {
            OutboundTelegramButtonStyle.PRIMARY
        } else {
            OutboundTelegramButtonStyle.DEFAULT
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

    private const val TEMPLATE_BUTTONS_PER_ROW = 3
    private const val VARIANT_BUTTONS_PER_ROW = 2
    private const val LONG_VARIANT_LABEL_LENGTH = 22
    private const val DIVIDER = "━━━━━━━━━━━━━━━━━━━━"
}
