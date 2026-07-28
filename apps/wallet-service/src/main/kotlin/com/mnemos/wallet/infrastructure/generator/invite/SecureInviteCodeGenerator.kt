package com.mnemos.accountservice.infrastructure.generator.invite

import com.mnemos.accountservice.application.port.invite.InviteCodeGenerator
import com.mnemos.accountservice.domain.model.account.invite.InviteCode
import org.springframework.stereotype.Component
import java.security.SecureRandom

@Component
class SecureInviteCodeGenerator : InviteCodeGenerator {
    private val secureRandom = SecureRandom()

    override fun generate(): InviteCode {
        val symbols =
            CharArray(InviteCode.SYMBOL_COUNT) {
                InviteCode.ALPHABET[
                    secureRandom.nextInt(InviteCode.ALPHABET.length),
                ]
            }

        return InviteCode.of(symbols.concatToString())
    }
}
