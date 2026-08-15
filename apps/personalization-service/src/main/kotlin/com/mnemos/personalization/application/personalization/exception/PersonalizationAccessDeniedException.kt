package com.mnemos.personalization.application.personalization.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class PersonalizationAccessDeniedException :
    AppException(
        "Personalization action is not allowed",
        "PERSONALIZATION_ACCESS_DENIED",
        HttpStatus.FORBIDDEN,
    )
