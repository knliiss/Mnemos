package com.mnemos.telegram.bot.application.account.management

import com.mnemos.telegram.bot.application.account.management.model.AccountManagementConfirmation
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMutationResult
import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementResult
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class AccountManagementCallbackHandler(
    private val gateway: AccountManagementGateway,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = AccountManagementCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session = currentSession(context) ?: return TelegramDeliveryResult.Delivered

        return when (context.data) {
            AccountManagementCallbackData.REFRESH -> refresh(context)
            AccountManagementCallbackData.OVERVIEW -> update(context, AccountManagementMenuContext::showOverview)
            AccountManagementCallbackData.MEMBERS -> update(context, AccountManagementMenuContext::showMembers)
            AccountManagementCallbackData.ROLES -> update(context, AccountManagementMenuContext::showRoles)
            AccountManagementCallbackData.CREATE_ROLE -> requestRoleInput(context, TelegramPendingInput.ACCOUNT_MANAGEMENT_ROLE_CREATE)
            AccountManagementCallbackData.EDIT_ROLE -> requestRoleEdit(context, session)
            AccountManagementCallbackData.CANCEL_INPUT -> cancelInput(context)
            AccountManagementCallbackData.KICK_MEMBER -> requestKick(context, session)
            AccountManagementCallbackData.KICK_MEMBER_CONFIRM -> confirmKick(context, session)
            AccountManagementCallbackData.DELETE_ROLE -> requestRoleDelete(context, session)
            AccountManagementCallbackData.DELETE_ROLE_CONFIRM -> confirmRoleDelete(context, session)
            AccountManagementCallbackData.CANCEL_CONFIRMATION ->
                update(context, AccountManagementMenuContext::cancelConfirmation)

            AccountManagementCallbackData.MOVE_ROLE_UP -> moveRole(context, session, offset = -1)
            AccountManagementCallbackData.MOVE_ROLE_DOWN -> moveRole(context, session, offset = 1)
            else -> handleParameterizedCallback(context, session)
        }
    }

    private fun handleParameterizedCallback(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        AccountManagementCallbackData.selectedMemberId(context.data)?.let { memberId ->
            return update(context) { screenContext ->
                AccountManagementMenuContext.showMember(screenContext, memberId)
            }
        }

        AccountManagementCallbackData.selectedRoleId(context.data)?.let { roleId ->
            return update(context) { screenContext ->
                AccountManagementMenuContext.showRole(screenContext, roleId)
            }
        }

        AccountManagementCallbackData.assignedRoleId(context.data)?.let { roleId ->
            return setMemberRole(
                context = context,
                session = session,
                roleId = roleId,
                assigned = true,
            )
        }

        AccountManagementCallbackData.revokedRoleId(context.data)?.let { roleId ->
            return setMemberRole(
                context = context,
                session = session,
                roleId = roleId,
                assigned = false,
            )
        }

        AccountManagementCallbackData.permissionMutation(context.data)?.let { mutation ->
            val roleId =
                AccountManagementMenuContext.selectedRoleId(session.screenContext)
                    ?: return TelegramDeliveryResult.Delivered
            val result =
                gateway.setRolePermission(
                    telegramId = context.telegramId,
                    roleId = roleId,
                    permission = mutation.permission,
                    enabled = mutation.enabled,
                )

            return applyMutationResult(
                context = context,
                result = result,
                onUpdated = AccountManagementMenuContext::updated,
            )
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun requestRoleInput(
        context: TelegramCallbackContext,
        pendingInput: TelegramPendingInput,
    ): TelegramDeliveryResult =
        menuService.requestTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.ACCOUNT_MANAGEMENT,
            pendingInput = pendingInput,
            updateContext = AccountManagementMenuContext::clearFeedback,
        )

    private fun requestRoleEdit(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        if (AccountManagementMenuContext.selectedRoleId(session.screenContext) == null) {
            return TelegramDeliveryResult.Delivered
        }

        return requestRoleInput(context, TelegramPendingInput.ACCOUNT_MANAGEMENT_ROLE_EDIT)
    }

    private fun cancelInput(context: TelegramCallbackContext): TelegramDeliveryResult =
        menuService.cancelTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.ACCOUNT_MANAGEMENT,
            updateContext = AccountManagementMenuContext::clearFeedback,
        )

    private fun requestKick(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        if (AccountManagementMenuContext.selectedMemberId(session.screenContext) == null) {
            return TelegramDeliveryResult.Delivered
        }

        return update(context, AccountManagementMenuContext::requestKick)
    }

    private fun confirmKick(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        val confirmation = AccountManagementMenuContext.confirmation(session.screenContext)
        val memberId = AccountManagementMenuContext.selectedMemberId(session.screenContext)

        if (confirmation != AccountManagementConfirmation.KICK_MEMBER || memberId == null) {
            return TelegramDeliveryResult.Delivered
        }

        val result =
            gateway.kickMember(
                telegramId = context.telegramId,
                memberId = memberId,
            )

        return applyMutationResult(
            context = context,
            result = result,
        ) { screenContext ->
            AccountManagementMenuContext.updated(
                AccountManagementMenuContext.showMembers(screenContext),
            )
        }
    }

    private fun setMemberRole(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
        roleId: UUID,
        assigned: Boolean,
    ): TelegramDeliveryResult {
        val memberId =
            AccountManagementMenuContext.selectedMemberId(session.screenContext)
                ?: return TelegramDeliveryResult.Delivered
        val result =
            gateway.setMemberRole(
                telegramId = context.telegramId,
                memberId = memberId,
                roleId = roleId,
                assigned = assigned,
            )

        return applyMutationResult(
            context = context,
            result = result,
            onUpdated = AccountManagementMenuContext::updated,
        )
    }

    private fun requestRoleDelete(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        if (AccountManagementMenuContext.selectedRoleId(session.screenContext) == null) {
            return TelegramDeliveryResult.Delivered
        }

        return update(context, AccountManagementMenuContext::requestRoleDelete)
    }

    private fun confirmRoleDelete(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
    ): TelegramDeliveryResult {
        val confirmation = AccountManagementMenuContext.confirmation(session.screenContext)
        val roleId = AccountManagementMenuContext.selectedRoleId(session.screenContext)

        if (confirmation != AccountManagementConfirmation.DELETE_ROLE || roleId == null) {
            return TelegramDeliveryResult.Delivered
        }

        val result =
            gateway.deleteRole(
                telegramId = context.telegramId,
                roleId = roleId,
            )

        return applyMutationResult(
            context = context,
            result = result,
        ) { screenContext ->
            AccountManagementMenuContext.updated(
                AccountManagementMenuContext.showRoles(screenContext),
            )
        }
    }

    private fun moveRole(
        context: TelegramCallbackContext,
        session: TelegramMenuSession,
        offset: Int,
    ): TelegramDeliveryResult {
        val roleId =
            AccountManagementMenuContext.selectedRoleId(session.screenContext)
                ?: return TelegramDeliveryResult.Delivered
        val management = gateway.getManagement(context.telegramId)
        val snapshot =
            (management as? TelegramAccountManagementResult.Available)?.snapshot
                ?: return refresh(context)
        val currentIndex = snapshot.roles.indexOfFirst { role -> role.id == roleId }
        val targetIndex = currentIndex + offset

        if (currentIndex < 0 || targetIndex !in snapshot.roles.indices) {
            return TelegramDeliveryResult.Delivered
        }

        val result =
            gateway.moveRole(
                telegramId = context.telegramId,
                roleId = roleId,
                targetIndex = targetIndex,
            )

        return applyMutationResult(
            context = context,
            result = result,
            onUpdated = AccountManagementMenuContext::updated,
        )
    }

    private fun applyMutationResult(
        context: TelegramCallbackContext,
        result: TelegramAccountManagementMutationResult,
        onUpdated: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        when (result) {
            TelegramAccountManagementMutationResult.UPDATED -> update(context, onUpdated)
            TelegramAccountManagementMutationResult.INVALID -> update(context, AccountManagementMenuContext::invalid)
            TelegramAccountManagementMutationResult.UNAVAILABLE -> update(context, AccountManagementMenuContext::unavailable)
            TelegramAccountManagementMutationResult.ACTIVE_ACCOUNT_REQUIRED,
            TelegramAccountManagementMutationResult.FORBIDDEN,
            TelegramAccountManagementMutationResult.REFRESH,
            -> refresh(context)
        }

    private fun refresh(context: TelegramCallbackContext): TelegramDeliveryResult =
        update(context) { screenContext ->
            AccountManagementMenuContext.clearFeedback(
                AccountManagementMenuContext.cancelConfirmation(screenContext),
            )
        }

    private fun update(
        context: TelegramCallbackContext,
        update: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.ACCOUNT_MANAGEMENT,
            update = update,
        )

    private fun currentSession(context: TelegramCallbackContext): TelegramMenuSession? {
        val session = sessionRepository.find(context.telegramId) ?: return null

        if (session.currentScreen != TelegramMenuScreen.ACCOUNT_MANAGEMENT || session.menuMessageId != context.messageId) {
            return null
        }

        return session
    }
}
