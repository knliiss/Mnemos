package com.mnemos.account.infrastructure.generator.invite

import com.mnemos.account.application.port.invite.InviteCodeGenerator
import com.mnemos.account.domain.model.account.invite.InviteCode
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
