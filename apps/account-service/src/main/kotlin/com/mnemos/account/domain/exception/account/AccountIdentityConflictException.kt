package com.mnemos.account.domain.exception.account

import com.mnemos.account.domain.model.account.CristalixUuid
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountIdentityConflictException(
    cristalixUuid: CristalixUuid,
) : AppException(
        message = "Cristalix account is already linked",
        code = "ACCOUNT_IDENTITY_CONFLICT",
        httpStatus = HttpStatus.CONFLICT,
        details = mapOf("cristalixUuid" to cristalixUuid.id),
    )
