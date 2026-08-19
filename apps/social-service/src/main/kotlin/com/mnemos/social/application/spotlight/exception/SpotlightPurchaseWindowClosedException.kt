package com.mnemos.social.application.spotlight.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus
import java.util.UUID

class SpotlightPurchaseWindowClosedException(
    itemDropEventId: UUID,
) : AppException(
        message = "Item drop is outside the spotlight purchase window",
        code = "SPOTLIGHT_PURCHASE_WINDOW_CLOSED",
        httpStatus = HttpStatus.CONFLICT,
        details = mapOf("itemDropEventId" to itemDropEventId),
    )
