package com.mnemos.accountservice.domain.exception.invite

import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountInviteUnavailableException :
    AppException(
        message = "Account invite unavailable",
        code = "ACCOUNT_INVITE_UNAVAILABLE",
        httpStatus = HttpStatus.BAD_REQUEST,
    )
