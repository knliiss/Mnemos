package com.mnemos.moderation.application.wallet.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class WalletAdjustmentIdempotencyConflictException :
    AppException(
        "Idempotency key is already used by another wallet adjustment",
        "WALLET_ADJUSTMENT_IDEMPOTENCY_CONFLICT",
        HttpStatus.CONFLICT,
    )
