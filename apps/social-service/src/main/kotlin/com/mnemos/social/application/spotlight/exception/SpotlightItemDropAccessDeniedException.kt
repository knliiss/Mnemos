package com.mnemos.social.application.spotlight.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus
import java.util.UUID

class SpotlightItemDropAccessDeniedException(
    itemDropEventId: UUID,
) : AppException(
        message = "Item drop does not belong to the active account",
        code = "SPOTLIGHT_ITEM_DROP_ACCESS_DENIED",
        httpStatus = HttpStatus.FORBIDDEN,
        details = mapOf("itemDropEventId" to itemDropEventId),
    )
