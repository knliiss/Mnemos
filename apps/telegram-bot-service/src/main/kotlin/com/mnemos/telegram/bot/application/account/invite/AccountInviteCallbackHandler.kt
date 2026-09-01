package com.mnemos.telegram.bot.application.account.invite

import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteMutationResult
import com.mnemos.telegram.bot.application.account.invite.port.out.AccountInviteGateway
import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class AccountInviteCallbackHandler(
    private val gateway: AccountInviteGateway,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = AccountInviteCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session = currentSession(context) ?: return TelegramDeliveryResult.Delivered

        return when (context.data) {
            AccountInviteCallbackData.REFRESH -> refresh(context)
            AccountInviteCallbackData.CREATE -> createInvite(context, session)
            AccountInviteCallbackData.REVOKE_CANCEL -> cancelRevoke(context)
            else -> handleParameterizedCallback(context, session)
        }
    }

    private fun handleParameterizedCallback(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        AccountInviteCallbackData.selectedExpiry(context.data)?.let { expiry ->
            return update(context) { screenContext ->
                AccountInviteMenuContext.selectExpiry(screenContext, expiry)
            }
        }

        AccountInviteCallbackData.requestedRevokeId(context.data)?.let { inviteId ->
            return update(context) { screenContext ->
                AccountInviteMenuContext.requestRevoke(screenContext, inviteId)
            }
        }

        val confirmedInviteId =
            AccountInviteCallbackData.confirmedRevokeId(context.data)
                ?: return TelegramDeliveryResult.Delivered
        val selectedInviteId = AccountInviteMenuContext.revokeConfirmationId(session.screenContext)

        if (confirmedInviteId != selectedInviteId) {
            return TelegramDeliveryResult.Delivered
        }

        return revokeInvite(context, confirmedInviteId)
    }

    private fun createInvite(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        val expiry = AccountInviteMenuContext.expiry(session.screenContext)
        val result =
            gateway.createInvite(
                telegramId = context.telegramId,
                expiresInMinutes = expiry.expiresInMinutes,
            )

        return applyMutationResult(
            context = context,
            result = result,
            onUpdated = AccountInviteMenuContext::created,
        )
    }

    private fun revokeInvite(
        context: TelegramCallbackContext,
        inviteId: UUID,
    ): TelegramDeliveryResult {
        val result =
            gateway.revokeInvite(
                telegramId = context.telegramId,
                inviteId = inviteId,
            )

        return applyMutationResult(
            context = context,
            result = result,
            onUpdated = AccountInviteMenuContext::revoked,
        )
    }

    private fun applyMutationResult(
        context: TelegramCallbackContext,
        result: TelegramAccountInviteMutationResult,
        onUpdated: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        when (result) {
            TelegramAccountInviteMutationResult.UPDATED -> update(context, onUpdated)
            TelegramAccountInviteMutationResult.UNAVAILABLE ->
                update(context, AccountInviteMenuContext::unavailable)

            TelegramAccountInviteMutationResult.ACTIVE_ACCOUNT_REQUIRED,
            TelegramAccountInviteMutationResult.FORBIDDEN,
            TelegramAccountInviteMutationResult.REFRESH,
            -> refresh(context)
        }

    private fun refresh(context: TelegramCallbackContext): TelegramDeliveryResult =
        update(context) { screenContext ->
            AccountInviteMenuContext.clearFeedback(
                AccountInviteMenuContext.cancelRevoke(screenContext),
            )
        }

    private fun cancelRevoke(context: TelegramCallbackContext): TelegramDeliveryResult =
        update(context, AccountInviteMenuContext::cancelRevoke)

    private fun update(
        context: TelegramCallbackContext,
        update: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.ACCOUNT_INVITES,
            update = update,
        )

    private fun currentSession(context: TelegramCallbackContext): TelegramMenuSession? {
        val session = sessionRepository.find(context.telegramId) ?: return null

        if (session.currentScreen != TelegramMenuScreen.ACCOUNT_INVITES || session.menuMessageId != context.messageId) {
            return null
        }

        return session
    }
}
