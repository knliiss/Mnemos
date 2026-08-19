package com.mnemos.moderation.application.wallet.exception

import com.mnemos.common.web.exception.AppException
import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperationId
import org.springframework.http.HttpStatus

class WalletAdjustmentOperationNotFoundException(
    operationId: WalletAdjustmentOperationId,
) : AppException(
        message = "Wallet adjustment operation was not found",
        code = "WALLET_ADJUSTMENT_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("operationId" to operationId.id),
    )
