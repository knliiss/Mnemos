package com.mnemos.moderation.domain.sanction.exception

import com.mnemos.common.web.exception.AppException
import com.mnemos.moderation.domain.sanction.model.SanctionId
import org.springframework.http.HttpStatus

class SanctionNotFoundException(
    sanctionId: SanctionId,
) : AppException(
        message = "Sanction $sanctionId was not found",
        code = "SANCTION_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
    )
