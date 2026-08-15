package com.mnemos.personalization.application.personalization.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class PersonalizationNotFoundException :
    AppException(
        "Personalization was not found",
        "PERSONALIZATION_NOT_FOUND",
        HttpStatus.NOT_FOUND,
    )
