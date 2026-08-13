package com.mnemos.moderation.domain.sanction.exception

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class SanctionTargetAccessDeniedException :
    AppException(
        message = "The selected moderation target cannot be sanctioned by this role",
        code = "SANCTION_TARGET_ACCESS_DENIED",
        httpStatus = HttpStatus.FORBIDDEN,
    )
