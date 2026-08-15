package com.mnemos.personalization.application.purchase.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class PersonalizationNotPurchasableException :
    AppException(
        "Personalization is not available for purchase",
        "PERSONALIZATION_NOT_PURCHASABLE",
        HttpStatus.CONFLICT,
    )
