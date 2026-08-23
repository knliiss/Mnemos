package com.mnemos.social.application.spotlight.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus
import java.util.UUID

class ItemDropAlreadySpotlightedException(
    itemDropEventId: UUID,
) : AppException(
        message = "Item drop has already been spotlighted",
        code = "ITEM_DROP_ALREADY_SPOTLIGHTED",
        httpStatus = HttpStatus.CONFLICT,
        details = mapOf("itemDropEventId" to itemDropEventId),
    )
