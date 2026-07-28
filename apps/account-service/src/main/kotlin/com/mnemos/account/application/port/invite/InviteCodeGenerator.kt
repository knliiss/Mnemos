package com.mnemos.account.application.port.invite

import com.mnemos.account.domain.model.account.invite.InviteCode

interface InviteCodeGenerator {
    fun generate(): InviteCode
}
