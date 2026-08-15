package com.mnemos.personalization.application.ownership.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class PersonalizationNotOwnedException :
    AppException(
        "Personalization is not owned",
        "PERSONALIZATION_NOT_OWNED",
        HttpStatus.CONFLICT,
    )
