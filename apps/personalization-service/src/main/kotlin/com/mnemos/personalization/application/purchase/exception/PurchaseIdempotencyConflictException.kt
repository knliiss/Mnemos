package com.mnemos.personalization.application.purchase.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class PurchaseIdempotencyConflictException :
    AppException(
        "Idempotency key is already used by another purchase",
        "PURCHASE_IDEMPOTENCY_CONFLICT",
        HttpStatus.CONFLICT,
    )
