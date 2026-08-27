package com.mnemos.payment.adapter.out.security

import com.mnemos.payment.application.security.port.out.CurrentPaymentUser
import com.mnemos.payment.application.security.port.out.CurrentPaymentUserProvider
import com.mnemos.security.CurrentMnemosUser
import org.springframework.stereotype.Component

@Component
class CurrentPaymentUserProviderAdapter(
    private val currentMnemosUser: CurrentMnemosUser,
) : CurrentPaymentUserProvider {
    override fun requireCurrent(): CurrentPaymentUser {
        val principal = currentMnemosUser.get()
        val userId = requireNotNull(principal.userId) { "Required JWT claim mnemos_user_id is missing" }

        return CurrentPaymentUser(
            userId = userId,
            telegramId = principal.telegramId,
        )
    }
}
