package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.events.EventHistoryMenuContext
import com.mnemos.telegram.bot.application.events.EventHistorySearchQuery
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.online.SaoOnlineMenuContext
import com.mnemos.telegram.bot.application.online.SaoOnlineSearchQuery
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.input.ApplyWidgetTextSearchUseCase
import com.mnemos.telegram.bot.application.widget.notifications.CreateWidgetItemDropSubscriptionUseCase
import com.mnemos.telegram.bot.application.widget.management.CreateWidgetAccountRoleUseCase
import com.mnemos.telegram.bot.application.widget.management.EditWidgetAccountRoleUseCase
import com.mnemos.telegram.bot.application.widget.profilecomments.CreateWidgetProfileCommentUseCase
import com.mnemos.telegram.bot.application.widget.profilecomments.EditWidgetProfileCommentUseCase
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetSearchQuery
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class HandleWidgetTextReplyService(
    private val properties: TelegramWidgetProperties,
    private val pending: WidgetTextInputSessionRepository,
    private val widgets: TelegramWidgetSessionRepository,
    private val menu: TelegramMenuService,
    private val search: ApplyWidgetTextSearchUseCase,
    private val accountInput: ApplyWidgetAccountInputUseCase,
    private val itemDropInput: CreateWidgetItemDropSubscriptionUseCase,
    private val roleInput: CreateWidgetAccountRoleUseCase,
    private val editRole: EditWidgetAccountRoleUseCase,
    private val commentInput: CreateWidgetProfileCommentUseCase,
    private val editComment: EditWidgetProfileCommentUseCase,
) : HandleWidgetTextReplyUseCase {
    override fun handle(telegramId: Long, promptMessageId: Int, text: String): TelegramDeliveryResult? {
        if (!properties.enabled) return null
        val session = pending.find(telegramId, promptMessageId) ?: return null
        val widget = widgets.find(telegramId, session.widgetMessageId)

        val screen =
            when (session.purpose) {
                WidgetTextInputPurpose.EVENT_SEARCH -> TelegramMenuScreen.EVENTS
                WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH -> TelegramMenuScreen.ONLINE
                WidgetTextInputPurpose.ACCOUNT_CREATE, WidgetTextInputPurpose.ACCOUNT_JOIN -> TelegramMenuScreen.ACCOUNT
                WidgetTextInputPurpose.ITEM_DROP_PLAYER,
                WidgetTextInputPurpose.ITEM_DROP_ITEM,
                WidgetTextInputPurpose.ITEM_DROP_BOTH -> TelegramMenuScreen.NOTIFICATIONS
                WidgetTextInputPurpose.PUBLIC_PROFILE_SEARCH -> TelegramMenuScreen.HOME
                WidgetTextInputPurpose.ACCOUNT_ROLE_CREATE,
                WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT -> TelegramMenuScreen.ACCOUNT_MANAGEMENT
                WidgetTextInputPurpose.PROFILE_COMMENT_CREATE,
                WidgetTextInputPurpose.PROFILE_COMMENT_REPLY,
                WidgetTextInputPurpose.PROFILE_COMMENT_EDIT -> TelegramMenuScreen.HOME
            }
        val result =
            when {
                text.trim().equals("скасувати", ignoreCase = true) -> TelegramDeliveryResult.Delivered
                widget == null || widget.screen != session.purpose.screen || widget.accountId != session.accountId ->
                    menu.open(telegramId, screen)
                session.purpose == WidgetTextInputPurpose.ACCOUNT_CREATE ||
                    session.purpose == WidgetTextInputPurpose.ACCOUNT_JOIN ->
                    accountInput.apply(session, widget, text.trim())
                session.purpose == WidgetTextInputPurpose.ITEM_DROP_PLAYER ||
                    session.purpose == WidgetTextInputPurpose.ITEM_DROP_ITEM ||
                    session.purpose == WidgetTextInputPurpose.ITEM_DROP_BOTH ->
                    itemDropInput.create(session, widget, text.trim())
                session.purpose == WidgetTextInputPurpose.ACCOUNT_ROLE_CREATE ->
                    roleInput.create(session, widget, text.trim())
                session.purpose == WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT ->
                    editRole.edit(session, widget, text.trim())
                session.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_CREATE ||
                    session.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_REPLY ->
                    commentInput.create(session, widget, text)
                session.purpose == WidgetTextInputPurpose.PROFILE_COMMENT_EDIT ->
                    editComment.edit(session, widget, text)
                else -> {
                    val normalized =
                        when (session.purpose) {
                            WidgetTextInputPurpose.EVENT_SEARCH -> EventHistorySearchQuery.normalize(text)
                            WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH -> SaoOnlineSearchQuery.normalize(text)
                            WidgetTextInputPurpose.PUBLIC_PROFILE_SEARCH -> ProfileWidgetSearchQuery.normalize(text)
                            else -> null
                        }

                    if (normalized == null) {
                        val context =
                            when (session.purpose) {
                                WidgetTextInputPurpose.EVENT_SEARCH ->
                                    EventHistoryMenuContext.withSearchError(emptyMap(), EventHistorySearchQuery.INVALID_MESSAGE)
                                WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH ->
                                    mapOf(SaoOnlineMenuContext.SEARCH_ERROR_KEY to SaoOnlineSearchQuery.INVALID_MESSAGE)
                                else -> emptyMap()
                            }
                        menu.open(telegramId, screen, screenContext = context)
                    } else {
                        search.apply(widget, normalized)
                    }
                }
            }

        if (result != TelegramDeliveryResult.Retryable) {
            pending.delete(telegramId, promptMessageId)
        }
        return result
    }
}
