package com.mnemos.personalization.application.purchase.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class PersonalizationPurchaseInProgressException :
    AppException(
        "Personalization purchase is already in progress",
        "PERSONALIZATION_PURCHASE_IN_PROGRESS",
        HttpStatus.CONFLICT,
    )
