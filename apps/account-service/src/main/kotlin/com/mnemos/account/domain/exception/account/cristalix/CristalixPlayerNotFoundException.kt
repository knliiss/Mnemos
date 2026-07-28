package com.mnemos.account.domain.exception.account.cristalix

import com.mnemos.account.domain.model.account.CristalixNickname
import com.mnemos.common.web.exception.AppException
import org.springframework.http.HttpStatus

class CristalixPlayerNotFoundException(
    nickname: CristalixNickname,
) : AppException(
        message = "Cristalix player with this nickname not found",
        code = "CRISTALIX_PLAYER_NOT_FOUND",
        httpStatus = HttpStatus.NOT_FOUND,
        details = mapOf("nickname" to nickname.value),
    )
