package com.mnemos.personalization.application.ownership.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class PersonalizationOwnershipConflictException :
    AppException(
        "Personalization ownership changed concurrently",
        "PERSONALIZATION_OWNERSHIP_CONFLICT",
        HttpStatus.CONFLICT,
    )
