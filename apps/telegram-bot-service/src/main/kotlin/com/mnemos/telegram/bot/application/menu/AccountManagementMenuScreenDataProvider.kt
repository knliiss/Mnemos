package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.account.management.AccountManagementMenuContext
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementInputMode
import com.mnemos.telegram.bot.application.account.management.model.AccountManagementMenuScreenData
import com.mnemos.telegram.bot.application.account.management.port.out.AccountManagementGateway
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import org.springframework.stereotype.Component

@Component
class AccountManagementMenuScreenDataProvider(
    private val gateway: AccountManagementGateway,
) : TelegramMenuScreenDataProvider {
    override val screen: TelegramMenuScreen = TelegramMenuScreen.ACCOUNT_MANAGEMENT

    override fun load(session: TelegramMenuSession): TelegramMenuScreenData =
        TelegramMenuScreenData.AccountManagement(
            AccountManagementMenuScreenData(
                result = gateway.getManagement(session.telegramId),
                view = AccountManagementMenuContext.view(session.screenContext),
                selectedMemberId = AccountManagementMenuContext.selectedMemberId(session.screenContext),
                selectedRoleId = AccountManagementMenuContext.selectedRoleId(session.screenContext),
                feedback = AccountManagementMenuContext.feedback(session.screenContext),
                confirmation = AccountManagementMenuContext.confirmation(session.screenContext),
                inputMode = inputMode(session.pendingInput),
            ),
        )

    private fun inputMode(pendingInput: TelegramPendingInput?): AccountManagementInputMode? =
        when (pendingInput) {
            TelegramPendingInput.ACCOUNT_MANAGEMENT_ROLE_CREATE -> AccountManagementInputMode.CREATE_ROLE
            TelegramPendingInput.ACCOUNT_MANAGEMENT_ROLE_EDIT -> AccountManagementInputMode.EDIT_ROLE
            else -> null
        }
}
