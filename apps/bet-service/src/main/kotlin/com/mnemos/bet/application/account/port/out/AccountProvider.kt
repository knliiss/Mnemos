package com.mnemos.bet.application.account.port.out

import com.mnemos.bet.application.account.model.AccountSnapshot

interface AccountProvider {
    fun requireActiveAccount(): AccountSnapshot

    fun findByNickname(nickname: String): AccountSnapshot?
}
