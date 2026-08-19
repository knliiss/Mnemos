package com.mnemos.social.application.spotlight.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus
import java.util.UUID

class SpotlightItemDropNotFoundException(
    itemDropEventId: UUID,
) : AppException(
        message = "Item drop was not found",
        code = "SPOTLIGHT_ITEM_DROP_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("itemDropEventId" to itemDropEventId),
    )
