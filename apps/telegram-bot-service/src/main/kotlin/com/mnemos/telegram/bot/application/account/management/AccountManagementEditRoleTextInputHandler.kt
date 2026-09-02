package com.mnemos.telegram.bot.application.account.management

import com.mnemos.telegram.bot.application.account.management.model.TelegramAccountManagementMutationResult
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import com.mnemos.telegram.bot.application.input.TelegramTextInputContext
import com.mnemos.telegram.bot.application.input.TelegramTextInputHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class AccountManagementEditRoleTextInputHandler(
    private val gateway: AccountManagementGateway,
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramTextInputHandler {
    override val pendingInput: TelegramPendingInput = TelegramPendingInput.ACCOUNT_MANAGEMENT_ROLE_EDIT

    override fun handle(context: TelegramTextInputContext): TelegramDeliveryResult {
        val session = sessionRepository.find(context.telegramId) ?: return TelegramDeliveryResult.Delivered
        val roleId =
            AccountManagementMenuContext.selectedRoleId(session.screenContext)
                ?: return menuService.completeTextInput(
                    telegramId = context.telegramId,
                    expectedScreen = TelegramMenuScreen.ACCOUNT_MANAGEMENT,
                    expectedInput = pendingInput,
                    updateContext = AccountManagementMenuContext::showRoles,
                )
        val input =
            AccountManagementRoleInputParser.parse(context.text)
                ?: return updatePending(context.telegramId, AccountManagementMenuContext::invalid)
        val result =
            gateway.updateRoleDetails(
                telegramId = context.telegramId,
                roleId = roleId,
                name = input.name,
                color = input.color,
            )

        return when (result) {
            TelegramAccountManagementMutationResult.UPDATED ->
                menuService.completeTextInput(
                    telegramId = context.telegramId,
                    expectedScreen = TelegramMenuScreen.ACCOUNT_MANAGEMENT,
                    expectedInput = pendingInput,
                    updateContext = AccountManagementMenuContext::updated,
                )

            TelegramAccountManagementMutationResult.INVALID ->
                updatePending(context.telegramId, AccountManagementMenuContext::invalid)

            TelegramAccountManagementMutationResult.UNAVAILABLE ->
                updatePending(context.telegramId, AccountManagementMenuContext::unavailable)

            TelegramAccountManagementMutationResult.ACTIVE_ACCOUNT_REQUIRED,
            TelegramAccountManagementMutationResult.FORBIDDEN,
            TelegramAccountManagementMutationResult.REFRESH,
            ->
                menuService.completeTextInput(
                    telegramId = context.telegramId,
                    expectedScreen = TelegramMenuScreen.ACCOUNT_MANAGEMENT,
                    expectedInput = pendingInput,
                    updateContext = AccountManagementMenuContext::showOverview,
                )
        }
    }

    private fun updatePending(
        telegramId: Long,
        update: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updatePendingTextInputContext(
            telegramId = telegramId,
            expectedScreen = TelegramMenuScreen.ACCOUNT_MANAGEMENT,
            expectedInput = pendingInput,
            updateContext = update,
        )
}
