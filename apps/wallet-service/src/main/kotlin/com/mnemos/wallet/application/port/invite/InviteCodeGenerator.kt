package com.mnemos.accountservice.application.port.invite

import com.mnemos.accountservice.domain.model.account.invite.InviteCode

interface InviteCodeGenerator {
    fun generate(): InviteCode
}
