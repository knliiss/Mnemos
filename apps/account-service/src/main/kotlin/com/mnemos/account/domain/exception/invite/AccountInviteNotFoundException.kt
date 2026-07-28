package com.mnemos.account.domain.exception.invite

import com.mnemos.account.domain.model.account.invite.InviteId
import com.mnemos.common.core.model.id.AccountId
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class AccountInviteNotFoundException(
    inviteId: InviteId,
    accountId: AccountId,
) : AppException(
        message = "Account invite was not found",
        code = "ACCOUNT_INVITE_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details =
            mapOf(
                "inviteId" to inviteId.value,
                "accountId" to accountId.value,
            ),
    )
