package com.mnemos.payment.application.stars

import com.mnemos.common.web.exception.AppException
import com.mnemos.payment.application.account.port.out.PaymentTelegramUserResolver
import com.mnemos.payment.application.stars.model.CreatedStarPrimePurchase
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class CreateTelegramStarPrimePurchaseService(
    private val telegramUserResolver: PaymentTelegramUserResolver,
    private val createPurchaseService: CreateStarPrimePurchaseService,
) {
    fun execute(
        telegramId: Long,
        packageId: String,
    ): CreatedStarPrimePurchase {
        val user =
            telegramUserResolver.findByTelegramId(telegramId)
                ?: throw AppException(
                    message = "Telegram user is not registered in Mnemos",
                    code = "TELEGRAM_USER_NOT_REGISTERED",
                    httpStatus = HttpStatus.NOT_FOUND,
                )

        return createPurchaseService.execute(
            packageId = packageId,
            user = user,
        )
    }
}
