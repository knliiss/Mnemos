package com.mnemos.moderation.domain.sanction.exception

import com.mnemos.common.core.model.id.UserId
import com.mnemos.common.web.exception.AppException
import com.mnemos.moderation.domain.sanction.model.DomainSanctionType
import org.springframework.http.HttpStatus

class SanctionConflictException(
    userId: UserId,
    type: DomainSanctionType,
) : AppException(
        message = "An active sanction of type $type already exists for user $userId",
        code = "SANCTION_ALREADY_ACTIVE",
        httpStatus = HttpStatus.CONFLICT,
    )
