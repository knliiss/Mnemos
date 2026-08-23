package com.mnemos.social.application.spotlight.exception

import com.mnemos.common.web.exception.AppException
import com.mnemos.social.domain.spotlight.model.DropSpotlightId
import org.springframework.http.HttpStatus

class DropSpotlightNotFoundException(
    spotlightId: DropSpotlightId,
) : AppException(
        message = "Drop spotlight was not found",
        code = "DROP_SPOTLIGHT_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("spotlightId" to spotlightId.id),
    )
