package com.mnemos.personalization.application.purchase.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class PersonalizationAlreadyOwnedException :
    AppException(
        "Personalization is already owned",
        "PERSONALIZATION_ALREADY_OWNED",
        HttpStatus.CONFLICT,
    )
